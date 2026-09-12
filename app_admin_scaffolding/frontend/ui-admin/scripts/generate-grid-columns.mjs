#!/usr/bin/env node

import fs from "node:fs/promises";
import process from "node:process";

const args = process.argv.slice(2);

const getArgValue = (name) => {
  const index = args.indexOf(name);
  if (index === -1 || index === args.length - 1) return undefined;
  return args[index + 1];
};

const hasFlag = (name) => args.includes(name);

const toStartCase = (value) =>
  value
    .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
    .replace(/[_-]+/g, " ")
    .replace(/\s+/g, " ")
    .trim()
    .replace(/\b\w/g, (ch) => ch.toUpperCase());

const inferColumn = (field, value) => {
  if (field === "id") {
    return `{ field: "id", headerName: "ID", minWidth: 220, flex: 1 }`;
  }

  const headerName = toStartCase(field);

  if (Array.isArray(value)) {
    return [
      "{",
      `  field: "${field}",`,
      `  headerName: "${headerName}",`,
      "  minWidth: 220,",
      "  flex: 1,",
      `  valueGetter: (_, row) => (Array.isArray(row?.${field}) ? row.${field}.join(", ") : "-"),`,
      "}",
    ].join("\n");
  }

  if (typeof value === "boolean") {
    return `{ field: "${field}", headerName: "${headerName}", minWidth: 120 }`;
  }

  if (typeof value === "number") {
    return `{ field: "${field}", headerName: "${headerName}", minWidth: 120 }`;
  }

  if (value !== null && typeof value === "object") {
    return [
      "{",
      `  field: "${field}",`,
      `  headerName: "${headerName}",`,
      "  minWidth: 220,",
      "  flex: 1,",
      `  valueGetter: (_, row) => (row?.${field} ? JSON.stringify(row.${field}) : "-"),`,
      "}",
    ].join("\n");
  }

  return `{ field: "${field}", headerName: "${headerName}", minWidth: 180, flex: 1 }`;
};

const readInput = async () => {
  const inputPath = getArgValue("--input");
  if (inputPath) {
    return fs.readFile(inputPath, "utf8");
  }

  if (process.stdin.isTTY) {
    throw new Error("No input provided. Use --input <file> or pipe JSON to stdin.");
  }

  const chunks = [];
  for await (const chunk of process.stdin) {
    chunks.push(chunk);
  }
  return Buffer.concat(chunks).toString("utf8");
};

const printUsage = () => {
  const usage = [
    "Usage:",
    "  node scripts/generate-grid-columns.mjs --input model.json",
    "  cat model.json | node scripts/generate-grid-columns.mjs",
    "",
    "Options:",
    "  --input <path>     Read JSON from file (default: stdin)",
    "  --name <constName> Output const name (default: LIST_COLUMNS)",
    "  --no-actions       Do not append actions column",
  ].join("\n");
  console.log(usage);
};

const main = async () => {
  if (hasFlag("--help") || hasFlag("-h")) {
    printUsage();
    return;
  }

  const source = await readInput();
  const parsed = JSON.parse(source);

  if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) {
    throw new Error("Input JSON must be an object.");
  }

  const constName = getArgValue("--name") ?? "LIST_COLUMNS";
  const includeActions = !hasFlag("--no-actions");

  const fields = Object.entries(parsed);
  const columns = fields.map(([field, value]) => inferColumn(field, value));

  if (includeActions) {
    columns.push(
      [
        "{",
        '  field: "actions",',
        '  headerName: "Actions",',
        '  align: "right",',
        '  headerAlign: "right",',
        "  minWidth: 140,",
        "  sortable: false,",
        "  renderCell: renderActions,",
        "}",
      ].join("\n"),
    );
  }

  const output = [
    'import type { GridColDef } from "@mui/x-data-grid";',
    "",
    'type ActionRenderer = NonNullable<GridColDef["renderCell"]>;',
    "",
    includeActions
      ? `export const ${constName} = (renderActions: ActionRenderer): GridColDef[] => [`
      : `export const ${constName}: GridColDef[] = [`,
    columns.map((col) => col.split("\n").map((line) => `  ${line}`).join("\n")).join(",\n"),
    "];",
  ].join("\n");

  console.log(output);
};

main().catch((error) => {
  console.error(`[generate-grid-columns] ${error.message}`);
  process.exit(1);
});
