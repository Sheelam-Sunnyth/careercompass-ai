package com.careerassistant.tracker;

import com.careerassistant.common.ApiException;
import com.careerassistant.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final JobApplicationRepository applications;
    public ApplicationController(JobApplicationRepository applications) { this.applications = applications; }
    @GetMapping
    public List<ApplicationResponse> list(@RequestParam(required = false) String status, @RequestParam(required = false) String company,
                                          @RequestParam(required = false, defaultValue = "applicationDate") String sort) {
        Long userId = CurrentUser.require().getId();
        return applications.findByUserIdOrderByApplicationDateDesc(userId).stream()
                .filter(item -> status == null || status.isBlank() || item.getStatus().name().equalsIgnoreCase(status))
                .filter(item -> company == null || company.isBlank() || item.getCompany().toLowerCase().contains(company.toLowerCase()))
                .sorted(sort.equalsIgnoreCase("company") ? Comparator.comparing(JobApplication::getCompany, String.CASE_INSENSITIVE_ORDER)
                        : Comparator.comparing(JobApplication::getApplicationDate).reversed())
                .map(this::map).toList();
    }
    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request) {
        JobApplication application = new JobApplication(); apply(application, request); application.setUser(CurrentUser.require());
        return ResponseEntity.status(HttpStatus.CREATED).body(map(applications.save(application)));
    }
    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
        JobApplication application = owned(id); apply(application, request); return map(applications.save(application));
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { applications.delete(owned(id)); }
    private JobApplication owned(Long id) {
        return applications.findByIdAndUserId(id, CurrentUser.require().getId()).orElseThrow(() -> ApiException.notFound("Application not found."));
    }
    private void apply(JobApplication a, ApplicationRequest r) {
        a.setCompany(r.company().trim()); a.setPosition(r.position().trim()); a.setJobDescription(blank(r.jobDescription()));
        a.setApplicationDate(r.applicationDate()); a.setStatus(r.status()); a.setInterviewDate(r.interviewDate());
        a.setJobUrl(blank(r.jobUrl())); a.setNotes(blank(r.notes()));
    }
    private String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private ApplicationResponse map(JobApplication a) {
        return new ApplicationResponse(a.getId(), a.getCompany(), a.getPosition(), a.getJobDescription(), a.getApplicationDate(),
                a.getStatus(), a.getInterviewDate(), a.getJobUrl(), a.getNotes());
    }
    public record ApplicationRequest(@NotBlank @Size(max = 160) String company, @NotBlank @Size(max = 160) String position,
            @Size(max = 20000) String jobDescription, @NotNull LocalDate applicationDate, @NotNull ApplicationStatus status,
            LocalDateTime interviewDate, @Size(max = 1000) String jobUrl, @Size(max = 10000) String notes) {}
    public record ApplicationResponse(Long id, String company, String position, String jobDescription, LocalDate applicationDate,
            ApplicationStatus status, LocalDateTime interviewDate, String jobUrl, String notes) {}
}
