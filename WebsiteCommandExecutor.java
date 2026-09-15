package com.hydraz.store;

import com.hydraz.store.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class WebsiteCommandExecutor implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final com.hydraz.store.practice.PracticeSyncManager practiceSyncManager;

    public WebsiteCommandExecutor(DatabaseManager databaseManager, com.hydraz.store.practice.PracticeSyncManager practiceSyncManager) {
        this.databaseManager = databaseManager;
        this.practiceSyncManager = practiceSyncManager;
    }

    public WebsiteCommandExecutor(DatabaseManager databaseManager) {
        this(databaseManager, null);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length >= 1 && (args[0].equalsIgnoreCase("synctiers") || args[0].equalsIgnoreCase("sync"))) {
            if (practiceSyncManager == null) {
                sender.sendMessage(ChatColor.RED + "PracticeSyncManager is not initialized.");
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "Triggering practice tiers synchronization in background...");
            practiceSyncManager.syncPracticeTiersAsync().thenAccept(count -> {
                sender.sendMessage(ChatColor.GREEN + "✓ Practice tiers sync complete! Updated " + count + " players (" + practiceSyncManager.getLastSyncStatus() + ")");
            }).exceptionally(ex -> {
                sender.sendMessage(ChatColor.RED + "✗ Practice tiers sync failed: " + ex.getMessage());
                return null;
            });
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
            String username = args[1];
            String password = args[2];

            String salt = generateSalt();
            String hash = hashPassword(password, salt);

            databaseManager.addAdminUser(username, hash, salt);

            sender.sendMessage(ChatColor.GREEN + "Website admin '" + username + "' created successfully!");
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.GRAY + "They can now log into the web UI.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /websiteadmin add <username> <password> OR /websiteadmin synctiers");
        return true;
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
