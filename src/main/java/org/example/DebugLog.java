package org.example;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

/** Writes NDJSON debug lines to .cursor/debug.log for this session. */
final class DebugLog {
    private static final Path LOG_PATH = Path.of("/home/user/IdeaProjects/MonetisationRAGChat/.cursor/debug.log");

    static void log(String location, String message, Map<String, Object> data, String hypothesisId) {
        try {
            String json = "{\"runId\":\"startup\",\"hypothesisId\":\"" + hypothesisId + "\",\"location\":\"" + escape(location) + "\",\"message\":\"" + escape(message) + "\",\"data\":" + mapToJson(data) + ",\"timestamp\":" + System.currentTimeMillis() + "}";
            Files.writeString(LOG_PATH, json + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {}
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private static String mapToJson(Map<String, Object> m) {
        if (m == null) return "{}";
        String entries = m.entrySet().stream()
            .map(e -> "\"" + e.getKey() + "\":" + jsonVal(e.getValue()))
            .collect(Collectors.joining(","));
        return "{" + entries + "}";
    }

    private static Object jsonVal(Object v) {
        if (v == null) return "null";
        if (v instanceof Number || v instanceof Boolean) return v;
        return "\"" + escape(String.valueOf(v)) + "\"";
    }
}
