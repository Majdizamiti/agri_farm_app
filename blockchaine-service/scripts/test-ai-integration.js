const hre = require("hardhat");

async function main() {
  console.log("🧪 Testing AI Service Integration with Blockchain\n");

  // Get contract instance
  const plantBatch = await hre.ethers.getContractAt("PlantBatch", "0x5FbDB2315678afecb367f032d93F642f64180aa3");

  // Simulate AI Service Response (Risk-Based)
  const aiServiceResponse = {
    risk_score: 60.24,
    risk_band: "Medium",
    confidence: 0.663,
    sub_scores: {
      plant: 0.702,
      soil: 0.711,
      water: 0.579,
      geo: 0.315
    },
    model_backend: "torchvision-resnet18",
    model_details: "Loaded pretrained ImageNet weights.",
    disclaimer: "Risk estimation only. Not a medical diagnosis.",
    comment: "Moderate estimated exposure risk based on submitted signals.",
    instruction: "Review pesticide handling, reinforce protection, and monitor symptoms for the next 24 to 48 hours."
  };

  console.log("📊 AI Service Response:");
  console.log(`   Risk Score: ${aiServiceResponse.risk_score}`);
  console.log(`   Risk Band: ${aiServiceResponse.risk_band}`);
  console.log(`   Sub-scores:`, aiServiceResponse.sub_scores);
  console.log(`   Confidence: ${aiServiceResponse.confidence}\n`);

  // Convert AI risk score to blockchain quality score
  const qualityScore = Math.round((1 - aiServiceResponse.risk_score / 100) * 100);
  console.log("🔄 Data Conversion:");
  console.log(`   AI Risk Score → Blockchain Quality Score: ${aiServiceResponse.risk_score} → ${qualityScore}\n`);

  // Format environmental data with AI sub_scores
  const environmentalData = JSON.stringify({
    ai_sub_scores: aiServiceResponse.sub_scores,
    processed_at: new Date().toISOString(),
    data_source: 'ai_service'
  });

  // Register plant batch using converted data
  console.log("⛓️ Registering on Blockchain...");
  const tx = await plantBatch.registerPlantBatch(
    "Unknown Plant", // TODO: Fetch from backend service
    "Unknown Farmer", // TODO: Fetch from backend service
    "Not analyzed", // TODO: Fetch from backend service
    "Not measured", // TODO: Fetch from backend service
    environmentalData,
    qualityScore
  );

  const receipt = await tx.wait();
  console.log("✅ Plant Batch registered successfully!\n");

  // Show the event
  const event = receipt.logs.find(log => {
    try {
      return log.fragment && log.fragment.name === "PlantBatchRegistered";
    } catch (e) {
      return false;
    }
  });

  if (event) {
    console.log("📜 Event emitted → Batch ID:", event.args[0].toString());
  }

  // Read the registered data
  const batchId = event ? event.args[0].toString() : 1;
  const plantData = await plantBatch.getPlantBatch(batchId);

  console.log("\n📋 Registered Plant Data:");
  console.log("   Plant Name      :", plantData.plantName);
  console.log("   Farmer          :", plantData.farmer);
  console.log("   Soil Quality    :", plantData.soilQuality);
  console.log("   Pesticide Level :", plantData.pesticideLevel);
  console.log("   AI Quality Score:", plantData.aiQualityScore.toString(), "/ 100");
  console.log("   Approved        :", plantData.isApproved ? "✅ YES" : "❌ NO");
  console.log("   Harvest Date    :", new Date(Number(plantData.harvestDate) * 1000).toLocaleString());

  // Parse and display environmental data
  try {
    const envData = JSON.parse(plantData.environmentalData);
    console.log("\n🌍 Environmental Data (from AI Service):");
    console.log("   Plant Score :", envData.ai_sub_scores.plant);
    console.log("   Soil Score  :", envData.ai_sub_scores.soil);
    console.log("   Water Score :", envData.ai_sub_scores.water);
    console.log("   Geo Score   :", envData.ai_sub_scores.geo);
    console.log("   Processed At:", envData.processed_at);
  } catch (e) {
    console.log("\n🌍 Environmental Data:", plantData.environmentalData);
  }

  console.log("\n🎉 AI Integration Test Completed Successfully!");
  console.log("   • AI risk scores converted to quality scores");
  console.log("   • AI sub_scores stored as environmental data");
  console.log("   • Blockchain registration working");
  console.log("   • Ready for backend service integration");
}

main().catch((error) => {
  console.error("❌ Error:", error.message);
  process.exitCode = 1;
});
