package com.hydraz.store;

import com.hydraz.store.ai.AdminAuth;
import com.hydraz.store.ai.AgentClient;
import com.hydraz.store.ai.AiManager;
import com.hydraz.store.ai.AuditLogger;
import com.hydraz.store.database.DatabaseManager;
import com.hydraz.store.discord.DiscordManager;
import com.hydraz.store.listeners.PlayerJoinListener;
import com.hydraz.store.web.WebServerManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HydrazWebsitePlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private WebServerManager webServerManager;
    private DiscordManager discordManager;
    private AiManager aiManager;
    private AgentClient agentClient;
    private com.hydraz.store.pterodactyl.PterodactylManager pterodactylManager;
    private com.hydraz.store.practice.PracticeSyncManager practiceSyncManager;

    @Override
    public void onEnable() {
        // Save default config if not exists
        saveDefaultConfig();

        // Initialize Database
        getLogger().info("Initializing Database...");
        this.databaseManager = new DatabaseManager(getDataFolder());

        // Initialize Pterodactyl Manager for direct console execution
        this.pterodactylManager = new com.hydraz.store.pterodactyl.PterodactylManager(this);

        // Initialize Practice Sync Manager for live competitive tiers
        this.practiceSyncManager = new com.hydraz.store.practice.PracticeSyncManager(this);
        this.practiceSyncManager.startPeriodicSync();

        // Initialize Command Executor
        getCommand("websiteadmin").setExecutor(new WebsiteCommandExecutor(this.databaseManager, this.practiceSyncManager));

        // Initialize Web Server
        getLogger().info("Initializing Web Server...");
        int port = getConfig().getInt("web-server-port", 25573);
        this.webServerManager = new WebServerManager(this, port);
        this.webServerManager.setPracticeSyncManager(this.practiceSyncManager);

        // Initialize Agent Client (used by store and AI)
        this.agentClient = new AgentClient(getLogger());
        ConfigurationSection agentsSection = getConfig().getConfigurationSection("agents");
        if (agentsSection != null) {
            for (String serverName : agentsSection.getKeys(false)) {
                String url = getConfig().getString("agents." + serverName + ".url", "");
                String key = getConfig().getString("agents." + serverName + ".key", "");
                if (!url.isEmpty() && !key.isEmpty()) {
                    agentClient.registerServer(serverName, url, key);
                }
            }
        }

        // Initialize AI Assistant (if enabled)
        initAi();

        this.webServerManager.start();

        // Initialize Discord Bot
        getLogger().info("Initializing Discord Bot...");
        String token = getConfig().getString("discord-bot-token");
        String channelId = getConfig().getString("discord-log-channel-id");

        if (token == null || token.isEmpty() || token.equals("YOUR_BOT_TOKEN_HERE")) {
            getLogger().warning("Discord bot token not set in config.yml! Discord integration will be disabled.");
        } else if (channelId == null || channelId.isEmpty() || channelId.equals("YOUR_CHANNEL_ID_HERE")) {
            getLogger().warning("Discord channel ID not set in config.yml! Discord integration will be disabled.");
        } else {
            this.discordManager = new DiscordManager(this, token, channelId, agentClient);
        }

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getLogger().info("HydrazWebsite enabled successfully!");
    }

    private void initAi() {
        getLogger().info("Initializing AI Assistant...");

        // Build AuditLogger
        AuditLogger auditLogger = new AuditLogger(databaseManager, getDataFolder(), getLogger());

        // Build AdminAuth
        String signingSecret = AdminAuth.generateSecret();
        AdminAuth adminAuth = new AdminAuth(signingSecret, databaseManager, getLogger());

        // Build AiManager
        this.aiManager = new AiManager(this, this.getLogger());

        // Wire into WebServerManager
        webServerManager.setAiManager(aiManager);
        webServerManager.setAdminAuth(adminAuth);

        getLogger().info("AI Assistant initialized successfully with automatic free-model fallback!");
    }

    @Override
    public void onDisable() {
        if (webServerManager != null) {
            webServerManager.stop();
        }
        if (discordManager != null) {
            discordManager.shutdown();
        }
        if (aiManager != null) {
            aiManager.shutdown();
        }
        if (pterodactylManager != null) {
            pterodactylManager.shutdown();
        }
        if (practiceSyncManager != null) {
            practiceSyncManager.shutdown();
        }
        getLogger().info("HydrazWebsite disabled.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public com.hydraz.store.pterodactyl.PterodactylManager getPterodactylManager() {
        return pterodactylManager;
    }

    public com.hydraz.store.practice.PracticeSyncManager getPracticeSyncManager() {
        return practiceSyncManager;
    }

    public AiManager getAiManager() {
        return aiManager;
    }
}


