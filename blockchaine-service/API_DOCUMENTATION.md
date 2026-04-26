# AgroPharm Shield Blockchain API Documentation

## Overview
This API provides blockchain integration for agricultural traceability, with support for both direct plant batch registration and AI service integration.

## Base URL
```
http://localhost:3001
```

## Authentication
Currently no authentication required (development mode).

## Endpoints

### 1. Health Check
```
GET /api/health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "AgroPharm Shield Blockchain API",
  "timestamp": "2026-04-26T02:49:00Z",
  "version": "1.0.0"
}
```

### 2. Register Plant Batch (Original)
```
POST /api/blockchain/register-plant-batch
```

**Request Body:**
```json
{
  "plantName": "Rosemary",
  "farmer": "Farmer Ahmed - Tunis",
  "soilQuality": "pH 7.2 - Fertile soil",
  "pesticideLevel": "Low - 0.05 mg/kg",
  "environmentalData": {
    "temperature": 22,
    "humidity": 65,
    "waterQuality": "clean"
  },
  "aiQualityScore": 88
}
```

**Response:**
```json
{
  "success": true,
  "batchId": 1,
  "transactionHash": "0x7f9a...",
  "blockNumber": 12345,
  "isApproved": true,
  "gasUsed": "0.05 ETH",
  "timestamp": "2026-04-26T02:49:00Z"
}
```

### 3. Process AI Result (NEW - AI Integration)
```
POST /api/blockchain/process-ai-result
```

**Request Body (AI Service Response):**
```json
{
  "risk_score": 60.24,
  "risk_band": "Medium",
  "confidence": 0.663,
  "sub_scores": {
    "plant": 0.702,
    "soil": 0.711,
    "water": 0.579,
    "geo": 0.315
  },
  "model_backend": "torchvision-resnet18",
  "model_details": "Loaded pretrained ImageNet weights.",
  "disclaimer": "Risk estimation only. Not a medical diagnosis.",
  "comment": "Moderate estimated exposure risk based on submitted signals.",
  "instruction": "Review pesticide handling, reinforce protection, and monitor symptoms for the next 24 to 48 hours."
}
```

**Response:**
```json
{
  "success": true,
  "batchId": 1,
  "transactionHash": "0x7f9a...",
  "blockNumber": 12345,
  "isApproved": false,
  "gasUsed": "0.05 ETH",
  "timestamp": "2026-04-26T02:49:00Z",
  "aiProcessing": {
    "originalRiskScore": 60.24,
    "convertedQualityScore": 39,
    "riskBand": "Medium",
    "confidence": 0.663
  },
  "dataSources": {
    "aiService": true,
    "backendService": false,
    "environmentalData": {
      "plant": 0.702,
      "soil": 0.711,
      "water": 0.579,
      "geo": 0.315
    }
  }
}
```

### 4. Get Plant Batch
```
GET /api/blockchain/batch/:batchId
```

**Response:**
```json
{
  "success": true,
  "batchId": 1,
  "plantData": {
    "plantName": "Unknown Plant",
    "farmer": "Unknown Farmer",
    "harvestDate": "1714100940",
    "soilQuality": "Not analyzed",
    "pesticideLevel": "Not measured",
    "environmentalData": "{\"ai_sub_scores\":{\"plant\":0.702,\"soil\":0.711,\"water\":0.579,\"geo\":0.315},\"processed_at\":\"2026-04-26T02:49:00Z\",\"data_source\":\"ai_service\"}",
    "aiQualityScore": "39",
    "isApproved": false
  }
}
```

## Data Conversion Logic

### Risk Score to Quality Score Conversion
```
Quality Score = Math.round((1 - Risk Score / 100) * 100)
```

Examples:
- Risk Score 60.24 → Quality Score 39
- Risk Score 20.00 → Quality Score 80
- Risk Score 80.00 → Quality Score 20

### Approval Threshold
- Quality Score ≥ 70 = Approved ✅
- Quality Score < 70 = Not Approved ❌

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "error": "Missing required fields"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "error": "Blockchain registration error: insufficient funds for gas"
}
```

## Testing

### Test AI Integration
```bash
# Start blockchain
npx hardhat node

# Deploy contract
npx hardhat run scripts/deploy.js --network localhost

# Start API server
npm run start-api

# Test AI integration
npx hardhat run scripts/test-ai-integration.js --network localhost
```

### Test API Endpoints
```bash
# Test AI integration endpoint
curl -X POST http://localhost:3001/api/blockchain/process-ai-result \
  -H "Content-Type: application/json" \
  -d '{
    "risk_score": 60.24,
    "risk_band": "Medium",
    "confidence": 0.663,
    "sub_scores": {
      "plant": 0.702,
      "soil": 0.711,
      "water": 0.579,
      "geo": 0.315
    }
  }'

# Get batch data
curl http://localhost:3001/api/blockchain/batch/1
```

## Future Enhancements

1. **Backend Service Integration**: Fetch plantName, farmer, soilQuality, pesticideLevel from backend service
2. **Authentication**: Add API key authentication
3. **Rate Limiting**: Implement rate limiting for production
4. **HTTPS**: Enable HTTPS for production deployment
5. **Database Caching**: Add caching for frequently accessed data

## Notes

- Currently uses placeholder values for missing data (plantName, farmer, etc.)
- Backend service integration will be implemented when backend is ready
- All blockchain transactions are recorded on the local Hardhat network
- Gas fees are covered by the test account in development mode
