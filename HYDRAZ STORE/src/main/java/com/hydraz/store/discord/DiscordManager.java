package com.hydraz.store.discord;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hydraz.store.HydrazWebsitePlugin;
import com.hydraz.store.ai.AgentClient;
import com.hydraz.store.data.Product;
import com.hydraz.store.data.Products;
import com.hydraz.store.models.PendingDelivery;
import com.hydraz.store.models.Transaction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordManager extends ListenerAdapter {

    private final HydrazWebsitePlugin plugin;
    private final String logChannelId;
    private static final String AI_KNOWLEDGE_CHANNEL_ID = "1534963149751451880";

    // Server IPs for presence checks
    private static final String LIFESTEAL_SERVER_IP = "buy.infinityhost.online:2583";
    private static final String PRACTICE_SERVER_IP = "amd2.infinityhost.online:19003";
    private static final String SURVIVAL_SERVER_IP = "buy.infinityhost.online:2583"; // Fallback to network server

    private JDA jda;
    private final MinecraftServerChecker serverChecker;
    private final ScheduledExecutorService scheduler;
    private final Gson gson = new Gson();

    // Sequential Command Dispatch Queue (Executes 1-by-1 directly via Pterodactyl Panel API)
    private final Queue<QueuedCommand> commandQueue = new ConcurrentLinkedQueue<>();

    private static class QueuedCommand {
        final String serverTarget;
        final String command;

        QueuedCommand(String serverTarget, String command) {
            this.serverTarget = serverTarget;
            this.command = command;
        }
    }

    public DiscordManager(HydrazWebsitePlugin plugin, String token, String logChannelId, AgentClient agentClient) {
        this.plugin = plugin;
        this.logChannelId = logChannelId;
        this.serverChecker = new MinecraftServerChecker(plugin.getLogger());
        this.scheduler = Executors.newScheduledThreadPool(2);

        try {
            this.jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();
            this.jda.awaitReady();
            plugin.getLogger().info("Discord Bot successfully connected!");

            // 1. Start sequential console command dispatcher
            startCommandQueueWorker();

            // 2. Start offline player presence delivery worker
            startDeliveryWorker();

            // 3. Sync initial AI Knowledge from Discord channel history
            syncAiKnowledgeChannel();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Discord Bot: " + e.getMessage());
            this.jda = null;
        }
    }

    /**
     * Reads past messages from AI knowledge channel (1534963149751451880) to seed knowledge base.
     */
    private void syncAiKnowledgeChannel() {
        if (jda == null) return;
        TextChannel channel = jda.getTextChannelById(AI_KNOWLEDGE_CHANNEL_ID);
        if (channel == null) {
            plugin.getLogger().info("AI Knowledge channel " + AI_KNOWLEDGE_CHANNEL_ID + " not found or inaccessible.");
            return;
        }

        channel.getHistory().retrievePast(50).queue(messages -> {
            List<String> facts = new ArrayList<>();
            // Iterate in chronological order
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message msg = messages.get(i);
                String content = msg.getContentRaw().trim();
                if (!content.isEmpty()) {
                    facts.add(content);
                }
            }
            if (plugin.getAiManager() != null) {
                plugin.getAiManager().setKnowledgeList(facts);
            }
        }, error -> {
            plugin.getLogger().warning("Could not sync AI knowledge history: " + error.getMessage());
        });
    }

    /**
     * Listens for new facts posted in AI knowledge channel (1534963149751451880).
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(AI_KNOWLEDGE_CHANNEL_ID)) return;

        String content = event.getMessage().getContentRaw().trim();
        if (!content.isEmpty()) {
            if (plugin.getAiManager() != null) {
                plugin.getAiManager().addKnowledge(content);
                // React with brain emoji to acknowledge learning the fact
                event.getMessage().addReaction(Emoji.fromUnicode("🧠")).queue(
                        success -> {},
                        error -> {}
                );
            }
        }
    }

    /**
     * Sends rich embedded order with Accept & Reject buttons and attached payment screenshot.
     */
    public void sendCheckoutEmbed(int txId, String ign, double total, List<Map<String, Object>> cartItems, File screenshotFile) {
        if (jda == null) return;
        TextChannel channel = jda.getTextChannelById(logChannelId);
        if (channel == null) {
            plugin.getLogger().warning("Log channel " + logChannelId + " not found!");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🛒 Store Purchase Submitted — Order #" + txId);
        embed.setColor(new Color(241, 196, 15)); // Gold / Pending
        embed.setThumbnail("https://mc-heads.net/avatar/" + ign + "/100");
        embed.setTimestamp(Instant.now());

        embed.addField("👤 Player", "`" + ign + "`", true);
        double fee = total - Math.floor(total);
        if (fee > 0.001) {
            embed.addField("💰 Total Amount", "**₹" + String.format("%.2f", total) + "** `(Fee: ₹" + String.format("%.2f", fee) + ")`", true);
        } else {
            embed.addField("💰 Total Amount", "**₹" + String.format("%.2f", total) + "**", true);
        }
        embed.addField("⏳ Status", "**AWAITING PAYMENT DETECTION**", true);

        // Build item list
        StringBuilder itemsText = new StringBuilder();
        for (Map<String, Object> item : cartItems) {
            String id = (String) item.get("id");
            String name = (String) item.get("name");
            Number priceNum = (Number) item.get("price");
            Number qtyNum = (Number) item.get("quantity");

            double price = priceNum != null ? priceNum.doubleValue() : 0.0;
            int qty = qtyNum != null ? qtyNum.intValue() : 1;

            Product product = Products.getProductById(id);
            String serverTarget = determineServerTarget(product, id);

            itemsText.append(String.format("• **[%s]** `%dx` %s — `₹%.0f`\n",
                    serverTarget.toUpperCase(), qty, name != null ? name : id, price * qty));
        }

        if (itemsText.length() == 0) {
            itemsText.append("No items listed.");
        }
        embed.addField("📦 Purchased Items", itemsText.toString(), false);

        embed.setFooter("Hydraz Automated Store • Awaiting Zero-Touch UPI Verification", "https://play.hydraz.online/img/logo.png");

        if (screenshotFile != null && screenshotFile.exists()) {
            embed.setImage("attachment://screenshot.png");
            channel.sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(screenshotFile, "screenshot.png"))
                    .queue();
        } else {
            channel.sendMessageEmbeds(embed.build())
                    .queue();
        }
    }

    /**
     * Sends rich embedded notification to log channel when an order is verified via MacroDroid / Bot webhook.
     */
    public void sendVerifiedWebhookEmbed(int txId, String ign, double total, String utr, String senderName, List<Map<String, Object>> cartItems) {
        if (jda == null) return;
        TextChannel channel = jda.getTextChannelById(logChannelId);
        if (channel == null) {
            plugin.getLogger().warning("Log channel " + logChannelId + " not found!");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚡ Instant UPI Verification — Order #" + txId);
        embed.setColor(new Color(34, 197, 94)); // Green
        embed.setThumbnail("https://mc-heads.net/avatar/" + ign + "/100");
        embed.setTimestamp(Instant.now());

        embed.addField("👤 Player", "`" + ign + "`", true);
        embed.addField("💰 Total Paid", "**₹" + String.format("%.2f", total) + "**", true);
        embed.addField("✅ Status", "**VERIFIED VIA MACRODROID & DELIVERED**", true);

        if (utr != null && !utr.isEmpty()) {
            embed.addField("🔢 UTR / Ref ID", "`" + utr + "`", true);
        }
        if (senderName != null && !senderName.isEmpty()) {
            embed.addField("📱 Payer Name", "`" + senderName + "`", true);
        }

        StringBuilder itemsText = new StringBuilder();
        if (cartItems != null) {
            for (Map<String, Object> item : cartItems) {
                String id = (String) item.get("id");
                String name = (String) item.get("name");
                Number priceNum = (Number) item.get("price");
                Number qtyNum = (Number) item.get("quantity");

                double price = priceNum != null ? priceNum.doubleValue() : 0.0;
                int qty = qtyNum != null ? qtyNum.intValue() : 1;

                Product product = Products.getProductById(id);
                String serverTarget = determineServerTarget(product, id);

                itemsText.append(String.format("• **[%s]** `%dx` %s — `₹%.0f`\n",
                        serverTarget.toUpperCase(), qty, name != null ? name : id, price * qty));
            }
        }

        if (itemsText.length() == 0) {
            itemsText.append("No items listed.");
        }
        embed.addField("📦 Delivered Items", itemsText.toString(), false);

        embed.setFooter("Hydraz Automated Gateway • Verified via MacroDroid Notification", "https://play.hydraz.online/img/logo.png");

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    /**
     * Executes order delivery commands directly upon auto-verification.
     */
    public void deliverOrderCommands(int txId, String ign, List<Map<String, Object>> cartItems) {
        scheduler.execute(() -> {
            try {
                for (Map<String, Object> item : cartItems) {
                    String id = (String) item.get("id");
                    Number qtyNum = (Number) item.get("quantity");
                    int qty = qtyNum != null ? qtyNum.intValue() : 1;

                    Product product = Products.getProductById(id);
                    String serverTarget = determineServerTarget(product, id);
                    List<String> rawCommands = getCommandsForProduct(product, item, serverTarget);

                    String serverAddress = getServerAddress(serverTarget);
                    boolean isOnline = serverChecker.isPlayerOnline(serverAddress, ign);

                    for (String rawCmd : rawCommands) {
                        String cmd = rawCmd.replace("%player%", ign);
                        for (int i = 0; i < qty; i++) {
                            if (isOnline) {
                                enqueueCommand(serverTarget, cmd);
                            } else {
                                plugin.getDatabaseManager().addPendingDelivery(txId, ign, serverTarget, cmd);
                            }
                        }
                    }
                }
                // Broadcast thank-you announcement to configured servers
                broadcastPurchaseAnnouncement(ign, cartItems);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing auto-verified commands in background: " + e.getMessage());
            }
        });
    }

    private String getServerAddress(String serverTarget) {
        if ("practice".equalsIgnoreCase(serverTarget)) return PRACTICE_SERVER_IP;
        if ("survival".equalsIgnoreCase(serverTarget)) return SURVIVAL_SERVER_IP;
        return LIFESTEAL_SERVER_IP;
    }

    private static final Set<String> ALLOWED_STAFF_USER_IDS = Set.of(
            "847320529210703892",
            "1375313949469184121"
    );

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        if (!buttonId.startsWith("order_accept:") && !buttonId.startsWith("order_reject:")) {
            return;
        }

        String userId = event.getUser().getId();
        if (!ALLOWED_STAFF_USER_IDS.contains(userId)) {
            event.reply("❌ You do not have permission to accept or reject orders. Only authorized owners can perform this action.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String[] parts = buttonId.split(":");
        if (parts.length < 2) return;

        String action = parts[0];
        int txId;
        try {
            txId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            event.reply("❌ Invalid transaction ID.").setEphemeral(true).queue();
            return;
        }

        Transaction tx = plugin.getDatabaseManager().getTransaction(txId);
        if (tx == null) {
            event.reply("❌ Transaction #" + txId + " not found in database!").setEphemeral(true).queue();
            return;
        }

        String txStatus = tx.getStatus() != null ? tx.getStatus().toUpperCase() : "";
        if (!"PENDING".equals(txStatus) && !"VERIFYING".equals(txStatus)) {
            event.reply("⚠️ Transaction #" + txId + " is already marked as `" + tx.getStatus() + "`!").setEphemeral(true).queue();
            return;
        }

        String staffUser = event.getUser().getAsTag();

        if ("order_accept".equals(action)) {
            handleAcceptOrder(event, tx, staffUser, userId);
        } else {
            handleRejectOrder(event, tx, staffUser, userId);
        }
    }

    private void handleAcceptOrder(ButtonInteractionEvent event, Transaction tx, String staffUser, String staffUserId) {
        // 1. Update status in Database immediately
        plugin.getDatabaseManager().updateTransactionStatus(tx.getId(), "COMPLETED");

        // 2. Build Updated Embed immediately
        MessageEmbed oldEmbed = event.getMessage().getEmbeds().get(0);
        EmbedBuilder newEmbed = new EmbedBuilder(oldEmbed);
        newEmbed.setTitle("✅ ORDER ACCEPTED — Order #" + tx.getId());
        newEmbed.setColor(new Color(34, 197, 94)); // Green
        newEmbed.setFooter("Accepted by " + staffUser + " • Hydraz Store", "https://play.hydraz.online/img/logo.png");

        // Replace Status field
        List<MessageEmbed.Field> fields = new ArrayList<>(newEmbed.getFields());
        newEmbed.clearFields();
        for (MessageEmbed.Field f : fields) {
            if ("⏳ Status".equals(f.getName()) || "Status".equals(f.getName()) || f.getName().contains("Status")) {
                newEmbed.addField("✅ Status", "**ACCEPTED** by <@" + staffUserId + "> (" + staffUser + ")", f.isInline());
            } else {
                newEmbed.addField(f);
            }
        }

        // 3. Edit Message and REMOVE ALL BUTTONS INSTANTLY (<20ms)
        event.editMessageEmbeds(newEmbed.build())
                .setComponents(Collections.emptyList())
                .queue(null, failure -> plugin.getLogger().warning("Failed to edit embed: " + failure.getMessage()));

        plugin.getLogger().info("Order #" + tx.getId() + " for " + tx.getIgn() + " accepted by " + staffUser);

        // 4. Process commands and server checks asynchronously in background thread
        scheduler.execute(() -> {
            try {
                Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                List<Map<String, Object>> cartItems = gson.fromJson(tx.getCartItems(), listType);
                if (cartItems == null) cartItems = Collections.emptyList();

                for (Map<String, Object> item : cartItems) {
                    String id = (String) item.get("id");
                    Number qtyNum = (Number) item.get("quantity");
                    int qty = qtyNum != null ? qtyNum.intValue() : 1;

                    Product product = Products.getProductById(id);
                    String serverTarget = determineServerTarget(product, id);
                    List<String> rawCommands = getCommandsForProduct(product, item, serverTarget);

                    String serverAddress = "practice".equalsIgnoreCase(serverTarget) ? PRACTICE_SERVER_IP : ("survival".equalsIgnoreCase(serverTarget) ? SURVIVAL_SERVER_IP : LIFESTEAL_SERVER_IP);
                    boolean isOnline = serverChecker.isPlayerOnline(serverAddress, tx.getIgn());

                    for (String rawCmd : rawCommands) {
                        String cmd = rawCmd.replace("%player%", tx.getIgn());
                        for (int i = 0; i < qty; i++) {
                            if (isOnline) {
                                enqueueCommand(serverTarget, cmd);
                            } else {
                                plugin.getDatabaseManager().addPendingDelivery(tx.getId(), tx.getIgn(), serverTarget, cmd);
                            }
                        }
                    }
                }
                // Broadcast thank-you announcement to both servers
                broadcastPurchaseAnnouncement(tx.getIgn(), cartItems);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing accepted commands in background: " + e.getMessage());
            }
        });
    }

    private void broadcastPurchaseAnnouncement(String ign, List<Map<String, Object>> cartItems) {
        if (!plugin.getConfig().getBoolean("purchase-broadcast.enabled", true)) {
            return;
        }

        // Build clean item summary, e.g. "1x Hydraz+ Rank, 2x Spawner Key"
        List<String> itemNames = new ArrayList<>();
        for (Map<String, Object> item : cartItems) {
            String name = (String) item.get("name");
            if (name == null) name = (String) item.get("id");
            Number qtyNum = (Number) item.get("quantity");
            int qty = qtyNum != null ? qtyNum.intValue() : 1;
            itemNames.add(qty > 1 ? qty + "x " + name : name);
        }
        String itemsSummary = itemNames.isEmpty() ? "Store Items" : String.join(", ", itemNames);

        List<String> targetServers = plugin.getConfig().getStringList("purchase-broadcast.servers");
        if (targetServers == null || targetServers.isEmpty()) {
            targetServers = Arrays.asList("lifesteal", "practice");
        }

        List<String> messageTemplates = plugin.getConfig().getStringList("purchase-broadcast.messages");
        if (messageTemplates == null || messageTemplates.isEmpty()) {
            messageTemplates = Arrays.asList(
                    "broadcast &8&m----------------------------------------",
                    "broadcast &d&lHYDRAZ STORE &8» &b%player% &7just purchased &e%items%&7!",
                    "broadcast &7Thanks for supporting our network, this helps in maintaining our server! &d❤",
                    "broadcast &8&m----------------------------------------"
            );
        }

        for (String serverTarget : targetServers) {
            for (String template : messageTemplates) {
                String formatted = template
                        .replace("%player%", ign)
                        .replace("%items%", itemsSummary);
                enqueueCommand(serverTarget, formatted);
            }
        }
    }

    private void handleRejectOrder(ButtonInteractionEvent event, Transaction tx, String staffUser, String staffUserId) {
        // 1. Update status in Database immediately
        plugin.getDatabaseManager().updateTransactionStatus(tx.getId(), "REJECTED");

        // 2. Build Updated Embed immediately
        MessageEmbed oldEmbed = event.getMessage().getEmbeds().get(0);
        EmbedBuilder newEmbed = new EmbedBuilder(oldEmbed);
        newEmbed.setTitle("❌ ORDER REJECTED — Order #" + tx.getId());
        newEmbed.setColor(new Color(239, 68, 68)); // Red
        newEmbed.setFooter("Rejected by " + staffUser + " • Hydraz Store", "https://play.hydraz.online/img/logo.png");

        List<MessageEmbed.Field> fields = new ArrayList<>(newEmbed.getFields());
        newEmbed.clearFields();
        for (MessageEmbed.Field f : fields) {
            if ("⏳ Status".equals(f.getName()) || "Status".equals(f.getName()) || f.getName().contains("Status")) {
                newEmbed.addField("❌ Status", "**REJECTED** by <@" + staffUserId + "> (" + staffUser + ")", f.isInline());
            } else {
                newEmbed.addField(f);
            }
        }

        // 3. Edit Message and REMOVE ALL BUTTONS INSTANTLY (<20ms)
        event.editMessageEmbeds(newEmbed.build())
                .setComponents(Collections.emptyList())
                .queue(null, failure -> plugin.getLogger().warning("Failed to edit reject embed: " + failure.getMessage()));

        plugin.getLogger().info("Order #" + tx.getId() + " for " + tx.getIgn() + " rejected by " + staffUser);
    }

    /**
     * Adds command to the sequential delivery queue.
     */
    public void enqueueCommand(String serverTarget, String command) {
        commandQueue.add(new QueuedCommand(serverTarget.toLowerCase(), command));
        plugin.getLogger().info("Enqueued console command for execution: " + serverTarget + " -> " + command);
    }

    public void sendCommandToDiscord(String serverTarget, String command) {
        enqueueCommand(serverTarget, command);
    }

    /**
     * Sequential background worker that executes commands directly into the server console via Pterodactyl API.
     */
    private void startCommandQueueWorker() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                QueuedCommand next = commandQueue.poll();
                if (next != null) {
                    if (plugin.getPterodactylManager() != null) {
                        plugin.getPterodactylManager().executeCommandSync(next.serverTarget, next.command);
                    } else {
                        plugin.getLogger().warning("PterodactylManager not available to execute: " + next.command);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing queued console command: " + e.getMessage());
            }
        }, 300, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Periodic background worker to check pending deliveries for offline players.
     */
    private void startDeliveryWorker() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                List<PendingDelivery> pending = plugin.getDatabaseManager().getAllPendingDeliveries();
                if (pending == null || pending.isEmpty()) return;

                Map<String, Boolean> onlineCache = new HashMap<>();

                for (PendingDelivery delivery : pending) {
                    String ign = delivery.getIgn();
                    String serverTarget = delivery.getServerTarget();
                    String serverAddress = "practice".equalsIgnoreCase(serverTarget) ? PRACTICE_SERVER_IP : ("survival".equalsIgnoreCase(serverTarget) ? SURVIVAL_SERVER_IP : LIFESTEAL_SERVER_IP);
                    String cacheKey = ign.toLowerCase() + "@" + serverTarget.toLowerCase();

                    boolean isOnline = onlineCache.computeIfAbsent(cacheKey, k -> serverChecker.isPlayerOnline(serverAddress, ign));

                    if (isOnline) {
                        plugin.getLogger().info("Player " + ign + " detected ONLINE on " + serverTarget + "! Executing queued command directly to console...");
                        enqueueCommand(serverTarget, delivery.getCommand());
                        plugin.getDatabaseManager().removePendingDelivery(delivery.getId());
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error in delivery worker: " + e.getMessage());
            }
        }, 12, 12, TimeUnit.SECONDS);
    }

    private String determineServerTarget(Product product, String id) {
        if (product != null && product.getServerTarget() != null) {
            String target = product.getServerTarget().toLowerCase();
            if (target.contains("practice")) return "practice";
            if (target.contains("survival")) return "survival";
            return "lifesteal";
        }
        if (id != null) {
            if (id.startsWith("pr_")) return "practice";
            if (id.startsWith("surv_") || id.startsWith("sv_")) return "survival";
        }
        return "lifesteal";
    }

    private List<String> getCommandsForProduct(Product product, Map<String, Object> item, String serverTarget) {
        if (product != null && product.getCommands() != null && product.getCommands().length > 0) {
            return Arrays.asList(product.getCommands());
        }

        // Fallback check in config.yml
        String name = (String) item.get("name");
        if (name != null) {
            List<String> cfgCmds = plugin.getConfig().getStringList("servers." + serverTarget + "." + name);
            if (cfgCmds != null && !cfgCmds.isEmpty()) {
                return cfgCmds;
            }
        }

        return Collections.emptyList();
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (jda != null) {
            jda.shutdown();
        }
    }
}
