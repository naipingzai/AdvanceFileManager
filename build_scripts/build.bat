@echo off
REM MaterialFile Build Script (CMD wrapper for build.ps1)
REM Usage: build.bat [command]

set COMMAND=%1
if "%COMMAND%"=="" set COMMAND=build

powershell -ExecutionPolicy Bypass -File "%~dp0build.ps1" %COMMAND%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build returned error code: %ERRORLEVEL%
)

pause
