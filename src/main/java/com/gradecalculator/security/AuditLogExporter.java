package com.gradecalculator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gradecalculator.model.AuditLog;
import com.gradecalculator.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

/**
 * SIEM-compatible audit log exporter.
 *
 * <p>Produces <b>JSON Lines</b> (NDJSON) format — one compact JSON object per line —
 * which is directly ingestible by Splunk, ELK, Datadog, and similar SIEM platforms.
 * Uses {@link StreamingResponseBody} so audit logs are streamed to the client without
 * loading the entire result set into memory.
 */
@Service
public class AuditLogExporter {

    private static final Logger log = LoggerFactory.getLogger(AuditLogExporter.class);
    private static final int DEFAULT_LIMIT = 10_000;

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogExporter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Returns a {@link StreamingResponseBody} that writes filtered audit records
     * as JSON Lines to the HTTP response output stream.
     *
     * @param from       inclusive lower bound for {@code timestamp} (null = no lower bound)
     * @param to         inclusive upper bound for {@code timestamp} (null = no upper bound)
     * @param action     filter by exact action name, case-insensitive (null = all)
     * @param entityName filter by entity type, case-insensitive (null = all)
     * @param username   filter by username, case-insensitive (null = all)
     * @param limit      maximum number of records to export (0 or null → {@value #DEFAULT_LIMIT})
     */
    @Transactional(readOnly = true)
    public StreamingResponseBody export(
            LocalDateTime from,
            LocalDateTime to,
            String action,
            String entityName,
            String username,
            Integer limit) {

        int effectiveLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : limit;
        PageRequest page = PageRequest.of(0, effectiveLimit);

        return outputStream -> {
            try (Stream<AuditLog> stream = auditLogRepository.streamFiltered(
                    from, to,
                    nullIfBlank(action),
                    nullIfBlank(entityName),
                    nullIfBlank(username),
                    page)) {

                long[] count = {0};
                stream.forEach(entry -> {
                    try {
                        writeJsonLine(outputStream, entry);
                        count[0]++;
                    } catch (IOException e) {
                        log.warn("Error writing audit log entry id={}: {}", entry.getId(), e.getMessage());
                    }
                });
                outputStream.flush();
                log.info("Audit export complete: {} records written", count[0]);

            } catch (Exception e) {
                log.error("Error during audit log export", e);
            }
        };
    }

    private void writeJsonLine(OutputStream out, AuditLog entry) throws IOException {
        // Build a flat map for SIEM compatibility
        Map<String, Object> record = Map.of(
                "id",            entry.getId(),
                "timestamp",     entry.getTimestamp().toString(),
                "username",      entry.getUsername(),
                "ip",            entry.getIp(),
                "action",        entry.getAction(),
                "entity_name",   entry.getEntityName(),
                "entity_id",     entry.getEntityId(),
                "previous_value", entry.getPreviousValue() != null ? entry.getPreviousValue() : "",
                "new_value",      entry.getNewValue()      != null ? entry.getNewValue()      : ""
        );
        byte[] line = objectMapper.writeValueAsBytes(record);
        out.write(line);
        out.write('\n');
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
