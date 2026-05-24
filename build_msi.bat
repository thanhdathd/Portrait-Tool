@echo off
setlocal enabledelayedexpansion

REM =========================
REM PARSE ARGUMENTS
REM =========================
set SKIP_MAVEN=0
set SKIP_RUNTIME=0

:parse_args
if "%~1"=="" goto end_parse_args
if /i "%~1"=="--skip-maven" set SKIP_MAVEN=1
if /i "%~1"=="--skip-runtime" set SKIP_RUNTIME=1
shift
goto parse_args
:end_parse_args

echo [INFO] Build Configuration:
echo - Skip Maven Build: %SKIP_MAVEN%
echo - Skip Custom Runtime: %SKIP_RUNTIME%
echo.

REM =========================
REM CHECK TOOLS (JAVA, WIX, MAVEN)
REM =========================
echo Checking Required Tools...

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] JDK is not installed or not in PATH.
    echo Please install JDK 17+ or JDK 21 ^(recommended^)
    echo https://adoptium.net/
    pause
    exit /b 1
)

where jpackage >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] jpackage not found.
    echo Please install JDK 14+ ^(recommended JDK 17+ or 21^).
    echo Make sure JAVA_HOME\bin is added to your PATH.
    pause
    exit /b 1
)

where candle >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] WiX Toolset not found ^(candle.exe missing^)
    echo Required for building MSI installer.
    echo.
    echo Install WiX Toolset:
    echo github.com/wixtoolset/wix3/releases
    echo ^(Recommended: WiX Toolset v3.14.1 or v4 depending on JDK^)
    pause
    exit /b 1
)

if %SKIP_MAVEN%==1 goto skip_mvn_check
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found ^(mvn missing^)
    echo Please install Apache Maven and add it to your PATH.
    pause
    exit /b 1
)
:skip_mvn_check

echo All tools OK!

REM =========================
REM CONFIG
REM =========================
set PROJECT_ROOT=%~dp0

set ARTIFACT_DIR=%PROJECT_ROOT%target
set MAIN_JAR=portrai-tool-2.0.0.jar

set BUILD_DIR=%PROJECT_ROOT%dist\build
set RESOURCE_SRC=%PROJECT_ROOT%resource-for-packaging
set RESOURCE_DST=%BUILD_DIR%\resources

set OUTPUT_DIR=%PROJECT_ROOT%dist\build\output
set RELEASE_DIR=%PROJECT_ROOT%dist\release\windows\msi
set RUNTIME_DIR=%PROJECT_ROOT%dist\runtime

set APP_NAME=PortraitTool
set MAIN_CLASS=core.launcher.AppLauncher

REM =========================
REM BUILD FAT JAR WITH MAVEN
REM =========================
echo.
echo ========================================

if %SKIP_MAVEN%==1 (
    echo STEP 1: Skipping Maven build... Using existing Fat JAR.
    if not exist "%ARTIFACT_DIR%\%MAIN_JAR%" (
        echo [ERROR] Fat JAR not found at %ARTIFACT_DIR%\%MAIN_JAR%
        echo Please build it first or run without --skip-maven
        pause
        exit /b 1
    )
    goto step_cleanup
)

echo STEP 1: Building Fat JAR with Maven...
echo ========================================
:: PHẢI CÓ chữ "call" để script không bị ngắt giữa chừng
call mvn clean package

if %errorlevel% neq 0 (
    echo [ERROR] MAVEN BUILD FAILED! Please check your code.
    pause
    exit /b 1
)

:step_cleanup
REM =========================
REM CLEAN OLD BUILD FOLDERS
REM =========================
echo.
echo Cleaning old packaging folders...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"

:: Chỉ xóa folder runtime nếu KHÔNG skip bước build runtime
if %SKIP_RUNTIME%==0 (
    if exist "%RUNTIME_DIR%" rmdir /s /q "%RUNTIME_DIR%"
)

REM =========================
REM CREATE FOLDERS
REM =========================
mkdir "%BUILD_DIR%"
mkdir "%RESOURCE_DST%"
mkdir "%OUTPUT_DIR%"
mkdir "%RELEASE_DIR%"

REM =========================
REM COPY JARS & RESOURCES
REM =========================
echo Copying jar and resources...
copy /Y "%ARTIFACT_DIR%\%MAIN_JAR%" "%BUILD_DIR%" >nul
copy /Y "%RESOURCE_SRC%\LICENSE.rtf" "%RESOURCE_DST%" >nul
copy /Y "%RESOURCE_SRC%\app2.ico" "%RESOURCE_DST%" >nul
copy /Y "%RESOURCE_SRC%\file-associations.properties" "%RESOURCE_DST%" >nul

REM =========================
REM RUN JDEPS (DYNAMIC MODULES)
REM =========================
echo.
echo ========================================

if %SKIP_RUNTIME%==1 (
    echo STEP 2 ^& 3: Skipping custom runtime build...
    if not exist "%RUNTIME_DIR%\bin\java.exe" (
        echo [ERROR] Custom runtime not found at %RUNTIME_DIR%
        echo Please build it first or run without --skip-runtime
        pause
        exit /b 1
    )
    goto step_jpackage
)

echo STEP 2: Analyzing dependencies (jdeps)
set MODULE_DEPS=

:: Chạy jdeps và lưu kết quả vào biến MODULE_DEPS
FOR /F "tokens=*" %%i IN ('jdeps --ignore-missing-deps -q --recursive --multi-release 23 --print-module-deps "%BUILD_DIR%\%MAIN_JAR%"') DO SET MODULE_DEPS=%%i

if "!MODULE_DEPS!"=="" (
    echo [ERROR] jdeps failed to find any modules. Check your jar file!
    pause
    exit /b 1
)
echo Found required modules: !MODULE_DEPS!

REM =========================
REM RUN JLINK (CUSTOM RUNTIME)
REM =========================
echo.
echo ========================================
echo STEP 3: Building Custom Runtime (jlink)
echo ========================================
jlink ^
  --add-modules !MODULE_DEPS! ^
  --strip-debug ^
  --compress=zip-9 ^
  --no-header-files ^
  --no-man-pages ^
  --output "%RUNTIME_DIR%"

if %errorlevel% neq 0 (
    echo [ERROR] JLINK FAILED!
    pause
    exit /b 1
)
echo Custom runtime built successfully.

:step_jpackage
REM =========================
REM STEP 4: JPACKAGE
REM =========================
echo.
echo ========================================
echo STEP 4: Packaging MSI Installer (jpackage)
echo ========================================
jpackage ^
  --type msi ^
  --name "%APP_NAME%" ^
  --input "%BUILD_DIR%" ^
  --dest "%OUTPUT_DIR%" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --runtime-image "%RUNTIME_DIR%" ^
  --icon "%RESOURCE_DST%\app2.ico" ^
  --license-file "%RESOURCE_DST%\LICENSE.rtf" ^
  --file-associations "%RESOURCE_DST%\file-associations.properties" ^
  --vendor "Phi Thanh Dat" ^
  --description "Measuring and constructing outline sketch for portrait drawing" ^
  --copyright "Copyright (c) 2026 Phi Thanh Dat. All rights reserved." ^
  --app-version 2.0.0 ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut

if %errorlevel% neq 0 (
    echo [ERROR] JPACKAGE FAILED!
    pause
    exit /b 1
)

REM =========================
REM COPY MSI RESULT
REM =========================
echo.
echo Copying MSI to release folder...
copy /Y "%OUTPUT_DIR%\*.msi" "%RELEASE_DIR%\" >nul


echo.
echo ========================================
echo           ALL DONE SUCCESSFULLY!
echo ========================================
echo MSI located at: %RELEASE_DIR%
echo ========================================
echo.

REM =========================
REM CLEANUP BUILD FOLDER
REM =========================
IF EXIST "%RELEASE_DIR%\*.msi" (
    echo Cleaning temporary build directory...
    rmdir /s /q "%BUILD_DIR%"
    echo Cleanup done.
)

endlocal
pause