@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Dorohedoro The Hole - NeoForge 1.21.1 Build Verification

echo ============================================
echo DOROHEDORO THE HOLE - BUILD AND TEST
 echo Minecraft 1.21.1 / NeoForge / Java 21
 echo ============================================
echo.
echo Step 1: Check local build prerequisites.
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java is not available on PATH. Install JDK 21.
  goto failed
)
where powershell >nul 2>&1
if errorlevel 1 (
  echo ERROR: Windows PowerShell is unavailable.
  goto failed
)
if not exist "gradlew.bat" (
  echo ERROR: Project launcher gradlew.bat is missing.
  goto failed
)

echo Step 2: Restore and SHA-256-verify the official Gradle wrapper if needed.
echo Step 3: Run Gradle help, NeoForge runData, and build with preserved logs.
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\verify-neoforge-build.ps1"
if errorlevel 1 goto failed

echo.
echo SUCCESS: Gradle tasks completed. In-game client/server tests are still required.
echo Built JARs, if present, are in build\libs.
echo Logs are in build\verification.
pause
exit /b 0

:failed
echo.
echo BUILD NOT VERIFIED. Read the error above and logs in build\verification.
pause
exit /b 1
