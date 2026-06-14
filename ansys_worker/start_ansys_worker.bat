@echo off
setlocal

REM Configure the official Workbench launcher before starting the worker.
REM runwb2.bat initializes the ANSYS environment before starting Workbench.
set "ANSYS_WORKBENCH_CMD=D:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat"
set "ANSYS_MESH_SIZE_MM=3"
set "ANSYS_KEEP_MECHANICAL_OPEN=1"

cd /d "%~dp0"
python ansys_import_worker.py
