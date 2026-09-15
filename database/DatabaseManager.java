package com.hydraz.store.database;

import com.hydraz.store.models.PendingDelivery;
import com.hydraz.store.models.Transaction;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DatabaseManager {
    private final String url;

    public DatabaseManager(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "store.db");
        this.url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        this.initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = this.getConnection();
             Statement stmt = conn.createStatement();){
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT,ign TEXT NOT NULL,cart_items TEXT NOT NULL,status TEXT NOT NULL,screenshot_path TEXT NOT NULL,amount REAL DEFAULT 0.0);");
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN cart_items TEXT NOT NULL DEFAULT '';");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN amount REAL DEFAULT 0.0;");
            } catch (SQLException ignored) {}
            stmt.execute("CREATE TABLE IF NOT EXISTS offline_commands (id INTEGER PRIMARY KEY AUTOINCREMENT,ign TEXT NOT NULL,command TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS pending_deliveries (id INTEGER PRIMARY KEY AUTOINCREMENT, transaction_id INTEGER, ign TEXT NOT NULL, server_target TEXT NOT NULL, command TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS ai_audit_log (id INTEGER PRIMARY KEY AUTOINCREMENT,session_id TEXT NOT NULL,role TEXT NOT NULL,action TEXT NOT NULL,detail TEXT,result TEXT,timestamp TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS admin_users (username TEXT PRIMARY KEY, password_hash TEXT NOT NULL, salt TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS practice_tiers (ign TEXT PRIMARY KEY, tier TEXT NOT NULL, elo INTEGER NOT NULL);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url);
    }

    // --- Admin Users ---
    public void addAdminUser(String username, String hash, String salt) {
        String sql = "INSERT OR REPLACE INTO admin_users(username, password_hash, salt) VALUES(?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getAdminCredentials(String username) {
        String sql = "SELECT password_hash, salt FROM admin_users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, String> creds = new HashMap<>();
                creds.put("hash", rs.getString("password_hash"));
                creds.put("salt", rs.getString("salt"));
                return creds;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Practice Tiers ---
    public void addOrUpdateTier(String ign, String tier, int elo) {
        String sql = "INSERT OR REPLACE INTO practice_tiers(ign, tier, elo) VALUES(?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ign);
            pstmt.setString(2, tier);
            pstmt.setInt(3, elo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeTier(String ign) {
        String sql = "DELETE FROM practice_tiers WHERE ign = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ign);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllTiers() {
        List<Map<String, Object>> tiers = new ArrayList<>();
        String sql = "SELECT ign, tier, elo FROM practice_tiers ORDER BY elo DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("ign", rs.getString("ign"));
                map.put("tier", rs.getString("tier"));
                map.put("elo", rs.getInt("elo"));
                tiers.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tiers;
    }

    // --- Other methods from CFR ---
    public int insertTransaction(String ign, String cartItems, String screenshotPath, double amount, String status) {
        String sql = "INSERT INTO transactions(ign, cart_items, status, screenshot_path, amount) VALUES(?,?,?,?,?)";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ign);
            pstmt.setString(2, cartItems);
            pstmt.setString(3, status != null ? status : "VERIFYING");
            pstmt.setString(4, screenshotPath);
            pstmt.setDouble(5, amount);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertTransaction(String ign, String cartItems, String screenshotPath, double amount) {
        return insertTransaction(ign, cartItems, screenshotPath, amount, "VERIFYING");
    }

    public int insertTransaction(String ign, String cartItems, String screenshotPath) {
        return insertTransaction(ign, cartItems, screenshotPath, 0.0, "VERIFYING");
    }

    public Transaction getTransactionById(int id) {
        return getTransaction(id);
    }

    public void updateTransactionStatus(int id, String status) {
        String sql = "UPDATE transactions SET status = ? WHERE id = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> getTransactionsByPlayer(String ign) {
        ArrayList<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE ign = ? ORDER BY id DESC";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){
            pstmt.setString(1, ign);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double amt = 0.0;
                try { amt = rs.getDouble("amount"); } catch (Exception ignored) {}
                list.add(new Transaction(rs.getInt("id"), rs.getString("ign"), rs.getString("cart_items"), rs.getString("status"), rs.getString("screenshot_path"), amt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaction> getPendingTransactions() {
        ArrayList<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE (status = 'PENDING' OR status = 'VERIFYING') ORDER BY id DESC";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double amt = 0.0;
                try { amt = rs.getDouble("amount"); } catch (Exception ignored) {}
                list.add(new Transaction(rs.getInt("id"), rs.getString("ign"), rs.getString("cart_items"), rs.getString("status"), rs.getString("screenshot_path"), amt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Transaction findPendingTransactionByAmount(double amount) {
        String sql = "SELECT * FROM transactions WHERE (status = 'PENDING' OR status = 'VERIFYING') ORDER BY id DESC";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double amt = 0.0;
                try { amt = rs.getDouble("amount"); } catch (Exception ignored) {}
                if (Math.abs(amt - amount) < 0.02) {
                    return new Transaction(rs.getInt("id"), rs.getString("ign"), rs.getString("cart_items"), rs.getString("status"), rs.getString("screenshot_path"), amt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void queueOfflineCommand(String ign, String command) {
        String sql = "INSERT INTO offline_commands(ign, command) VALUES(?,?)";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){
            pstmt.setString(1, ign);
            pstmt.setString(2, command);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getOfflineCommands(String ign) {
        ArrayList<String> commands = new ArrayList<>();
        String sql = "SELECT command FROM offline_commands WHERE ign = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){
            pstmt.setString(1, ign);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                commands.add(rs.getString("command"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commands;
    }

    public void removeOfflineCommands(String ign) {
        String sql = "DELETE FROM offline_commands WHERE ign = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){
            pstmt.setString(1, ign);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Transaction getTransaction(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (java.sql.Connection conn = this.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double amt = 0.0;
                try { amt = rs.getDouble("amount"); } catch (Exception ignored) {}
                return new Transaction(rs.getInt("id"), rs.getString("ign"), rs.getString("cart_items"), rs.getString("status"), rs.getString("screenshot_path"), amt);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertOfflineCommand(String ign, String command) {
        queueOfflineCommand(ign, command);
    }

    public java.util.List<String> getAndRemoveOfflineCommands(String ign) {
        java.util.List<String> cmds = getOfflineCommands(ign);
        removeOfflineCommands(ign);
        return cmds;
    }

    public java.util.List<Map<String, Object>> getTopDonators(int limit) {
        List<Map<String, Object>> topDonators = new ArrayList<>();
        String sql = "SELECT ign, COUNT(*) as orders FROM transactions WHERE status = 'APPROVED' OR status = 'VERIFIED' GROUP BY ign ORDER BY orders DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                String ign = rs.getString("ign");
                entry.put("rank", rank++);
                entry.put("ign", ign);
                entry.put("orders", rs.getInt("orders"));
                entry.put("avatar", "https://mc-heads.net/avatar/" + ign + "/64");
                topDonators.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topDonators;
    }

    public java.util.List<Transaction> getTransactionsByIgn(String ign) {
        return getTransactionsByPlayer(ign);
    }

    // --- Pending Deliveries Queue ---
    public void addPendingDelivery(int transactionId, String ign, String serverTarget, String command) {
        String sql = "INSERT INTO pending_deliveries(transaction_id, ign, server_target, command) VALUES(?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            pstmt.setString(2, ign);
            pstmt.setString(3, serverTarget);
            pstmt.setString(4, command);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PendingDelivery> getAllPendingDeliveries() {
        List<PendingDelivery> list = new ArrayList<>();
        String sql = "SELECT id, transaction_id, ign, server_target, command FROM pending_deliveries ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new PendingDelivery(
                        rs.getInt("id"),
                        rs.getInt("transaction_id"),
                        rs.getString("ign"),
                        rs.getString("server_target"),
                        rs.getString("command")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void removePendingDelivery(int id) {
        String sql = "DELETE FROM pending_deliveries WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PendingDelivery> getPendingDeliveriesForPlayer(String ign) {
        List<PendingDelivery> list = new ArrayList<>();
        String sql = "SELECT id, transaction_id, ign, server_target, command FROM pending_deliveries WHERE ign = ? ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ign);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new PendingDelivery(
                        rs.getInt("id"),
                        rs.getInt("transaction_id"),
                        rs.getString("ign"),
                        rs.getString("server_target"),
                        rs.getString("command")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}


