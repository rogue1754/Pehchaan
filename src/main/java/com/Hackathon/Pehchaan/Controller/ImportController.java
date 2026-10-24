
package com.Hackathon.Pehchaan.Controller;

import com.Hackathon.Pehchaan.Entity.IdentityRecord;
import com.Hackathon.Pehchaan.Entity.ImportFile;
import com.Hackathon.Pehchaan.Entity.ImportSession;
import com.Hackathon.Pehchaan.Enums.RecordStatus;
import com.Hackathon.Pehchaan.Enums.ReportCategory;
import com.Hackathon.Pehchaan.Repository.IdentityRecordRepository;
import com.Hackathon.Pehchaan.Repository.ImportFileRepository;
import com.Hackathon.Pehchaan.Repository.ImportSessionRepository;
import com.Hackathon.Pehchaan.Service.ComparisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.Hackathon.Pehchaan.Entity.ImportResult;
import com.Hackathon.Pehchaan.Repository.ImportResultRepository;
import com.Hackathon.Pehchaan.Service.ImportProcessingService;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/imports")
@CrossOrigin(origins = "*")
public class ImportController {

    private final ImportSessionRepository sessionRepository;
    private final ImportFileRepository fileRepository;
    private final ImportResultRepository resultRepository;
    private final ImportProcessingService processingService;
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private final IdentityRecordRepository identityRecordRepository;
    private final ComparisonService comparisonService;
    private static final Set<String> ALLOWED_TYPES =
            Set.of("pdf", "csv", "json", "txt");
    public ImportController(
            ImportSessionRepository sessionRepository,
            ImportFileRepository fileRepository,
            ImportResultRepository resultRepository,
            ImportProcessingService processingService,
            IdentityRecordRepository identityRecordRepository, ComparisonService comparisonService) {
        this.sessionRepository = sessionRepository;
        this.fileRepository = fileRepository;
        this.resultRepository = resultRepository;
        this.processingService = processingService;
        this.identityRecordRepository = identityRecordRepository;
        this.comparisonService = comparisonService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImportSession createImport() {
        return sessionRepository.save(new ImportSession());
    }

    @GetMapping
    public List<ImportSession> getAllImports() {
        return sessionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ImportSession getImportById(@PathVariable Long id) {
        return getSession(id);
    }

    @GetMapping("/{id}/status")
    public String getImportStatus(@PathVariable Long id) {
        return getSession(id).getStatus();
    }

    @PostMapping("/{id}/files")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportFile uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        ImportSession session = getSession(id);

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Maximum file size is 20 MB");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()
                || originalName.contains("/")
                || originalName.contains("\\")
                || originalName.contains("..")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        int dot = originalName.lastIndexOf('.');
        if (dot < 1 || dot == originalName.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supported types: PDF, CSV, JSON, TXT");
        }

        String extension =
                originalName.substring(dot + 1).toLowerCase();

        if (!ALLOWED_TYPES.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supported types: PDF, CSV, JSON, TXT");
        }

        try {
            ImportFile savedFile = new ImportFile();
            savedFile.setImportSession(session);
            savedFile.setFileName(originalName);
            savedFile.setFileType(extension.toUpperCase());
            savedFile.setFileSize(file.getSize());
            savedFile.setContent(file.getBytes());
            savedFile.setStatus("UPLOADED");

            return fileRepository.save(savedFile);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read uploaded file");
        }
    }

    @GetMapping("/{id}/files")
    public List<ImportFile> getFiles(@PathVariable Long id) {
        getSession(id);
        return fileRepository.findByImportSessionId(id);
    }

    @DeleteMapping("/{id}/files/{fileId}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
            @PathVariable Long id,
            @PathVariable Long fileId) {

        getSession(id);

        ImportFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File not found"));

        if (!file.getImportSession().getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "File does not belong to this import session");
        }

        fileRepository.delete(file);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImport(@PathVariable Long id)
    {
        ImportSession session = getSession(id);

        // Delete preview results first
        List<ImportResult> results =
                resultRepository.findByImportSessionId(id);
        resultRepository.deleteAll(results);

        // Then delete uploaded files
        List<ImportFile> files =
                fileRepository.findByImportSessionId(id);
        fileRepository.deleteAll(files);

        // Finally delete the import session
        sessionRepository.delete(session);

    }

    private ImportSession getSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import session not found"));
    }
    @PostMapping("/{id}/process")
    public List<com.Hackathon.Pehchaan.Entity.ImportResult> processImport(
            @PathVariable Long id) {

        List<com.Hackathon.Pehchaan.Entity.ImportResult> results =
                processingService.process(id);

        if (results != null && !results.isEmpty()) {
            comparisonService.compare(id);
        }

        return results;
    }

    @GetMapping("/{id}/results")
    public List<com.Hackathon.Pehchaan.Entity.ImportResult> getResults(
            @PathVariable Long id) {
        getSession(id);
        return resultRepository.findByImportSessionId(id);
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    public List<IdentityRecord> confirmImport(@PathVariable Long id) {

        ImportSession session = getSession(id);
        if (!"READY_FOR_REVIEW".equals(session.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Process the import before confirming it"
            );
        }

        List<ImportResult> results =
                resultRepository.findByImportSessionId(id);

        if (results.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No import results available to confirm"
            );
        }

        for (ImportResult result : results) {
            if (result.isConfirmed()) {
                continue;
            }

            try {
                if (result.getName() == null || result.getName().isBlank()
                        || result.getSource() == null || result.getSource().isBlank()
                        || result.getDate() == null || result.getDate().isBlank()
                        || result.getStatus() == null
                        || result.getCategory() == null) {
                    throw new IllegalArgumentException();
                }

                java.time.LocalDate.parse(result.getDate());
                RecordStatus.valueOf(result.getStatus());
                ReportCategory.valueOf(result.getCategory());

            } catch (RuntimeException exception) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid import result ID: " + result.getId()
                );
            }
        }

        List<IdentityRecord> savedRecords = new java.util.ArrayList<>();

        for (ImportResult result : results) {
            if (result.isConfirmed()) {
                continue;
            }

            if (result.getName() == null || result.getName().isBlank()
                    || result.getSource() == null || result.getSource().isBlank()
                    || result.getDate() == null || result.getDate().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Every record must have name, source, and date"
                );
            }

            IdentityRecord record = new IdentityRecord();
            record.setName(result.getName());
            record.setSource(result.getSource());
            record.setDate(java.time.LocalDate.parse(result.getDate()));
            record.setStatus(RecordStatus.valueOf(result.getStatus()));
            record.setCategory(ReportCategory.valueOf(result.getCategory()));
            record.setDetail(result.getDetail());

            savedRecords.add(identityRecordRepository.save(record));

            result.setConfirmed(true);
            resultRepository.save(result);
        }

        session.setStatus("COMPLETED");
        sessionRepository.save(session);

        return savedRecords;
    }
    @PutMapping("/{id}/results/{resultId}")
    @Transactional
    public ImportResult updateImportResult(
            @PathVariable Long id,
            @PathVariable Long resultId,
            @RequestBody ImportResult updatedResult) {

        ImportSession session = getSession(id);

        if (!"READY_FOR_REVIEW".equals(session.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Process the import before editing results"
            );
        }

        ImportResult result = resultRepository.findById(resultId)
                .filter(item -> item.getImportSession().getId().equals(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Import result not found"
                ));

        if (result.isConfirmed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Confirmed results cannot be edited"
            );
        }

        result.setName(updatedResult.getName());
        result.setSource(updatedResult.getSource());
        result.setDate(updatedResult.getDate());
        result.setStatus(updatedResult.getStatus());
        result.setCategory(updatedResult.getCategory());
        result.setDetail(updatedResult.getDetail());

        return resultRepository.save(result);
    }

}
