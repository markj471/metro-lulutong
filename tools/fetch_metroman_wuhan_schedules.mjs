import fs from "node:fs/promises";
import path from "node:path";

const baseUrl = "https://www.metroman.cn";
const stationIndexUrl = `${baseUrl}/cities/wuhan/stations`;
const outputPath = path.resolve("app/src/main/assets/wuhan_station_schedules.json");

function decodeHtml(value) {
  return value
    .replace(/<[^>]+>/g, " ")
    .replace(/&#(\d+);/g, (_, code) => String.fromCodePoint(Number(code)))
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/\s+/g, " ")
    .trim();
}

async function fetchText(url) {
  const response = await fetch(url, {
    headers: { "User-Agent": "MetroRoutePlanner course project data refresh" }
  });
  if (!response.ok) throw new Error(`${response.status} ${url}`);
  return response.text();
}

function extractStationPaths(html) {
  return [...html.matchAll(/href=["'](\/cities\/wuhan\/stations\/[^"'#?]+)["']/g)]
    .map((match) => match[1])
    .filter((value, index, values) => values.indexOf(value) === index)
    .sort();
}

function extractSchedules(html) {
  const stationNameMatch = html.match(/<h1[^>]*class="[^"]*display-5[^"]*"[^>]*>([\s\S]*?)<small/i);
  const stationName = decodeHtml(stationNameMatch?.[1] ?? "");
  if (!stationName) throw new Error("Station name not found");

  const lineBadges = [...html.matchAll(/<span class="mm-sm-line-badge"[^>]*>([\s\S]*?)<\/span>/gi)]
    .map((match) => ({ index: match.index ?? 0, lineName: decodeHtml(match[1]) }));
  const directionCards = [...html.matchAll(/<div class="mm-sm-direction-card"[^>]*>([\s\S]*?)<\/table>[\s\S]*?<\/div>\s*<\/div>/gi)];
  const schedules = [];

  for (const card of directionCards) {
    const cardIndex = card.index ?? 0;
    const lineName = lineBadges.filter((badge) => badge.index < cardIndex).at(-1)?.lineName ?? "";
    const direction = decodeHtml(card[1].match(/<div class="mm-sm-card-header">([\s\S]*?)<\/div>/i)?.[1] ?? "");
    const rows = [...card[1].matchAll(/<tr>\s*<td[^>]*>([\s\S]*?)<\/td>\s*<td[^>]*>([\s\S]*?)<\/td>\s*<td[^>]*>([\s\S]*?)<\/td>\s*<\/tr>/gi)];
    for (const row of rows) {
      const serviceDay = decodeHtml(row[1]);
      const firstTrain = decodeHtml(row[2]);
      const lastTrain = decodeHtml(row[3]);
      if (lineName && direction && serviceDay && firstTrain && lastTrain) {
        schedules.push({ lineName, direction, serviceDay, firstTrain, lastTrain });
      }
    }
  }

  return { stationName, schedules };
}

async function mapWithConcurrency(items, concurrency, mapper) {
  const results = new Array(items.length);
  let nextIndex = 0;

  async function worker() {
    while (true) {
      const index = nextIndex++;
      if (index >= items.length) return;
      results[index] = await mapper(items[index], index);
    }
  }

  await Promise.all(Array.from({ length: concurrency }, worker));
  return results;
}

const indexHtml = await fetchText(stationIndexUrl);
const stationPaths = extractStationPaths(indexHtml);
const records = await mapWithConcurrency(stationPaths, 8, async (stationPath, index) => {
  const html = await fetchText(`${baseUrl}${stationPath}`);
  const record = extractSchedules(html);
  if ((index + 1) % 25 === 0 || index + 1 === stationPaths.length) {
    process.stdout.write(`Fetched ${index + 1}/${stationPaths.length}\n`);
  }
  return record;
});

await fs.writeFile(outputPath, `${JSON.stringify(records, null, 2)}\n`, "utf8");
const scheduleCount = records.reduce((sum, record) => sum + record.schedules.length, 0);
process.stdout.write(`Wrote ${records.length} stations and ${scheduleCount} schedule rows to ${outputPath}\n`);
