package myau.module.modules;

import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.event.types.Priority;
import myau.events.Render2DEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Timers extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    private static final int[] TARGET_IDS = {29, 31};
    
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+):(\\d{2})");
    
    private Map<Integer, Long> timerMap = new HashMap<>();
    
    private Map<Integer, Long> lastFoundTime = new HashMap<>();
    
    private Map<Integer, Long> startTick = new HashMap<>();
    
    private Map<Integer, Boolean> found = new HashMap<>();
    public final ModeProperty displayMode = new ModeProperty("display", 0, new String[]{"Both", "Feast", "Arena Final"});

    public Timers() {
        super("Timers", false);
        for (int id : TARGET_IDS) {
            timerMap.put(id, 0L);
            lastFoundTime.put(id, 0L);
            found.put(id, false);
            startTick.put(id, 0L);
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{displayMode.getModeString()};
    }

    @Override
    public void onEnabled() {
        for (int id : TARGET_IDS) {
            timerMap.put(id, 0L);
            lastFoundTime.put(id, 0L);
            found.put(id, false);
            startTick.put(id, 0L);
        }
    }

    @Override
    public void onDisabled() {
    }

    @EventTarget(Priority.LOW)
    public void onTick(TickEvent event) {
        if (!this.isEnabled() || mc.theWorld == null) {
            return;
        }

        for (int targetId : TARGET_IDS) {
            boolean foundEntity = false;
            
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityArmorStand && entity.getEntityId() == targetId) {
                    foundEntity = true;
                    String name = entity.getName();
                    
                    Matcher matcher = TIME_PATTERN.matcher(name);
                    if (matcher.find()) {
                        int minutes = Integer.parseInt(matcher.group(1));
                        int seconds = Integer.parseInt(matcher.group(2));
                        long totalSeconds = minutes * 60L + seconds;
                        
                        if (!found.get(targetId) || lastFoundTime.get(targetId) != totalSeconds) {
                            lastFoundTime.put(targetId, totalSeconds);
                            startTick.put(targetId, System.currentTimeMillis());
                            found.put(targetId, true);
                        }
                    }
                    break;
                }
            }
            
            if (found.get(targetId)) {
                long elapsedSeconds = (System.currentTimeMillis() - startTick.get(targetId)) / 1000;
                long newTime = lastFoundTime.get(targetId) - elapsedSeconds;
                
                if (newTime >= 0) {
                    timerMap.put(targetId, newTime);
                } else {
                    timerMap.put(targetId, 0L);
                }
            }
        }
    }

    @EventTarget(Priority.LOW)
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        int yOffset = 10;
        int mode = displayMode.getValue();
        
        if (mode == 0 || mode == 1) {
            renderTimer(29, "&6&lFEAST: &f", yOffset);
            yOffset += 12;
        }
        
        if (mode == 0 || mode == 2) {
            renderTimer(31, "&5&lARENA FINAL: &f", yOffset);
        }
    }
    
    private void renderTimer(int targetId, String label, int yOffset) {
        long seconds = timerMap.getOrDefault(targetId, 0L);
        long minutes = seconds / 60;
        long secs = seconds % 60;
        
        String timerDisplay = String.format("%d:%02d", minutes, secs);
        String fullText = label + timerDisplay;
        
        String formattedText = ChatColors.formatColor(fullText);
        mc.fontRendererObj.drawStringWithShadow(formattedText, 10, yOffset, -1);
    }
}
