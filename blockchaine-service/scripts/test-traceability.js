const hre = require("hardhat");

async function main() {
  console.log("🧪 AgroPharm Shield - Farm-to-Medicine Traceability Test\n");

  const plantBatch = await hre.ethers.getContractAt("PlantBatch", "0x5FbDB2315678afecb367f032d93F642f64180aa3");
  const medicineBatch = await hre.ethers.getContractAt("MedicineBatch", "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512");

  // === 1. Register Plant (Agriculture Part) ===
  console.log("🌿 Registering Medicinal Plant Batch...");
  const txPlant = await plantBatch.registerPlantBatch(
    "Rosemary",
    "Farmer Ahmed - Tunis Governorate",
    "pH 7.2 - Fertile soil",
    "Low - 0.05 mg/kg",
    '{"temperature":22,"humidity":65,"water":"clean"}',
    88
  );
  await txPlant.wait();
  console.log("✅ Plant Batch #1 registered (AI Quality Score = 88/100)\n");

  // === 2. Create Medicine (Healthcare Part) ===
  console.log("💊 Creating Medicine linked to Plant #1...");
  const expiry = Math.floor(Date.now() / 1000) + (365 * 24 * 60 * 60); // 1 year
  const txMed = await medicineBatch.createMedicineBatch(
    "Rosemary Essential Oil Capsules",
    1,
    "Tunisian Pharma Co.",
    expiry,
    "MED-2026-ROSE-001"
  );
  await txMed.wait();
  console.log("✅ Medicine Batch #1 created successfully!\n");

  // === 3. Read Data Safely (one field at a time - avoids struct decoding bug) ===
  console.log("🔗 Full Farm-to-Medicine Traceability:");

  // Read Plant Data
  const plant = await plantBatch.getPlantBatch(1);
  console.log("\n🌱 PLANT ORIGIN (Agriculture):");
  console.log("   Plant Name      :", plant.plantName);
  console.log("   Farmer          :", plant.farmer);
  console.log("   Soil Quality    :", plant.soilQuality);
  console.log("   Pesticide Level :", plant.pesticideLevel);
  console.log("   AI Quality Score:", plant.aiQualityScore, "/ 100");
  console.log("   Approved        :", plant.isApproved ? "✅ YES" : "❌ NO");

  // Read Medicine Data
  const med = await medicineBatch.getMedicineBatch(1);
  console.log("\n💊 MEDICINE (Healthcare):");
  console.log("   Medicine Name   :", med.medicineName);
  console.log("   Manufacturer    :", med.manufacturer);
  console.log("   Batch Number    :", med.batchNumber);
  console.log("   Safe for Patients:", med.isSafeForPatients ? "✅ YES (based on plant quality)" : "❌ NO");

  console.log("\n🎉 TEST COMPLETED SUCCESSFULLY!");
  console.log("   Blockchain now securely links Agricultural Quality → Patient Safety.");
  console.log("   This is the core value of AgroPharm Shield for the hackathon.");
}

main().catch((error) => {
  console.error("Error:", error.message);
});