package com.hydraz.store.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hydraz.store.HydrazWebsitePlugin;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class AiManager {
    private final HydrazWebsitePlugin plugin;
    private final Logger logger;
    private final Map<String, AiSession> sessions = new ConcurrentHashMap<>();
    private final List<String> dynamicKnowledge = new CopyOnWriteArrayList<>();
    private final Gson gson = new Gson();

    private final String apiKey;

    // Automatic free model fallback list
    private static final List<String> FREE_MODELS = List.of(
            "openrouter/free",
            "google/gemma-4-31b-it:free",
            "google/gemma-4-26b-a4b-it:free",
            "liquid/lfm-2.5-2.6b:free",
            "nvidia/nemotron-3-nano-30b-a3b:free",
            "nvidia/nemotron-3.5-lightning:free"
    );

    public AiManager(HydrazWebsitePlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.apiKey = plugin.getConfig().getString("ai.openrouter-api-key", "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");
    }

    public void addKnowledge(String fact) {
        if (fact == null || fact.trim().isEmpty()) return;
        String clean = fact.trim();
        if (!dynamicKnowledge.contains(clean)) {
            dynamicKnowledge.add(clean);
            logger.info("🧠 [AI Knowledge Feed] Learned new fact: " + clean);
        }
    }

    public void setKnowledgeList(List<String> facts) {
        dynamicKnowledge.clear();
        if (facts != null) {
            for (String f : facts) {
                if (f != null && !f.trim().isEmpty()) {
                    dynamicKnowledge.add(f.trim());
                }
            }
        }
        logger.info("🧠 [AI Knowledge Feed] Loaded " + dynamicKnowledge.size() + " facts from Discord channel history.");
    }

    public List<String> getDynamicKnowledge() {
        return Collections.unmodifiableList(dynamicKnowledge);
    }

    public AiSession createSession(String ign, boolean isAdmin) {
        AiSession session = new AiSession(UUID.randomUUID().toString(), ign, isAdmin);
        sessions.put(session.getSessionId(), session);
        return session;
    }

    public AiSession getSession(String sessionId) {
        AiSession s = sessions.get(sessionId);
        if (s != null && System.currentTimeMillis() - s.getLastActive() > 60 * 60 * 1000L) {
            sessions.remove(sessionId);
            return null;
        }
        return s;
    }

    public void endSession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Handles a user message by querying OpenRouter with automatic free-model fallback.
     */
    public String handleUserMessage(AiSession session, String userMessage) {
        session.addMessage("user", userMessage);

        String systemPrompt = buildSystemPrompt(session);

        // Build messages array
        JsonArray messagesArr = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        messagesArr.add(sysMsg);

        for (Map<String, String> h : session.getHistory()) {
            JsonObject m = new JsonObject();
            m.addProperty("role", h.get("role"));
            m.addProperty("content", h.get("content"));
            messagesArr.add(m);
        }

        // Try models sequentially (failover to next if one fails/rate-limited)
        for (String modelName : FREE_MODELS) {
            try {
                String reply = callOpenRouter(modelName, messagesArr);
                if (reply != null && !reply.trim().isEmpty()) {
                    session.addMessage("assistant", reply);
                    return reply;
                }
            } catch (Exception e) {
                logger.warning("OpenRouter model '" + modelName + "' failed (" + e.getMessage() + "), shifting to next free model...");
            }
        }

        return "I'm currently having trouble reaching the knowledge servers. Please ask a staff member on Discord!";
    }

    private String callOpenRouter(String model, JsonArray messages) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.add("messages", messages);
        payload.addProperty("temperature", 0.7);
        payload.addProperty("max_tokens", 300);

        URL url = URI.create("https://openrouter.ai/api/v1/chat/completions").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("HTTP-Referer", "https://store.hydraz.online");
        conn.setRequestProperty("X-Title", "Hydraz Store Assistant");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(12000);

        byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
            os.flush();
        }

        int status = conn.getResponseCode();
        if (status == 200) {
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject respJson = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray choices = respJson.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject msgObj = firstChoice.getAsJsonObject("message");
                    if (msgObj != null && msgObj.has("content")) {
                        return msgObj.get("content").getAsString().trim();
                    }
                }
            }
        } else {
            throw new RuntimeException("HTTP " + status);
        }

        return null;
    }

    private String buildSystemPrompt(AiSession session) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are HydrazBot, the friendly, helpful AI assistant for Hydraz Network (Minecraft server).\n\n");
        sb.append("SERVER INFO:\n");
        sb.append("- Server IP: play.hydraz.online\n");
        sb.append("- Server Modes: Lifesteal, Practice, Deathbound PvP\n");
        sb.append("- Official Website & Store: store.hydraz.online\n");
        sb.append("- Official Discord: discord.gg/5uDZxAY4Dp\n\n");

        sb.append("STORE CATALOG & PRICING:\n");
        sb.append("- Lifesteal Ranks: VIP Rank (₹100), TITAN Rank (₹300), Supreme Rank (₹500), Hydraz KING Rank (₹900)\n");
        sb.append("- Keys & Crates: Common Key (₹10), Epic Key (₹20), Spawner Key (₹40), Rare Key (₹50), Hydraz Key (₹65)\n");
        sb.append("- Coins: 700 Coins (₹90), 1500 Coins (₹180), 2800 Coins (₹375), 5560 Coins (₹680)\n\n");

        sb.append("ACTIVE COMMUNITY EVENT:\n");
        sb.append("- Event: Deathbound Kill Leader Tournament\n");
        sb.append("- Prize: ₹100 Real Cash (UPI) + 200 In-Game Coins\n");
        sb.append("- Ends: August 25, 2026\n");
        sb.append("- Rules: Player with the #1 highest kills on leaderboard on August 25 wins.\n\n");

        sb.append("LIVE SERVER KNOWLEDGE & FACTS (UPDATED IN REAL-TIME VIA DISCORD FEED):\n");
        if (dynamicKnowledge.isEmpty()) {
            sb.append("- No extra announcements at this time.\n");
        } else {
            for (String fact : dynamicKnowledge) {
                sb.append("• ").append(fact).append("\n");
            }
        }
        sb.append("\n");

        sb.append("USER INFO:\n");
        sb.append("- Minecraft Username: ").append(session.getIgn()).append("\n");
        sb.append("- Role: ").append(session.isAdmin() ? "Staff / Admin" : "Player").append("\n\n");

        sb.append("INSTRUCTIONS:\n");
        sb.append("- Be concise, friendly, and helpful.\n");
        sb.append("- Answer questions accurately based on the Server Info, Store Catalog, and Live Server Knowledge above.\n");
        sb.append("- If asked about who the owner or staff is, or server rules, ALWAYS refer to the Live Server Knowledge.\n");

        return sb.toString();
    }

    public void shutdown() {
        sessions.clear();
    }
}
