import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import auth.AuthService;
import auth.FileAuthRepository;
import domain.Scrim;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class WebServer {
    private final AuthService auth;
    private final HttpServer server;
    private final Path webRoot;

    public WebServer(int port, int backlog) throws Exception {
        this.auth = new AuthService(new FileAuthRepository());
        this.server = HttpServer.create(new InetSocketAddress(port), backlog);
        this.webRoot = Paths.get(System.getProperty("user.dir"), "web");
    // ensure data dir
    Path dataDir = Paths.get(System.getProperty("user.dir"), "data");
    if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

    // static
    server.createContext("/", this::handleStatic);

        // API
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/logout", new LogoutHandler());
        server.createContext("/api/session", new SessionHandler());
        server.createContext("/api/run", new RunHandler());
    // demo endpoint to create a confirmed scrim scheduled shortly
    // create a facade instance for use by handlers
    facade.ScrimAPIFacade facade = new facade.ScrimAPIFacade(Paths.get(System.getProperty("user.dir"), "data"));
    server.createContext("/api/demo", new DemoHandler(facade));
    // scrims persistence (NDJSON simple storage)
    server.createContext("/api/scrims", new ScrimsHandler());
    server.createContext("/api/scrims/delete", new ScrimDeleteHandler());
    server.createContext("/api/scrims/update", new ScrimUpdateHandler());
    // reports
    server.createContext("/api/report", new ReportHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
    }

    public void start() {
        server.start();
        System.out.println("Web server started. Open http://localhost:" + server.getAddress().getPort());
    }

    private void handleStatic(HttpExchange ex) {
        try {
            String path = ex.getRequestURI().getPath();
            System.out.println("[STATIC] Requested path: " + path);
            if (path.equals("/")) path = "/index.html";
            Path file = webRoot.resolve(path.substring(1)).normalize();
            System.out.println("[STATIC] webRoot= " + webRoot.toString() + " resolved file= " + file.toString());
            if (!file.startsWith(webRoot) || !Files.exists(file) || Files.isDirectory(file)) {
                System.out.println("[STATIC] File not found or invalid: exists=" + Files.exists(file) + " isDir=" + Files.isDirectory(file));
                sendText(ex, 404, "Not found");
                return;
            }
            String ct = guessContentType(file.toString());
            byte[] data = Files.readAllBytes(file);
            ex.getResponseHeaders().set("Content-Type", ct + "; charset=utf-8");
            // HEAD requests should not send a body
            if (ex.getRequestMethod().equalsIgnoreCase("HEAD")) {
                ex.sendResponseHeaders(200, -1);
                return;
            }
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(data); }
        } catch (Exception e) {
            e.printStackTrace();
            try { sendText(ex, 500, "Server error: " + e.getMessage()); } catch (Exception ignored) {}
        }
    }

    // Demo handler: crea un scrim confirmado con fechaHora = now + 5s
    class DemoHandler implements HttpHandler {
        private final facade.ScrimAPIFacade facade;
        public DemoHandler(facade.ScrimAPIFacade facade){ this.facade = facade; }
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try {
                java.time.LocalDateTime when = java.time.LocalDateTime.now().plusSeconds(5);
                Scrim s = facade.crearScrim("Demo Scrim","5v5","NA","demo-owner", when);
                // mark as confirmado in-memory (so scheduler can pick it up)
                s.setFechaHora(when);
                s.setState(new state.ConfirmadoState());
                String json = String.format("{\"ok\":true,\"id\":\"%s\",\"start\":\"%s\"}", s.getId().toString(), when.toString());
                sendJson(ex,200,json);
            } catch (Exception e) { e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }

    private static String guessContentType(String s) {
        if (s.endsWith(".html")) return "text/html";
        if (s.endsWith(".css")) return "text/css";
        if (s.endsWith(".js")) return "application/javascript";
        if (s.endsWith(".png")) return "image/png";
        if (s.endsWith(".jpg") || s.endsWith(".jpeg")) return "image/jpeg";
        if (s.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }

    private void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    private void sendText(HttpExchange ex, int code, String text) throws IOException {
        byte[] b = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    private Map<String,String> parseJsonBody(HttpExchange ex) throws IOException {
        InputStream in = ex.getRequestBody();
        String s = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        Map<String,String> m = new HashMap<>();
        if (s.isEmpty()) return m;
        // remove newlines
        s = s.replaceAll("\\r?\\n", "").trim();
        // expect object like {"k":"v","k2":"v2"}
        if (!s.startsWith("{" ) || !s.endsWith("}")) return m;
        s = s.substring(1, s.length()-1).trim();
        // parse by scanning for quoted key and quoted value pairs to allow commas/colons inside strings
        int i = 0; int n = s.length();
        while (i < n) {
            // skip whitespace and commas
            while (i<n && (Character.isWhitespace(s.charAt(i)) || s.charAt(i)==',')) i++;
            if (i>=n) break;
            // parse key (expecting quoted string)
            if (s.charAt(i)!='"') break;
            int kstart = i+1;
            StringBuilder key = new StringBuilder();
            boolean esc = false;
            for (int j=kstart;j<n;j++){
                char c = s.charAt(j);
                if (esc) { key.append(c); esc = false; continue; }
                if (c=='\\') { esc = true; continue; }
                if (c=='\"') { i = j+1; break; }
                key.append(c);
            }
            // skip to ':'
            while (i<n && Character.isWhitespace(s.charAt(i))) i++;
            if (i<n && s.charAt(i)==':') i++; else break;
            while (i<n && Character.isWhitespace(s.charAt(i))) i++;
            // parse value (accept quoted string or bare token)
            String value = "";
            if (i<n && s.charAt(i)=='\"'){
                i++; StringBuilder val = new StringBuilder(); esc = false;
                for (int j=i;j<n;j++){
                    char c = s.charAt(j);
                    if (esc) { val.append(c); esc = false; continue; }
                    if (c=='\\') { esc = true; continue; }
                    if (c=='\"') { i = j+1; break; }
                    val.append(c);
                }
                value = val.toString();
            } else {
                // unquoted value (number, true, false, null)
                int j=i; while (j<n && s.charAt(j)!=',' ) j++; value = s.substring(i,j).trim(); i = j;
            }
            m.put(key.toString(), value);
            // advance past comma handled at loop start
        }
        return m;
    }

    // NDJSON helpers (one JSON object per line)
    private synchronized void appendNdjson(String filename, String jsonObject) throws IOException {
        Path p = Paths.get(System.getProperty("user.dir"), "data", filename);
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
            w.write(jsonObject.replaceAll("\r?\n"," "));
            w.write("\n");
        }
    }

    private synchronized String readNdjsonAsArray(String filename) throws IOException {
        Path p = Paths.get(System.getProperty("user.dir"), "data", filename);
        if (!Files.exists(p)) return "[]";
        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        List<String> trimmed = new ArrayList<>();
        for (String l : lines) { String t = l.trim(); if (!t.isEmpty()) trimmed.add(t); }
        if (trimmed.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder(); sb.append('[');
        for (int i=0;i<trimmed.size();i++){ if (i>0) sb.append(','); sb.append(trimmed.get(i)); }
        sb.append(']');
        return sb.toString();
    }

    private synchronized boolean removeNdjsonById(String filename, String id) throws IOException {
        Path p = Paths.get(System.getProperty("user.dir"), "data", filename);
        if (!Files.exists(p)) return false;
        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        List<String> kept = new ArrayList<>();
        String needle = "\"id\"\s*:\s*\"" + id + "\"";
        boolean removed = false;
        for (String l : lines) {
            if (l.matches(".*" + needle + ".*")) { removed = true; continue; }
            // fallback: simple contains
            if (l.contains("\"id\":\"" + id + "\"")) { removed = true; continue; }
            kept.add(l);
        }
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String k : kept) { w.write(k); w.write('\n'); }
        }
        return removed;
    }

    // Handlers
    class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try {
                Map<String,String> body = parseJsonBody(ex);
                String username = body.getOrDefault("username", "");
                String email = body.getOrDefault("email", "");
                String password = body.getOrDefault("password", "");
                AuthService.RegisterResult r = auth.register(username, email, password);
                String json = String.format("{\"ok\":%b,\"message\":\"%s\"}", r.ok, escapeJson(r.message));
                sendJson(ex, 200, json);
            } catch (Exception e) { e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }

    class LoginHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try {
                Map<String,String> body = parseJsonBody(ex);
                String email = body.getOrDefault("email", "");
                String password = body.getOrDefault("password", "");
                AuthService.LoginResult r = auth.login(email, password);
                if (!r.ok) {
                    sendJson(ex,200,String.format("{\"ok\":false,\"message\":\"%s\"}", escapeJson(r.message)));
                    return;
                }
                String json = String.format("{\"ok\":true,\"message\":\"%s\",\"token\":\"%s\",\"username\":\"%s\",\"role\":\"%s\"}",
                        escapeJson(r.message), r.token, escapeJson(r.username), escapeJson(r.role));
                sendJson(ex,200,json);
            } catch (Exception e) { e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }

    class LogoutHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try {
                Map<String,String> body = parseJsonBody(ex);
                String token = body.getOrDefault("token", "");
                auth.logout(token);
                sendJson(ex,200,"{\"ok\":true,\"message\":\"logged out\"}");
            } catch (Exception e) { e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }

    class SessionHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String token = null;
            String query = ex.getRequestURI().getQuery();
            if (query != null) {
                for (String q : query.split("&")) {
                    String[] kv = q.split("=",2);
                    if (kv.length==2 && kv[0].equals("token")) token = kv[1];
                }
            }
            boolean ok = auth.isAuthenticated(token);
            sendJson(ex,200,String.format("{\"ok\":%b}", ok));
        }
    }

    class RunHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try {
                Map<String,String> body = parseJsonBody(ex);
                String token = body.getOrDefault("token", "");
                if (!auth.isAuthenticated(token)) { sendJson(ex,403,"{\"ok\":false,\"message\":\"forbidden\"}"); return; }

                // Capture System.out while running the simulation copy
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                PrintStream oldOut = System.out;
                try (PrintStream ps = new PrintStream(bout, true, "UTF-8")) {
                    System.setOut(ps);
                    // --- run the same simulation as Main ---
                    Main.runSimulation(auth);
                    System.out.flush();
                } finally {
                    System.setOut(oldOut);
                }
                String output = bout.toString("UTF-8").replace("\r\n", "\n");
                String json = String.format("{\"ok\":true,\"output\":\"%s\"}", escapeJson(output));
                sendJson(ex,200,json);
            } catch (Exception e) { e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }

    // GET -> list scrims as JSON array, POST -> append scrim JSON object
    class ScrimsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            try{
                if (ex.getRequestMethod().equalsIgnoreCase("GET")){
                    String arr = readNdjsonAsArray("scrims.ndjson");
                    sendJson(ex,200,arr);
                    return;
                }
                if (ex.getRequestMethod().equalsIgnoreCase("POST")){
                    // read raw body and append as one line
                    InputStream in = ex.getRequestBody();
                    String body = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                    if (body.isEmpty()) { sendJson(ex,400,"{\"ok\":false,\"message\":\"empty body\"}"); return; }
                    appendNdjson("scrims.ndjson", body);
                    sendJson(ex,200,"{\"ok\":true}");
                    return;
                }
                sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}");
            }catch(Exception e){ e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }

    // DELETE-like endpoint: POST {id:...} to /api/scrims/delete
    class ScrimDeleteHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try{
                Map<String,String> body = parseJsonBody(ex);
                String id = body.getOrDefault("id","");
                if (id.isEmpty()){ sendJson(ex,400,"{\"ok\":false,\"message\":\"missing id\"}"); return; }
                boolean ok = removeNdjsonById("scrims.ndjson", id);
                sendJson(ex, ok?200:404, String.format("{\"ok\":%b}", ok));
            }catch(Exception e){ e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }


    // Update endpoint: replace scrim with same id (remove existing then append new JSON object)
    class ScrimUpdateHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try{
                InputStream in = ex.getRequestBody();
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                if (body.isEmpty()){ sendJson(ex,400,"{\"ok\":false,\"message\":\"empty body\"}"); return; }
                // attempt to extract id from the JSON body using simple scan for "id":"..."
                String id = null;
                int idx = body.indexOf("\"id\"");
                if (idx!=-1){ int col = body.indexOf(':', idx); if(col!=-1){ int q1 = body.indexOf('"', col); int q2 = body.indexOf('"', q1+1); if(q1!=-1 && q2!=-1) id = body.substring(q1+1,q2); } }
                if (id==null || id.isEmpty()){ sendJson(ex,400,"{\"ok\":false,\"message\":\"missing id in body\"}"); return; }
                // remove existing
                removeNdjsonById("scrims.ndjson", id);
                // append new object
                appendNdjson("scrims.ndjson", body);
                sendJson(ex,200,"{\"ok\":true}");
            }catch(Exception e){ e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }
    // Report handler: append a report and optionally increment strikes file
    class ReportHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendJson(ex,405,"{\"ok\":false,\"message\":\"Method not allowed\"}"); return; }
            try{
                InputStream in = ex.getRequestBody();
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                if (body.isEmpty()){ sendJson(ex,400,"{\"ok\":false,\"message\":\"empty body\"}"); return; }
                // body should contain at least {"reported":"user","reason":"...","by":"...","scrimId":"...","id":"..."}
                appendNdjson("reports.ndjson", body);
                // also append a strike entry for the reported user
                // quick parse for reported username
                String reported = null;
                int idx = body.indexOf("\"reported\"");
                if (idx!=-1){ int col = body.indexOf(':', idx); if(col!=-1){ int q1 = body.indexOf('"', col); int q2 = body.indexOf('"', q1+1); if(q1!=-1 && q2!=-1) reported = body.substring(q1+1,q2); } }
                if (reported!=null && !reported.isEmpty()){
                    String strikeJson = String.format("{\"id\":\"strike_%d\",\"player\":\"%s\",\"time\":%d}", System.currentTimeMillis(), reported, System.currentTimeMillis());
                    appendNdjson("strikes.ndjson", strikeJson);
                }
                sendJson(ex,200,"{\"ok\":true}");
            }catch(Exception e){ e.printStackTrace(); sendJson(ex,500,"{\"ok\":false,\"message\":\"server error\"}"); }
        }
    }

    private static String escapeJson(String s) {
        if (s==null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
