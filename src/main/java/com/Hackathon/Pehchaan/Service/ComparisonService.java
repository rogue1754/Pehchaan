
package com.Hackathon.Pehchaan.Service;

import com.Hackathon.Pehchaan.Entity.IdentityRecord;
import com.Hackathon.Pehchaan.Entity.ImportResult;
import com.Hackathon.Pehchaan.Enums.RecordStatus;
import com.Hackathon.Pehchaan.Repository.IdentityRecordRepository;
import com.Hackathon.Pehchaan.Repository.ImportResultRepository;
import com.Hackathon.Pehchaan.Repository.ImportSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ComparisonService {

    private final IdentityRecordRepository identityRecordRepository;
    private final ImportResultRepository importResultRepository;
    private final ImportSessionRepository importSessionRepository;

    public ComparisonService(
            IdentityRecordRepository identityRecordRepository,
            ImportResultRepository importResultRepository,
            ImportSessionRepository importSessionRepository) {
        this.identityRecordRepository = identityRecordRepository;
        this.importResultRepository = importResultRepository;
        this.importSessionRepository = importSessionRepository;
    }

    @Transactional
    public List<IdentityRecord> compare(Long importId) {
        importSessionRepository.findById(importId)
                .orElseThrow(() ->
                        new RuntimeException("Import session not found: " + importId));

        List<ImportResult> importedResults =
                importResultRepository.findByImportSessionId(importId);

        if (importedResults.isEmpty()) {
            throw new RuntimeException(
                    "No imported records found. Process the import file first.");
        }

        Set<String> importedKeys = new HashSet<>();

        for (ImportResult imported : importedResults) {
            if (imported.getName() == null
                    || imported.getSource() == null
                    || imported.getDate() == null
                    || imported.getCategory() == null) {
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(imported.getDate());

                importedKeys.add(createKey(
                        imported.getName(),
                        imported.getSource(),
                        date,
                        imported.getCategory()
                ));
            } catch (Exception ignored) {
                // Invalid imported rows are skipped.
            }
        }

        if (importedKeys.isEmpty()) {
            throw new RuntimeException(
                    "No valid records found for comparison."
            );
        }
        List<IdentityRecord> existingRecords =
                identityRecordRepository.findAll();

        int recognizedCount = 0;

        for (IdentityRecord record : existingRecords) {
            boolean matched = importedKeys.contains(createKey(
                    record.getName(),
                    record.getSource(),
                    record.getDate(),
                    record.getCategory() == null
                            ? null
                            : record.getCategory().name()
            ));

            record.setStatus(matched
                    ? RecordStatus.RECOGNIZED
                    : RecordStatus.NOT_RECOGNIZED);

            if (matched) {
                recognizedCount++;
            }
        }
        identityRecordRepository.saveAll(existingRecords);

        return existingRecords;
    }

    private String createKey(
            String name,
            String source,
            LocalDate date,
            String category) {

        return normalize(name) + "|"
                + normalize(source) + "|"
                + date + "|"
                + normalize(category);
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
