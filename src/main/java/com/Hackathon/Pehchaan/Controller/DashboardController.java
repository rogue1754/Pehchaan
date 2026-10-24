
package com.Hackathon.Pehchaan.Controller;

import com.Hackathon.Pehchaan.Entity.IdentityRecord;
import com.Hackathon.Pehchaan.Enums.RecordStatus;
import com.Hackathon.Pehchaan.Enums.ReportCategory;
import com.Hackathon.Pehchaan.Repository.IdentityRecordRepository;

import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final IdentityRecordRepository repository;

    public DashboardController(IdentityRecordRepository repository) {
        this.repository = repository;
    }

    // GET /api/dashboard
    // GET /api/dashboard/summary
    @GetMapping({"", "/summary"})
    public Map<String, Object> getDashboard() {
        List<IdentityRecord> records = repository.findAll();

        long recognized = records.stream()
                .filter(record ->
                        record.getStatus() == RecordStatus.RECOGNIZED)
                .count();

        long needsAttention = records.stream()
                .filter(record ->
                        record.getStatus() == RecordStatus.NOT_RECOGNIZED)
                .count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalRecords", records.size());
        response.put("recognized", recognized);
        response.put("needsAttention", needsAttention);

        return response;
    }

    // GET /api/dashboard/recent-records
    @GetMapping("/recent-records")
    public List<IdentityRecord> getRecentRecords() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(
                        IdentityRecord::getDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                ))
                .limit(5)
                .toList();
    }

    // GET /api/dashboard/category-summary
    @GetMapping("/category-summary")
    public Map<String, Long> getCategorySummary() {
        List<IdentityRecord> records = repository.findAll();
        Map<String, Long> response = new LinkedHashMap<>();

        for (ReportCategory category : ReportCategory.values()) {
            long count = records.stream()
                    .filter(record ->
                            record.getCategory() == category)
                    .count();

            response.put(category.name(), count);
        }

        long uncategorized = records.stream()
                .filter(record -> record.getCategory() == null)
                .count();

        response.put("UNCATEGORIZED", uncategorized);

        return response;
    }
}
