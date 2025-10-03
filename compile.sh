#!/bin/bash
# Simple compilation script for the Automated Purchase Order Management System

echo "🧹 Cleaning previous compilation..."
rm -f *.class
find . -name "*.class" -delete

echo "📁 Creating directory structure..."
mkdir -p compiled

echo "🔨 Compiling all Java files to current directory..."
cd Src
find . -name "*.java" -exec javac -d ../ {} \;
cd ..

echo "✅ Compilation complete!"
echo "🚀 To run the application: java Main"
