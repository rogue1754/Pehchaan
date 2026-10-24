
package com.Hackathon.Pehchaan.Service;

import com.Hackathon.Pehchaan.Entity.ImportFile;
import com.Hackathon.Pehchaan.Entity.ImportResult;
import com.Hackathon.Pehchaan.Entity.ImportSession;
import com.Hackathon.Pehchaan.Enums.RecordStatus;
import com.Hackathon.Pehchaan.Enums.ReportCategory;
import com.Hackathon.Pehchaan.Repository.ImportFileRepository;
import com.Hackathon.Pehchaan.Repository.ImportResultRepository;
import com.Hackathon.Pehchaan.Repository.ImportSessionRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ImportProcessingService {

    private final ImportFileRepository fileRepository;
    private final ImportResultRepository resultRepository;
    private final ImportSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public ImportProcessingService(
            ImportFileRepository fileRepository,
            ImportResultRepository resultRepository,
            ImportSessionRepository sessionRepository,
            ObjectMapper objectMapper) {
        this.fileRepository = fileRepository;
        this.resultRepository = resultRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<ImportResult> process(Long sessionId) {
        ImportSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Import session not found"));

        List<ImportFile> files =
                fileRepository.findByImportSessionId(sessionId);

        if (files.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Upload a file first");
        }

        resultRepository.deleteAll(
                resultRepository.findByImportSessionId(sessionId));

        List<ImportResult> results = new ArrayList<>();

        for (ImportFile file : files) {
            String type = file.getFileType().toUpperCase();

            if (type.equals("PDF")) {
                file.setStatus("PARSING_UNSUPPORTED");
                continue;
            }

            String text = new String(
                    file.getContent(), StandardCharsets.UTF_8);

            List<Map<String, Object>> rows;

            try {
                if (type.equals("JSON")) {
                    rows = parseJson(text);
                } else if (type.equals("CSV")) {
                    rows = parseCsv(text);
                } else if (type.equals("TXT")) {
                    rows = parseTxt(text);
                } else {
                    file.setStatus("UNSUPPORTED");
                    continue;
                }
            } catch (Exception exception) {
                file.setStatus("INVALID_CONTENT");
                continue;
            }

            for (Map<String, Object> row : rows) {
                ImportResult result = toResult(session, row);
                result.setConfirmed(false);
                results.add(result);
            }

            file.setStatus("PROCESSED");
        }

        fileRepository.flush();
        List<ImportResult> saved = resultRepository.saveAll(results);

        session.setStatus(
                saved.isEmpty() ? "NEEDS_REVIEW" : "READY_FOR_REVIEW");

        sessionRepository.save(session);
        return saved;
    }

    private List<Map<String, Object>> parseJson(String text)
            throws Exception {
        return objectMapper.readValue(
                text,
                new TypeReference<List<Map<String, Object>>>() {});
    }

    private List<Map<String, Object>> parseCsv(String text) {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new StringReader(text))) {
            String headerLine = reader.readLine();

            if (headerLine == null) {
                return rows;
            }

            String[] headers = headerLine.split(",", -1);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] values = line.split(",", -1);
                Map<String, Object> row = new java.util.LinkedHashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i].trim(),
                            i < values.length ? values[i].trim() : "");
                }

                rows.add(row);
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid CSV", exception);
        }

        return rows;
    }

    private List<Map<String, Object>> parseTxt(String text) {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (String line : text.split("\\R")) {
            if (line.isBlank()) continue;

            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("name", line.trim());
            row.put("source", "TXT Import");
            row.put("status", "NOT_RECOGNIZED");
            row.put("category", "OTHER");
            rows.add(row);
        }

        return rows;
    }

    private ImportResult toResult(
            ImportSession session, Map<String, Object> row) {

        ImportResult result = new ImportResult();
        result.setImportSession(session);

        result.setName(value(row, "name"));
        result.setSource(value(row, "source"));
        result.setDate(value(row, "date"));
        result.setDetail(value(row, "detail"));

        String status = value(row, "status");
        if (status == null || status.isBlank()) {
            status = "NOT_RECOGNIZED";
        }

        try {
            result.setStatus(RecordStatus.valueOf(
                    status.trim().toUpperCase().replace(' ', '_')).name());
        } catch (IllegalArgumentException exception) {
            result.setStatus("NOT_RECOGNIZED");
        }

        String category = value(row, "category");
        if (category == null || category.isBlank()) {
            category = "OTHER";
        }

        try {
            result.setCategory(ReportCategory.valueOf(
                    category.trim().toUpperCase().replace(' ', '_')).name());
        } catch (IllegalArgumentException exception) {
            result.setCategory("OTHER");
        }

        return result;
    }

    private String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : value.toString().trim();
    }
}
