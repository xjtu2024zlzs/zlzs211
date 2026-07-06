@echo off
cd /d "%~dp0"
set "ALGORITHM_DATA_ROOT=D:\2.11\data\topic3\FaultIdentifyData\AlgorithmData"
python -m uvicorn main:app --host 0.0.0.0 --port 9741
