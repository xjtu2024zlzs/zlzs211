@echo off
setlocal

cd /d "%~dp0"
set PYTHONNOUSERSITE=1

where conda >nul 2>nul
if errorlevel 1 (
  echo Conda was not found. Please open an Anaconda Prompt or make sure conda is on PATH.
  exit /b 1
)

conda env list | findstr /C:"project22-fatigue" >nul 2>nul
if errorlevel 1 (
  echo Creating conda environment project22-fatigue...
  conda create -n project22-fatigue python=3.13 -y
  if errorlevel 1 exit /b 1
)

echo Installing fatigue worker dependencies...
conda run -n project22-fatigue python -m pip install --upgrade pip
if errorlevel 1 exit /b 1

conda run -n project22-fatigue python -m pip install --upgrade -r requirements.txt
if errorlevel 1 exit /b 1

conda run -n project22-fatigue python -c "import fastapi, uvicorn, click, numpy, pandas, sklearn, joblib, openpyxl; print('Dependencies verified')"
if errorlevel 1 exit /b 1

echo.
echo Environment is ready. Start the service with:
echo start_fatigue_worker.bat

endlocal
