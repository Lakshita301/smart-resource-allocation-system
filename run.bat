@echo off
setlocal

set "PROJECT_ROOT=%~dp0"
set "OUT_DIR=%PROJECT_ROOT%out"

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

dir /s /b "%PROJECT_ROOT%src\main\java\*.java" > "%OUT_DIR%\sources.txt"
javac -d "%OUT_DIR%" @"%OUT_DIR%\sources.txt"
if errorlevel 1 exit /b 1

java -cp "%OUT_DIR%" com.example.smartalloc.SmartAllocApplication
