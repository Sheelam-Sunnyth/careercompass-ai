package com.careerassistant.resume;

import com.careerassistant.security.CurrentUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService service;
    public ResumeController(ResumeService service) { this.service = service; }
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeService.UploadResult upload(@RequestPart("file") MultipartFile file) { return service.upload(CurrentUser.require(), file); }
    @GetMapping public List<ResumeService.ResumeView> list() { return service.list(CurrentUser.require()); }
    @GetMapping("/{id}") public ResumeService.ResumeDetails get(@PathVariable Long id) { return service.detail(CurrentUser.require(), id); }
    @PostMapping("/{id}/analyze") public ResumeService.AnalysisView analyze(@PathVariable Long id) { return service.analyze(CurrentUser.require(), id); }
    @GetMapping("/{id}/analyses/{analysisId}/report")
    public ResponseEntity<String> downloadReport(@PathVariable Long id, @PathVariable Long analysisId) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume-analysis.txt")
                .contentType(MediaType.TEXT_PLAIN).body(service.report(CurrentUser.require(), id, analysisId));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(CurrentUser.require(), id); return ResponseEntity.noContent().build(); }
}
