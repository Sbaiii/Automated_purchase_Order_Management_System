#!/bin/bash
# Clean compilation script that keeps classes organized

echo "🧹 Cleaning up old .class files..."
find . -name "*.class" -delete
rm -rf compiled-classes

echo "📁 Creating organized compilation directory..."
mkdir -p compiled-classes

echo "🔨 Compiling all Java files to compiled-classes folder..."
find Src -name "*.java" -print0 | xargs -0 javac -d compiled-classes

echo ""
echo "✅ Clean compilation complete!"
echo "📂 All .class files are now in: compiled-classes/"
echo "🚀 To run: java -cp compiled-classes Main"
