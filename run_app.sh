#!/bin/bash
# Simple script to run your application cleanly

# Clean up any old compilation artifacts
find . -name "*.class" -not -path "./compiled-classes/*" -delete

# Compile everything to the organized directory
if [ ! -d "compiled-classes" ] || [ ! -f "compiled-classes/Main.class" ]; then
    echo "🔨 Compiling application..."
    find Src -name "*.java" -print0 | xargs -0 javac -d compiled-classes
fi

# Run the application
echo "🚀 Starting Application..."
java -cp compiled-classes Main
