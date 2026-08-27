@echo off
setlocal EnableExtensions EnableDelayedExpansion

title WarehousePutaway - Sync Scan Records

echo ==========================================
echo   WarehousePutaway - Sync Scan Records
echo ==========================================
echo.

REM ============================================================
REM Configuration
REM ============================================================

set "BASE_DIR=%~dp0"
set "REMOTE_DIR=/sdcard/Download/WarehousePutaway"
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

set "SYNC_COUNT=0"
set "SKIP_COUNT=0"
set "OLD_COUNT=0"
set "ERROR_COUNT=0"

echo [OK] Base folder:
echo %BASE_DIR%
echo.

REM ============================================================
REM Check ADB
REM ============================================================

if not exist "%ADB%" (
    echo [ERROR] ADB not found:
    echo %ADB%
    echo.
    pause
    exit /b 1
)

echo [OK] ADB found.
echo.

REM ============================================================
REM 1. Check PDA connection
REM ============================================================

echo [1/4] Checking PDA connection...

"%ADB%" get-state >nul 2>&1

if errorlevel 1 (
    echo.
    echo [ERROR] PDA is not connected!
    echo.
    pause
    exit /b 1
)

"%ADB%" devices

echo.
echo [OK] PDA connected.
echo.

REM ============================================================
REM 2. Check remote folder
REM ============================================================

echo [2/4] Checking scan records on PDA...

"%ADB%" shell "test -d %REMOTE_DIR%" >nul 2>&1

if errorlevel 1 (
    echo.
    echo [ERROR] Scan record folder not found:
    echo %REMOTE_DIR%
    echo.
    pause
    exit /b 1
)

echo [OK] Scan record folder found.
echo.

REM ============================================================
REM 3. Detect CSV files
REM ============================================================

echo [3/4] Detecting scan record CSV files...
echo.

set "CSV_LIST=%TEMP%\warehouse_scan_records_%RANDOM%_%RANDOM%.txt"

"%ADB%" shell "ls %REMOTE_DIR%/*.csv" > "%CSV_LIST%" 2>nul

if not exist "%CSV_LIST%" (
    echo [ERROR] No scan record CSV files found.
    pause
    exit /b 1
)

for %%A in ("%CSV_LIST%") do (
    if %%~zA EQU 0 (
        echo [ERROR] No scan record CSV files found.
        del "%CSV_LIST%" >nul 2>&1
        pause
        exit /b 1
    )
)

echo [OK] CSV files found.
echo.

echo Files detected:
echo ------------------------------------------

for /f "usebackq delims=" %%F in ("%CSV_LIST%") do echo %%F

echo ------------------------------------------
echo.

REM ============================================================
REM 4. Sync
REM ============================================================

echo [4/4] Syncing scan records...
echo.

for /f "usebackq delims=" %%F in ("%CSV_LIST%") do (
    call :PROCESS_FILE "%%F"
)

REM ============================================================
REM Cleanup
REM ============================================================

del "%CSV_LIST%" >nul 2>&1

REM ============================================================
REM Summary
REM ============================================================

echo.
echo ==========================================
echo   Sync Summary
echo ==========================================
echo.
echo [OK] Newly synced     : %SYNC_COUNT%
echo [SKIP] Already synced : %SKIP_COUNT%
echo [SKIP] Old format     : %OLD_COUNT%
echo [ERROR] Failed        : %ERROR_COUNT%
echo.

if %ERROR_COUNT% GTR 0 (
    echo ==========================================
    echo [WARNING] Sync completed with errors.
    echo ==========================================
) else (
    echo ==========================================
    echo [SUCCESS] Scan records sync completed!
    echo ==========================================
)

echo.

pause
exit /b 0


REM ============================================================
REM Process one CSV file
REM ============================================================

:PROCESS_FILE

set "REMOTE_FILE=%~1"
set "FILE_NAME=%~nx1"

echo.
echo ------------------------------------------
echo Processing:
echo !FILE_NAME!
echo ------------------------------------------

REM ============================================================
REM Check filename prefix
REM ============================================================

if /I not "!FILE_NAME:~0,13!"=="scan_records_" (
    echo [SKIP] !FILE_NAME!
    echo        Unsupported filename.
    set /a OLD_COUNT+=1
    exit /b 0
)

REM ============================================================
REM Remove "scan_records_"
REM ============================================================

set "PART=!FILE_NAME:~13!"

REM ============================================================
REM Get first character after scan_records_
REM ============================================================

set "FIRST_CHAR=!PART:~0,1!"

REM ============================================================
REM Old format:
REM
REM scan_records_20260821_144755.csv
REM
REM New format:
REM
REM scan_records_Howard_20260827_101735.csv
REM
REM If first character is a number, treat as old format.
REM ============================================================

echo(!FIRST_CHAR!| findstr /R /C:"[0-9]" >nul 2>&1

if not errorlevel 1 (
    echo [SKIP] !FILE_NAME!
    echo        Old filename format.
    set /a OLD_COUNT+=1
    exit /b 0
)

REM ============================================================
REM Remove final:
REM
REM _YYYYMMDD_HHMMSS.csv
REM
REM 20 characters
REM ============================================================

set "OPERATOR_NAME=!PART:~0,-20!"

if "!OPERATOR_NAME!"=="" (
    echo [ERROR] Cannot determine operator name.
    echo         File: !FILE_NAME!
    set /a ERROR_COUNT+=1
    exit /b 0
)

REM ============================================================
REM Destination folder
REM ============================================================

set "DEST_DIR=%BASE_DIR%!OPERATOR_NAME! - scan_records"

set "DEST_FILE=!DEST_DIR!\!FILE_NAME!"

echo Operator:
echo !OPERATOR_NAME!
echo.

REM ============================================================
REM Create destination folder
REM ============================================================

if not exist "!DEST_DIR!" (
    echo [CREATE] !DEST_DIR!

    mkdir "!DEST_DIR!" 2>nul

    if errorlevel 1 (
        echo [ERROR] Cannot create destination folder.
        echo         !DEST_DIR!
        set /a ERROR_COUNT+=1
        exit /b 0
    )
)

REM ============================================================
REM Check if file already exists
REM ============================================================

if exist "!DEST_FILE!" (
    echo [SKIP] !FILE_NAME!
    echo        Already exists locally.
    set /a SKIP_COUNT+=1
    exit /b 0
)

REM ============================================================
REM Copy file
REM ============================================================

echo [COPY] !FILE_NAME!
echo.
echo From:
echo !REMOTE_FILE!
echo.
echo To:
echo !DEST_DIR!
echo.

"%ADB%" pull "!REMOTE_FILE!" "!DEST_DIR!"

if errorlevel 1 (
    echo.
    echo [ERROR] ADB pull failed.
    echo         !FILE_NAME!
    set /a ERROR_COUNT+=1
    exit /b 0
)

REM ============================================================
REM Verify local file
REM ============================================================

if exist "!DEST_FILE!" (
    echo.
    echo [OK] File copied successfully.
    echo     !DEST_FILE!

    set /a SYNC_COUNT+=1

) else (
    echo.
    echo [ERROR] ADB reported success,
    echo         but local file was not found.
    echo.
    echo Expected:
    echo !DEST_FILE!

    set /a ERROR_COUNT+=1
)

exit /b 0