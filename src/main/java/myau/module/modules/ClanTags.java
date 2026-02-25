package myau.module.modules;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import myau.Myau;
import myau.event.EventTarget;
import myau.events.LoadWorldEvent;
import myau.events.TickEvent;
import myau.event.types.EventType;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class ClanTags extends Module {
    public final BooleanProperty debug = new BooleanProperty("debug", false);
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String API_BASE = "https://mush.com.br/api/player/";
    private static final long DELAY_BETWEEN_REQUESTS_MS = 800;

    private final ConcurrentHashMap<String, ClanData> cache = new ConcurrentHashMap<>();
    private final Set<String> pendingFetch = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<String> fetchQueue = new LinkedBlockingQueue<>();
    private volatile boolean workerRunning = false;
    private long lastRefresh = 0;
    private long lastLobbyReset = 0;
    private static final long REFRESH_INTERVAL_MS = 60000;
    private static final long LOBBY_RESET_COOLDOWN_MS = 5000;

    public ClanTags() {
        super("ClanTags", false);
    }

    @Override
    public void onEnabled() {
        startWorker();
        refreshCache();
    }

    @Override
    public void onDisabled() {
        workerRunning = false;
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        if (isEnabled()) {
            onNewLobby();
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!isEnabled() || event.getType() != EventType.PRE) return;
        if (mc.getNetHandler() == null || mc.thePlayer == null) return;
        detectNewLobby();
    }

    private void onNewLobby() {
        long now = System.currentTimeMillis();
        if (now - lastLobbyReset < LOBBY_RESET_COOLDOWN_MS) return;
        lastLobbyReset = now;
        cache.clear();
        pendingFetch.clear();
        fetchQueue.clear();
        lastRefresh = 0;
        if (debug.getValue() && mc.thePlayer != null) {
            ChatUtil.sendFormatted(String.format("%s&7ClanTags: &aNovo lobby detectado - buscando clans...&r", Myau.clientName));
        }
    }

    private void detectNewLobby() {
        if (mc.getNetHandler() == null) return;
        List<String> currentPlayers = new ArrayList<>();
        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            String name = info.getGameProfile().getName();
            if (name != null && !name.isEmpty()) {
                currentPlayers.add(name);
            }
        }
        if (currentPlayers.isEmpty()) return;
        int cachedCount = 0;
        for (String name : currentPlayers) {
            if (cache.containsKey(name) || pendingFetch.contains(name)) cachedCount++;
        }
        if (cachedCount < 2) {
            onNewLobby();
        }
    }

    private void startWorker() {
        if (workerRunning) return;
        workerRunning = true;
        Thread worker = new Thread(() -> {
            while (workerRunning) {
                try {
                    String playerName = fetchQueue.poll(1, TimeUnit.SECONDS);
                    if (playerName != null) {
                        fetchPlayerSync(playerName);
                        Thread.sleep(DELAY_BETWEEN_REQUESTS_MS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ClanTags-API");
        worker.setDaemon(true);
        worker.start();
    }

    public void refreshCache() {
        if (mc.getNetHandler() == null) return;
        long now = System.currentTimeMillis();
        boolean forceRefresh = cache.isEmpty() && pendingFetch.isEmpty();
        if (!forceRefresh && now - lastRefresh < REFRESH_INTERVAL_MS) return;
        lastRefresh = now;

        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            String name = info.getGameProfile().getName();
            if (name != null && !name.isEmpty() && !cache.containsKey(name) && !pendingFetch.contains(name)) {
                queueFetch(name);
            }
        }
    }

    private void queueFetch(String playerName) {
        if (pendingFetch.add(playerName)) {
            fetchQueue.offer(playerName);
            if (debug.getValue() && mc.thePlayer != null) {
                ChatUtil.sendFormatted(String.format("%s&7ClanTags: &fRequest enfileirado -> &e%s &7(%s%s)&r", Myau.clientName, playerName, API_BASE, playerName));
            }
        }
    }

    private void fetchPlayerSync(String playerName) {
        String url = API_BASE + playerName;
        if (debug.getValue() && mc.thePlayer != null) {
            ChatUtil.sendFormatted(String.format("%s&7ClanTags: &fEnviando request -> &e%s&r", Myau.clientName, url));
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Myau/1.0");

            int code = conn.getResponseCode();
            if (debug.getValue() && mc.thePlayer != null) {
                ChatUtil.sendFormatted(String.format("%s&7ClanTags: &f%s -> HTTP &e%d&r", Myau.clientName, playerName, code));
            }
            if (code != 200) {
                cache.put(playerName, ClanData.NO_CLAN);
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                parseResponse(playerName, sb.toString());
            }
        } catch (Exception e) {
            if (debug.getValue() && mc.thePlayer != null) {
                ChatUtil.sendFormatted(String.format("%s&7ClanTags: &cErro ao buscar %s -> %s&r", Myau.clientName, playerName, e.getMessage()));
            }
            cache.put(playerName, ClanData.NO_CLAN);
        } finally {
            pendingFetch.remove(playerName);
        }
    }

    private void parseResponse(String playerName, String json) {
        try {
            JsonObject root = new JsonParser().parse(json).getAsJsonObject();
            if (!root.has("success") || !root.get("success").getAsBoolean()) {
                cache.put(playerName, ClanData.NO_CLAN);
                return;
            }
            if (!root.has("response")) {
                cache.put(playerName, ClanData.NO_CLAN);
                return;
            }
            JsonObject response = root.getAsJsonObject("response");
            if (response.has("clan") && response.get("clan").isJsonObject()) {
                JsonObject clan = response.getAsJsonObject("clan");
                String tag = clan.has("tag") ? clan.get("tag").getAsString() : (clan.has("name") ? clan.get("name").getAsString() : "");
                String color = clan.has("tag_color") ? clan.get("tag_color").getAsString() : "#ffffff";
                cache.put(playerName, new ClanData(tag, color));
                if (debug.getValue() && mc.thePlayer != null) {
                    ChatUtil.sendFormatted(String.format("%s&7ClanTags: &a%s -> clan &e[ %s ]&a &7(%s)&r", Myau.clientName, playerName, tag, color));
                }
            } else {
                cache.put(playerName, ClanData.NO_CLAN);
                if (debug.getValue() && mc.thePlayer != null) {
                    ChatUtil.sendFormatted(String.format("%s&7ClanTags: &7%s -> sem clan&r", Myau.clientName, playerName));
                }
            }
        } catch (Exception e) {
            cache.put(playerName, ClanData.NO_CLAN);
        }
    }

    public String getClanSuffix(String playerName) {
        refreshCache();
        if (!cache.containsKey(playerName) && !pendingFetch.contains(playerName)) {
            queueFetch(playerName);
            return null;
        }
        ClanData data = cache.get(playerName);
        if (data == null) return null;
        if (data == ClanData.NO_CLAN) return " §c§lX";
        return " " + hexToMcColor(data.color) + "[ " + data.tag + " ]";
    }

    private String hexToMcColor(String hex) {
        if (hex == null || !hex.startsWith("#") || hex.length() != 7) return "§f";
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return nearestColorCode(r, g, b);
        } catch (Exception e) {
            return "§f";
        }
    }

    private String nearestColorCode(int r, int g, int b) {
        int[][] colors = {
                {0, 0, 0}, {170, 0, 170}, {0, 170, 0}, {0, 170, 170},
                {170, 0, 0}, {170, 85, 0}, {170, 170, 0}, {85, 85, 85},
                {85, 85, 85}, {85, 85, 255}, {85, 255, 85}, {85, 255, 255},
                {255, 85, 85}, {255, 85, 255}, {255, 255, 85}, {255, 255, 255}
        };
        char[] codes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int best = 0;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < colors.length; i++) {
            int dr = r - colors[i][0], dg = g - colors[i][1], db = b - colors[i][2];
            int dist = dr * dr + dg * dg + db * db;
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return "§" + codes[best];
    }

    public static class ClanData {
        public static final ClanData NO_CLAN = new ClanData("", "");
        public final String tag;
        public final String color;

        public ClanData(String tag, String color) {
            this.tag = tag;
            this.color = color;
        }
    }
}
