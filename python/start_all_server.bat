@echo off
chcp 65001 >nul
title 后台启动全部 FastAPI 服务

cd /d D:\2.11\RuoYi-Cloud-master\python

echo ==================================================
echo 正在后台启动全部 FastAPI 服务...
echo 不会弹出多个终端窗口。
echo ==================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0all_fastapi.ps1"

echo.
echo 启动脚本执行完成。
echo 如果服务未正常启动，请查看 logs 文件夹中的 stderr 日志。
echo.
pause