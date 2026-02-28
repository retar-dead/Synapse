package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.events.RenderLivingEvent;
import myau.events.TickEvent;
import myau.event.types.EventType;
import myau.module.Module;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Cape extends Module {
    private static Minecraft getMc() {
        return Minecraft.getMinecraft();
    }

    public static final String[] CAPE_NAMES = {
            "Founder's",
            "Zombie",
            "MCE",
            "2016",
            "2015",
            "2013",
            "2012",
            "2011",
            "Cherry",
            "MapMaker",
            "Mojang",
            "MojangStudios",
            "Mojira",
            "Classic",
            "Cobalt"
    };

    public static final String[] CAPE_FILES = {
            "Founder's.png",
            "zombie.png",
            "MCE.png",
            "2016.png",
            "2015.png",
            "2013.png",
            "2012.png",
            "2011.png",
            "Cherry.png",
            "MapMaker.png",
            "Mojang.png",
            "MojangStudios.png",
            "Mojira.png",
            "Classic.png",
            "cobalt.png"
    };

    private static final Map<Integer, ResourceLocation> capeCache = new HashMap<>();
    private int lastCapeIndex = -1;

    public final ModeProperty cape = new ModeProperty("cape", 0, CAPE_NAMES);

    public Cape() {
        super("Cape", true);
        System.out.println("[Cape] Module initialized successfully");
    }

    @Override
    public String[] getSuffix() {
        int index = this.cape.getValue();
        if (index >= 0 && index < CAPE_NAMES.length) {
            return new String[]{CAPE_NAMES[index]};
        }
        return new String[0];
    }

    @Override
    public void onEnabled() {
        lastCapeIndex = -1;
        updateCape();
    }

    @Override
    public void onDisabled() {
        Myau.customCape = null;
    }

    private void updateCape() {
        if (this.isEnabled()) {
            Myau.customCape = getSelectedCape();
        } else {
            Myau.customCape = null;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            int currentIndex = this.cape.getValue();
            if (currentIndex != lastCapeIndex) {
                lastCapeIndex = currentIndex;
                updateCape();
            }
        }
    }

    @EventTarget
    public void onRenderLiving(RenderLivingEvent event) {
        if (this.isEnabled() && Myau.customCape != null && event.getEntity() instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP) event.getEntity();
            if (event.getType() == EventType.POST) {
                renderCape(player);
            }
        }
    }

    private void renderCape(EntityPlayer player) {
        if (Myau.customCape == null) return;
        try {
            Minecraft mc = getMc();
            GlStateManager.pushMatrix();
            mc.getTextureManager().bindTexture(Myau.customCape);
            GlStateManager.translate(0, 0, 0.1);
            GlStateManager.popMatrix();
        } catch (Exception ignored) {
        }
    }

    public ResourceLocation getSelectedCape() {
        int index = this.cape.getValue();
        if (index < 0 || index >= CAPE_FILES.length) {
            return null;
        }

        if (capeCache.containsKey(index)) {
            return capeCache.get(index);
        }

        BufferedImage img = null;
        String capeFileName = CAPE_FILES[index];

        // Try 1: Load from resources (src/main/resources/myau/capes/)
        try {
            String resourcePath = "/myau/capes/" + capeFileName;
            InputStream in = Cape.class.getResourceAsStream(resourcePath);
            if (in != null) {
                img = ImageIO.read(in);
                in.close();
            }
        } catch (Exception e) {
            System.out.println("[Cape] Failed to load from resources: " + capeFileName);
        }

        // Try 2: Load from Java source folder (src/main/java/myau/capes/)
        if (img == null) {
            try {
                String filePath = "src/main/java/myau/capes/" + capeFileName;
                File capeFile = new File(filePath);
                if (capeFile.exists()) {
                    img = ImageIO.read(capeFile);
                } else {
                    System.out.println("[Cape] File not found at " + filePath);
                }
            } catch (Exception e) {
                System.out.println("[Cape] Failed to load from source: " + capeFileName);
            }
        }

        // Try 3: Load from current working directory
        if (img == null) {
            try {
                File capeFile = new File(capeFileName);
                if (!capeFile.exists()) {
                    capeFile = new File("myau/capes/" + capeFileName);
                }
                if (capeFile.exists()) {
                    img = ImageIO.read(capeFile);
                }
            } catch (Exception ignored) {
            }
        }

        // Create and cache the texture if loaded
        if (img != null) {
            try {
                DynamicTexture dyn = new DynamicTexture(img);
                ResourceLocation rl = getMc().getTextureManager().getDynamicTextureLocation("cape_" + index, dyn);
                capeCache.put(index, rl);
                return rl;
            } catch (Exception e) {
                System.out.println("[Cape] Failed to create DynamicTexture: " + e.getMessage());
            }
        }

        System.out.println("[Cape] Could not load cape: " + capeFileName);
        return null;
    }
}
