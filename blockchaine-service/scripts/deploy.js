const hre = require("hardhat");

async function main() {
  console.log("🚀 Deploying AgroPharm Shield - PlantBatch (Agriculture + AI Traceability)...");

  // Deploy only PlantBatch
  const PlantBatch = await hre.ethers.getContractFactory("PlantBatch");
  const plantBatch = await PlantBatch.deploy();
  await plantBatch.waitForDeployment();
  const plantAddress = await plantBatch.getAddress();

  console.log("✅ PlantBatch Contract deployed successfully!");
  console.log("📍 Contract Address:", plantAddress);
  console.log("\n🌿 Now you can register medicinal plants with:");
  console.log("   • Plant name, farmer, harvest date");
  console.log("   • Soil quality, pesticide levels");
  console.log("   • Environmental data");
  console.log("   • AI Quality Score (0-100)");
  console.log("\nThis contract is the core of AgroPharm Shield's agricultural traceability.");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});