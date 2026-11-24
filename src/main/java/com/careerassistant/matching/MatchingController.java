package com.careerassistant.matching;
import com.careerassistant.common.ApiException; import com.careerassistant.job.*; import com.careerassistant.resume.*;
import com.careerassistant.security.CurrentUser; import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotNull; import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.*;
@RestController @RequestMapping("/api/matches")
public class MatchingController {
 private final ResumeRepository resumes; private final ResumeSkillRepository skills; private final JobDescriptionRepository jobs; private final JobMatchRepository matches; private final ObjectMapper mapper;
 public MatchingController(ResumeRepository resumes,ResumeSkillRepository skills,JobDescriptionRepository jobs,JobMatchRepository matches,ObjectMapper mapper){this.resumes=resumes;this.skills=skills;this.jobs=jobs;this.matches=matches;this.mapper=mapper;}
 @PostMapping public MatchView create(@Valid @RequestBody MatchRequest request){
  Long userId=CurrentUser.require().getId(); Resume resume=resumes.findByIdAndUserId(request.resumeId(),userId).orElseThrow(()->ApiException.notFound("Resume not found."));
  JobDescription job=jobs.findByIdAndUserId(request.jobId(),userId).orElseThrow(()->ApiException.notFound("Job description not found."));
  Set<String> resumeSkills=new LinkedHashSet<>(); skills.findByResumeId(resume.getId()).forEach(s->resumeSkills.add(s.getSkillName()));
  Set<String> jobSkills=SkillCatalog.extract(job.getDescription()); List<String> matched=jobSkills.stream().filter(resumeSkills::contains).toList();
  List<String> missing=jobSkills.stream().filter(s->!resumeSkills.contains(s)).toList(); int score=jobSkills.isEmpty()?0:(int)Math.round(100.0*matched.size()/jobSkills.size());
  String report=jobSkills.isEmpty()?"No catalogued technical skills were detected in the job description; add a more detailed description.":"Transparent score: "+matched.size()+" of "+jobSkills.size()+" detected job skills appear in this resume. Improve evidence for: "+String.join(", ",missing);
  JobMatch match=new JobMatch();match.setResume(resume);match.setJob(job);match.setMatchScore(score);match.setMatchedSkills(json(matched));match.setMissingSkills(json(missing));match.setPartialSkills(json(List.of()));match.setReport(report);matches.save(match);return map(match);
 }
 @GetMapping("/{id}") public MatchView get(@PathVariable Long id){return map(matches.findByIdAndJobUserId(id,CurrentUser.require().getId()).orElseThrow(()->ApiException.notFound("Match not found.")));}
 private String json(Object value){try{return mapper.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
 private List<String> array(String value){try{return mapper.readValue(value,mapper.getTypeFactory().constructCollectionType(List.class,String.class));}catch(Exception e){return List.of();}}
 private MatchView map(JobMatch m){return new MatchView(m.getId(),m.getResume().getId(),m.getJob().getId(),m.getMatchScore(),array(m.getMatchedSkills()),array(m.getMissingSkills()),m.getReport(),m.getCreatedAt());}
 public record MatchRequest(@NotNull Long resumeId,@NotNull Long jobId){} public record MatchView(Long id,Long resumeId,Long jobId,int matchScore,List<String> matchedSkills,List<String> missingSkills,String report,Instant createdAt){}
}
