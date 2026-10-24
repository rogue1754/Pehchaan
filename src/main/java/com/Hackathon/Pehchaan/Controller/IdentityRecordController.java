
package com.Hackathon.Pehchaan.Controller;

import com.Hackathon.Pehchaan.Entity.IdentityRecord;
import com.Hackathon.Pehchaan.Enums.GuidanceType;
import com.Hackathon.Pehchaan.Enums.RecordStatus;
import com.Hackathon.Pehchaan.Repository.IdentityRecordRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.Hackathon.Pehchaan.Enums.GuidanceType;
import com.Hackathon.Pehchaan.Enums.ReportCategory;
import java.util.*;

@RestController
@RequestMapping("/api/records")
@CrossOrigin(origins = "*")
public class IdentityRecordController {

    private final IdentityRecordRepository repository;

    public IdentityRecordController(IdentityRecordRepository repository) {
        this.repository = repository;
    }


    @GetMapping
    public List<IdentityRecord> getRecords(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {

        RecordStatus statusFilter = parseStatus(status);
        ReportCategory categoryFilter = parseCategory(category);

        return repository.findAll().stream()
                .filter(record ->
                        search == null || search.isBlank()
                                || contains(record.getName(), search)
                                || contains(record.getSource(), search)
                                || contains(record.getDetail(), search))
                .filter(record ->
                        statusFilter == null
                                || statusFilter == record.getStatus())
                .filter(record ->
                        categoryFilter == null
                                || categoryFilter == record.getCategory())
                .toList();
    }

    private ReportCategory parseCategory(String value) {
        if (value == null || value.isBlank()
                || value.equalsIgnoreCase("all")) {
            return null;
        }

        try {
            return ReportCategory.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
                            .replace(' ', '_')
                            .replace('-', '_')
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid category. Use a valid ReportCategory value."
            );
        }
    }


    @GetMapping("/{id}")
    public IdentityRecord getRecord(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Record not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdentityRecord createRecord(
            @RequestBody IdentityRecord request) {

        if (request.getName() == null || request.getName().isBlank()
                || request.getSource() == null || request.getSource().isBlank()
                || request.getDate() == null
                || request.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name, source, date and status are required");
        }

        request.setId(null);
        return repository.save(request);
    }

    @PatchMapping("/{id}/status")
    public IdentityRecord updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {

        if (request == null || request.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status is required");
        }

        RecordStatus newStatus = parseStatus(request.getStatus());

        if (newStatus == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be RECOGNIZED or NOT_RECOGNIZED");
        }

        IdentityRecord record = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Record not found: " + id));

        record.setStatus(newStatus);
        return repository.save(record);
    }

    private RecordStatus parseStatus(String value) {
        if (value == null || value.isBlank()
                || value.equalsIgnoreCase("all")) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');

        try {
            return RecordStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status. Use RECOGNIZED or NOT_RECOGNIZED");
        }
    }

    private boolean contains(String value, String search) {
        return value != null
                && value.toLowerCase(Locale.ROOT)
                .contains(search.toLowerCase(Locale.ROOT));
    }

    public static class StatusUpdateRequest {
        private String status;

        public StatusUpdateRequest() {
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @PutMapping("/{id}")
    public IdentityRecord updateRecord(
            @PathVariable Long id,
            @RequestBody IdentityRecord request) {

        if (request.getName() == null || request.getName().isBlank()
                || request.getSource() == null || request.getSource().isBlank()
                || request.getDate() == null
                || request.getStatus() == null
                || request.getDetail() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name, source, date, status and detail are required"
            );
        }

        IdentityRecord existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Record not found: " + id
                ));

        existing.setName(request.getName());
        existing.setSource(request.getSource());
        existing.setDate(request.getDate());
        existing.setStatus(request.getStatus());
        existing.setDetail(request.getDetail());

        return repository.save(existing);
    }


    @GetMapping("/{id}/guidance")
    public Map<String, Object> getRecordGuidance(@PathVariable Long id) {

        IdentityRecord record = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Record not found"
                ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("recordId", record.getId());
        response.put("category", record.getCategory());

        GuidanceType guidanceType = mapGuidanceType(record.getCategory());

        if (guidanceType == null) {
            response.put("guidanceAvailable", false);
            response.put("guidanceUrl", null);
        } else {
            response.put("guidanceAvailable", true);
            response.put("guidanceType", guidanceType.name());
            response.put("guidanceUrl",
                    "/api/guidance/" + guidanceType.name());
        }

        return response;
    }

    private GuidanceType mapGuidanceType(ReportCategory category) {
        if (category == null) {
            return null;
        }

        return switch (category) {
            case CREDIT -> GuidanceType.CREDIT;
            case INCOME_TAX, TAX_RETURNS -> GuidanceType.TAX;
            case IDENTITY -> GuidanceType.AADHAAR;
            case BANKING -> GuidanceType.BANKING;
            case DRIVING, VEHICLE -> GuidanceType.VEHICLE;
            case VOTER -> GuidanceType.VOTER;
            case PROPERTY -> GuidanceType.PROPERTY;
            case BUSINESS -> GuidanceType.BUSINESS;
            case TELECOM -> GuidanceType.MOBILE;
            default -> null;
        };
    }


}
