@echo off
echo This script needs to run as Administrator
echo Right-click this file and select "Run as administrator"
echo.
pause
echo.
echo Cleaning npm cache...
call npm cache clean --force
echo.
echo Installing dependencies with local cache...
call npm install --cache=.npm-cache --prefer-offline=false
echo.
echo Installation complete!
pause
