import assert from "node:assert/strict";
import { after, test } from "node:test";
import { cleanupAccount, registerAccount, request, skipWhenGeminiConfigured } from "./helpers.js";

const accounts = [];
after(() => accounts.forEach((account) => cleanupAccount(account.email)));

test("AI feature routes return explicit configuration errors and chat CRUD persists", async (context) => {
  await skipWhenGeminiConfigured(context);
  const account = await registerAccount();
  accounts.push(account);

  const interviews = await request("/api/interviews", { token: account.token });
  assert.equal(interviews.response.status, 200);
  assert.deepEqual(interviews.payload, []);

  const generateInterview = await request("/api/interviews/generate", {
    token: account.token,
    method: "POST",
    body: { category: "Core Java", difficulty: "BEGINNER", questionCount: 3 },
  });
  assert.equal(generateInterview.response.status, 503);
  assert.match(generateInterview.payload.message, /GEMINI_API_KEY/);

  const roadmaps = await request("/api/roadmaps", { token: account.token });
  assert.equal(roadmaps.response.status, 200);
  assert.deepEqual(roadmaps.payload, []);

  const generateRoadmap = await request("/api/roadmaps/generate", {
    token: account.token,
    method: "POST",
    body: { targetRole: "Java Developer", currentSkills: "Java, SQL" },
  });
  assert.equal(generateRoadmap.response.status, 503);
  assert.match(generateRoadmap.payload.message, /GEMINI_API_KEY/);

  const created = await request("/api/chat/conversations", {
    token: account.token,
    method: "POST",
    body: { title: "Runtime verification" },
  });
  assert.equal(created.response.status, 200);
  const conversationId = created.payload.id;

  const listed = await request("/api/chat/conversations", { token: account.token });
  assert.equal(listed.response.status, 200);
  assert.ok(listed.payload.some((conversation) => conversation.id === conversationId));

  const messages = await request(`/api/chat/${conversationId}/messages`, {
    token: account.token,
  });
  assert.deepEqual(messages.payload, []);

  const ask = await request(`/api/chat/${conversationId}/messages`, {
    token: account.token,
    method: "POST",
    body: { message: "What skills does my resume show?" },
  });
  assert.equal(ask.response.status, 503);
  assert.match(ask.payload.message, /GEMINI_API_KEY/);

  const deleted = await request(`/api/chat/conversations/${conversationId}`, {
    token: account.token,
    method: "DELETE",
  });
  assert.equal(deleted.response.status, 200);
  const afterDelete = await request("/api/chat/conversations", { token: account.token });
  assert.equal(afterDelete.payload.length, 0);
});
