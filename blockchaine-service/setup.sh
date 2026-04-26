#!/bin/bash

echo "🚀 Setting up AgroPharm Shield Blockchain Service with AI Integration"
echo "=================================================================="

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm first."
    exit 1
fi

echo "✅ Node.js and npm are installed"

# Install dependencies
echo "📦 Installing dependencies..."
npm install

if [ $? -ne 0 ]; then
    echo "❌ Failed to install dependencies"
    exit 1
fi

echo "✅ Dependencies installed successfully"

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
    echo "✅ .env file created. Please review and update the configuration."
else
    echo "✅ .env file already exists"
fi

echo ""
echo "🎉 Setup completed successfully!"
echo ""
echo "📋 Next Steps:"
echo "1. Start blockchain:     npx hardhat node"
echo "2. Deploy contract:      npx hardhat run scripts/deploy.js --network localhost"
echo "3. Start API server:     npm run start-api"
echo "4. Test AI integration:  npx hardhat run scripts/test-ai-integration.js --network localhost"
echo ""
echo "📚 Documentation:        See API_DOCUMENTATION.md for API reference"
echo "🔧 Configuration:        Review .env file for settings"
