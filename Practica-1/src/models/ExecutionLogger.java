package models;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExecutionLogger implements AutoCloseable {

    private final StringBuilder buffer = new StringBuilder();
    private final Path path;

    public ExecutionLogger(String dir, String name) throws IOException {
        Files.createDirectories(Paths.get(dir));
        String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        this.path = Paths.get(dir, safe + ".txt");
    }

    // Solo guarda en memoria, no imprime por consola
    public void line(String s) {
        buffer.append(s).append(System.lineSeparator());
    }

    public void section(String title) {
        line("========================================");
        line(title);
        line("========================================");
    }

    public void sub(String title) {
        line("");
        line("--- " + title + " ---");
    }

    public void kv(String key, Object value) {
        line(String.format("%-22s: %s", key, value));
    }

    public static String route(List<Integer> r) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < r.size(); i++) {
            sb.append(r.get(i));
            if (i < r.size() - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    public Path getPath() { return path; }

    // Al cerrar se vuelca todo al archivo
    @Override
    public void close() {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path))) {
            out.print(buffer);
        } catch (IOException e) {
            System.out.println("Error escribiendo el log: " + e);
        }
    }
}