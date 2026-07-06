@echo off
title ARP Spoofing Detector (обычный запуск)
echo ========================================
echo    ARP Spoofing Detector
echo    ВНИМАНИЕ! Для полной функциональности
echo    запустите программу от имени администратора
echo ========================================
echo.

set PATH=%~dp0jre\bin;%PATH%
set JAVA_HOME=%~dp0jre

"%~dp0jre\bin\java.exe" -jar arpdetector.jar

pause