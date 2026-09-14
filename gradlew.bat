@echo off
setlocal
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle is not installed on this Windows shell. Install Gradle 9.6.1 or use GitHub Actions.
exit /b 1
