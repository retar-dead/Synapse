package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.IntProperty;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;

public class ForceSlot extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    /** Slot da hotbar (1-9) onde a espada deve ficar travada. */
    public final IntProperty slot = new IntProperty("slot", 1, 1, 9);
    /** Delay em ms entre cada tentativa de swap. */
    public final IntProperty delay = new IntProperty("delay", 100, 0, 500);

    private final TimerUtil swapTimer = new TimerUtil();

    public ForceSlot() {
        super("ForceSlot", false);
    }

    @Override
    public void onDisabled() {
        swapTimer.reset();
    }

    private int findSwordContainerSlot() {
        for (int i = 9; i <= 44; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemSword) {
                return i;
            }
        }
        return -1;
    }

    /** Converte slot hotbar 1-9 para índice do container (36-44). */
    private int getTargetContainerSlot() {
        return 35 + this.slot.getValue();
    }

    private void doSwap(int swordSlot, int targetSlot) {
        int w = mc.thePlayer.inventoryContainer.windowId;

        mc.playerController.windowClick(w, targetSlot, 0, 0, mc.thePlayer);
        mc.playerController.windowClick(w, swordSlot, 0, 0, mc.thePlayer);
        mc.playerController.windowClick(w, targetSlot, 0, 0, mc.thePlayer);

        swapTimer.reset();
        mc.displayGuiScreen(null);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null || event.getType() != EventType.PRE) {
            return;
        }

        int swordSlot = findSwordContainerSlot();
        if (swordSlot < 0) {
            return;
        }

        int targetSlot = getTargetContainerSlot();
        if (swordSlot == targetSlot) {
            return;
        }

        if (!swapTimer.hasTimeElapsed(this.delay.getValue())) {
            return;
        }

        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiInventory)) {
            return;
        }

        if (!(mc.currentScreen instanceof GuiInventory)) {
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
            return;
        }
        if (!(mc.thePlayer.openContainer instanceof ContainerPlayer)) {
            return;
        }

        doSwap(swordSlot, targetSlot);
    }
}
