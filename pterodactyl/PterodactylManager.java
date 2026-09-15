package com.hydraz.store.pterodactyl;

import com.hydraz.store.HydrazWebsitePlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PterodactylManager {

    private final HydrazWebsitePlugin plugin;
    private final String baseUrl;
    private final String apiKey;
    private final Map<String, String> serverIdMap = new HashMap<>();
    private final Map<String, String> serverBaseUrlMap = new HashMap<>();
    private final ExecutorService httpExecutor = Executors.newCachedThreadPool();

    public PterodactylManager(HydrazWebsitePlugin plugin) {
        this.plugin = plugin;
        this.baseUrl = plugin.getConfig().getString("pterodactyl.base-url", "https://panel.infinityhost.online").replaceAll("/+$", "");
        this.apiKey = plugin.getConfig().getString("pterodactyl.api-key", "ptlc_pPq2HKl0fG2ZWR7q0nwxlAGefUPxW6I9oVHYUGHzPg2").trim();

        // Load configured server IDs or URLs
        loadServerMapping("lifesteal", "7be0b55a");
        loadServerMapping("practice", "4fd8cba7");
        loadServerMapping("survival", "a26cdec1");

        // Dynamically load any other servers from config section if present
        if (plugin.getConfig().isConfigurationSection("pterodactyl.servers")) {
            for (String key : plugin.getConfig().getConfigurationSection("pterodactyl.servers").getKeys(false)) {
                if (key.endsWith("-url")) {
                    String base = extractBaseUrl(plugin.getConfig().getString("pterodactyl.servers." + key, ""));
                    if (base != null) {
                        String realKey = key.substring(0, key.length() - 4).toLowerCase();
                        serverBaseUrlMap.put(realKey, base);
                    }
                    continue;
                }
                String val = plugin.getConfig().getString("pterodactyl.servers." + key, "");
                if (!val.isEmpty()) {
                    serverIdMap.put(key.toLowerCase(), extractServerId(val));
                    String customBase = extractBaseUrl(val);
                    if (customBase != null) serverBaseUrlMap.put(key.toLowerCase(), customBase);
                }
            }
        }

        plugin.getLogger().info("Pterodactyl Manager initialized: Base URL: " + baseUrl + " | Servers: " + serverIdMap + " | Panel URLs: " + serverBaseUrlMap);
    }

    private void loadServerMapping(String key, String defaultId) {
        String val = plugin.getConfig().getString("pterodactyl.servers." + key, "");
        String urlVal = plugin.getConfig().getString("pterodactyl.servers." + key + "-url", "");
        if (val.isEmpty()) {
            val = !urlVal.isEmpty() ? urlVal : defaultId;
        }
        serverIdMap.put(key.toLowerCase(), extractServerId(val));
        String customBase = extractBaseUrl(!urlVal.isEmpty() ? urlVal : val);
        if (customBase != null) {
            serverBaseUrlMap.put(key.toLowerCase(), customBase);
        }
    }

    private String extractServerId(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String trimmed = input.trim();
        if (trimmed.contains("/server/")) {
            String[] parts = trimmed.split("/server/");
            if (parts.length > 1) {
                return parts[1].replaceAll("/.*$", "").trim();
            }
        }
        return trimmed.replaceAll("^.*/", "").replaceAll("/+$", "").trim();
    }

    private String extractBaseUrl(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String trimmed = input.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                URI uri = URI.create(trimmed);
                return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Executes a console command on the target server asynchronously.
     */
    public CompletableFuture<Boolean> executeCommand(String serverTarget, String command) {
        return CompletableFuture.supplyAsync(() -> executeCommandSync(serverTarget, command), httpExecutor);
    }

    /**
     * Executes a console command on the target server synchronously.
     */
    public boolean executeCommandSync(String serverTarget, String command) {
        String targetKey = serverTarget.toLowerCase();
        String serverId = serverIdMap.getOrDefault(targetKey, targetKey);

        if (serverId == null || serverId.isEmpty()) {
            plugin.getLogger().severe("No Pterodactyl server ID mapped for target: " + serverTarget);
            return false;
        }

        try {
            String targetBaseUrl = serverBaseUrlMap.getOrDefault(targetKey, baseUrl);
            String endpoint = targetBaseUrl.replaceAll("/+$", "") + "/api/client/servers/" + serverId + "/command";
            URL url = URI.create(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            // Payload: {"command": "..."}
            String jsonPayload = "{\"command\":" + com.google.gson.JsonParser.parseString("\"" + command.replace("\"", "\\\"") + "\"").toString() + "}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 204 || responseCode == 200) {
                plugin.getLogger().info("✓ [Panel API] Executed command on " + serverTarget + " (" + serverId + "): " + command);
                return true;
            } else {
                plugin.getLogger().warning("✗ [Panel API] Failed command on " + serverTarget + " (" + serverId + ") - HTTP " + responseCode);
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("✗ [Panel API] Exception executing command on " + serverTarget + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads a remote file's text content from the Pterodactyl server synchronously.
     */
    public String getFileContentSync(String serverTarget, String filePath) {
        String targetKey = serverTarget.toLowerCase();
        String serverId = serverIdMap.getOrDefault(targetKey, targetKey);

        if (serverId == null || serverId.isEmpty()) {
            plugin.getLogger().warning("No Pterodactyl server ID mapped for target: " + serverTarget);
            return null;
        }

        try {
            String targetBaseUrl = serverBaseUrlMap.getOrDefault(targetKey, baseUrl);
            String encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8);
            String endpoint = targetBaseUrl.replaceAll("/+$", "") + "/api/client/servers/" + serverId + "/files/contents?file=" + encodedPath;
            URL url = URI.create(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Accept", "application/json, text/plain, */*");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                }
            } else {
                plugin.getLogger().warning("Failed to fetch file " + filePath + " from " + serverTarget + " (" + serverId + ") - HTTP " + responseCode);
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Exception fetching file " + filePath + " from " + serverTarget + ": " + e.getMessage());
            return null;
        }
    }

    public void shutdown() {
        if (!httpExecutor.isShutdown()) {
            httpExecutor.shutdown();
        }
    }
}
