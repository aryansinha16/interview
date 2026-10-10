package com.optiflow.shared;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JsonDb {

    private static final DateTimeFormatter ISO_SECONDS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Path dataDir;

    JsonDb(@Value("${optiflow.data-dir}") String dataDir) {
        this.dataDir = Path.of(dataDir);
    }

    void ensureFile(String fileName) {
        Path path = file(fileName);
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "[]");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    List<Map<String, Object>> read(String fileName) {
        try {
            return mapper.readValue(Files.readString(file(fileName)), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void write(String fileName, List<Map<String, Object>> records) {
        try {
            mapper.writeValue(file(fileName).toFile(), records);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path file(String fileName) {
        return dataDir.resolve(fileName);
    }

    public static String newId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    public static String nowIso() {
        return ISO_SECONDS.format(Instant.now());
    }
}
