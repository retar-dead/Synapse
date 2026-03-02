package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.Priority;
import myau.events.UpdateEvent;
import myau.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class AntiHulk extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public AntiHulk() {
        super("AntiHulk", false);
    }

    @EventTarget(Priority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) {
            return;
        }

        if (mc.thePlayer.isRiding()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        }
    }
}
