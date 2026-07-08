@echo off
setlocal

cd /d "%~dp0"
set PYTHONNOUSERSITE=1

where conda >nul 2>nul
if errorlevel 1 (
  echo Conda was not found. Please open an Anaconda Prompt or make sure conda is on PATH.
  exit /b 1
)

conda run -n project22-fatigue python -c "import uvicorn, click" >nul 2>nul
if errorlevel 1 (
  echo Dependencies are not installed in conda env project22-fatigue.
  echo Please run setup_project22_fatigue_env.bat first.
  exit /b 1
)

echo Starting fatigue crack prediction worker on http://127.0.0.1:5000
conda run -n project22-fatigue python -m uvicorn fatigue_worker:app --host 127.0.0.1 --port 5000

endlocal
