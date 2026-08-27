@echo off
setlocal

title WarehousePutaway - Update APK

echo ==========================================
echo   WarehousePutaway APK Update
echo ==========================================
echo.

REM ==========================================
REM 1. Project Directory
REM ==========================================
cd /d C:\AndroidProjects\WarehousePutaway

if errorlevel 1 (
    echo [ERROR] Failed to access the project directory!
    pause
    exit /b 1
)

echo [1/4] Building APK...
echo.

call gradlew.bat assembleDebug

if errorlevel 1 (
    echo.
    echo ==========================================
    echo [ERROR] APK build failed!
    echo ==========================================
    pause
    exit /b 1
)

echo.
echo [OK] APK build completed successfully!
echo.

REM ==========================================
REM 2. APK Path
REM ==========================================
set APK=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK%" (
    echo [ERROR] APK not found:
    echo %APK%
    pause
    exit /b 1
)

echo APK:
echo %CD%\%APK%
echo.

REM ==========================================
REM 3. Check ADB
REM ==========================================
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe

if not exist "%ADB%" (
    echo [ERROR] adb.exe not found!
    echo.
    echo Expected location:
    echo %ADB%
    pause
    exit /b 1
)

echo [2/4] Checking PDA connection...
echo.

"%ADB%" devices

echo.

REM ==========================================
REM 4. Check PDA Connection
REM ==========================================
"%ADB%" get-state >nul 2>&1

if errorlevel 1 (
    echo [ERROR] No available PDA detected!
    echo.
    echo Please check:
    echo   1. The PDA is connected via USB.
    echo   2. USB debugging is enabled.
    echo   3. The PDA has authorized this computer for USB debugging.
    echo.
    pause
    exit /b 1
)

echo [OK] PDA connected!
echo.

REM ==========================================
REM 5. Install APK
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
    echo If you see INSTALL_FAILED_UPDATE_INCOMPATIBLE,
    echo the APK may have been signed with a different key.
    echo.
    pause
    exit /b 1
)

echo.
echo [OK] APK installed successfully!
echo.

REM ==========================================
REM 6. Complete
REM ==========================================
echo ==========================================
echo   Update Completed!
echo ==========================================
echo.
echo The APK has been installed on the PDA.
echo.
pause

endlocal