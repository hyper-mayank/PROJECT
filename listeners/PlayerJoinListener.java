package com.hydraz.store.listeners;

import com.hydraz.store.HydrazWebsitePlugin;
import com.hydraz.store.models.PendingDelivery;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private final HydrazWebsitePlugin plugin;

    public PlayerJoinListener(HydrazWebsitePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String playerName = e.getPlayer().getName();

        // 1. Process legacy offline commands
        List<String> offlineCommands = this.plugin.getDatabaseManager().getAndRemoveOfflineCommands(playerName);
        for (String cmd : offlineCommands) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
        }

        // 2. Process pending store deliveries if connected locally
        List<PendingDelivery> pending = this.plugin.getDatabaseManager().getPendingDeliveriesForPlayer(playerName);
        if (pending != null && !pending.isEmpty()) {
            for (PendingDelivery delivery : pending) {
                if (plugin.getDiscordManager() != null) {
                    plugin.getDiscordManager().sendCommandToDiscord(delivery.getServerTarget(), delivery.getCommand());
                }
                plugin.getDatabaseManager().removePendingDelivery(delivery.getId());
            }
        }
    }
}
