import { randomUUID } from "node:crypto";
import { existsSync } from "node:fs";
import { readFile, writeFile } from "node:fs/promises";

const DATA_DIR = new URL("../../data/", import.meta.url);

export function newId(prefix) {
  return `${prefix}_${randomUUID().replaceAll("-", "").slice(0, 6)}`;
}

export function nowIso() {
  return new Date().toISOString().replace(/\.\d{3}Z$/, "Z");
}

export async function readDb(file) {
  return JSON.parse(await readFile(new URL(file, DATA_DIR), "utf8"));
}

export async function writeDb(file, records) {
  await writeFile(new URL(file, DATA_DIR), `${JSON.stringify(records, null, 2)}\n`);
}

export async function ensureDataFile(file) {
  if (!existsSync(new URL(file, DATA_DIR))) await writeDb(file, []);
}
