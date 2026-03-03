package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class Refill extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final IntProperty refilMinDelay = new IntProperty("refil-min-delay", 3, 1, 400);
    public final IntProperty refilMaxDelay = new IntProperty("refil-max-delay", 5, 1, 400);
    public final IntProperty startWith = new IntProperty("start-with", 4, 0, 9);
    public final BooleanProperty randomize = new BooleanProperty("randomize", false);
    public final BooleanProperty autoOpen = new BooleanProperty("auto-open", true);
    public final BooleanProperty autoclose = new BooleanProperty("autoclose", false);

    private final TimerUtil refilTimer = new TimerUtil();

    private long delay = 0;
    private boolean start = false;
    // true quando o inventário foi aberto automaticamente pelo módulo (para não fechar GUIs abertas manualmente)
    private boolean openedByAutoOpen = false;

    public Refill() {
        super("Refill", false);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        reset();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null || event.getType() != EventType.PRE) {
            return;
        }

        AutoRecraft autoRecraft = (AutoRecraft) Myau.moduleManager.getModule(AutoRecraft.class);
        if (autoRecraft != null && autoRecraft.isEnabled() && autoRecraft.start) {
            reset();
            return;
        }

        if (!(mc.currentScreen instanceof GuiInventory)) {
            // Se auto-open ativado e as condições exigirem refill, abra automaticamente o inventário
            if (autoOpen.getValue() && shouldAutoOpen() && mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                openedByAutoOpen = true;
                // continuar execução com a GUI aberta
            } else {
                reset();
                return;
            }
        }

        if (start) {
            if (hasSoupInHotbar() && isHotbarFull()) {
                start = false;
                return;
            }

            if (refilTimer.hasTimeElapsed(delay)) {
                refilTimer.reset();
                delay = RandomUtils.nextLong(
                        Math.min(refilMinDelay.getValue(), refilMaxDelay.getValue()),
                        Math.max(refilMinDelay.getValue(), refilMaxDelay.getValue())
                );

                if (randomize.getValue()) {
                    ArrayList<Integer> soupSlots = getSoupSlots();
                    if (!soupSlots.isEmpty()) {
                        int index = RandomUtils.nextInt(0, soupSlots.size());
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, soupSlots.get(index), 0, 1, mc.thePlayer);
                    } else {
                        start = false;
                    }
                } else {
                    int soupSlot = getSoupInInventory();
                    if (soupSlot != Integer.MIN_VALUE) {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, soupSlot, 0, 1, mc.thePlayer);
                    } else {
                        start = false;
                    }
                }
            }
        } else {
            if (getSoupAmountInHotbar() <= this.startWith.getValue()) {
                delay = RandomUtils.nextLong(
                        Math.min(refilMinDelay.getValue(), refilMaxDelay.getValue()),
                        Math.max(refilMinDelay.getValue(), refilMaxDelay.getValue())
                );
                start = true;
            }
        }
    }

    private void reset() {
        refilTimer.reset();
        start = false;
        delay = 0;
        // Só fechar a GUI se ela foi aberta automaticamente por este módulo e autoclose estiver ativado
        if (autoclose.getValue() && openedByAutoOpen && mc.currentScreen instanceof GuiInventory) {
            mc.displayGuiScreen(null);
        }
        openedByAutoOpen = false;
    }

    /**
     * Decide se devemos abrir o inventário automaticamente para iniciar/continuar o refill.
     */
    private boolean shouldAutoOpen() {
        if (!start) {
            // Verifica se há sopa no inventário e se a quantidade na hotbar está baixa
            return hasSoupInInventory() && getSoupAmountInHotbar() <= startWith.getValue();
        } else {
            // se já iniciou o refill, continuar enquanto houver sopa
            return hasSoupInInventory();
        }
    }

    private boolean hasSoupInInventory() {
        return getSoupInInventory() != Integer.MIN_VALUE || !getSoupSlots().isEmpty();
    }

    private boolean hasSoupInHotbar() {
        return getSoupInHotbar() != Integer.MIN_VALUE;
    }

    private int getSoupInHotbar() {
        for (int i = 36; i < 45; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
                return i - 36;
            }
        }
        return Integer.MIN_VALUE;
    }

    private ArrayList<Integer> getSoupSlots() {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
                temp.add(i);
            }
        }
        return temp;
    }

    private int getSoupInInventory() {
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    private int getSoupAmountInHotbar() {
        int counter = 0;
        for (int i = 36; i < 45; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
                counter++;
            }
        }
        return counter;
    }

    private boolean isHotbarFull() {
        int counter = 0;
        for (int i = 36; i < 45; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null) {
                counter++;
            }
        }
        return counter == 9;
    }
}

