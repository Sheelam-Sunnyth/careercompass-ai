package com.careerassistant.interview;

import com.careerassistant.ai.GeminiClient;
import com.careerassistant.common.ApiException;
import com.careerassistant.security.CurrentUser;
import com.careerassistant.user.UserAccount;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name="interview_sessions")
class InterviewSession {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") UserAccount user;
 @Column(name="session_type") String sessionType; String category; String difficulty; Integer score;
 @Column(name="created_at") Instant createdAt=Instant.now();
}
@Entity @Table(name="interview_questions")
class InterviewQuestion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="session_id") InterviewSession session;
 @Column(columnDefinition="TEXT") String question; @Column(name="suggested_answer",columnDefinition="TEXT") String suggestedAnswer;
 @Column(name="user_answer",columnDefinition="TEXT") String userAnswer; @Column(columnDefinition="TEXT") String feedback; Integer score;
}
interface InterviewSessionRepository extends JpaRepository<InterviewSession,Long>{List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId); Optional<InterviewSession> findByIdAndUserId(Long id,Long userId);}
interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion,Long>{List<InterviewQuestion> findBySessionId(Long id); Optional<InterviewQuestion> findByIdAndSessionUserId(Long id,Long userId);}

@Service
class InterviewService {
 private final InterviewSessionRepository sessions; private final InterviewQuestionRepository questions; private final GeminiClient gemini;
 InterviewService(InterviewSessionRepository sessions,InterviewQuestionRepository questions,GeminiClient gemini){this.sessions=sessions;this.questions=questions;this.gemini=gemini;}
 @Transactional InterviewView generate(UserAccount user, GenerateRequest request) {
  JsonNode node=gemini.generateJson("Generate exactly "+request.questionCount()+" interview questions for category "+request.category()+" at "+request.difficulty()+" difficulty. Return JSON {questions:[{question:string,suggestedAnswer:string}]}. Do not include invented resume facts.");
  if(!node.path("questions").isArray()||node.path("questions").isEmpty()) throw ApiException.serviceUnavailable("Gemini returned an invalid interview-question schema.");
  InterviewSession session=new InterviewSession();session.user=user;session.sessionType="AI_GENERATED";session.category=request.category();session.difficulty=request.difficulty();sessions.save(session);
  for(JsonNode q:node.path("questions")){if(q.path("question").asText().isBlank())continue; InterviewQuestion question=new InterviewQuestion();question.session=session;question.question=q.path("question").asText();question.suggestedAnswer=q.path("suggestedAnswer").asText();questions.save(question);}
  return view(session);
 }
 @Transactional QuestionView evaluate(UserAccount user,Long sessionId,Long questionId,AnswerRequest answer){
  InterviewSession session=owned(user,sessionId); InterviewQuestion q=questions.findByIdAndSessionUserId(questionId,user.getId()).orElseThrow(()->ApiException.notFound("Interview question not found."));
  if(!q.session.id.equals(session.id)) throw ApiException.notFound("Interview question not found.");
  JsonNode result=gemini.generateJson("Evaluate this candidate answer for the following interview question. Return JSON {score: integer 0-100, feedback:string}. Be constructive, concise, and do not claim certainty. Question: "+q.question+" Answer: "+answer.answer());
  q.userAnswer=answer.answer();q.score=Math.max(0,Math.min(100,result.path("score").asInt()));q.feedback=result.path("feedback").asText("No feedback returned.");questions.save(q);
  List<InterviewQuestion> all=questions.findBySessionId(session.id);int total=all.stream().filter(x->x.score!=null).mapToInt(x->x.score).sum();long evaluated=all.stream().filter(x->x.score!=null).count();session.score=evaluated==0?null:(int)(total/evaluated);sessions.save(session);return question(q);
 }
 List<InterviewView> list(UserAccount user){return sessions.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::view).toList();}
 private InterviewSession owned(UserAccount u,Long id){return sessions.findByIdAndUserId(id,u.getId()).orElseThrow(()->ApiException.notFound("Interview session not found."));}
 private InterviewView view(InterviewSession s){return new InterviewView(s.id,s.category,s.difficulty,s.score,s.createdAt,questions.findBySessionId(s.id).stream().map(this::question).toList());}
 private QuestionView question(InterviewQuestion q){return new QuestionView(q.id,q.question,q.suggestedAnswer,q.userAnswer,q.feedback,q.score);}
 record GenerateRequest(@NotBlank String category,@NotBlank String difficulty,@NotNull Integer questionCount){}
 record AnswerRequest(@NotBlank String answer){}
 record QuestionView(Long id,String question,String suggestedAnswer,String userAnswer,String feedback,Integer score){}
 record InterviewView(Long id,String category,String difficulty,Integer score,Instant createdAt,List<QuestionView> questions){}
}
@RestController @RequestMapping("/api/interviews")
class InterviewController {
 private final InterviewService service; InterviewController(InterviewService service){this.service=service;}
 @PostMapping("/generate") InterviewService.InterviewView generate(@Valid @RequestBody InterviewService.GenerateRequest request){if(request.questionCount()<1||request.questionCount()>10)throw ApiException.badRequest("Choose between 1 and 10 questions.");return service.generate(CurrentUser.require(),request);}
 @PostMapping("/{sessionId}/questions/{questionId}/evaluate") InterviewService.QuestionView evaluate(@PathVariable Long sessionId,@PathVariable Long questionId,@Valid @RequestBody InterviewService.AnswerRequest answer){return service.evaluate(CurrentUser.require(),sessionId,questionId,answer);}
 @GetMapping List<InterviewService.InterviewView> list(){return service.list(CurrentUser.require());}
}
