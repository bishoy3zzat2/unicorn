@echo off
setlocal enabledelayedexpansion
set count=0
for /R %%f in (*.java) do (
    for /f %%a in ('find /c /v "" "%%f"') do (
        set /a count+=%%a
    )
)
echo Total lines in all Java files: !count!
pause
