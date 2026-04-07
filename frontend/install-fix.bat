@echo off
echo Cleaning npm cache...
call npm cache clean --force
echo.
echo Installing frontend dependencies...
call npm install --verbose
echo.
echo Installation complete!
pause
