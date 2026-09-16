package com.hydraz.store.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AiSession {
    private final String sessionId;
    private final String ign;
    private final boolean admin;
    private final List<Map<String, String>> history = new ArrayList<>();
    private long lastActive = System.currentTimeMillis();

    public AiSession(String sessionId, String ign, boolean admin) {
        this.sessionId = sessionId;
        this.ign = ign;
        this.admin = admin;
    }

    public String getSessionId() { return sessionId; }
    public String getIgn() { return ign; }
    public boolean isAdmin() { return admin; }

    public synchronized List<Map<String, String>> getHistory() {
        return new ArrayList<>(history);
    }

    public synchronized void addMessage(String role, String content) {
        history.add(Map.of("role", role, "content", content));
        if (history.size() > 20) {
            history.remove(0);
        }
        this.lastActive = System.currentTimeMillis();
    }

    public long getLastActive() {
        return lastActive;
    }
}
