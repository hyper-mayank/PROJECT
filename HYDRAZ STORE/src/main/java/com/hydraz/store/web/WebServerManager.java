package com.hydraz.store.web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hydraz.store.HydrazWebsitePlugin;
import com.hydraz.store.ai.AdminAuth;
import com.hydraz.store.ai.AiManager;
import com.hydraz.store.ai.AiSession;
import com.hydraz.store.data.Product;
import com.hydraz.store.data.Products;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import com.hydraz.store.models.Transaction;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class WebServerManager {
    private final HydrazWebsitePlugin plugin;
    private Javalin app;
    private final int port;
    private final File screenshotsFolder;
    private final Gson gson = new Gson();
    
    private AiManager aiManager;
    private AdminAuth adminAuth;
    private com.hydraz.store.practice.PracticeSyncManager practiceSyncManager;

    private final Map<String, Long> recentPayments = new ConcurrentHashMap<>();

    private static final Pattern IGN_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{2,20}$");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public WebServerManager(HydrazWebsitePlugin plugin, int port) {
        this.plugin = plugin;
        this.port = port;
        this.screenshotsFolder = new File(plugin.getDataFolder(), "screenshots");
        if (!this.screenshotsFolder.exists()) {
            this.screenshotsFolder.mkdirs();
        }
    }

    public void setAiManager(AiManager aiManager) {
        this.aiManager = aiManager;
    }

    public void setAdminAuth(AdminAuth adminAuth) {
        this.adminAuth = adminAuth;
    }

    public void setPracticeSyncManager(com.hydraz.store.practice.PracticeSyncManager practiceSyncManager) {
        this.practiceSyncManager = practiceSyncManager;
    }

    public void start() {
        new Thread(() -> {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(WebServerManager.class.getClassLoader());

                app = Javalin.create(config -> {
                    config.showJavalinBanner = false;
                    config.staticFiles.add(staticFiles -> {
                        staticFiles.hostedPath = "/";
                        staticFiles.directory = "/web";
                        staticFiles.location = Location.CLASSPATH;
                    });
                    config.spaRoot.addFile("/", "/web/index.html");
                    config.spaRoot.addFile("/store", "/web/store.html");
                    config.spaRoot.addFile("/tiers", "/web/tiers.html");
                    config.spaRoot.addFile("/cart.html", "/web/cart.html");
                }).start(port);

                app.before(ctx -> {
                    String path = ctx.path();
                    if (path.endsWith(".js")) {
                        ctx.header("Content-Type", "application/javascript; charset=utf-8");
                        ctx.header("Cache-Control", "no-cache, must-revalidate");
                    } else if (path.endsWith(".css")) {
                        ctx.header("Content-Type", "text/css; charset=utf-8");
                        ctx.header("Cache-Control", "no-cache, must-revalidate");
                    } else if (path.endsWith(".html") || path.equals("/") || path.equals("/store") || path.equals("/tiers")) {
                        ctx.header("Content-Type", "text/html; charset=utf-8");
                        ctx.header("Cache-Control", "no-cache, no-store, must-revalidate");
                    }
                });

                // --- Store API ---
                app.get("/api/products", this::handleGetProducts);
                app.post("/api/checkout", this::handleCheckout);
                app.get("/api/checkouts", this::handleGetCheckouts);
                app.get("/api/order-status", this::handleGetOrderStatus);
                app.get("/api/top-donators", this::handleGetTopDonators);
                app.post("/api/verify-payment", this::handleVerifyPaymentWebhook);
                app.post("/api/payment-webhook", this::handleVerifyPaymentWebhook);
                app.post("/webhook/payment", this::handleVerifyPaymentWebhook);

                // --- Tiers API ---
                app.get("/api/tiers", this::handleGetTiers);
                app.post("/api/admin/tiers", this::handleUpdateTier);
                app.delete("/api/admin/tiers", this::handleDeleteTier);
                app.post("/api/admin/sync-tiers", this::handleSyncTiers);

                // --- AI API ---
                app.post("/api/ai/session", this::handleAiCreateSession);
                app.post("/api/ai/admin/login", this::handleAiAdminLogin);
                app.post("/api/ai/chat", this::handleAiChat);
                app.delete("/api/ai/session", this::handleAiDeleteSession);

                Thread.currentThread().setContextClassLoader(classLoader);
                plugin.getLogger().info("Web server started on port " + port);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to start web server: " + e.getMessage());
            }
        }).start();
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    // --- Tiers API ---
    private void handleGetTiers(Context ctx) {
        if (practiceSyncManager != null) {
            // Trigger throttled background sync if data is older than 2 minutes
            practiceSyncManager.syncIfStale(120_000);
        }
        ctx.json(plugin.getDatabaseManager().getAllTiers());
    }

    private void handleSyncTiers(Context ctx) {
        String token = ctx.header("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (adminAuth != null && !adminAuth.verifyToken(token)) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        if (practiceSyncManager == null) {
            ctx.status(500).json(Map.of("error", "PracticeSyncManager not available"));
            return;
        }

        int updated = practiceSyncManager.syncPracticeTiersSync();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("updated", updated);
        resp.put("status", practiceSyncManager.getLastSyncStatus());
        resp.put("timestamp", practiceSyncManager.getLastSyncTimestamp());
        ctx.json(resp);
    }

    private void handleUpdateTier(Context ctx) {
        String token = ctx.header("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!adminAuth.verifyToken(token)) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        String ign = ctx.formParam("ign");
        String tier = ctx.formParam("tier");
        String eloStr = ctx.formParam("elo");
        
        if (ign == null || tier == null || eloStr == null || !IGN_PATTERN.matcher(ign).matches()) {
            ctx.status(400).result("Invalid input");
            return;
        }
        
        try {
            int elo = Integer.parseInt(eloStr);
            plugin.getDatabaseManager().addOrUpdateTier(ign, tier, elo);
            ctx.status(200).result("Success");
        } catch (Exception e) {
            ctx.status(400).result("Invalid elo format");
        }
    }

    private void handleDeleteTier(Context ctx) {
        String token = ctx.header("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!adminAuth.verifyToken(token)) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        String ign = ctx.queryParam("ign");
        if (ign == null) {
            ctx.status(400).result("Invalid input");
            return;
        }

        plugin.getDatabaseManager().removeTier(ign);
        ctx.status(200).result("Success");
    }

    // --- AI API Handlers ---
    private void handleAiAdminLogin(Context ctx) {
        String user = ctx.formParam("username");
        String pass = ctx.formParam("password");
        if (adminAuth.authenticate(user, pass)) {
            String token = adminAuth.generateToken(user);
            ctx.json(Map.of("token", token));
        } else {
            ctx.status(401).json(Map.of("error", "Invalid credentials"));
        }
    }

    private void handleAiCreateSession(Context ctx) {
        if (aiManager == null) {
            ctx.status(503).json(Map.of("error", "AI disabled"));
            return;
        }

        String ign = null;
        String adminToken = null;

        try {
            String bodyStr = ctx.body();
            if (bodyStr != null && bodyStr.trim().startsWith("{")) {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(bodyStr).getAsJsonObject();
                if (json.has("ign")) ign = json.get("ign").getAsString();
                if (json.has("adminToken")) adminToken = json.get("adminToken").getAsString();
            }
        } catch (Exception ignored) {}

        if (ign == null) ign = ctx.formParam("ign");
        if (adminToken == null) adminToken = ctx.formParam("adminToken");

        boolean isAdmin = false;
        if (adminToken != null && !adminToken.isEmpty() && adminAuth != null) {
            isAdmin = adminAuth.verifyToken(adminToken);
            if (isAdmin) {
                ign = adminAuth.getAdminUsername(adminToken) + " (Admin)";
            }
        }

        if (ign == null || ign.trim().isEmpty() || (!isAdmin && !IGN_PATTERN.matcher(ign).matches())) {
            ign = "Guest";
        }

        AiSession session = aiManager.createSession(ign, isAdmin);
        ctx.json(Map.of(
            "sessionId", session.getSessionId(),
            "isAdmin", session.isAdmin()
        ));
    }

    private void handleAiChat(Context ctx) {
        if (aiManager == null) {
            ctx.status(503).json(Map.of("error", "AI disabled"));
            return;
        }

        String sessionId = null;
        String message = null;

        try {
            String bodyStr = ctx.body();
            if (bodyStr != null && bodyStr.trim().startsWith("{")) {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(bodyStr).getAsJsonObject();
                if (json.has("sessionId")) sessionId = json.get("sessionId").getAsString();
                if (json.has("message")) message = json.get("message").getAsString();
            }
        } catch (Exception ignored) {}

        if (sessionId == null) sessionId = ctx.formParam("sessionId");
        if (message == null) message = ctx.formParam("message");

        if (message == null || message.trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "Message cannot be empty"));
            return;
        }

        AiSession session = null;
        if (sessionId != null) {
            session = aiManager.getSession(sessionId);
        }
        if (session == null) {
            session = aiManager.createSession("Guest", false);
        }

        try {
            String reply = aiManager.handleUserMessage(session, message);
            ctx.json(Map.of(
                "reply", reply != null ? reply : "Hello! How can I help you?",
                "text", reply != null ? reply : "Hello! How can I help you?",
                "sessionId", session.getSessionId()
            ));
        } catch (Exception e) {
            plugin.getLogger().warning("AI error: " + e.getMessage());
            ctx.status(500).json(Map.of("error", "AI failed to respond: " + e.getMessage()));
        }
    }

    private void handleAiDeleteSession(Context ctx) {
        if (aiManager == null) return;
        String sessionId = ctx.queryParam("sessionId");
        if (sessionId != null) {
            aiManager.endSession(sessionId);
        }
        ctx.status(200).result("OK");
    }

    // --- Original API Handlers ---
    private void handleGetProducts(Context ctx) {
        ctx.json(Products.getAllProducts());
    }

    private void handleCheckout(Context ctx) {
        try {
            String ign = ctx.formParam("ign");
            String cartJson = ctx.formParam("cart");

            if (ign == null || !IGN_PATTERN.matcher(ign).matches()) {
                ctx.status(400).json(Map.of("success", false, "message", "Invalid Minecraft IGN."));
                return;
            }
            if (cartJson == null || cartJson.isEmpty()) {
                ctx.status(400).json(Map.of("success", false, "message", "Cart is empty."));
                return;
            }

            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> cartItems = gson.fromJson(cartJson, listType);

            if (cartItems == null || cartItems.isEmpty()) {
                ctx.status(400).json(Map.of("success", false, "message", "Cart is empty."));
                return;
            }

            // Screenshot is completely optional (no upload required for automated UPI verification)
            String fileName = "AUTO_VERIFIED";
            File targetFile = null;
            UploadedFile screenshot = ctx.uploadedFile("screenshot");
            if (screenshot != null && screenshot.size() > 0 && screenshot.size() <= MAX_FILE_SIZE) {
                try {
                    String ext = "";
                    String originalName = screenshot.filename();
                    int dotIndex = originalName.lastIndexOf('.');
                    if (dotIndex > 0) {
                        ext = originalName.substring(dotIndex);
                    }
                    if (ext.equalsIgnoreCase(".png") || ext.equalsIgnoreCase(".jpg") || ext.equalsIgnoreCase(".jpeg")) {
                        fileName = ign + "_" + System.currentTimeMillis() + ext;
                        targetFile = new File(screenshotsFolder, fileName);
                        try (InputStream is = screenshot.content()) {
                            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (Exception ignored) {}
            }

            double total = calculateTotal(cartItems);
            String finalTotalStr = ctx.formParam("final_total");
            if (finalTotalStr != null && !finalTotalStr.isEmpty()) {
                try {
                    total = Double.parseDouble(finalTotalStr);
                } catch (NumberFormatException ignored) {}
            }

            // Check if payment was already received via MacroDroid before checkout was submitted
            boolean autoVerified = false;
            for (Map.Entry<String, Long> entry : recentPayments.entrySet()) {
                if (entry.getValue() > System.currentTimeMillis()) {
                    try {
                        double paidAmt = Double.parseDouble(entry.getKey());
                        if (Math.abs(paidAmt - total) < 0.02) {
                            autoVerified = true;
                            recentPayments.remove(entry.getKey());
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }

            String initialStatus = autoVerified ? "COMPLETED" : "VERIFYING";
            int txId = plugin.getDatabaseManager().insertTransaction(ign, cartJson, fileName, total, initialStatus);

            if (autoVerified) {
                if (plugin.getDiscordManager() != null) {
                    plugin.getDiscordManager().deliverOrderCommands(txId, ign, cartItems);
                    plugin.getDiscordManager().sendVerifiedWebhookEmbed(txId, ign, total, "AUTO-MATCH", "Verified via UPI Gateway", cartItems);
                }
                ctx.status(200).json(Map.of(
                    "success", true,
                    "status", "COMPLETED",
                    "orderId", txId,
                    "message", "Payment verified! Your order has been delivered."
                ));
            } else {
                if (plugin.getDiscordManager() != null) {
                    plugin.getDiscordManager().sendCheckoutEmbed(txId, ign, total, cartItems, targetFile);
                }
                ctx.status(200).json(Map.of(
                    "success", true,
                    "status", "VERIFYING",
                    "orderId", txId,
                    "message", "Checkout submitted! Awaiting automatic UPI payment detection."
                ));
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error processing checkout: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of("success", false, "message", "An internal error occurred: " + e.getMessage()));
        }
    }

    private void handleVerifyPaymentWebhook(Context ctx) {
        try {
            String body = ctx.body();
            plugin.getLogger().info("Received payment verification webhook: " + body);

            double amount = 0.0;
            String utr = "";
            String senderName = "UPI Customer";
            String rawText = "";

            if (body != null && body.trim().startsWith("{")) {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                if (json.has("amount") && !json.get("amount").isJsonNull()) {
                    try { amount = json.get("amount").getAsDouble(); } catch (Exception ignored) {}
                }
                if (json.has("utr") && !json.get("utr").isJsonNull()) {
                    utr = json.get("utr").getAsString();
                } else if (json.has("txn_id") && !json.get("txn_id").isJsonNull()) {
                    utr = json.get("txn_id").getAsString();
                } else if (json.has("ref_no") && !json.get("ref_no").isJsonNull()) {
                    utr = json.get("ref_no").getAsString();
                }
                if (json.has("sender_name") && !json.get("sender_name").isJsonNull()) {
                    senderName = json.get("sender_name").getAsString();
                } else if (json.has("sender") && !json.get("sender").isJsonNull()) {
                    senderName = json.get("sender").getAsString();
                }
                if (json.has("raw_text") && !json.get("raw_text").isJsonNull()) {
                    rawText = json.get("raw_text").getAsString();
                } else if (json.has("text") && !json.get("text").isJsonNull()) {
                    rawText = json.get("text").getAsString();
                } else if (json.has("message") && !json.get("message").isJsonNull()) {
                    rawText = json.get("message").getAsString();
                }
            } else {
                String amtStr = ctx.formParam("amount");
                if (amtStr != null) {
                    try { amount = Double.parseDouble(amtStr); } catch (Exception ignored) {}
                }
                utr = ctx.formParam("utr");
                senderName = ctx.formParam("sender_name");
                rawText = ctx.formParam("text");
            }

            // Fallback: extract amount using regex from rawText (e.g. from SMS or MacroDroid push notifications)
            if (amount <= 0 && rawText != null && !rawText.isEmpty()) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(rawText);
                if (m.find()) {
                    try {
                        amount = Double.parseDouble(m.group(1).replace(",", ""));
                    } catch (Exception ignored) {}
                }
            }

            if (amount <= 0) {
                ctx.status(400).json(Map.of("status", "error", "message", "Invalid or missing amount"));
                return;
            }

            String key = String.format(java.util.Locale.US, "%.2f", amount);
            recentPayments.put(key, System.currentTimeMillis() + 1800000L); // 30 min cache

            Transaction matchedTx = plugin.getDatabaseManager().findPendingTransactionByAmount(amount);

            if (matchedTx != null) {
                plugin.getLogger().info("MacroDroid/Bot webhook matched Order #" + matchedTx.getId() + " for " + matchedTx.getIgn() + " with amount ₹" + amount);
                plugin.getDatabaseManager().updateTransactionStatus(matchedTx.getId(), "COMPLETED");

                Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                List<Map<String, Object>> cartItems = gson.fromJson(matchedTx.getCartItems(), listType);
                if (cartItems == null) cartItems = Collections.emptyList();

                if (plugin.getDiscordManager() != null) {
                    plugin.getDiscordManager().deliverOrderCommands(matchedTx.getId(), matchedTx.getIgn(), cartItems);
                    plugin.getDiscordManager().sendVerifiedWebhookEmbed(matchedTx.getId(), matchedTx.getIgn(), amount, utr, senderName, cartItems);
                }

                ctx.json(Map.of(
                    "status", "success",
                    "verified", true,
                    "order_id", matchedTx.getId(),
                    "ign", matchedTx.getIgn(),
                    "amount", amount
                ));
            } else {
                plugin.getLogger().info("MacroDroid/Bot payment of ₹" + amount + " received & cached, awaiting matching order checkout.");
                ctx.json(Map.of(
                    "status", "recorded",
                    "verified", false,
                    "amount", amount,
                    "message", "Payment recorded, waiting for matching order checkout"
                ));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error in verify payment webhook: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    private void handleGetOrderStatus(Context ctx) {
        try {
            String idStr = ctx.queryParam("id");
            if (idStr == null || idStr.isEmpty()) {
                ctx.status(400).json(Map.of("status", "error", "message", "Missing order id"));
                return;
            }
            int txId = Integer.parseInt(idStr);
            Transaction tx = plugin.getDatabaseManager().getTransaction(txId);
            if (tx == null) {
                ctx.status(404).json(Map.of("status", "error", "message", "Order not found"));
                return;
            }

            String currentStatus = tx.getStatus() != null ? tx.getStatus().toUpperCase() : "VERIFYING";

            // If still in VERIFYING / PENDING state, check if payment notification was cached in recentPayments
            if ("VERIFYING".equals(currentStatus) || "PENDING".equals(currentStatus)) {
                double targetAmt = tx.getAmount();
                boolean matched = false;
                for (Map.Entry<String, Long> entry : recentPayments.entrySet()) {
                    if (entry.getValue() > System.currentTimeMillis()) {
                        try {
                            double paidAmt = Double.parseDouble(entry.getKey());
                            if (Math.abs(paidAmt - targetAmt) < 0.02) {
                                matched = true;
                                recentPayments.remove(entry.getKey());
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (matched) {
                    currentStatus = "COMPLETED";
                    plugin.getDatabaseManager().updateTransactionStatus(txId, "COMPLETED");

                    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                    List<Map<String, Object>> cartItems = gson.fromJson(tx.getCartItems(), listType);
                    if (cartItems == null) cartItems = Collections.emptyList();

                    if (plugin.getDiscordManager() != null) {
                        plugin.getDiscordManager().deliverOrderCommands(txId, tx.getIgn(), cartItems);
                        plugin.getDiscordManager().sendVerifiedWebhookEmbed(txId, tx.getIgn(), tx.getAmount(), "AUTO-MATCH", "Verified via UPI Gateway", cartItems);
                    }
                }
            }

            boolean isCompleted = "COMPLETED".equals(currentStatus) || "ACCEPTED".equals(currentStatus);
            String clientStatus = isCompleted ? "COMPLETED" : "VERIFYING";

            ctx.json(Map.of(
                "id", tx.getId(),
                "ign", tx.getIgn(),
                "status", clientStatus,
                "verified", isCompleted,
                "amount", tx.getAmount()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    private double calculateTotal(List<Map<String, Object>> cartItems) {
        double total = 0.0;
        for (Map<String, Object> itemMap : cartItems) {
            String productId = (String) itemMap.get("id");
            Number qtyNum = (Number) itemMap.get("quantity");
            if (productId != null && qtyNum != null) {
                Product product = Products.getProductById(productId);
                if (product != null) {
                    total += product.getPrice() * qtyNum.intValue();
                }
            }
        }
        return total;
    }

    private void handleGetCheckouts(Context ctx) {
        String ign = ctx.queryParam("ign");
        if (ign == null || !IGN_PATTERN.matcher(ign).matches()) {
            ctx.status(400).result("Invalid username.");
            return;
        }

        List<Transaction> transactions = plugin.getDatabaseManager().getTransactionsByPlayer(ign);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction tx : transactions) {
            String rawStatus = tx.getStatus() != null ? tx.getStatus().toUpperCase() : "VERIFYING";
            if ("REJECTED".equals(rawStatus)) {
                continue;
            }
            String mappedStatus = ("ACCEPTED".equals(rawStatus) || "COMPLETED".equals(rawStatus)) ? "COMPLETED" : "VERIFYING";

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", tx.getId());
            entry.put("ign", tx.getIgn());
            entry.put("items", tx.getCartItems());
            entry.put("status", mappedStatus);
            entry.put("amount", tx.getAmount());
            result.add(entry);
        }
        ctx.json(result);
    }

    private void handleGetTopDonators(Context ctx) {
        List<Map<String, Object>> result = plugin.getDatabaseManager().getTopDonators(10);
        ctx.json(result);
    }
}
