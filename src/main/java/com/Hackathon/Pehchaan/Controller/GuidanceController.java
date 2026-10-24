
package com.Hackathon.Pehchaan.Controller;

import com.Hackathon.Pehchaan.Enums.GuidanceType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/guidance")
@CrossOrigin(origins = "*")
public class GuidanceController {

    @GetMapping
    public List<Map<String, Object>> getAllGuidance() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (GuidanceType type : GuidanceType.values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", type.name());
            item.put("title", formatTitle(type.name()));
            item.put("description",
                    "Guidance and resources for " + formatTitle(type.name()));
            result.add(item);
        }

        return result;
    }

    @GetMapping("/{type}")
    public Map<String, Object> getGuidance(
            @PathVariable String type) {
        GuidanceType guidanceType = parseType(type);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", guidanceType.name());
        result.put("title", formatTitle(guidanceType.name()));
        result.put("description",
                "Guidance and resources for "
                        + formatTitle(guidanceType.name()));

        return result;
    }

    @GetMapping("/{type}/steps")
    public List<String> getSteps(@PathVariable String type) {
        GuidanceType guidanceType = parseType(type);

        return switch (guidanceType) {
            case CREDIT -> List.of(
                    "Obtain your credit report from an authorized provider.",
                    "Review personal details and account information.",
                    "Identify inaccurate entries and gather supporting documents.",
                    "Submit a correction request to the relevant provider."
            );
            case TAX -> List.of(
                    "Open the official income-tax portal.",
                    "Sign in using your own credentials.",
                    "Review the relevant tax information.",
                    "Follow the portal's instructions for corrections or filing."
            );
            case MOBILE -> List.of(
                    "Review mobile connections registered in your name.",
                    "Identify connections you do not recognize.",
                    "Follow the official reporting or disconnection process."
            );
            case AADHAAR -> List.of(
                    "Use the official UIDAI website.",
                    "Review the available Aadhaar services.",
                    "Follow the official process for updating or reporting details."
            );
            case VEHICLE -> List.of(
                    "Open the official Parivahan portal.",
                    "Review the available vehicle-related services.",
                    "Verify details using the relevant official records."
            );
            case PROPERTY -> List.of(
                    "Identify the relevant state or local land-records portal.",
                    "Search using the required property details.",
                    "Compare the results with your supporting documents."
            );
            case VOTER -> List.of(
                    "Open the official Election Commission voter services portal.",
                    "Check the available electoral-roll services.",
                    "Follow the official process to correct or report details."
            );
            case BANKING -> List.of(
                    "Contact your bank through its official channel.",
                    "Review account information and transactions.",
                    "Report discrepancies using the bank's formal process."
            );
            case BUSINESS -> List.of(
                    "Identify the relevant official business-registration portal.",
                    "Review the available registration or filing records.",
                    "Contact the responsible authority if details appear incorrect."
            );
        };
    }

    @GetMapping("/{type}/checklist")
    public List<String> getChecklist(@PathVariable String type) {
        parseType(type);

        return List.of(
                "Keep relevant identity and supporting documents ready.",
                "Check that names and dates match your documents.",
                "Save reference numbers and acknowledgements.",
                "Review information before submitting changes."
        );
    }

    @GetMapping("/{type}/official-links")
    public List<Map<String, String>> getOfficialLinks(
            @PathVariable String type) {
        GuidanceType guidanceType = parseType(type);

        Map<String, String> link = new LinkedHashMap<>();
        link.put("title", "Official website");
        link.put("url", officialUrl(guidanceType));

        return List.of(link);
    }

    private GuidanceType parseType(String type) {
        try {
            return GuidanceType.valueOf(
                    type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Guidance type not found");
        }
    }

    private String formatTitle(String value) {
        return value.charAt(0)
                + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private String officialUrl(GuidanceType type) {
        return switch (type) {
            case CREDIT -> "https://www.cibil.com/";
            case TAX -> "https://www.incometax.gov.in/";
            case MOBILE -> "https://www.sancharsaathi.gov.in/";
            case AADHAAR -> "https://uidai.gov.in/";
            case VEHICLE -> "https://parivahan.gov.in/";
            case PROPERTY -> "https://dolr.gov.in/";
            case VOTER -> "https://voters.eci.gov.in/";
            case BANKING -> "https://www.rbi.org.in/";
            case BUSINESS -> "https://www.mca.gov.in/";
        };
    }
}
