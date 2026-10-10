import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import { randomUUID } from "node:crypto";
import { readdir, readFile, writeFile } from "node:fs/promises";
import { after, before } from "node:test";
import { fileURLToPath } from "node:url";

const SERVER_DIR = fileURLToPath(new URL("../..", import.meta.url));
const DATA_DIR = new URL("../../data/", import.meta.url);
const BASE = "http://127.0.0.1:3000";
export const SOURCE_CONFIG = "server_port=8080\ntimeout=30";
export const TARGET_CONFIG = "server_port=9090\ntimeout=60\nssl=true";

let serverProcess;
const seedFiles = new Map();

before(async () => {
  for (const file of await readdir(DATA_DIR)) {
    seedFiles.set(file, await readFile(new URL(file, DATA_DIR), "utf8"));
  }
  serverProcess = spawn(process.execPath, ["server.js"], {
    cwd: SERVER_DIR,
    stdio: ["ignore", "inherit", "inherit"],
  });
  await waitForHealth();
});

after(async () => {
  serverProcess.kill();
  await new Promise((resolve) => serverProcess.once("exit", resolve));
  await Promise.all(
    [...seedFiles].map(([file, content]) => writeFile(new URL(file, DATA_DIR), content)),
  );
});

async function waitForHealth() {
  for (let attempt = 0; attempt < 50; attempt += 1) {
    try {
      const response = await fetch(`${BASE}/health`);
      if (response.ok) return;
    } catch {
    }
    await new Promise((resolve) => setTimeout(resolve, 200));
  }
  throw new Error("server did not become healthy on port 3000");
}

export async function request(method, path, body) {
  const response = await fetch(`${BASE}${path}`, {
    method,
    headers: body === undefined ? {} : { "content-type": "application/json" },
    body: body === undefined || typeof body === "string" ? body : JSON.stringify(body),
  });
  const text = await response.text();
  return { status: response.status, body: text === "" ? null : JSON.parse(text) };
}

export function unique(prefix) {
  return `${prefix}_${randomUUID().slice(0, 8)}`;
}
