@echo off
title OpenTowns Launcher
echo Starting OpenTowns...
cd /d "%~dp0"
call gradlew.bat run
pause
