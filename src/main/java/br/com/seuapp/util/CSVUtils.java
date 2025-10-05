package br.com.seuapp.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class CSVUtils {
    public static void ensureParentExists(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        if (!Files.exists(path)) Files.createFile(path);
    }

    public static List<String> readAllLines(Path path) throws IOException {
        if (!Files.exists(path)) return List.of();
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public static void writeAllLines(Path path, List<String> lines) throws IOException {
        ensureParentExists(path);
        Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
