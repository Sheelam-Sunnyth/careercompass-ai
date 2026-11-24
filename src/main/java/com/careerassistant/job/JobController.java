package com.careerassistant.job;
import com.careerassistant.common.ApiException; import com.careerassistant.security.CurrentUser;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.List;
@RestController @RequestMapping("/api/jobs")
public class JobController {
 private final JobDescriptionRepository jobs; public JobController(JobDescriptionRepository jobs){this.jobs=jobs;}
 @PostMapping public ResponseEntity<JobView> create(@Valid @RequestBody JobRequest request){
  JobDescription job=new JobDescription(); job.setUser(CurrentUser.require()); job.setCompany(request.company().trim()); job.setJobTitle(request.jobTitle().trim()); job.setDescription(request.description().trim());
  return ResponseEntity.status(HttpStatus.CREATED).body(map(jobs.save(job)));
 }
 @GetMapping public List<JobView> list(){return jobs.findByUserIdOrderByCreatedAtDesc(CurrentUser.require().getId()).stream().map(this::map).toList();}
 private JobView map(JobDescription j){return new JobView(j.getId(),j.getCompany(),j.getJobTitle(),j.getDescription(),j.getCreatedAt());}
 public record JobRequest(@NotBlank @Size(max=160) String company,@NotBlank @Size(max=160) String jobTitle,@NotBlank @Size(max=30000) String description){}
 public record JobView(Long id,String company,String jobTitle,String description,Instant createdAt){}
}
