@echo off
echo Checking if yarn is installed...
call yarn --version >nul 2>&1
if errorlevel 1 (
    echo Yarn is not installed. Installing yarn globally...
    call npm install -g yarn
    echo.
)

echo Installing frontend dependencies with yarn...
call yarn install
echo.
echo Installation complete!
echo.
echo To start the dev server, run: yarn dev
pause
