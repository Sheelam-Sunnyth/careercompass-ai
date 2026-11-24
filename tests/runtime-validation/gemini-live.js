import assert from "node:assert/strict";
import {
  baseUrl,
  cleanupAccount,
  createPdf,
  registerAccount,
  request,
} from "./helpers.js";

const results = [];
let account;
let resumeId;
let conversationId;

async function verify(feature, action) {
  for (let attempt = 1; attempt <= 2; attempt++) {
    try {
      const detail = await action();
      results.push({ feature, status: "PASS", attempts: attempt, detail });
      return detail;
    } catch (error) {
      if (attempt === 1 && error.httpStatus === 503) {
        await new Promise((resolve) => setTimeout(resolve, 3000));
        continue;
      }
      results.push({ feature, status: "FAIL", attempts: attempt, detail: error.message });
      return null;
    }
  }
  return null;
}

function assertStatus(result, status) {
  if (result.response.status !== status) {
    const error = new Error(`${JSON.stringify(result.payload)}\n${result.response.status} !== ${status}`);
    error.httpStatus = result.response.status;
    throw error;
  }
  return result.payload;
}

const health = assertStatus(await request("/api/health"), 200);
assert.equal(health.geminiConfigured, true, "GEMINI_API_KEY must be configured");
account = await registerAccount();

try {
  await verify("Resume PDF upload and Gemini embedding/indexing", async () => {
    const form = new FormData();
    form.append(
      "file",
      new Blob([createPdf("Java Spring Boot REST APIs MySQL Docker JUnit")], {
        type: "application/pdf",
      }),
      "live-gemini-smoke-test.pdf",
    );
    const response = await fetch(`${baseUrl}/api/resumes/upload`, {
      method: "POST",
      headers: { Authorization: `Bearer ${account.token}` },
      body: form,
    });
    const payload = await response.json();
    assert.equal(response.status, 200, JSON.stringify(payload));
    resumeId = payload.resume.id;
    assert.equal(payload.resume.ragIndexed, true, payload.notice ?? "Resume was not indexed");
    return { resumeId, indexed: payload.resume.ragIndexed };
  });

  const analysis = resumeId
    ? await verify("Resume AI analysis", async () => {
        const payload = assertStatus(
          await request(`/api/resumes/${resumeId}/analyze`, {
            token: account.token,
            method: "POST",
          }),
          200,
        );
        assert.ok(Number.isInteger(payload.overallScore));
        assert.ok(payload.overallScore >= 0 && payload.overallScore <= 100);
        assert.ok(Array.isArray(payload.strengths));
        return { analysisId: payload.id, score: payload.overallScore };
      })
    : null;

  if (analysis && resumeId) {
    await verify("Resume analysis report download", async () => {
      const response = await fetch(
        `${baseUrl}/api/resumes/${resumeId}/analyses/${analysis.analysisId}/report`,
        { headers: { Authorization: `Bearer ${account.token}` } },
      );
      const report = await response.text();
      assert.equal(response.status, 200, report);
      assert.match(report, /Estimated application-readiness score/);
      return "Report returned";
    });
  } else {
    results.push({
      feature: "Resume analysis report download",
      status: "SKIPPED",
      detail: "Resume analysis did not produce an analysis record.",
    });
  }

  const interview = await verify("Interview question generation", async () => {
    const payload = assertStatus(
      await request("/api/interviews/generate", {
        token: account.token,
        method: "POST",
        body: { category: "Core Java", difficulty: "BEGINNER", questionCount: 1 },
      }),
      200,
    );
    assert.ok(payload.questions.length > 0);
    assert.ok(payload.questions[0].question.trim());
    return { sessionId: payload.id, questionId: payload.questions[0].id };
  });

  if (interview) {
    await verify("Interview answer evaluation", async () => {
      const payload = assertStatus(
        await request(
          `/api/interviews/${interview.sessionId}/questions/${interview.questionId}/evaluate`,
          {
            token: account.token,
            method: "POST",
            body: { answer: "I use Java with Spring Boot to build REST APIs and test them with JUnit." },
          },
        ),
        200,
      );
      assert.ok(Number.isInteger(payload.score));
      assert.ok(payload.score >= 0 && payload.score <= 100);
      assert.ok(payload.feedback.trim());
      return { score: payload.score };
    });
  } else {
    results.push({
      feature: "Interview answer evaluation",
      status: "SKIPPED",
      detail: "Interview generation did not produce a question.",
    });
  }

  const roadmap = await verify("Learning roadmap generation", async () => {
    const payload = assertStatus(
      await request("/api/roadmaps/generate", {
        token: account.token,
        method: "POST",
        body: { targetRole: "Java Developer", currentSkills: "Java, SQL, REST APIs" },
      }),
      200,
    );
    assert.ok(payload.tasks.length > 0);
    return { roadmapId: payload.id, taskId: payload.tasks[0].id, tasks: payload.tasks.length };
  });

  if (roadmap) {
    await verify("Roadmap task progress update", async () => {
      const payload = assertStatus(
        await request(`/api/roadmaps/tasks/${roadmap.taskId}`, {
          token: account.token,
          method: "PUT",
          body: { completed: true },
        }),
        200,
      );
      assert.equal(payload.status, "COMPLETED");
      return { taskId: payload.id, status: payload.status };
    });
  } else {
    results.push({
      feature: "Roadmap task progress update",
      status: "SKIPPED",
      detail: "Roadmap generation did not produce a task.",
    });
  }

  const conversation = await verify("Chat conversation setup", async () => {
    const payload = assertStatus(
      await request("/api/chat/conversations", {
        token: account.token,
        method: "POST",
        body: { title: "Gemini runtime verification" },
      }),
      200,
    );
    conversationId = payload.id;
    return { conversationId };
  });

  if (conversation) {
    await verify("Grounded chat embedding, retrieval, and answer", async () => {
      const payload = assertStatus(
        await request(`/api/chat/${conversationId}/messages`, {
          token: account.token,
          method: "POST",
          body: { message: "Which programming and backend skills are listed in my resume?" },
        }),
        200,
      );
      assert.ok(payload.message.message.trim());
      assert.ok(Array.isArray(payload.message.sourceRefs));
      assert.ok(payload.message.sourceRefs.length > 0);
      return { answerReturned: true, sourceCount: payload.message.sourceRefs.length };
    });
  } else {
    results.push({
      feature: "Grounded chat embedding, retrieval, and answer",
      status: "SKIPPED",
      detail: "Chat conversation setup failed.",
    });
  }
} finally {
  if (conversationId) {
    const deleted = await request(`/api/chat/conversations/${conversationId}`, {
      token: account.token,
      method: "DELETE",
    });
    assert.equal(deleted.response.status, 200);
  }
  if (resumeId) {
    const deleted = await request(`/api/resumes/${resumeId}`, {
      token: account.token,
      method: "DELETE",
    });
    assert.equal(deleted.response.status, 204);
  }
  cleanupAccount(account.email);
}

console.log(JSON.stringify({ results }, null, 2));
if (results.some((result) => result.status === "FAIL" || result.status === "SKIPPED")) {
  process.exitCode = 1;
}
