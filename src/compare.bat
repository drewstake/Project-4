@echo off
setlocal enabledelayedexpansion

REM Create actual_outputs folder (one level up) if it doesn't exist.
if not exist "..\actual_outputs" mkdir "..\actual_outputs"

REM Define the testcases directory relative to the src folder.
set TEST_DIR=..\testcases

REM ----------------------------
REM FAIL tests: results in 4 columns
REM ----------------------------
set "fail_line="
set fail_count=0

echo FAIL tests:

for %%i in (01 02 03 04 05 06 07 08 09 10) do (
    if "%%i"=="03" (
        call :processTest fail_03a
        call :processTest fail_03b
    ) else if "%%i"=="05" (
        call :processTest fail_05a
        call :processTest fail_05b
    ) else if "%%i"=="08" (
        call :processTest fail_08a
        call :processTest fail_08b
    ) else (
        call :processTest fail_%%i
    )
)
if not "!fail_line!"=="" echo !fail_line!

REM ----------------------------
REM SUCCESS tests: results in 4 columns
REM ----------------------------
set "succ_line="
set succ_count=0

echo.
echo SUCCESS tests:

for %%i in (01 02 03 04 05 06 07 08 09 10) do (
    call :processTestSucc succ_%%i
)
if not "!succ_line!"=="" echo !succ_line!

endlocal
exit /b

:processTest
REM %1 is the test identifier for a FAIL test.
set "ident=%~1"
set "testfile=%TEST_DIR%\%ident%.minc"
set "expected=%TEST_DIR%\output_%ident%.txt"
java -cp "." Program "!testfile!" > ..\actual_outputs\%ident%.txt 2>&1
fc ..\actual_outputs\%ident%.txt "!expected!" >nul
if errorlevel 1 (
    set "result=%ident%: FAIL"
) else (
    set "result=%ident%: PASS"
)
call :accumulateFail "!result!"
exit /b

:accumulateFail
set /A fail_count+=1
set "fail_line=!fail_line!    %~1"
if !fail_count! GEQ 4 (
    echo !fail_line!
    set "fail_line="
    set fail_count=0
)
exit /b

:processTestSucc
REM %1 is the test identifier for a SUCCESS test.
set "ident=%~1"
set "testfile=%TEST_DIR%\%ident%.minc"
set "expected=%TEST_DIR%\output_%ident%.txt"
java -cp "." Program "!testfile!" > ..\actual_outputs\%ident%.txt 2>&1
fc ..\actual_outputs\%ident%.txt "!expected!" >nul
if errorlevel 1 (
    set "result=%ident%: FAIL"
) else (
    set "result=%ident%: PASS"
)
call :accumulateSucc "!result!"
exit /b

:accumulateSucc
set /A succ_count+=1
set "succ_line=!succ_line!    %~1"
if !succ_count! GEQ 4 (
    echo !succ_line!
    set "succ_line="
    set succ_count=0
)
exit /b
