package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.Priority;
import myau.events.UpdateEvent;
import myau.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class AntiHulk extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private boolean wasRiding = false;

    public AntiHulk() {
        super("AntiHulk", false);
    }

    @EventTarget(Priority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) {
            return;
        }

        boolean isRiding = mc.thePlayer.isRiding();

        // Ao montar, apertar shift
        if (isRiding && !wasRiding) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        }
        // Ao desmontar, soltar shift
        else if (!isRiding && wasRiding) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }

        wasRiding = isRiding;
    }
}
