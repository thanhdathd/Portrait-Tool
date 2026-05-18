@echo off
setlocal enabledelayedexpansion

REM =========================
REM CHECK JDK / JPACKAGE
REM =========================
echo Checking Java / JPackage...

REM Check for javac (The Java Compiler, which only exists in the JDK)
where javac >nul 2>&1

REM Check the error level of the 'where' command (0 = found, 1 = not found)
if %errorlevel% neq 0 (
    echo [ERROR] JDK is not installed or not in PATH.
    echo Please install JDK 17+ or JDK 21 ^(recommended^)
    echo https://adoptium.net/
    pause
    exit /b 1
)

where jpackage >nul 2>&1

:: Check the result (0 = found, anything else = not found)
if %errorlevel% neq 0 (
    echo jpackage not found.
    echo Please install JDK 14+ ^(recommended JDK 17+ or 21^).
    echo Make sure JAVA_HOME\bin is added to your PATH.
    pause
    exit /b 1
)


echo Java OK
echo jpackage OK

REM =========================
REM CHECK WIX TOOLSET
REM =========================
echo Checking WiX Toolset...

where candle >nul 2>&1
if %errorlevel% neq 0 (
    echo WiX Toolset not found ^(candle.exe missing^)
    echo Required for building MSI installer.
    echo.
    echo Install WiX Toolset:
    echo https://wixtoolset.org/releases/
    echo ^(Recommended: WiX Toolset v3.11 or v4 depending on JDK^)
    pause
    exit /b 1
)

where light >nul 2>&1
if %errorlevel% neq 0 (
    echo WiX Toolset not found ^(light.exe missing^)
    echo Required for building MSI installer.
    echo Install WiX Toolset:
    echo https://wixtoolset.org/releases/
    pause
    exit /b 1
)

echo WiX Toolset OK

REM =========================
REM CONFIG
REM =========================
set PROJECT_ROOT=%~dp0

set ARTIFACT_DIR=%PROJECT_ROOT%out\artifacts\portrai_tool_jar
set BUILD_DIR=%PROJECT_ROOT%dist\build
set RESOURCE_SRC=%PROJECT_ROOT%resource-for-packaging
set RESOURCE_DST=%BUILD_DIR%\resources

set OUTPUT_DIR=%PROJECT_ROOT%dist\build\output
set RELEASE_DIR=%PROJECT_ROOT%dist\release\windows\msi

set APP_NAME=PortraitTool
set MAIN_JAR=portrai-tool.jar
set MAIN_CLASS=core.launcher.AppLauncher

set RUNTIME_DIR=%PROJECT_ROOT%dist\runtime

REM =========================
REM CLEAN OLD BUILD
REM =========================
echo Cleaning old build...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"

REM =========================
REM CREATE FOLDERS
REM =========================
mkdir "%BUILD_DIR%"
mkdir "%RESOURCE_DST%"
mkdir "%OUTPUT_DIR%"
mkdir "%RELEASE_DIR%"

REM =========================
REM COPY JARS
REM =========================
echo Copying jars...
copy /Y "%ARTIFACT_DIR%\*.jar" "%BUILD_DIR%" >nul

REM =========================
REM COPY RESOURCES
REM =========================
echo Copying resources...
copy /Y "%RESOURCE_SRC%\LICENSE.rtf" "%RESOURCE_DST%" >nul
copy /Y "%RESOURCE_SRC%\app2.ico" "%RESOURCE_DST%" >nul
copy /Y "%RESOURCE_SRC%\file-associations.properties" "%RESOURCE_DST%" >nul

REM =========================
REM RUN JPACKAGE
REM =========================
echo Running jpackage...

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
  --file-associations "%RESOURCE_DST%/file-associations.properties" ^
  --vendor "Phi Thanh Dat" ^
  --description "Measuring and constructing outline sketch for portrait drawing" ^
  --copyright "Copyright © 2026 Phi Thanh Dat. All rights reserved." ^
  --app-version 2.0.0 ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut

IF ERRORLEVEL 1 (
    echo JPACKAGE FAILED!
    exit /b 1
)

REM =========================
REM COPY MSI RESULT
REM =========================
echo Copying MSI to release folder...

copy /Y "%OUTPUT_DIR%\*.msi" "%RELEASE_DIR%\" >nul

echo.
echo BUILD SUCCESS!
echo MSI located at: %RELEASE_DIR%
echo.

REM =========================
REM CLEANUP BUILD FOLDER
REM =========================
echo Cleaning build directory...

IF EXIST "%RELEASE_DIR%\*.msi" (
    echo Cleaning build directory...
    rmdir /s /q "%BUILD_DIR%"
    echo Build folder cleaned.
)

endlocal
pause