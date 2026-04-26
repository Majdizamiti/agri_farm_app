# AgroPharm Shield - Blockchain Service

## Overview
Blockchain layer for **AgroPharm Shield** — secure traceability of medicinal plants (Agriculture domain).

It records farming data + AI quality assessment on an immutable ledger, ensuring raw materials are safe for pharmaceutical use.

## 🚀 NEW: AI Service Integration
Now supports direct integration with AI service that provides risk-based analysis:
- **Risk Score → Quality Score Conversion**: Automatically converts AI risk scores (0-100) to quality scores (0-100)
- **Sub-score Storage**: AI sub_scores (plant, soil, water, geo) stored as environmental data
- **Backend Service Ready**: Prepared for backend service integration when available

## Core Contract: PlantBatch.sol
- Registers medicinal plants (Rosemary, Artemisia, etc.)
- Stores: soil quality, pesticide levels, environmental data, **AI Quality Score**
- Automatic approval based on AI score (≥ 70 = approved for pharma)

## API Endpoints

### 📊 AI Integration (NEW)
```
POST /api/blockchain/process-ai-result
```
Accepts AI service response and automatically:
- Converts risk scores to quality scores
- Stores AI sub_scores as environmental data
- Registers batch on blockchain

### 📋 Direct Registration (Original)
```
POST /api/blockchain/register-plant-batch
```
Direct plant batch registration with all fields.

## How to Run (Demo for Jury)

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start local blockchain:
   ```bash
   npx hardhat node
   ```

3. Deploy contract:
   ```bash
   npx hardhat run scripts/deploy.js --network localhost
   ```

4. Start API server:
   ```bash
   npm run start-api
   ```

5. Test AI integration:
   ```bash
   npx hardhat run scripts/test-ai-integration.js --network localhost
   ```

## 🧪 Testing AI Integration

### Test AI Service Response
```bash
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
```

### Check Results
```bash
curl http://localhost:3001/api/blockchain/batch/1
```

## 📊 Data Flow

```
🌱 AI Service → 🔄 Risk→Quality Conversion → ⛓️ Blockchain → ✅ Immutable Record
```

**Conversion Formula**: `Quality Score = (1 - Risk Score/100) * 100`

**Example**: Risk Score 60.24 → Quality Score 39 (Not Approved)

## 🔧 Configuration

Copy `.env.example` to `.env` and configure:
- `BLOCKCHAIN_RPC_URL`: Blockchain node URL
- `CONTRACT_ADDRESS`: Deployed contract address
- `PRIVATE_KEY`: Account private key for transactions
- `PORT`: API server port (default: 3001)

## 📚 Documentation

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for complete API reference.