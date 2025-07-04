#!/bin/bash

echo "Starting AI Medical Score System..."
echo

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "Error: Java not found! Please install JDK 17 or higher."
    exit 1
fi

echo "Java version:"
java -version

# 检查JAR文件是否存在
if [ ! -f "target/ai-medical-score-1.0.0-SNAPSHOT.jar" ]; then
    echo "JAR file not found. Building project..."
    if command -v mvnd &> /dev/null; then
        mvnd clean package -DskipTests
    else
        mvn clean package -DskipTests
    fi
    
    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
fi

# 启动应用
echo "Starting application on port 8080..."
java -jar target/ai-medical-score-1.0.0-SNAPSHOT.jar 