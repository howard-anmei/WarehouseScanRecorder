@echo off
setlocal

echo ==========================================
echo   WarehousePutaway - Clear Scan Records
echo ==========================================
echo.

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "REMOTE_DIR=/sdcard/Download/WarehousePutaway"

if not exist "%ADB%" (
    echo [ERROR] ADB not found!
    pause
    exit /b 1
)

echo [1/3] Checking PDA connection...
"%ADB%" get-state >nul 2>&1

if errorlevel 1 (
    echo [ERROR] PDA not connected!
    pause
    exit /b 1
)

echo [OK] PDA connected.
echo.

echo [2/3] Checking scan record folder...

"%ADB%" shell "test -d %REMOTE_DIR%"

if errorlevel 1 (
    echo [ERROR] Scan record folder not found!
    pause
    exit /b 1
)

echo [OK] Scan record folder found.
echo.

echo Files currently on PDA:
echo ------------------------------------------
"%ADB%" shell "ls -1 %REMOTE_DIR%/*.csv" 2>nul
echo ------------------------------------------
echo.

echo WARNING:
echo This will DELETE ALL CSV scan record files
echo in:
echo %REMOTE_DIR%
echo.

set /p "CONFIRM=Type YES to continue: "

if /I not "%CONFIRM%"=="YES" (
    echo.
    echo [CANCELLED] No files were deleted.
    pause
    exit /b 0
)

echo.
echo [3/3] Deleting all scan record CSV files...
echo.

"%ADB%" shell "rm -f %REMOTE_DIR%/*.csv"

if errorlevel 1 (
    echo ==========================================
    echo [ERROR] Failed to delete scan records!
    echo ==========================================
    pause
    exit /b 1
)

echo [OK] All scan record CSV files deleted.
echo.

echo Remaining files:
echo ------------------------------------------
"%ADB%" shell "ls -la %REMOTE_DIR%"
echo ------------------------------------------
echo.

echo ==========================================
echo [SUCCESS] Scan records cleared!
echo ==========================================

pause