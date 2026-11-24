package com.careerassistant.resume;

import com.careerassistant.ai.GeminiClient;
import com.careerassistant.common.ApiException;
import com.careerassistant.config.AppProperties;
import com.careerassistant.rag.ChromaClient;
import com.careerassistant.rag.DocumentChunk;
import com.careerassistant.rag.DocumentChunkRepository;
import com.careerassistant.user.UserAccount;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ResumeService {
    private static final Pattern PDF_MAGIC = Pattern.compile("^%PDF-");
    private final ResumeRepository resumes;
    private final ResumeAnalysisRepository analyses;
    private final ResumeSkillRepository skills;
    private final DocumentChunkRepository chunks;
    private final GeminiClient gemini;
    private final ChromaClient chroma;
    private final ObjectMapper mapper;
    private final Path uploadRoot;
    public ResumeService(ResumeRepository resumes, ResumeAnalysisRepository analyses, ResumeSkillRepository skills,
                         DocumentChunkRepository chunks, GeminiClient gemini, ChromaClient chroma, ObjectMapper mapper,
                         AppProperties properties) {
        this.resumes = resumes; this.analyses = analyses; this.skills = skills; this.chunks = chunks;
        this.gemini = gemini; this.chroma = chroma; this.mapper = mapper; this.uploadRoot = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
    }
    @Transactional
    public UploadResult upload(UserAccount user, MultipartFile file) {
        validatePdf(file);
        try {
            String original = Path.of(Objects.requireNonNull(file.getOriginalFilename())).getFileName().toString();
            Path userDirectory = uploadRoot.resolve(user.getId().toString());
            Files.createDirectories(userDirectory);
            Path target = userDirectory.resolve(UUID.randomUUID() + "-" + original).normalize();
            if (!target.startsWith(uploadRoot)) throw ApiException.badRequest("Invalid file path.");
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String text = extractPdf(target);
            if (text.isBlank()) { Files.deleteIfExists(target); throw ApiException.badRequest("No readable text was found in this PDF."); }
            Resume resume = new Resume();
            resume.setUser(user); resume.setFileName(original); resume.setFilePath(target.toString()); resume.setExtractedText(text); resume.setRagIndexed(false);
            resumes.save(resume);
            replaceSkills(resume, SkillCatalog.extract(text));
            String notice = null;
            if (gemini.available()) {
                try { index(resume); resume.setRagIndexed(true); resumes.save(resume); }
                catch (ApiException ex) { notice = "Resume uploaded, but RAG indexing is pending: " + ex.getMessage(); }
            } else {
                notice = "Resume uploaded. Add GEMINI_API_KEY and start ChromaDB to enable semantic document search.";
            }
            return new UploadResult(summary(resume), notice);
        } catch (IOException ex) {
            throw new ApiException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the uploaded resume.");
        }
    }
    @Transactional
    public AnalysisView analyze(UserAccount user, Long resumeId) {
        Resume resume = owned(user, resumeId);
        if (!gemini.available()) throw ApiException.serviceUnavailable("Resume analysis requires GEMINI_API_KEY. Configure it and restart the application.");
        JsonNode output = gemini.generateJson("""
                Analyze the resume below only using the supplied content. Do not invent qualifications, metrics, employment, projects, or credentials.
                Return JSON with: overallScore (integer 0-100, an estimated application-readiness score, not an official ATS score),
                strengths (string array), weaknesses (string array), recommendations (string array), sectionFeedback (object of section names to concise feedback).
                Resume:
                """ + limit(resume.getExtractedText(), 24000));
        int score = Math.max(0, Math.min(100, output.path("overallScore").asInt(0)));
        if (score == 0 && !output.has("overallScore")) throw ApiException.serviceUnavailable("Gemini returned a resume analysis with an invalid schema.");
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResume(resume); analysis.setOverallScore(score);
        analysis.setStrengths(toJson(array(output, "strengths"))); analysis.setWeaknesses(toJson(array(output, "weaknesses")));
        analysis.setRecommendations(toJson(array(output, "recommendations")));
        analysis.setSectionFeedback(toJson(output.path("sectionFeedback").isObject() ? output.path("sectionFeedback") : mapper.createObjectNode()));
        analyses.save(analysis);
        return analysisView(analysis);
    }
    public List<ResumeView> list(UserAccount user) { return resumes.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::summary).toList(); }
    public ResumeDetails detail(UserAccount user, Long id) {
        Resume resume = owned(user, id);
        return new ResumeDetails(summary(resume), skills.findByResumeId(id).stream().map(ResumeSkill::getSkillName).toList(),
                analyses.findByResumeIdOrderByCreatedAtDesc(id).stream().map(this::analysisView).toList());
    }
    @Transactional
    public void delete(UserAccount user, Long id) {
        Resume resume = owned(user, id);
        chroma.delete(chunks.findByResumeId(id).stream().map(DocumentChunk::getVectorId).toList());
        chunks.deleteByResumeId(id);
        try { Files.deleteIfExists(Path.of(resume.getFilePath())); } catch (IOException ignored) { }
        resumes.delete(resume);
    }
    public String report(UserAccount user, Long resumeId, Long analysisId) {
        owned(user, resumeId);
        ResumeAnalysis analysis = analyses.findByIdAndResumeId(analysisId, resumeId).orElseThrow(() -> ApiException.notFound("Analysis not found."));
        return "Resume analysis report\\nGenerated: " + analysis.getCreatedAt() + "\\n\\nEstimated application-readiness score: "
                + analysis.getOverallScore() + "/100 (not an official ATS score)\\n\\nStrengths\\n" + bullets(analysis.getStrengths())
                + "\\nWeaknesses\\n" + bullets(analysis.getWeaknesses()) + "\\nRecommendations\\n" + bullets(analysis.getRecommendations())
                + "\\nSection feedback\\n" + analysis.getSectionFeedback();
    }
    private void index(Resume resume) {
        chunks.deleteByResumeId(resume.getId());
        int index = 0;
        for (String chunkText : split(resume.getExtractedText())) {
            String vectorId = "resume-" + resume.getId() + "-" + index + "-" + UUID.randomUUID();
            List<Double> vector = gemini.embed("title: " + resume.getFileName() + " | text: " + chunkText);
            chroma.add(vectorId, vector, chunkText, resume.getUser().getId(), resume.getId());
            DocumentChunk chunk = new DocumentChunk();
            chunk.setResume(resume); chunk.setChunkIndex(index++); chunk.setChunkText(chunkText); chunk.setVectorId(vectorId); chunks.save(chunk);
        }
    }
    private void replaceSkills(Resume resume, Set<String> found) {
        skills.deleteByResumeId(resume.getId());
        found.forEach(skill -> { ResumeSkill entity = new ResumeSkill(); entity.setResume(resume); entity.setSkillName(skill); skills.save(entity); });
    }
    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) throw ApiException.badRequest("Choose a non-empty PDF resume.");
        if (file.getSize() > 10 * 1024 * 1024) throw ApiException.badRequest("PDF files must be 10 MB or smaller.");
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".pdf")) throw ApiException.badRequest("Only PDF resumes are supported.");
        try {
            byte[] head = file.getInputStream().readNBytes(5);
            if (!PDF_MAGIC.matcher(new String(head)).find()) throw ApiException.badRequest("The uploaded file is not a valid PDF.");
        } catch (IOException ex) { throw ApiException.badRequest("Could not read the uploaded PDF."); }
    }
    private String extractPdf(Path path) {
        try (PDDocument document = Loader.loadPDF(path.toFile())) { return new PDFTextStripper().getText(document).replaceAll("\\\\s+", " ").trim(); }
        catch (IOException ex) { throw ApiException.badRequest("Could not extract text from this PDF. It may be password protected or scanned."); }
    }
    private List<String> split(String text) {
        List<String> result = new ArrayList<>(); int step = 1200, overlap = 200;
        for (int start = 0; start < text.length(); start += step - overlap) {
            int end = Math.min(text.length(), start + step);
            if (end < text.length()) { int boundary = text.lastIndexOf(' ', end); if (boundary > start + 700) end = boundary; }
            result.add(text.substring(start, end)); if (end == text.length()) break;
        }
        return result;
    }
    private Resume owned(UserAccount user, Long id) { return resumes.findByIdAndUserId(id, user.getId()).orElseThrow(() -> ApiException.notFound("Resume not found.")); }
    private ResumeView summary(Resume r) { return new ResumeView(r.getId(), r.getFileName(), r.isRagIndexed(), r.getCreatedAt()); }
    private AnalysisView analysisView(ResumeAnalysis a) { return new AnalysisView(a.getId(), a.getOverallScore(), fromJson(a.getStrengths()), fromJson(a.getWeaknesses()), fromJson(a.getRecommendations()), fromJson(a.getSectionFeedback()), a.getCreatedAt()); }
    private List<String> array(JsonNode node, String field) { List<String> values = new ArrayList<>(); node.path(field).forEach(x -> { if (x.isTextual() && !x.asText().isBlank()) values.add(x.asText().trim()); }); return values; }
    private String toJson(Object value) { try { return mapper.writeValueAsString(value); } catch (Exception ex) { throw new IllegalStateException(ex); } }
    private Object fromJson(String json) { try { return mapper.readValue(json, Object.class); } catch (Exception ex) { return List.of(); } }
    private String bullets(String json) { Object object = fromJson(json); return object instanceof List<?> list ? list.stream().map(item -> "- " + item).reduce("", (a,b) -> a + b + "\\n") : json + "\\n"; }
    private String limit(String value, int max) { return value.length() <= max ? value : value.substring(0, max); }
    public record UploadResult(ResumeView resume, String notice) {}
    public record ResumeView(Long id, String fileName, boolean ragIndexed, Instant createdAt) {}
    public record ResumeDetails(ResumeView resume, List<String> skills, List<AnalysisView> analyses) {}
    public record AnalysisView(Long id, int overallScore, Object strengths, Object weaknesses, Object recommendations, Object sectionFeedback, Instant createdAt) {}
}
