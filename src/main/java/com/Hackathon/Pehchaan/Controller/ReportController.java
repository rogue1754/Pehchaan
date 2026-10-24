
package com.Hackathon.Pehchaan.Controller;

import com.Hackathon.Pehchaan.Entity.Report;
import com.Hackathon.Pehchaan.Repository.ReportRepository;
import com.Hackathon.Pehchaan.Repository.IdentityRecordRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
class ReportController {

    private final ReportRepository reportRepository;
    private final IdentityRecordRepository recordRepository;

    public ReportController(
            ReportRepository reportRepository,
            IdentityRecordRepository recordRepository) {
        this.reportRepository = reportRepository;
        this.recordRepository = recordRepository;
    }

    // Generate and save a report
    @PostMapping("/generate")
    public Report generateReport(
            @RequestParam(defaultValue = "Pehchaan Evidence Report")
            String title) {

        Report report = new Report();
        report.setTitle(title);
        report.setFormat("CSV");
        report.setStatus("GENERATED");
        report.setContent(buildCsv());

        return reportRepository.save(report);
    }

    // List all reports
    @GetMapping
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // Get one report
    @GetMapping("/{id}")
    public Report getReportById(@PathVariable Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Report not found"));
    }

    // Export report as CSV
    @GetMapping("/{id}/export/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long id) {
        Report report = getReportById(id);

        byte[] content = (report.getContent() == null
                ? "" : report.getContent())
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }

    // Download report as PDF

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Report report = getReportById(id);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);

            document.open();
            Font heading = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 16);

            document.add(new Paragraph(report.getTitle(), heading));
            document.add(new Paragraph("Report ID: " + report.getId()));
            document.add(new Paragraph("Generated at: " + report.getCreatedAt()));
            document.add(new Paragraph(" "));

            if (report.getContent() != null) {
                for (String line : report.getContent().split("\\R")) {
                    document.add(new Paragraph(line));
                }
            }

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"report-" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(output.toByteArray());

        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not generate PDF report");
        }
    }


    // Delete report
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        Report report = getReportById(id);
        reportRepository.delete(report);
        return ResponseEntity.noContent().build();
    }

    private String buildCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,source,date,status,category,detail\n");

        recordRepository.findAll().forEach(record -> {
            csv.append(record.getId()).append(',')
                    .append(escapeCsv(record.getName())).append(',')
                    .append(escapeCsv(record.getSource())).append(',')
                    .append(record.getDate() == null ? "" : record.getDate()).append(',')
                    .append(record.getStatus() == null ? "" : record.getStatus()).append(',')
                    .append(record.getCategory() == null ? "" : record.getCategory()).append(',')
                    .append(escapeCsv(record.getDetail()))
                    .append('\n');
        });

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
