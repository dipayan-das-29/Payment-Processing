@echo off
setlocal

if "%~1"=="" (
    echo Usage: run-service.bat service-name
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    if not "%%A"=="" (
        if not "%%A:~0,1%"=="#" (
            set "%%A=%%B"
        )
    )
)

call gradlew.bat :%~1:bootRun

endlocal