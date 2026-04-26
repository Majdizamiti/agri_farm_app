// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract PlantBatch {
    struct PlantData {
        string plantName;           // e.g. "Rosemary", "Artemisia"
        string farmer;              // Farmer name or wallet
        uint256 harvestDate;
        string soilQuality;         // e.g. "pH 7.2 - Good"
        string pesticideLevel;      // e.g. "Low - 0.1 mg/kg"
        string environmentalData;   // JSON string with temp, humidity, etc.
        uint256 aiQualityScore;     // 0-100 from AI later
        bool isApproved;
    }

    mapping(uint256 => PlantData) public plantBatches;
    uint256 public batchCounter;

    event PlantBatchRegistered(uint256 batchId, string plantName, uint256 harvestDate);

    function registerPlantBatch(
        string memory _plantName,
        string memory _farmer,
        string memory _soilQuality,
        string memory _pesticideLevel,
        string memory _environmentalData,
        uint256 _aiQualityScore
    ) public {
        batchCounter++;
        
        bool approved = _aiQualityScore >= 70;

        plantBatches[batchCounter] = PlantData({
            plantName: _plantName,
            farmer: _farmer,
            harvestDate: block.timestamp,
            soilQuality: _soilQuality,
            pesticideLevel: _pesticideLevel,
            environmentalData: _environmentalData,
            aiQualityScore: _aiQualityScore,
            isApproved: approved
        });

        emit PlantBatchRegistered(batchCounter, _plantName, block.timestamp);
    }

    function getPlantBatch(uint256 _batchId) public view returns (PlantData memory) {
        return plantBatches[_batchId];
    }
}