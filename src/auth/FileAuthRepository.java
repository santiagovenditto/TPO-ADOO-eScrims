package auth;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileAuthRepository implements AuthRepository {
    private final File file;

    public FileAuthRepository() {
        String cwd = System.getProperty("user.dir"); // raíz donde se ejecuta el proceso
        Path dataPath = Paths.get(cwd, "data");
        Path filePath = dataPath.resolve("users.csv");

        // DEBUG para verificar rutas
        System.out.println("[AUTH][DEBUG] CWD: " + cwd);
        System.out.println("[AUTH][DEBUG] DATA DIR: " + dataPath.toAbsolutePath());
        System.out.println("[AUTH][DEBUG] USERS FILE: " + filePath.toAbsolutePath());

        File dir = dataPath.toFile();
        if (!dir.exists()) dir.mkdirs();

        this.file = filePath.toFile();
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("No pude crear users.csv: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UserCredentials> findByEmail(String email) {
        String em = email.toLowerCase();
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                UserCredentials u = UserCredentials.fromCsv(line);
                if (u.getEmail().equalsIgnoreCase(em)) return Optional.of(u);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public synchronized void save(UserCredentials user) throws Exception {
        List<String> lines = new ArrayList<>();
        boolean replaced = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.trim().isEmpty()) continue;
                UserCredentials existing = UserCredentials.fromCsv(l);
                if (existing.getEmail().equalsIgnoreCase(user.getEmail())) {
                    lines.add(user.toCsv());
                    replaced = true;
                } else {
                    lines.add(l);
                }
            }
        }

        if (!replaced) lines.add(user.toCsv());

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (String l : lines) pw.println(l);
        }
    }
}
