/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.reflect.TypeToken
 */
package com.hydraz.store.ai;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class AgentClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(10L);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final Gson gson = new Gson();
    private final Logger logger;
    private final Map<String, String[]> serverConfigs = new LinkedHashMap<String, String[]>();

    public AgentClient(Logger logger) {
        this.logger = logger;
    }

    public void registerServer(String name, String url2, String key) {
        this.serverConfigs.put(name.toLowerCase(), new String[]{url2, key});
        this.logger.info("[AgentClient] Registered agent for server: " + name + " \u2192 " + url2);
    }

    public Set<String> getRegisteredServers() {
        return this.serverConfigs.keySet();
    }

    public boolean hasServer(String name) {
        return this.serverConfigs.containsKey(name.toLowerCase());
    }

    public Map<String, Object> getStatus(String server) throws IOException, InterruptedException {
        String body2 = this.get(server, "/agent/status", null);
        return this.parseMap(body2);
    }

    public Map<String, Object> getLogs(String server, int lines) throws IOException, InterruptedException {
        String body2 = this.get(server, "/agent/logs", "lines=" + lines);
        return this.parseMap(body2);
    }

    public Map<String, Object> listPlugins(String server) throws IOException, InterruptedException {
        String body2 = this.get(server, "/agent/plugins", null);
        return this.parseMap(body2);
    }

    public Map<String, Object> readFile(String server, String path) throws IOException, InterruptedException {
        String body2 = this.get(server, "/agent/file", "path=" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        return this.parseMap(body2);
    }

    public Map<String, Object> writeFile(String server, String path, String content) throws IOException, InterruptedException {
        Map<String, String> payload = Map.of("path", path, "content", content);
        String body2 = this.post(server, "/agent/file", payload);
        return this.parseMap(body2);
    }

    public Map<String, Object> executeCommand(String server, String command) throws IOException, InterruptedException {
        Map<String, String> payload = Map.of("command", command);
        String body2 = this.post(server, "/agent/command", payload);
        return this.parseMap(body2);
    }

    public Map<String, Object> reloadPlugin(String server, String plugin) throws IOException, InterruptedException {
        Map<String, String> payload = Map.of("plugin", plugin);
        String body2 = this.post(server, "/agent/reload-plugin", payload);
        return this.parseMap(body2);
    }

    private String get(String server, String path, String query) throws IOException, InterruptedException {
        String[] cfg = this.requireServer(server);
        String url2 = cfg[0] + path + (String)(query != null ? "?" + query : "");
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url2)).header("X-Agent-Key", cfg[1]).header("Accept", "application/json").timeout(TIMEOUT).GET().build();
        HttpResponse<String> resp = this.http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Agent returned HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return resp.body();
    }

    private String post(String server, String path, Object payload) throws IOException, InterruptedException {
        String[] cfg = this.requireServer(server);
        String url2 = cfg[0] + path;
        String json = this.gson.toJson(payload);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url2)).header("X-Agent-Key", cfg[1]).header("Content-Type", "application/json").header("Accept", "application/json").timeout(TIMEOUT).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> resp = this.http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new IOException("Agent returned HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return resp.body();
    }

    private String[] requireServer(String server) {
        String[] cfg = this.serverConfigs.get(server.toLowerCase());
        if (cfg == null) {
            throw new IllegalArgumentException("No agent registered for server: " + server);
        }
        return cfg;
    }

    private Map<String, Object> parseMap(String json) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map result = (Map)this.gson.fromJson(json, type);
        return result != null ? result : new LinkedHashMap();
    }
}

