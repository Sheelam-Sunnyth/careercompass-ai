import assert from "node:assert/strict";
import { after, test } from "node:test";
import { baseUrl, cleanupAccount, createPdf, registerAccount, request, skipWhenGeminiConfigured } from "./helpers.js";

const accounts = [];
after(() => accounts.forEach((account) => cleanupAccount(account.email)));

test("upload a resume, save a job, compare skills and handle AI configuration", async (context) => {
  await skipWhenGeminiConfigured(context);
  const account = await registerAccount();
  accounts.push(account);
  let resumeId;

  try {
    const denied = await request("/api/resumes");
    assert.equal(denied.response.status, 403);

    const form = new FormData();
    form.append(
      "file",
      new Blob([createPdf("Java Spring Boot REST APIs MySQL Docker JUnit")], {
        type: "application/pdf",
      }),
      "runtime-resume.pdf",
    );
    const upload = await fetch(`${baseUrl}/api/resumes/upload`, {
      method: "POST",
      headers: { Authorization: `Bearer ${account.token}` },
      body: form,
    });
    assert.equal(upload.status, 200);
    const uploadResult = await upload.json();
    resumeId = uploadResult.resume.id;
    assert.equal(uploadResult.resume.ragIndexed, false);
    assert.match(uploadResult.notice, /GEMINI_API_KEY/);

    const resumes = await request("/api/resumes", { token: account.token });
    assert.equal(resumes.response.status, 200);
    assert.equal(resumes.payload.length, 1);
    const details = await request(`/api/resumes/${resumeId}`, { token: account.token });
    assert.ok(details.payload.skills.includes("Java"));
    assert.ok(details.payload.skills.includes("Spring Boot"));

    const job = await request("/api/jobs", {
      token: account.token,
      method: "POST",
      body: {
        company: "Runtime Test Co",
        jobTitle: "Java Engineer",
        description: "We need Java Spring Boot REST APIs and MySQL experience.",
      },
    });
    assert.equal(job.response.status, 201);

    const jobs = await request("/api/jobs", { token: account.token });
    assert.equal(jobs.response.status, 200);
    assert.equal(jobs.payload.length, 1);

    const match = await request("/api/matches", {
      token: account.token,
      method: "POST",
      body: { resumeId, jobId: job.payload.id },
    });
    assert.equal(match.response.status, 200);
    assert.ok(match.payload.matchScore > 0);
    assert.ok(match.payload.matchedSkills.includes("Java"));

    const persistedMatch = await request(`/api/matches/${match.payload.id}`, {
      token: account.token,
    });
    assert.equal(persistedMatch.response.status, 200);
    assert.equal(persistedMatch.payload.id, match.payload.id);

    const analysis = await request(`/api/resumes/${resumeId}/analyze`, {
      token: account.token,
      method: "POST",
    });
    assert.equal(analysis.response.status, 503);
    assert.match(analysis.payload.message, /GEMINI_API_KEY/);
  } finally {
    if (resumeId) {
      await request(`/api/resumes/${resumeId}`, {
        token: account.token,
        method: "DELETE",
      });
    }
  }
});
