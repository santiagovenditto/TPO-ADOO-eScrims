package facade;

import domain.Scrim;
import strategy.ByMMRStrategy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simple facade that exposes high-level operations on scrims.
 * It encapsulates creation and simple persistence to data/scrims.ndjson.
 */
public class ScrimAPIFacade {

    private final Path dataDir;

    public ScrimAPIFacade(Path dataDir) {
        this.dataDir = dataDir;
    }

    /**
     * Create a new Scrim with minimal metadata and persist it to scrims.ndjson.
     * Returns the created Scrim instance.
     */
    public Scrim crearScrim(String title, String format, String region, String owner, LocalDateTime fechaHora) throws IOException {
        Scrim s = new Scrim(new ByMMRStrategy());
        // set some basic fields if available via reflection-like setters
        try { s.setFormato(format); } catch (Exception ignored) {}
        try { s.setRegion(region); } catch (Exception ignored) {}
        try { s.setFechaHora(fechaHora); } catch (Exception ignored) {}

        // persist minimal JSON object to scrims.ndjson
        String id = s.getId().toString();
        String start = (fechaHora==null)?"null":fechaHora.toString();
        String json = String.format("{\"id\":\"%s\",\"title\":\"%s\",\"format\":\"%s\",\"region\":\"%s\",\"owner\":\"%s\",\"state\":\"Buscando\",\"start\":%s}", id, escape(title), escape(format), escape(region), escape(owner), (start.equals("null")?"null\"":"\""+start+"\""));
        appendNdjson("scrims.ndjson", json);
        return s;
    }

    private void appendNdjson(String filename, String jsonObject) throws IOException {
        Path p = dataDir.resolve(filename);
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
            w.write(jsonObject.replaceAll("\r?\n"," "));
            w.write("\n");
        }
    }

    private static String escape(String s){ if(s==null) return ""; return s.replace("\\","\\\\").replace("\"","\\\""); }
}
