const hre = require("hardhat");

async function main() {
  console.log("🧪 AgroPharm Shield - PlantBatch Test (Agriculture + AI)\n");

  const plantBatch = await hre.ethers.getContractAt("PlantBatch", "0x5FbDB2315678afecb367f032d93F642f64180aa3");

  console.log("🌿 Registering Rosemary from Tunisian farm...");

  const tx = await plantBatch.registerPlantBatch(
    "Rosemary",
    "Farmer Ahmed Ben Ali - Tunis",
    "pH 7.2 - Fertile soil",
    "Low - 0.05 mg/kg",
    "Clean conditions",
    88
  );

  const receipt = await tx.wait();

  console.log("✅ Plant Batch registered successfully!\n");

  // Show the event (this always works)
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

  console.log("📋 Plant data is now stored on blockchain (immutable)");
  console.log("   • Plant Name, Farmer, Soil, Pesticide, AI Score = 88/100");
  console.log("   • Ready for AI Service to analyze health impact\n");

  console.log("🎉 SUCCESS - Blockchain Service is WORKING!");
  console.log("   Agriculture traceability with AI quality score is live.");
}

main().catch((error) => {
  console.error("❌ Error:", error.message);
});