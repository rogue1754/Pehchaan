
package com.Hackathon.Pehchaan.Controller;

import com.Hackathon.Pehchaan.Entity.GuidanceProgress;
import com.Hackathon.Pehchaan.Enums.GuidanceType;
import com.Hackathon.Pehchaan.Repository.GuidanceProgressRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/guidance")
@CrossOrigin(origins = "*")
public class GuidanceProgressController {

    private final GuidanceProgressRepository repository;

    public GuidanceProgressController(
            GuidanceProgressRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{type}/progress")
    public List<GuidanceProgress> getProgress(
            @PathVariable String type) {
        return repository.findByGuidanceType(parseType(type));
    }

    @PostMapping("/{type}/progress")
    public GuidanceProgress saveProgress(
            @PathVariable String type,
            @RequestBody Map<String, Object> body) {

        String guidanceType = parseType(type);
        Object rawStepKey = body.get("stepKey");
        Object rawCompleted = body.get("completed");

        if (!(rawStepKey instanceof String stepKey)
                || stepKey.isBlank()
                || !(rawCompleted instanceof Boolean completed)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide stepKey and completed");
        }

        GuidanceProgress progress =
                repository.findByGuidanceTypeAndStepKey(
                                guidanceType, stepKey)
                        .orElseGet(GuidanceProgress::new);

        progress.setGuidanceType(guidanceType);
        progress.setStepKey(stepKey);
        progress.setCompleted(completed);

        return repository.save(progress);
    }

    private String parseType(String type) {
        try {
            return GuidanceType.valueOf(
                    type.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Guidance type not found");
        }
    }
}
