package com.hydraz.store.practice;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hydraz.store.HydrazWebsitePlugin;
import com.hydraz.store.database.DatabaseManager;
import com.hydraz.store.pterodactyl.PterodactylManager;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PracticeSyncManager {

    private final HydrazWebsitePlugin plugin;
    private final DatabaseManager databaseManager;
    private final PterodactylManager pterodactylManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final LinkedHashMap<String, Integer> tierThresholds = new LinkedHashMap<>();
    private boolean autoSyncEnabled = true;
    private int syncIntervalMinutes = 5;
    private String practiceServer = "practice";
    private String localStrikePracticePath = "C:\\Users\\Admin\\Documents\\SERVERS FILES\\hydraz-practice_plugins\\StrikePractice";

    private volatile long lastSyncTimestamp = 0;
    private volatile String lastSyncStatus = "Not yet synced";
    private volatile int lastSyncedPlayerCount = 0;
    private volatile boolean isSyncing = false;

    private static final Pattern IGN_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{2,20}$");

    public PracticeSyncManager(HydrazWebsitePlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.pterodactylManager = plugin.getPterodactylManager();
        loadConfiguration();
    }

    public void loadConfiguration() {
        ConfigurationSection tiersSec = plugin.getConfig().getConfigurationSection("tiers");
        if (tiersSec != null) {
            this.autoSyncEnabled = tiersSec.getBoolean("auto-sync", true);
            this.syncIntervalMinutes = Math.max(1, tiersSec.getInt("sync-interval-minutes", 5));
            this.practiceServer = tiersSec.getString("practice-server", "practice");
            this.localStrikePracticePath = tiersSec.getString("local-strikepractice-path", localStrikePracticePath);

            ConfigurationSection threshSec = tiersSec.getConfigurationSection("thresholds");
            if (threshSec != null) {
                tierThresholds.clear();
                for (String tierKey : threshSec.getKeys(false)) {
                    tierThresholds.put(tierKey.toUpperCase(), threshSec.getInt(tierKey));
                }
            }
        }

        // Apply defaults if thresholds are empty
        if (tierThresholds.isEmpty()) {
            tierThresholds.put("HT1", 1200);
            tierThresholds.put("HT2", 1150);
            tierThresholds.put("HT3", 1100);
            tierThresholds.put("HT4", 1060);
            tierThresholds.put("HT5", 1030);
            tierThresholds.put("LT1", 1010);
            tierThresholds.put("LT2", 1000);
            tierThresholds.put("LT3", 950);
            tierThresholds.put("LT4", 900);
            tierThresholds.put("LT5", 0);
        }

        plugin.getLogger().info("[PracticeSync] Initialized with thresholds: " + tierThresholds);
    }

    public void startPeriodicSync() {
        if (!autoSyncEnabled) {
            plugin.getLogger().info("[PracticeSync] Auto-sync is disabled in config.");
            return;
        }

        // Initial sync after 10 seconds
        scheduler.schedule(this::syncPracticeTiersSync, 10, TimeUnit.SECONDS);

        // Repeating sync every N minutes
        scheduler.scheduleAtFixedRate(this::syncPracticeTiersSync, syncIntervalMinutes, syncIntervalMinutes, TimeUnit.MINUTES);
        plugin.getLogger().info("[PracticeSync] Periodic sync scheduled every " + syncIntervalMinutes + " minutes.");
    }

    public CompletableFuture<Integer> syncPracticeTiersAsync() {
        return CompletableFuture.supplyAsync(this::syncPracticeTiersSync, scheduler);
    }

    /**
     * Trigger sync in background if last sync is older than minIntervalMs.
     */
    public void syncIfStale(long minIntervalMs) {
        if (!autoSyncEnabled) return;
        if (isSyncing) return;
        if (System.currentTimeMillis() - lastSyncTimestamp > minIntervalMs) {
            syncPracticeTiersAsync();
        }
    }

    public synchronized int syncPracticeTiersSync() {
        if (isSyncing) {
            plugin.getLogger().info("[PracticeSync] Sync already in progress, skipping duplicate run.");
            return lastSyncedPlayerCount;
        }

        isSyncing = true;
        int count = 0;
        String source = "";

        try {
            Map<String, Double> eloMap = new HashMap<>();

            // 1. Try Pterodactyl Panel API
            if (pterodactylManager != null) {
                String remoteJson = pterodactylManager.getFileContentSync(practiceServer, "/plugins/StrikePractice/top_stats_cache.json");
                if (remoteJson != null && !remoteJson.trim().isEmpty() && !remoteJson.contains("ServerStateConflictException")) {
                    try {
                        JsonObject root = JsonParser.parseString(remoteJson).getAsJsonObject();
                        if (root.has("global_elo") && root.get("global_elo").isJsonObject()) {
                            JsonObject eloJson = root.getAsJsonObject("global_elo");
                            for (Map.Entry<String, JsonElement> entry : eloJson.entrySet()) {
                                try {
                                    eloMap.put(entry.getKey(), entry.getValue().getAsDouble());
                                } catch (Exception ignored) {}
                            }
                            source = "Pterodactyl Panel API (" + practiceServer + ")";
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[PracticeSync] Failed to parse JSON from panel: " + e.getMessage());
                    }
                }
            }

            // 2. Fallback to local files if panel was empty, offline, or suspended
            if (eloMap.isEmpty() && localStrikePracticePath != null && !localStrikePracticePath.trim().isEmpty()) {
                File localDir = new File(localStrikePracticePath);
                File localCacheFile = new File(localDir, "top_stats_cache.json");

                if (localCacheFile.exists() && localCacheFile.canRead()) {
                    try {
                        String localJson = Files.readString(localCacheFile.toPath(), StandardCharsets.UTF_8);
                        JsonObject root = JsonParser.parseString(localJson).getAsJsonObject();
                        if (root.has("global_elo") && root.get("global_elo").isJsonObject()) {
                            JsonObject eloJson = root.getAsJsonObject("global_elo");
                            for (Map.Entry<String, JsonElement> entry : eloJson.entrySet()) {
                                try {
                                    eloMap.put(entry.getKey(), entry.getValue().getAsDouble());
                                } catch (Exception ignored) {}
                            }
                            source = "Local StrikePractice Cache (" + localCacheFile.getName() + ")";
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[PracticeSync] Failed reading local top_stats_cache.json: " + e.getMessage());
                    }
                }

                // Also check playerdata directory for any additional players
                File playerdataDir = new File(localDir, "playerdata");
                if (playerdataDir.exists() && playerdataDir.isDirectory()) {
                    File[] files = playerdataDir.listFiles((dir, name) -> name.endsWith(".yml"));
                    if (files != null) {
                        for (File ymlFile : files) {
                            try {
                                parsePlayerDataYml(ymlFile, eloMap);
                            } catch (Exception ignored) {}
                        }
                        if (source.isEmpty()) {
                            source = "Local playerdata directory";
                        }
                    }
                }
            }

            if (eloMap.isEmpty()) {
                lastSyncStatus = "No player data found (Panel suspended/offline and local files unavailable)";
                plugin.getLogger().warning("[PracticeSync] " + lastSyncStatus);
                return 0;
            }

            // Update database with calculated tiers
            for (Map.Entry<String, Double> entry : eloMap.entrySet()) {
                String ign = entry.getKey();
                if (ign == null || !IGN_PATTERN.matcher(ign).matches()) continue;

                int elo = (int) Math.round(entry.getValue());
                String tier = calculateTier(elo);

                databaseManager.addOrUpdateTier(ign, tier, elo);
                count++;
            }

            lastSyncTimestamp = System.currentTimeMillis();
            lastSyncedPlayerCount = count;
            lastSyncStatus = "Synced " + count + " players successfully via " + source;
            plugin.getLogger().info("[PracticeSync] ✓ " + lastSyncStatus);

        } catch (Exception e) {
            lastSyncStatus = "Sync failed: " + e.getMessage();
            plugin.getLogger().severe("[PracticeSync] Error during practice sync: " + e.getMessage());
        } finally {
            isSyncing = false;
        }

        return count;
    }

    private void parsePlayerDataYml(File ymlFile, Map<String, Double> eloMap) {
        try {
            List<String> lines = Files.readAllLines(ymlFile.toPath(), StandardCharsets.UTF_8);
            String username = null;
            Double elo = null;

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("username:")) {
                    username = line.substring("username:".length()).trim();
                } else if (line.startsWith("global_elo:")) {
                    try {
                        elo = Double.parseDouble(line.substring("global_elo:".length()).trim());
                    } catch (Exception ignored) {}
                }
            }

            if (username != null && !username.isEmpty() && elo != null) {
                // If not already in eloMap or if playerdata has a higher/newer elo
                if (!eloMap.containsKey(username)) {
                    eloMap.put(username, elo);
                }
            }
        } catch (Exception ignored) {}
    }

    public String calculateTier(int elo) {
        for (Map.Entry<String, Integer> entry : tierThresholds.entrySet()) {
            if (elo >= entry.getValue()) {
                return entry.getKey();
            }
        }
        return "LT5";
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    public long getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public String getLastSyncStatus() {
        return lastSyncStatus;
    }

    public int getLastSyncedPlayerCount() {
        return lastSyncedPlayerCount;
    }

    public boolean isAutoSyncEnabled() {
        return autoSyncEnabled;
    }
}
