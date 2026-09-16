/*
 * Decompiled with CFR 0.152.
 */
package com.hydraz.store.ai;

import com.hydraz.store.database.DatabaseManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class AuditLogger {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private final DatabaseManager dbManager;
    private final File logFile;
    private final Logger logger;

    public AuditLogger(DatabaseManager dbManager, File dataFolder, Logger logger) {
        this.dbManager = dbManager;
        this.logFile = new File(dataFolder, "ai-audit.log");
        this.logger = logger;
    }

    public void log(String sessionId, String role, String action, String detail, String result) {
        String timestamp = FMT.format(Instant.now());
        String truncatedDetail = detail != null && detail.length() > 500 ? detail.substring(0, 500) + "..." : detail;
        this.writeToDatabase(sessionId, role, action, truncatedDetail, result, timestamp);
        this.writeToFile(timestamp, sessionId, role, action, truncatedDetail, result);
    }

    private void writeToDatabase(String sessionId, String role, String action, String detail, String result, String timestamp) {
        String sql = "INSERT INTO ai_audit_log(session_id, role, action, detail, result, timestamp) VALUES(?,?,?,?,?,?)";
        try (Connection conn = this.dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, sessionId);
            ps.setString(2, role);
            ps.setString(3, action);
            ps.setString(4, detail);
            ps.setString(5, result);
            ps.setString(6, timestamp);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            this.logger.warning("[AI Audit] DB write failed: " + e.getMessage());
        }
    }

    private void writeToFile(String timestamp, String sessionId, String role, String action, String detail, String result) {
        String line = String.format("[%s] [%s] [%s] [%s] %s \u2192 %s%n", timestamp, sessionId.substring(0, 8), role, action, detail, result);
        try {
            Files.writeString(this.logFile.toPath(), (CharSequence)line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            this.logger.warning("[AI Audit] File write failed: " + e.getMessage());
        }
    }
}

