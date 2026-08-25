// test_summary.js
import { buildClearanceSummary, computeSummaryHash } from "./services/ai_advisor_service.js";

async function run() {
  for (const id of [4, 5]) {
    console.log(`\n--- Clearance #${id} ---`);
    const summary = await buildClearanceSummary(id);
    console.log(JSON.stringify(summary, null, 2));
    console.log("hash:", computeSummaryHash(summary));
  }
  process.exit(0);
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});