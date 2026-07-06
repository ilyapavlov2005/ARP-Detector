@echo off
title ARP Spoofing Detector
echo ========================================
echo    ARP Spoofing Detector
echo    Обнаружение ARP-атак в локальной сети
echo ========================================
echo.

:: Переходим в папку, где находится run.bat
cd /d "%~dp0"

set "JAVA_PATH=%CD%\jre\bin\java.exe"

if not exist "%JAVA_PATH%" (
    echo [ОШИБКА] Не найден java.exe в папке: %JAVA_PATH%
    pause
    exit /b 1
)

if not exist "%CD%\arpdetector.jar" (
    echo [ОШИБКА] Не найден arpdetector.jar в папке: %CD%
    pause
    exit /b 1
)

echo Запуск программы...
echo Папка программы: %CD%
echo Используется JRE: %JAVA_PATH%
echo.

:: Запуск программы с полным путём к JAR
"%JAVA_PATH%" -jar "%CD%\arpdetector.jar"

if errorlevel 1 (
    echo.
    echo Ошибка при запуске программы!
    echo.
    pause
)