import assert from "node:assert/strict";
import { after, test } from "node:test";
import { cleanupAccount, registerAccount, request } from "./helpers.js";

const accounts = [];
after(() => accounts.forEach((account) => cleanupAccount(account.email)));

test("register, login, update profile and track an application", async () => {
  const account = await registerAccount();
  accounts.push(account);

  const unauthenticated = await request("/api/users/profile");
  assert.equal(unauthenticated.response.status, 403);

  const login = await request("/api/auth/login", {
    method: "POST",
    body: { email: account.email, password: account.password },
  });
  assert.equal(login.response.status, 200);
  assert.ok(login.payload.token);

  const invalidLogin = await request("/api/auth/login", {
    method: "POST",
    body: { email: account.email, password: "Wrong-password-123!" },
  });
  assert.equal(invalidLogin.response.status, 401);

  const profileUpdate = await request("/api/users/profile", {
    token: account.token,
    method: "PUT",
    body: {
      name: "Updated Runtime User",
      email: account.email,
      phone: "555-0100",
      location: "Test City",
      education: "Computer Science",
      targetRole: "Java Developer",
      careerPreferences: "Backend roles",
    },
  });
  assert.equal(profileUpdate.response.status, 200);
  const profileRead = await request("/api/users/profile", { token: account.token });
  assert.equal(profileRead.payload.name, "Updated Runtime User");
  assert.equal(profileRead.payload.targetRole, "Java Developer");

  const passwordChange = await request("/api/users/password", {
    token: account.token,
    method: "PUT",
    body: {
      currentPassword: account.password,
      newPassword: "Updated-Runtime-Password-123!",
    },
  });
  assert.equal(passwordChange.response.status, 200);
  const oldPasswordLogin = await request("/api/auth/login", {
    method: "POST",
    body: { email: account.email, password: account.password },
  });
  assert.equal(oldPasswordLogin.response.status, 401);
  const newPasswordLogin = await request("/api/auth/login", {
    method: "POST",
    body: { email: account.email, password: "Updated-Runtime-Password-123!" },
  });
  assert.equal(newPasswordLogin.response.status, 200);

  const application = {
    company: "Runtime Test Co",
    position: "Java Engineer",
    jobDescription: "Build backend services",
    applicationDate: "2026-11-24",
    status: "APPLIED",
    interviewDate: "2026-11-25T09:00:00",
    jobUrl: "https://example.com/jobs/runtime-test",
    notes: "Created by runtime verification",
  };
  const created = await request("/api/applications", {
    token: account.token,
    method: "POST",
    body: application,
  });
  assert.equal(created.response.status, 201);
  const applicationId = created.payload.id;

  const filtered = await request("/api/applications?status=APPLIED&company=Runtime", {
    token: account.token,
  });
  assert.equal(filtered.response.status, 200);
  assert.equal(filtered.payload.length, 1);
  assert.equal(filtered.payload[0].id, applicationId);

  const updated = await request(`/api/applications/${applicationId}`, {
    token: account.token,
    method: "PUT",
    body: { ...application, status: "INTERVIEW", notes: "Updated by runtime verification" },
  });
  assert.equal(updated.response.status, 200);
  assert.equal(updated.payload.status, "INTERVIEW");

  const dashboard = await request("/api/dashboard/summary", { token: account.token });
  assert.equal(dashboard.response.status, 200);
  assert.equal(dashboard.payload.totalApplications, 1);
  assert.equal(dashboard.payload.statusCounts.INTERVIEW, 1);

  const deleted = await request(`/api/applications/${applicationId}`, {
    token: account.token,
    method: "DELETE",
  });
  assert.equal(deleted.response.status, 204);
  const applications = await request("/api/applications", { token: account.token });
  assert.equal(applications.payload.length, 0);
});
