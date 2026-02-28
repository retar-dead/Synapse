package myau.module.modules;

import myau.event.EventTarget;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;

public class Tag extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    

    private static final String[] TAGS = {
            "Blade", "Master", "Vip", "Pro", "Mvp", "Ultra", "Enderlore",
            "Férias claro", "Férias escuro", "Férias azul", "Férias ciano",
            "Carnaval", "Natal",
            "2017", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026",
            "Nitro", "Beta", "Champion", "Streamer", "Yt", "Partner", "Partner+",
            "Builder", "Helper", "Trial", "Mod", "Mod+", "Admin"
    };

    private static final String[] TAG_FORMATS = {
            "&e&lBLADE &r&e{player}",
            "&6&lMASTER &r&6{player}",
            "&a&lVIP &r&a{player}",
            "&6&lPRO &r&6{player}",
            "&9&lMVP &r&9{player}",
            "&d&lULTRA &r&d{player}",
            "&5&lENDERLORE &r&5{player}",
            "&a&lFÉRIAS &r&a{player}",
            "&2&lFÉRIAS &r&2{player}",
            "&9&lFÉRIAS &r&9{player}",
            "&3&lFÉRIAS &r&3{player}",
            "&6&lCARNAVAL &r&6{player}",
            "&c&lNATAL &r&c{player}",
            "&b&l2017 &r&b{player}",
            "&b&l2019 &r&b{player}",
            "&b&l2020 &r&b{player}",
            "&b&l2021 &r&b{player}",
            "&b&l2022 &r&b{player}",
            "&b&l2023 &r&b{player}",
            "&b&l2024 &r&b{player}",
            "&b&l2025 &r&b{player}",
            "&b&l2026 &r&b{player}",
            "&d&lNITRO &r&d{player}",
            "&1&lBETA &r&1{player}",
            "&6&lCHAMPION &r&6{player}",
            "&b&lSTREAMER &r&b{player}",
            "&a&lYT &r&a{player}",
            "&b&lPARTNER &r&b{player}",
            "&3&lPARTNER+ &r&3{player}",
            "&2&lBUILDER &r&2{player}",
            "&9&lHELPER &r&9{player}",
            "&d&lTRIAL &r&d{player}",
            "&5&lMOD &r&5{player}",
            "&5&lMOD+ &r&5{player}",
            "&4&lADMIN &r&4{player}"
    };
    
    public final ModeProperty selectedTag = new ModeProperty("tag", 0, TAGS);
    
    public Tag() {
        super("Tag", false);
    }
    
    public String getTaggedName(String playerName) {
        if (!this.isEnabled() || mc.thePlayer == null || playerName == null || playerName.isEmpty()) {
            return playerName;
        }
        
        String myPlayerName = mc.thePlayer.getName();
        String myPlayerDisplayName = mc.thePlayer.getDisplayName().getUnformattedText();
        
        // Remove color codes from input name for comparison
        String cleanPlayerName = playerName.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Apply tag to local player name (check multiple formats)
        boolean isMyName = cleanPlayerName.equalsIgnoreCase(myPlayerName) || 
                          playerName.equalsIgnoreCase(myPlayerName) ||
                          cleanPlayerName.equalsIgnoreCase(myPlayerDisplayName);
        
        if (isMyName) {
            String tagFormat = TAG_FORMATS[this.selectedTag.getValue()];
            
            // Convert & to § for Minecraft color codes and replace {player} with actual name
            tagFormat = tagFormat.replace('&', '§').replace("{player}", myPlayerName);
            
            return tagFormat;
        }
        
        return playerName;
    }
    
    @Override
    public String[] getSuffix() {
        String selectedTagName = TAGS[this.selectedTag.getValue()];
        return new String[]{selectedTagName};
    }
    
    @EventTarget
    public void onTick(TickEvent event) {
        // Keep-alive tick - tag rendering is handled by MixinFontRenderer
    }
}