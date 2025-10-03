#!/bin/bash

echo "🧹 Cleaning old compilation files..."
find . -name "*.class" -delete

echo "📁 Creating clean compilation directory..."
rm -rf build
mkdir -p build

echo "🔨 Compiling all Java files..."
javac -d build -sourcepath Src Src/Main.java Src/**/*.java

echo ""
echo "🚀 Running application..."
java -cp build Main
