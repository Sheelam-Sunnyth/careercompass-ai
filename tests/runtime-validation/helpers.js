import { execFileSync } from "node:child_process";
import { randomUUID } from "node:crypto";
import assert from "node:assert/strict";

export const baseUrl = process.env.CAREER_BASE_URL ?? "http://localhost:8080";

export function newCredentials() {
  return {
    name: "Runtime Verification",
    email: `runtime-${randomUUID()}@example.com`,
    password: `Runtime-${randomUUID()}!`,
  };
}

export function createPdf(text) {
  const stream = `BT /F1 14 Tf 50 700 Td (${text}) Tj ET`;
  const objects = [
    "<< /Type /Catalog /Pages 2 0 R >>",
    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
    "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    `<< /Length ${Buffer.byteLength(stream)} >>\nstream\n${stream}\nendstream`,
  ];
  let pdf = "%PDF-1.4\n";
  const offsets = [0];
  for (let i = 0; i < objects.length; i++) {
    offsets.push(Buffer.byteLength(pdf));
    pdf += `${i + 1} 0 obj\n${objects[i]}\nendobj\n`;
  }
  const xrefOffset = Buffer.byteLength(pdf);
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  for (const offset of offsets.slice(1)) {
    pdf += `${String(offset).padStart(10, "0")} 00000 n \n`;
  }
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF\n`;
  return Buffer.from(pdf, "ascii");
}

export async function request(path, { token, method = "GET", body } = {}) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const response = await fetch(`${baseUrl}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  let payload = null;
  if (response.status !== 204) {
    const text = await response.text();
    payload = text ? JSON.parse(text) : null;
  }
  return { response, payload };
}

export async function registerAccount() {
  const credentials = newCredentials();
  const { response, payload } = await request("/api/auth/register", {
    method: "POST",
    body: credentials,
  });
  assert.equal(response.status, 201, JSON.stringify(payload));
  assert.ok(payload.token);
  return { ...credentials, token: payload.token, user: payload.user };
}

export async function skipWhenGeminiConfigured(context) {
  const { response, payload } = await request("/api/health");
  assert.equal(response.status, 200);
  if (payload.geminiConfigured) {
    context.skip("This live-AI flow is not run automatically to avoid external API usage.");
  }
}

export function cleanupAccount(email) {
  const sql = `DELETE FROM users WHERE email = '${email}';\n`;
  execFileSync(
    "docker",
    [
      "compose",
      "--env-file",
      ".env",
      "exec",
      "-T",
      "mysql",
      "sh",
      "-c",
      'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" career_assistant_db',
    ],
    { cwd: process.cwd(), input: sql, stdio: ["pipe", "ignore", "pipe"] },
  );
}
