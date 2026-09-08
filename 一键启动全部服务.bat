@echo off
setlocal
chcp 65001 >nul

title 小铜人中医侦探社 - 一键启动全部服务
cd /d "%~dp0"

if not exist "%~dp0start.ps1" (
    echo [错误] 未找到启动脚本：%~dp0start.ps1
    echo 请确认本文件位于项目根目录。
    pause
    exit /b 1
)

set "ALL_RUNNING=1"
for %%P in (3306 8889 8800) do (
    netstat -ano | findstr /R /C:":%%P .*LISTENING" >nul
    if errorlevel 1 set "ALL_RUNNING=0"
)

if "%ALL_RUNNING%"=="1" (
    echo MySQL、后端和前端已经在运行，正在打开游戏首页……
    start "" "http://127.0.0.1:8800/index.html#/home-map"
    exit /b 0
)

echo 正在启动 MySQL、Spring Boot 后端和 Vue 前端……
echo 启动成功后会自动打开游戏首页，请不要关闭此窗口。
echo.

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"

if errorlevel 1 (
    echo.
    echo [错误] 启动过程异常结束，请查看项目 logs 文件夹中的日志。
    pause
)

endlocal
