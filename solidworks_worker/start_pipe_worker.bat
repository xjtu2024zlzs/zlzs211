@echo off
setlocal
cd /d "%~dp0"
set PIPE_WORKER_RUN_SOLIDWORKS=1
python pipe_worker.py
