@echo off
chcp 65001 >nul
title Pediatric Acupuncture - Starting...

cd /d "%~dp0"

if not exist "%~dp0start.ps1" (
    echo [ERROR] start.ps1 not found
    echo Path: %~dp0start.ps1
    pause
    exit /b 1
)

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1" %*

pause
