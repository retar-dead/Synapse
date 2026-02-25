package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class AutoSoup extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final FloatProperty health = new FloatProperty("health", 10.0F, 0.5F, 20.0F);
    public final BooleanProperty dropSoup = new BooleanProperty("drop-soup", true);
    public final IntProperty healDelay = new IntProperty("heal-delay", 50, 1, 400);
    public final IntProperty dropDelay = new IntProperty("drop-delay", 50, 1, 400);
    public final IntProperty switchDelay = new IntProperty("switch-delay", 50, 1, 400);

    private final TimerUtil healTimer = new TimerUtil();
    private final TimerUtil dropTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();

    private int soupIndex = Integer.MIN_VALUE;
    private int originalIndex = Integer.MIN_VALUE;
    private int step = 1;
    public boolean start = false;

    public AutoSoup() {
        super("AutoSoup", false);
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

        if (mc.currentScreen != null) {
            return;
        }

        if (start) {
            if (step >= 2 && step <= 3 && mc.thePlayer.inventory.currentItem != soupIndex) {
                mc.thePlayer.inventory.currentItem = soupIndex;
            }

            switch (step) {
                case 1:
                    if (switchTimer.hasTimeElapsed(this.switchDelay.getValue())) {
                        switchTimer.reset();
                        mc.thePlayer.inventory.currentItem = soupIndex;
                        step++;
                    }
                    break;
                case 2:
                    if (healTimer.hasTimeElapsed(this.healDelay.getValue())) {
                        healTimer.reset();
                        if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                            step++;
                            break;
                        }
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                    }
                    break;
                case 3:
                    if (dropSoup.getValue()) {
                        if (dropTimer.hasTimeElapsed(this.dropDelay.getValue())) {
                            dropTimer.reset();
                            C07PacketPlayerDigging.Action action = GuiScreen.isCtrlKeyDown()
                                    ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS
                                    : C07PacketPlayerDigging.Action.DROP_ITEM;
                            mc.getNetHandler().getNetworkManager().sendPacket(
                                    new C07PacketPlayerDigging(action, BlockPos.ORIGIN, EnumFacing.DOWN)
                            );
                            step++;
                        }
                    } else {
                        step++;
                    }
                    break;
                case 4:
                    if (switchTimer.hasTimeElapsed(this.switchDelay.getValue())) {
                        switchTimer.reset();
                        mc.thePlayer.inventory.currentItem = originalIndex;
                        step++;
                    }
                    break;
                case 5:
                    reset();
                    break;
            }
        } else {
            soupIndex = getSoupInHotbar();
            if (soupIndex != Integer.MIN_VALUE) {
                if (mc.thePlayer.getHealth() <= this.health.getValue()) {
                    originalIndex = mc.thePlayer.inventory.currentItem;
                    start = true;
                }
            }
        }
    }

    private void reset() {
        healTimer.reset();
        dropTimer.reset();
        switchTimer.reset();
        originalIndex = Integer.MIN_VALUE;
        soupIndex = Integer.MIN_VALUE;
        start = false;
        step = 1;
    }
}

