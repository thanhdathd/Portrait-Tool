@echo off
setlocal enabledelayedexpansion

:: 1. Khởi tạo biến classpath với thư mục hiện tại
set "MY_CP=."

:: 2. Quét tất cả file .jar và nối vào biến (trừ file chính portrai-tool.jar)
for %%i in (*.jar) do (
    if /I not "%%i"=="portrai-tool.jar" (
        set "MY_CP=!MY_CP!;%%i"
    )
)

:: 3. In ra để kiểm tra
echo [INFO] Classpath: !MY_CP!

:: 4. Chạy lệnh jdeps
jdeps --ignore-missing-deps -q --recursive --multi-release 23 --class-path "!MY_CP!" --print-module-deps portrai-tool.jar

pause