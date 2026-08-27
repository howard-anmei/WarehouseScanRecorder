@echo off
setlocal

title WarehouseScanRecorder - Update APK

echo ==========================================
echo   WarehouseScanRecorder APK Update
echo ==========================================
echo.

REM ==========================================
REM 1. Project Directory
REM ==========================================

REM Automatically use the directory where this BAT file is located.
set "PROJECT_DIR=%~dp0"

cd /d "%PROJECT_DIR%"

if errorlevel 1 (
    echo [ERROR] Failed to access the project directory!
    echo.
    echo Project directory:
    echo %PROJECT_DIR%
    pause
    exit /b 1
)

echo [OK] Project directory:
echo %PROJECT_DIR%
echo.

REM ==========================================
REM 2. Build APK
REM ==========================================

echo [1/4] Building APK...
echo.

call gradlew.bat assembleDebug

if errorlevel 1 (
    echo.
    echo ==========================================
    echo [ERROR] APK build failed!
    echo ==========================================
    echo.
    pause
    exit /b 1
)

echo.
echo [OK] APK build completed successfully!
echo.

REM ==========================================
REM 3. APK Path
REM ==========================================

set "APK=app\build\outputs\apk\debug\app-debug.apk"

if not exist "%APK%" (
    echo [ERROR] APK not found:
    echo %PROJECT_DIR%%APK%
    echo.
    pause
    exit /b 1
)

echo APK:
echo %PROJECT_DIR%%APK%
echo.

REM ==========================================
REM 4. Check ADB
REM ==========================================

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if not exist "%ADB%" (
    echo [ERROR] adb.exe not found!
    echo.
    echo Expected location:
    echo %ADB%
    echo.
    echo Please install Android SDK Platform-Tools
    echo or check your Android Studio SDK installation.
    echo.
    pause
    exit /b 1
)

echo [2/4] Checking PDA connection...
echo.

"%ADB%" devices

echo.

REM ==========================================
REM 5. Check PDA Connection
REM ==========================================

"%ADB%" get-state >nul 2>&1

if errorlevel 1 (
    echo [ERROR] No available PDA detected!
    echo.
    echo Please check:
    echo   1. The PDA is connected via USB.
    echo   2. USB debugging is enabled.
    echo   3. The PDA has authorized this computer.
    echo   4. The USB connection is working correctly.
    echo.
    pause
    exit /b 1
)

echo [OK] PDA connected!
echo.

REM ==========================================
REM 6. Install APK
REM ==========================================

echo [3/4] Installing APK...
echo.

"%ADB%" install -r "%APK%"

if errorlevel 1 (
    echo.
    echo ==========================================
    echo [ERROR] APK installation failed!
    echo ==========================================
    echo.
    echo Possible causes:
    echo   - PDA is not authorized for USB debugging.
    echo   - Existing application has a different signature.
    echo   - USB connection was interrupted.
    echo.
    pause
    exit /b 1
)

echo.
echo [OK] APK installed successfully!
echo.

REM ==========================================
REM 7. Complete
REM ==========================================

echo ==========================================
echo   Update Completed!
echo ==========================================
echo.
echo WarehouseScanRecorder has been installed
echo or updated on the PDA.
echo.
echo The application can collect scanner data
echo in the background without keeping the UI open.
echo.

pause

endlocal