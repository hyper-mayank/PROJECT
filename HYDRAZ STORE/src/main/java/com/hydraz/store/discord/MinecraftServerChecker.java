package com.hydraz.store.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class MinecraftServerChecker {

    private final Logger logger;
    private final HttpClient httpClient;

    public MinecraftServerChecker(Logger logger) {
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Checks if a player is online on the specified server (host:port).
     */
    public boolean isPlayerOnline(String hostPort, String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }

        // 1. Check local server if player is connected locally
        Player localPlayer = Bukkit.getPlayerExact(playerName);
        if (localPlayer != null && localPlayer.isOnline()) {
            return true;
        }

        String host = hostPort;
        int port = 25565;
        if (hostPort.contains(":")) {
            String[] parts = hostPort.split(":");
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }

        // 2. Query via native Minecraft Server List Ping (SLP) protocol
        try {
            Set<String> onlinePlayers = queryServerListPing(host, port);
            if (onlinePlayers != null) {
                for (String p : onlinePlayers) {
                    if (p.equalsIgnoreCase(playerName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Fall through to API check
        }

        // 3. Fallback to mcsrvstat API
        try {
            return queryViaMcSrvStat(host, port, playerName);
        } catch (Exception e) {
            // Logger debug
        }

        return false;
    }

    /**
     * Minecraft Server List Ping (SLP) protocol 1.7+
     */
    private Set<String> queryServerListPing(String host, int port) throws IOException {
        Set<String> players = new HashSet<>();
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(3000);
            socket.connect(new InetSocketAddress(host, port), 3000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // Handshake packet (0x00)
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(b);
            handshake.writeByte(0x00); // packet id
            writeVarInt(handshake, 765); // protocol version (1.20.4)
            writeString(handshake, host);
            handshake.writeShort(port);
            writeVarInt(handshake, 1); // next state: status

            byte[] handshakeBytes = b.toByteArray();
            writeVarInt(out, handshakeBytes.length);
            out.write(handshakeBytes);

            // Status Request packet (0x00)
            out.writeByte(0x01); // size
            out.writeByte(0x00); // packet id

            // Read response
            int size = readVarInt(in);
            int id = readVarInt(in);
            if (id != 0x00) {
                return null;
            }

            int length = readVarInt(in);
            byte[] data = new byte[length];
            in.readFully(data);
            String jsonStr = new String(data, StandardCharsets.UTF_8);

            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            if (json.has("players") && json.getAsJsonObject("players").has("sample")) {
                JsonArray sample = json.getAsJsonObject("players").getAsJsonArray("sample");
                for (JsonElement elem : sample) {
                    if (elem.isJsonObject() && elem.getAsJsonObject().has("name")) {
                        players.add(elem.getAsJsonObject().get("name").getAsString());
                    }
                }
            }
        }
        return players;
    }

    private boolean queryViaMcSrvStat(String host, int port, String playerName) {
        try {
            String url = "https://api.mcsrvstat.us/3/" + host + ":" + port;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("players") && json.getAsJsonObject("players").has("list")) {
                    JsonArray list = json.getAsJsonObject("players").getAsJsonArray("list");
                    for (JsonElement elem : list) {
                        if (elem.isJsonObject() && elem.getAsJsonObject().has("name")) {
                            if (elem.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(playerName)) {
                                return true;
                            }
                        } else if (elem.isJsonPrimitive()) {
                            if (elem.getAsString().equalsIgnoreCase(playerName)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << (j++ * 7);
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    private void writeString(DataOutputStream out, String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }
}
