package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.Priority;
import myau.events.Render3DEvent;
import myau.module.Module;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

import java.util.HashSet;
import java.util.Set;

public class DevTest extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private Set<Integer> detectedArmorStands = new HashSet<>();

    public DevTest() {
        super("DevTest", false);
    }

    @Override
    public void onEnabled() {
        detectedArmorStands.clear();
        ChatUtil.sendFormatted("&7DevTest &aativado&r!");
    }

    @Override
    public void onDisabled() {
        detectedArmorStands.clear();
        ChatUtil.sendFormatted("&7DevTest &cdesativado&r!");
    }

    @EventTarget(Priority.LOW)
    public void onRender3D(Render3DEvent event) {
        if (!this.isEnabled() || mc.theWorld == null) {
            return;
        }

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
                int entityId = entity.getEntityId();
                
                // Apenas notificar sobre armor stands novos
                if (!detectedArmorStands.contains(entityId)) {
                    detectedArmorStands.add(entityId);
                    
                    String name = entity.getName();
                    String message = String.format("&e[DevTest] &7ArmorStand encontrado: &lID: &e%d &7| &lNome: &e%s&r", entityId, name);
                    ChatUtil.sendFormatted(message);
                }
            }
        }
    }
}
