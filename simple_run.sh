#!/bin/bash
# Simple working solution for your application

echo "🔨 Compiling all Java files..."

# Compile everything together 
find Src -name "*.java" -print0 | xargs -0 javac -d .

echo ""
echo "✅ Compilation complete!"
echo "🚀 Running application..."

# Run the application
java Main
