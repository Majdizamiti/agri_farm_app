require('dotenv').config();
const express = require('express');
const { ethers } = require('ethers');
const cors = require('cors');
const axios = require('axios');

const app = express();
app.use(cors());
app.use(express.json());

// Blockchain Configuration
const provider = new ethers.JsonRpcProvider(process.env.BLOCKCHAIN_RPC_URL || 'http://localhost:8545');
const contractAddress = process.env.CONTRACT_ADDRESS || '0x5FbDB2315678afecb367f032d93F642f64180aa3';
const privateKey = process.env.PRIVATE_KEY || '0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80';
const wallet = new ethers.Wallet(privateKey, provider);

// Contract ABI
const contractABI = [
  "function registerPlantBatch(string,string,string,string,string,uint256) public",
  "event PlantBatchRegistered(uint256,string,uint256)"
];

const contract = new ethers.Contract(contractAddress, contractABI, wallet);

// Data Conversion Utilities
class DataConverter {
  static convertRiskToQuality(riskScore) {
    // Convert risk score (0-100, higher = more risk) to quality score (0-100, higher = better)
    return Math.round((1 - riskScore / 100) * 100);
  }

  static formatEnvironmentalData(subScores) {
    return JSON.stringify({
      ai_sub_scores: subScores,
      processed_at: new Date().toISOString(),
      data_source: 'ai_service'
    });
  }

  static validateAIResponse(aiResponse) {
    const required = ['risk_score', 'sub_scores'];
    for (const field of required) {
      if (!aiResponse[field]) {
        throw new Error(`Missing required field in AI response: ${field}`);
      }
    }

    if (typeof aiResponse.risk_score !== 'number' || aiResponse.risk_score < 0 || aiResponse.risk_score > 100) {
      throw new Error('Invalid risk_score: must be number between 0-100');
    }

    if (typeof aiResponse.sub_scores !== 'object') {
      throw new Error('sub_scores must be an object');
    }

    return true;
  }
}

// Backend Service Client (Placeholder for future implementation)
class BackendService {
  constructor() {
    this.baseURL = process.env.BACKEND_SERVICE_URL || 'http://localhost:4000';
    this.apiKey = process.env.BACKEND_API_KEY;
  }

  async fetchPlantData(plantId) {
    // TODO: Implement when backend service is ready
    // For now, return default values
    console.log('⚠️ Backend service not implemented yet, using default values');
    return {
      plantName: 'Unknown Plant',
      farmer: 'Unknown Farmer',
      soilQuality: 'Not analyzed',
      pesticideLevel: 'Not measured'
    };
  }

  async fetchFarmerData(farmerId) {
    // TODO: Implement when backend service is ready
    return {
      farmerName: 'Unknown Farmer',
      location: 'Unknown Location'
    };
  }
}

const backendService = new BackendService();

// API Endpoints

// Health check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'AgroPharm Shield Blockchain API',
    timestamp: new Date().toISOString(),
    version: '1.0.0'
  });
});

// Original endpoint for direct registration (maintains existing functionality)
app.post('/api/blockchain/register-plant-batch', async (req, res) => {
  try {
    const {
      plantName,
      farmer,
      soilQuality,
      pesticideLevel,
      environmentalData,
      aiQualityScore
    } = req.body;

    // Validate input
    if (!plantName || !farmer || !soilQuality || !pesticideLevel || !environmentalData || aiQualityScore === undefined) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields"
      });
    }

    // Call smart contract
    const tx = await contract.registerPlantBatch(
      plantName,
      farmer,
      soilQuality,
      pesticideLevel,
      JSON.stringify(environmentalData),
      aiQualityScore
    );

    const receipt = await tx.wait();

    // Extract batch ID from event
    const event = receipt.logs.find(log => log.fragment && log.fragment.name === "PlantBatchRegistered");
    const batchId = event ? event.args[0].toString() : null;

    // Determine approval
    const isApproved = aiQualityScore >= 70;

    res.json({
      success: true,
      batchId: batchId,
      transactionHash: tx.hash,
      blockNumber: receipt.blockNumber,
      isApproved: isApproved,
      gasUsed: ethers.formatEther(receipt.gasUsed * receipt.gasPrice),
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('Blockchain registration error:', error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// NEW: AI Service Integration Endpoint
app.post('/api/blockchain/process-ai-result', async (req, res) => {
  try {
    const aiResponse = req.body;
    
    // Validate AI response
    DataConverter.validateAIResponse(aiResponse);
    
    // Convert AI risk score to quality score
    const qualityScore = DataConverter.convertRiskToQuality(aiResponse.risk_score);
    
    // Format environmental data with AI sub_scores
    const environmentalData = DataConverter.formatEnvironmentalData(aiResponse.sub_scores);
    
    // TODO: Fetch missing data from backend service
    // For now, use placeholder values since backend is not ready
    const plantData = await backendService.fetchPlantData('placeholder');
    
    // Prepare blockchain transaction data
    const txData = {
      plantName: plantData.plantName,
      farmer: plantData.farmer,
      soilQuality: plantData.soilQuality,
      pesticideLevel: plantData.pesticideLevel,
      environmentalData: environmentalData,
      aiQualityScore: qualityScore
    };
    
    // Call smart contract
    const tx = await contract.registerPlantBatch(
      txData.plantName,
      txData.farmer,
      txData.soilQuality,
      txData.pesticideLevel,
      txData.environmentalData,
      txData.aiQualityScore
    );
    
    const receipt = await tx.wait();
    
    // Extract batch ID from event
    const event = receipt.logs.find(log => log.fragment && log.fragment.name === "PlantBatchRegistered");
    const batchId = event ? event.args[0].toString() : null;
    
    // Determine approval
    const isApproved = qualityScore >= 70;
    
    res.json({
      success: true,
      batchId: batchId,
      transactionHash: tx.hash,
      blockNumber: receipt.blockNumber,
      isApproved: isApproved,
      gasUsed: ethers.formatEther(receipt.gasUsed * receipt.gasPrice),
      timestamp: new Date().toISOString(),
      aiProcessing: {
        originalRiskScore: aiResponse.risk_score,
        convertedQualityScore: qualityScore,
        riskBand: aiResponse.risk_band,
        confidence: aiResponse.confidence
      },
      dataSources: {
        aiService: true,
        backendService: false, // TODO: Change to true when backend is implemented
        environmentalData: aiResponse.sub_scores
      }
    });
    
  } catch (error) {
    console.error('AI result processing error:', error);
    res.status(500).json({
      success: false,
      error: error.message,
      details: 'Failed to process AI result and register on blockchain'
    });
  }
});

// Get plant batch data
app.get('/api/blockchain/batch/:batchId', async (req, res) => {
  try {
    const batchId = req.params.batchId;
    
    // Read contract data
    const plantData = await contract.getPlantBatch(batchId);
    
    res.json({
      success: true,
      batchId: batchId,
      plantData: {
        plantName: plantData.plantName,
        farmer: plantData.farmer,
        harvestDate: plantData.harvestDate.toString(),
        soilQuality: plantData.soilQuality,
        pesticideLevel: plantData.pesticideLevel,
        environmentalData: plantData.environmentalData,
        aiQualityScore: plantData.aiQualityScore.toString(),
        isApproved: plantData.isApproved
      }
    });
    
  } catch (error) {
    console.error('Get batch error:', error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    success: false,
    error: 'Internal server error'
  });
});

// Start server
const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`🚀 AgroPharm Shield Blockchain API running on port ${PORT}`);
  console.log(`📋 Available endpoints:`);
  console.log(`   GET  /api/health`);
  console.log(`   POST /api/blockchain/register-plant-batch (Original)`);
  console.log(`   POST /api/blockchain/process-ai-result (NEW - AI Integration)`);
  console.log(`   GET  /api/blockchain/batch/:batchId`);
  console.log(`\n🔗 Blockchain RPC: ${process.env.BLOCKCHAIN_RPC_URL || 'http://localhost:8545'}`);
  console.log(`📍 Contract Address: ${contractAddress}`);
});
