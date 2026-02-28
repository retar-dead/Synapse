package myau.command.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import myau.Myau;
import myau.command.Command;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class CSkinCommand extends Command {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public CSkinCommand() {
        super(new ArrayList<>(Arrays.asList("cskin")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() < 2) {
            ChatUtil.sendFormatted(String.format("%sUsage: .cskin <playerName|reset>", Myau.clientName));
            return;
        }

        String target = args.get(1).trim();
        if (target.equalsIgnoreCase("reset") || target.equalsIgnoreCase("clear")) {
            Myau.customSkin = null;
            Myau.customSkinSlim = false;
            ChatUtil.sendFormatted(String.format("%sCustom skin cleared", Myau.clientName));
            return;
        }

        final String username = target;
        ChatUtil.sendFormatted(String.format("%sFetching skin for %s...", Myau.clientName, username));

        new Thread(() -> {
            try {
                // 1) Resolve username -> uuid
                String profileUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
                HttpURLConnection conn = (HttpURLConnection) new URL(profileUrl).openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 204 || code == 404) {
                    ChatUtil.sendFormatted(String.format("%sPlayer %s not found", Myau.clientName, username));
                    return;
                }
                InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                JsonObject profileJson = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
                String uuid = profileJson.get("id").getAsString();

                // 2) Get session profile to extract texture url
                String sessionUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
                HttpURLConnection conn2 = (HttpURLConnection) new URL(sessionUrl).openConnection();
                conn2.setConnectTimeout(5000);
                conn2.setReadTimeout(5000);
                if (conn2.getResponseCode() >= 400) {
                    ChatUtil.sendFormatted(String.format("%sFailed to fetch profile for %s", Myau.clientName, username));
                    return;
                }
                InputStreamReader reader2 = new InputStreamReader(conn2.getInputStream(), StandardCharsets.UTF_8);
                JsonObject profileFull = new JsonParser().parse(reader2).getAsJsonObject();
                reader2.close();
                JsonArray props = profileFull.getAsJsonArray("properties");
                String texturesBase64 = null;
                for (int i = 0; i < props.size(); i++) {
                    JsonObject prop = props.get(i).getAsJsonObject();
                    if (prop.has("name") && "textures".equals(prop.get("name").getAsString())) {
                        texturesBase64 = prop.get("value").getAsString();
                        break;
                    }
                }
                if (texturesBase64 == null) {
                    ChatUtil.sendFormatted(String.format("%sNo textures property for %s", Myau.clientName, username));
                    return;
                }
                String texturesJson = new String(Base64.getDecoder().decode(texturesBase64), StandardCharsets.UTF_8);
                JsonObject textures = new JsonParser().parse(texturesJson).getAsJsonObject();
                if (!textures.has("textures") || !textures.getAsJsonObject("textures").has("SKIN")) {
                    ChatUtil.sendFormatted(String.format("%sNo skin found for %s", Myau.clientName, username));
                    return;
                }
                JsonObject skinObj = textures.getAsJsonObject("textures").getAsJsonObject("SKIN");
                String skinUrl = skinObj.get("url").getAsString();
                boolean slim = false;
                if (skinObj.has("metadata")) {
                    try {
                        JsonObject meta = skinObj.getAsJsonObject("metadata");
                        if (meta.has("model") && "slim".equalsIgnoreCase(meta.get("model").getAsString())) {
                            slim = true;
                        }
                    } catch (Exception ignored) {
                    }
                }

                // 3) Disk cache
                File cacheDir = new File("run/skins");
                if (!cacheDir.exists()) cacheDir.mkdirs();
                File skinFile = new File(cacheDir, uuid + ".png");
                BufferedImage img = null;
                if (skinFile.exists()) {
                    try (InputStream fis = new FileInputStream(skinFile)) {
                        img = ImageIO.read(fis);
                    } catch (Exception e) {
                        // fallthrough to download
                        img = null;
                    }
                }

                if (img == null) {
                    HttpURLConnection skinConn = (HttpURLConnection) new URL(skinUrl).openConnection();
                    skinConn.setConnectTimeout(5000);
                    skinConn.setReadTimeout(5000);
                    if (skinConn.getResponseCode() >= 400) {
                        ChatUtil.sendFormatted(String.format("%sFailed to download skin for %s", Myau.clientName, username));
                        return;
                    }
                    try (InputStream in = skinConn.getInputStream()) {
                        img = ImageIO.read(in);
                    }
                    if (img == null) {
                        ChatUtil.sendFormatted(String.format("%sDownloaded skin is invalid for %s", Myau.clientName, username));
                        return;
                    }
                    // save to disk
                    try (OutputStream out = new FileOutputStream(skinFile)) {
                        ImageIO.write(img, "PNG", out);
                    } catch (Exception ignored) {
                    }
                }

                final BufferedImage finalImg = img;
                // register texture on main thread
                final boolean finalSlim = slim;
                mc.addScheduledTask(() -> {
                    try {
                        DynamicTexture dyn = new DynamicTexture(finalImg);
                        ResourceLocation rl = mc.getTextureManager().getDynamicTextureLocation("cskin_" + uuid, dyn);
                        Myau.customSkin = rl;
                        Myau.customSkinSlim = finalSlim;
                        ChatUtil.sendFormatted(String.format("%sApplied skin of %s", Myau.clientName, username));
                    } catch (Exception e) {
                        ChatUtil.sendFormatted(String.format("%sFailed to apply skin: %s", Myau.clientName, e.getMessage()));
                    }
                });

            } catch (Exception e) {
                ChatUtil.sendFormatted(String.format("%sError fetching skin: %s", Myau.clientName, e.getMessage()));
            }
        }).start();
    }
}
