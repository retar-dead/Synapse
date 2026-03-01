package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.TickEvent;
import myau.mixin.IAccessorS02PacketChat;
import myau.module.Module;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

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
            "&a&lFERIAS &r&a{player}",
            "&2&lFERIAS &r&2{player}",
            "&9&lFERIAS &r&9{player}",
            "&3&lFERIAS &r&3{player}",
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
            "&b&lYT &r&b{player}",
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

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled()) return;
        if (event.getType() != EventType.RECEIVE) return;
        if (!(event.getPacket() instanceof S02PacketChat)) return;

        S02PacketChat packet = (S02PacketChat) event.getPacket();
        IChatComponent chatComponent = packet.getChatComponent();
        String formattedText = chatComponent.getFormattedText();

        if (formattedText == null || formattedText.isEmpty()) return;

        // Try building a component-based replacement (more robust)
        IChatComponent newComp = buildTaggedComponent(chatComponent);
        if (newComp != null) {
            ((IAccessorS02PacketChat) packet).setChatComponent(newComp);
            return;
        }

        // Fallback to string-based replacement
        String modifiedText = applyTagToChatMessage(formattedText);

        if (!modifiedText.equals(formattedText)) {
            ((IAccessorS02PacketChat) packet).setChatComponent(new ChatComponentText(modifiedText));
        }
    }

    private IChatComponent buildTaggedComponent(IChatComponent original) {
        if (original == null) return null;
        String formatted = original.getFormattedText();
        if (formatted == null || formatted.isEmpty()) return null;

        String myPlayerName = mc.thePlayer.getName();
        String myPlayerDisplayName = mc.thePlayer.getDisplayName().getUnformattedText();
        String[] candidates = new String[]{myPlayerName, myPlayerDisplayName};

        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) continue;
            int[] found = findNameAndColon(formatted, candidate);
            if (found != null) {
                int start = found[0];
                int colonIdx = found[1];

                // Build tag parts
                String tagFormat = TAG_FORMATS[this.selectedTag.getValue()];
                int placeholderPos = tagFormat.indexOf("{player}");
                if (placeholderPos < 0) placeholderPos = tagFormat.length();
                int resetPos = tagFormat.lastIndexOf("&r", placeholderPos);
                String tagLabelRaw;
                String playerFormatRaw;
                if (resetPos >= 0) {
                    tagLabelRaw = tagFormat.substring(0, resetPos);
                    playerFormatRaw = tagFormat.substring(resetPos + 2, placeholderPos);
                } else {
                    tagLabelRaw = tagFormat.substring(0, placeholderPos);
                    playerFormatRaw = "";
                }
                String tagLabel = tagLabelRaw.replace('&', '§');
                String playerFormat = playerFormatRaw.replace('&', '§');

                String colorForColon = getLastColorCode(tagLabel + playerFormat);
                if (colorForColon.isEmpty()) colorForColon = "§f";

                // rest of message after colon
                String rest = formatted.substring(colonIdx + 1);

                ChatComponentText first = new ChatComponentText(tagLabel + playerFormat + candidate + colorForColon + ":" + "§r");
                if (rest != null && !rest.isEmpty()) {
                    ChatComponentText second = new ChatComponentText(rest);
                    first.appendSibling(second);
                }

                return first;
            }
        }

        return null;
    }

    private String applyTagToChatMessage(String formatted) {
        if (mc.thePlayer == null) return formatted;

        String myPlayerName = mc.thePlayer.getName();
        String myPlayerDisplayName = mc.thePlayer.getDisplayName().getUnformattedText();
        String tagFormat = TAG_FORMATS[this.selectedTag.getValue()];

        // Prepare parts of tag format to control where the colon gets its color
        int placeholderPos = tagFormat.indexOf("{player}");
        if (placeholderPos < 0) placeholderPos = tagFormat.length();
        int resetPos = tagFormat.lastIndexOf("&r", placeholderPos);
        String tagLabelRaw;
        String playerFormatRaw;
        String afterPlayerRaw;
        if (resetPos >= 0) {
            tagLabelRaw = tagFormat.substring(0, resetPos);
            playerFormatRaw = tagFormat.substring(resetPos + 2, placeholderPos);
        } else {
            tagLabelRaw = tagFormat.substring(0, placeholderPos);
            playerFormatRaw = "";
        }
        afterPlayerRaw = (placeholderPos < tagFormat.length()) ? tagFormat.substring(placeholderPos + "{player}".length()) : "";

        String tagLabel = tagLabelRaw.replace('&', '§');
        String playerFormat = playerFormatRaw.replace('&', '§');
        String afterPlayer = afterPlayerRaw.replace('&', '§');

        // Robust scan: find occurrences of the player name and look ahead for a colon,
        // skipping color codes ("§x") and spaces. This handles cases where server splits
        // components or inserts formatting between name and colon.
        String[] candidates = new String[]{myPlayerName, myPlayerDisplayName};
        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) continue;
            int[] found = findNameAndColon(formatted, candidate);
            if (found != null) {
                int start = found[0];
                int colonIdx = found[1];
                String before = formatted.substring(0, start);
                String rest = formatted.substring(colonIdx + 1);
                String colorForColon = getLastColorCode(tagLabel + playerFormat);
                if (colorForColon.isEmpty()) colorForColon = "§f";
                String replacement = before + tagLabel + playerFormat + candidate + colorForColon + ":" + "§r" + rest;
                return replacement;
            }
        }

        return formatted;
    }

    private String getLastColorCode(String s) {
        if (s == null) return "";
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '§' && i + 1 < s.length()) {
                char code = Character.toLowerCase(s.charAt(i + 1));
                if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
                    return "§" + code;
                }
            }
        }
        return "";
    }

    /**
     * Find the start index of the candidate name in the formatted string and the index of the colon after it.
     * Returns int[]{startIndex, colonIndex} or null if not found.
     */
    private int[] findNameAndColon(String formatted, String candidate) {
        int len = formatted.length();
        for (int start = 0; start < len; start++) {
            int i = start;
            int j = 0;
            // try to match candidate ignoring color codes
            while (i < len && j < candidate.length()) {
                char c = formatted.charAt(i);
                if (c == '§') {
                    i += 2; // skip formatting
                    continue;
                }
                if (formatted.charAt(i) == candidate.charAt(j)) {
                    i++; j++;
                } else {
                    break;
                }
            }
            if (j == candidate.length()) {
                // matched full name, now look for colon after i
                int k = i;
                while (k < len) {
                    char d = formatted.charAt(k);
                    if (d == '§') { k += 2; continue; }
                    if (Character.isWhitespace(d)) { k++; continue; }
                    if (d == ':') {
                        return new int[]{start, k};
                    }
                    break;
                }
            }
        }
        return null;
    }
}