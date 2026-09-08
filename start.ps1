# ==============================================================
# 儿童针灸系统 — 一键启动脚本
# 自动启动后端 (SpringBoot :8889) + 前端 (Vue3 :8800)
# 双击 start.bat 即可运行
# ==============================================================
param(
    [switch]$SkipBuild = $false,
    [switch]$NoKill = $false
)

$ErrorActionPreference = 'Continue'

# 确保中文输出不乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding  = [System.Text.Encoding]::UTF8

$host.UI.RawUI.WindowTitle = '儿童针灸系统 — 启动中...'

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$springbootDir = Join-Path $repoRoot 'springboot'
$vueDir = Join-Path $repoRoot 'vue3'
$mavenCmd = Join-Path $repoRoot 'tools\apache-maven-3.9.9\bin\mvn.cmd'
$jarPath = Join-Path $springbootDir 'target\springboot-0.0.1-SNAPSHOT.jar'
$logDir = Join-Path $repoRoot 'logs'
$backendPort = 8889
$frontendPort = 8800

# ---- 彩色输出 ----
function Write-Info  { Write-Host "[INFO]  $args" -ForegroundColor Cyan }
function Write-OK    { Write-Host "[ OK ]  $args" -ForegroundColor Green }
function Write-Warn  { Write-Host "[WARN]  $args" -ForegroundColor Yellow }
function Write-ErrorMsg { Write-Host "[ERR ]  $args" -ForegroundColor Red }

# ---- 日志目录 ----
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force -ErrorAction SilentlyContinue | Out-Null
}
$backendLog = Join-Path $logDir 'backend.log'
$frontendLog = Join-Path $logDir 'frontend.log'
$buildLog = Join-Path $logDir 'maven-build.log'
$mysqlLog = Join-Path $logDir 'mysql.err.log'
$mysqlOutLog = Join-Path $logDir 'mysql.out.log'

# ---- 端口检查 ----
function Test-PortFree {
    param([int]$Port)
    try {
        $listener = New-Object System.Net.Sockets.TcpListener([System.Net.IPAddress]::Loopback, $Port)
        $listener.Start()
        $listener.Stop()
        return $true
    } catch {
        return $false
    }
}

function Test-PortOpen {
    param([int]$Port, [string]$HostName = '127.0.0.1')

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($HostName, $Port, $null, $null)
        if (-not $async.AsyncWaitHandle.WaitOne(1000, $false)) {
            return $false
        }
        $client.EndConnect($async)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Clear-Port {
    param([int]$Port, [string]$ServiceName)

    if (Test-PortOpen $Port) {
        if (-not $NoKill) {
            Write-Warn "端口 ${Port} 被占用，尝试释放..."

            $pids = New-Object System.Collections.Generic.HashSet[int]

            # 方法1: netstat，兼容当前环境里 Get-NetTCPConnection 权限不足的情况
            try {
                $lines = netstat -ano 2>$null | Select-String ":$Port\s+.*LISTENING"
                foreach ($line in $lines) {
                    $parts = ($line.Line.Trim() -split '\s+')
                    $pidText = $parts[-1]
                    $pidValue = 0
                    if ([int]::TryParse($pidText, [ref]$pidValue) -and $pidValue -gt 0) {
                        [void]$pids.Add($pidValue)
                    }
                }
            } catch { }

            # 方法2: Get-NetTCPConnection 回退
            try {
                $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
                foreach ($conn in $conns) {
                    if ($conn.OwningProcess -gt 0) {
                        [void]$pids.Add([int]$conn.OwningProcess)
                    }
                }
            } catch { }

            if ($pids.Count -gt 0) {
                foreach ($processId in $pids) {
                    $proc = Get-Process -Id $processId -ErrorAction SilentlyContinue
                    if ($proc) {
                        Write-Warn "关闭 $($proc.ProcessName) (PID:$($proc.Id))..."
                        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
                    } else {
                        Write-Warn "关闭 PID:$processId..."
                        taskkill /PID $processId /F 2>$null | Out-Null
                    }
                }
            } else {
                Write-Warn "无法识别占用端口 ${Port} 的进程。"
            }

            for ($i = 0; $i -lt 10; $i++) {
                Start-Sleep -Milliseconds 500
                if (-not (Test-PortOpen $Port)) {
                    break
                }
            }
        }

        if (Test-PortOpen $Port) {
            throw "端口 ${Port} 仍被占用，请手动释放后重试。"
        }
    }
    Write-OK "端口 ${Port} 可用"
}

# ---- 启动后台进程 ----
function Start-Background {
    param(
        [string]$FilePath,
        [string[]]$ArgumentList,
        [string]$WorkingDirectory,
        [string]$LogPath,
        [string]$DisplayName
    )
    if (Test-Path $LogPath) {
        Remove-Item $LogPath -Force -ErrorAction SilentlyContinue
    }
    $errLogPath = $LogPath -replace '\.log$', '.err.log'
    if ($errLogPath -eq $LogPath) {
        $errLogPath = "$LogPath.err"
    }
    if (Test-Path $errLogPath) {
        Remove-Item $errLogPath -Force -ErrorAction SilentlyContinue
    }

    function Quote-CmdArg {
        param([string]$Value)
        return '"' + ($Value -replace '"', '\"') + '"'
    }

    $cmdParts = @((Quote-CmdArg $FilePath))
    foreach ($arg in $ArgumentList) {
        $cmdParts += (Quote-CmdArg $arg)
    }
    $cmdInner = ($cmdParts -join ' ') + " > " + (Quote-CmdArg $LogPath) + " 2> " + (Quote-CmdArg $errLogPath)
    $cmd = '"' + $cmdInner + '"'

    $proc = Start-Process -FilePath 'cmd.exe' `
        -ArgumentList @('/d', '/s', '/c', $cmd) `
        -WorkingDirectory $WorkingDirectory `
        -PassThru `
        -WindowStyle Hidden

    Write-Info "${DisplayName} 已启动 (PID:$($proc.Id))，日志: $LogPath / $errLogPath"
    return $proc
}

# ---- 等待端口就绪 ----
function Wait-ForPort {
    param([int]$Port, [string]$ServiceName, [int]$TimeoutSec = 60)
    $elapsed = 0
    while ($elapsed -lt $TimeoutSec) {
        if (Test-PortOpen $Port) {
            Write-OK "${ServiceName} 端口 ${Port} 已就绪"
            return $true
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
        if ($elapsed % 10 -eq 0) {
            Write-Info "等待 ${ServiceName} 启动... (${elapsed}s)"
        }
    }
    throw "${ServiceName} 在 ${TimeoutSec}s 内未就绪，请检查日志。"
}

function Show-LogTail {
    param([string]$LogPath, [int]$Lines = 20)

    if (Test-Path $LogPath) {
        Get-Content $LogPath -Tail $Lines -ErrorAction SilentlyContinue | ForEach-Object {
            Write-Host $_ -ForegroundColor Red
        }
    }
}

# ---- 校验前端构建工具是否真的可用 ----
# node_modules 可能存在，但其中的 esbuild 二进制可能因复制目录、Node 升级或
# 安装中断而与 Vite 需要的版本不一致。只检查目录存在无法发现这种损坏。
function Test-FrontendDependencies {
    param([string]$NodePath, [string]$WorkingDirectory)

    Push-Location $WorkingDirectory
    try {
        $checkScript = "require('./node_modules/vite/node_modules/esbuild').transformSync('const ok = true')"
        & $NodePath -e $checkScript *> $null
        return ($LASTEXITCODE -eq 0)
    } catch {
        return $false
    } finally {
        Pop-Location
    }
}

function Wait-ForMySql {
    param([System.Diagnostics.Process]$Process, [string]$LogPath, [int]$TimeoutSec = 60)

    $elapsed = 0
    while ($elapsed -lt $TimeoutSec) {
        if (Test-PortOpen 3306) {
            Write-OK 'MySQL 端口 3306 已就绪'
            return $true
        }

        if ($Process -and $Process.HasExited) {
            Write-ErrorMsg "MySQL 进程提前退出 (退出码: $($Process.ExitCode))"
            Write-Host ''
            Write-Host '---- MySQL 最后日志 ----' -ForegroundColor DarkGray
            Show-LogTail -LogPath $LogPath -Lines 30
            throw 'MySQL start failed'
        }

        Start-Sleep -Seconds 2
        $elapsed += 2
        if ($elapsed % 10 -eq 0) {
            Write-Info "等待 MySQL 启动... (${elapsed}s)"
        }
    }

    Write-Host ''
    Write-Host '---- MySQL 最后日志 ----' -ForegroundColor DarkGray
    Show-LogTail -LogPath $LogPath -Lines 30
    throw "MySQL 在 ${TimeoutSec}s 内未就绪，请检查日志: $LogPath"
}

# ==================== 主流程 ====================

try {

Write-Host ''
Write-Host '==========================================================' -ForegroundColor Magenta
Write-Host '   儿童针灸系统 — 一键启动' -ForegroundColor Magenta
Write-Host '==========================================================' -ForegroundColor Magenta
Write-Host ''

# 1. 检查 Java
Write-Info '检查 Java 环境...'
try {
    $javaVer = & java -version 2>&1 | Select-Object -First 1
    Write-OK "Java: $javaVer"
} catch {
    Write-ErrorMsg '未找到 Java！请安装 JDK 17+ 并添加到系统 PATH。'
    Write-Host '  下载: https://adoptium.net/' -ForegroundColor DarkGray
    throw 'Java not found'
}

# 2. 启动 MySQL
Write-Info '检查 MySQL 连接...'

if (-not (Test-PortOpen 3306)) {
    Write-Info 'MySQL 未运行，尝试自动启动...'

    $mysqlService = Get-Service -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '^mysql' -or $_.DisplayName -match 'MySQL' } |
        Select-Object -First 1

    if ($mysqlService) {
        Write-Info "发现 MySQL 服务: $($mysqlService.Name) ($($mysqlService.Status))"
        if ($mysqlService.Status -ne 'Running') {
            try {
                Start-Service -Name $mysqlService.Name -ErrorAction Stop
                Wait-ForMySql -LogPath $mysqlLog -TimeoutSec 60 | Out-Null
            } catch {
                Write-Warn "MySQL 服务启动失败，改用 mysqld.exe 直接启动。原因: $_"
            }
        }
    }
}

if (-not (Test-PortOpen 3306)) {
    # 按优先级查找 mysqld.exe
    $mysqlCandidates = @(
        'E:\mysql\mysql-8.0.29-winx64\bin\mysqld.exe',
        'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe',
        'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe'
    )
    # 尝试从 PATH 中找
    try { $mysqlCandidates += (Get-Command mysqld.exe -ErrorAction Stop).Source } catch { }

    $mysqldPath = $null
    foreach ($candidate in ($mysqlCandidates | Select-Object -Unique)) {
        if ($candidate -and (Test-Path $candidate)) {
            $mysqldPath = $candidate
            break
        }
    }

    if (-not $mysqldPath) {
        Write-ErrorMsg '未找到 MySQL (mysqld.exe)，请安装 MySQL 或手动启动后重试。'
        throw 'MySQL not found'
    }

    $mysqlBin = Split-Path -Parent $mysqldPath
    $mysqlData = Join-Path (Split-Path -Parent $mysqlBin) 'data'
    if (-not (Test-Path $mysqlData)) {
        Write-ErrorMsg "MySQL 数据目录不存在: $mysqlData"
        throw 'MySQL data directory not found'
    }

    Remove-Item $mysqlLog, $mysqlOutLog -Force -ErrorAction SilentlyContinue

    Write-Info "启动 MySQL: $mysqldPath"
    Write-Info "数据目录: $mysqlData"
    Write-Info "MySQL 日志: $mysqlLog"
    $mysqlCmdInner = "`"$mysqldPath`" --console `"--datadir=$mysqlData`" > `"$mysqlOutLog`" 2> `"$mysqlLog`""
    $mysqlCmd = "`"$mysqlCmdInner`""
    $mysqlProc = Start-Process -FilePath 'cmd.exe' `
        -ArgumentList @('/d', '/s', '/c', $mysqlCmd) `
        -WorkingDirectory $mysqlBin `
        -PassThru `
        -WindowStyle Hidden

    Wait-ForMySql -Process $mysqlProc -LogPath $mysqlLog -TimeoutSec 60 | Out-Null
}
Write-OK 'MySQL 已连接 (端口 3306)'

# 3. 检查 npm
Write-Info '检查 npm 环境...'
try {
    $npmPath = (Get-Command npm.cmd -ErrorAction Stop).Source
} catch {
    try {
        $npmPath = (Get-Command npm -ErrorAction Stop).Source
    } catch {
        Write-ErrorMsg '未找到 npm！请安装 Node.js。'
        Write-Host '  下载: https://nodejs.org/' -ForegroundColor DarkGray
        throw 'npm not found'
    }
}
Write-OK "npm: $npmPath"

try {
    $nodePath = (Get-Command node.exe -ErrorAction Stop).Source
} catch {
    try {
        $nodePath = (Get-Command node -ErrorAction Stop).Source
    } catch {
        Write-ErrorMsg '未找到 node！请安装 Node.js。'
        throw 'node not found'
    }
}

# 4. 释放端口
Write-Info '检查端口占用...'
Clear-Port -Port $backendPort -ServiceName '后端'
Clear-Port -Port $frontendPort -ServiceName '前端'

# 5. 构建后端
if (-not $SkipBuild -or -not (Test-Path $jarPath)) {
    if (-not (Test-Path $mavenCmd)) {
        Write-ErrorMsg "Maven 未找到: $mavenCmd"
        throw 'Maven not found'
    }
    Write-Info '编译后端 (Maven package, 首次可能较慢)...'
    Write-Info "构建日志: $buildLog"

    Push-Location $springbootDir
    try {
        $env:HOME = $springbootDir
        $env:MAVEN_OPTS = '-Dmaven.repo.local=.m2\repository'
        $result = & $mavenCmd -DskipTests package 2>&1
        $result | Out-File $buildLog -Encoding UTF8
        if ($LASTEXITCODE -ne 0) {
            Write-ErrorMsg "Maven 构建失败！查看: $buildLog"
            # 打印最后几行错误
            $result | Select-Object -Last 15 | ForEach-Object { Write-Host $_ -ForegroundColor Red }
            throw 'Maven build failed'
        }
    } finally {
        Pop-Location
    }
    Write-OK '后端编译完成'
} else {
    Write-Info '跳过编译（使用已有 jar）'
}

# 6. 启动后端
Write-Info '启动后端 SpringBoot (:8889)...'
$backendProc = Start-Background `
    -FilePath 'java' `
    -ArgumentList @('-jar', $jarPath, '--spring.sql.init.mode=always') `
    -WorkingDirectory $springbootDir `
    -LogPath $backendLog `
    -DisplayName '后端'

Wait-ForPort -Port $backendPort -ServiceName '后端' -TimeoutSec 45 | Out-Null

# 7. 检查并修复前端依赖
$frontendDepsOK = Test-FrontendDependencies -NodePath $nodePath -WorkingDirectory $vueDir
if (-not $frontendDepsOK) {
    if (Test-Path (Join-Path $vueDir 'node_modules')) {
        Write-Warn '检测到前端依赖损坏或版本不匹配，正在自动修复（可能较慢）...'
    } else {
        Write-Info '首次运行，安装前端依赖（可能较慢）...'
    }

    $npmInstallLog = Join-Path $logDir 'npm-install.log'
    Push-Location $vueDir
    try {
        # 有锁文件时使用 npm ci，确保 Vite 与 esbuild 的 JS 包和二进制严格匹配。
        if (Test-Path (Join-Path $vueDir 'package-lock.json')) {
            & $npmPath ci 2>&1 | Out-File $npmInstallLog -Encoding UTF8
        } else {
            & $npmPath install 2>&1 | Out-File $npmInstallLog -Encoding UTF8
        }
        if ($LASTEXITCODE -ne 0) {
            Write-ErrorMsg "前端依赖安装失败！查看: $npmInstallLog"
            Show-LogTail -LogPath $npmInstallLog -Lines 20
            throw 'Frontend dependency installation failed'
        }
    } finally {
        Pop-Location
    }

    if (-not (Test-FrontendDependencies -NodePath $nodePath -WorkingDirectory $vueDir)) {
        Write-ErrorMsg "前端依赖修复后仍不可用，请查看: $npmInstallLog"
        throw 'Frontend dependency validation failed'
    }
    Write-OK '前端依赖安装完成'
} else {
    Write-OK '前端依赖检查通过'
}

# 8. 启动前端
Write-Info '启动前端 Vue3 (:8800)...'
$viteCli = Join-Path $vueDir 'node_modules\vite\bin\vite.js'
if (-not (Test-Path $viteCli)) {
    Write-ErrorMsg "Vite 未找到: $viteCli"
    throw 'vite not found'
}
$frontendProc = Start-Background `
    -FilePath $nodePath `
    -ArgumentList @($viteCli, '--mode', 'development', '--host', '127.0.0.1', '--port', '8800', '--strictPort') `
    -WorkingDirectory $vueDir `
    -LogPath $frontendLog `
    -DisplayName '前端'

Wait-ForPort -Port $frontendPort -ServiceName '前端' -TimeoutSec 30 | Out-Null

# ==================== 启动完成 ====================
Write-Host ''
Write-Host '==========================================================' -ForegroundColor Green
Write-Host '  系统启动成功！' -ForegroundColor Green
Write-Host '  前端:  http://127.0.0.1:8800/#/home-map' -ForegroundColor Cyan
Write-Host '  后端:  http://localhost:8889/' -ForegroundColor Cyan
Write-Host '  API文档: http://localhost:8889/swagger-ui.html' -ForegroundColor DarkGray
Write-Host '  管理员:  admin / 123456' -ForegroundColor Yellow
Write-Host '==========================================================' -ForegroundColor Green
Write-Host ''
Write-Host '日志文件:' -ForegroundColor DarkGray
Write-Host "  后端: $backendLog / $($backendLog -replace '\.log$', '.err.log')" -ForegroundColor DarkGray
Write-Host "  前端: $frontendLog / $($frontendLog -replace '\.log$', '.err.log')" -ForegroundColor DarkGray
Write-Host ''
Write-Host '按 Ctrl+C 或关闭窗口停止所有服务' -ForegroundColor Yellow

# 自动打开浏览器
Start-Sleep -Seconds 2
Start-Process 'http://127.0.0.1:8800/#/home-map'
Write-OK '已自动打开浏览器'

# ---- 监控进程 ----
while ($true) {
    Start-Sleep -Seconds 2
    if ($backendProc.HasExited) {
        Write-ErrorMsg "后端意外退出 (退出码: $($backendProc.ExitCode))"
        Write-ErrorMsg "查看日志: $backendLog"
        Write-Host ''
        Write-Host '---- 后端最后 20 行日志 ----' -ForegroundColor DarkGray
        if (Test-Path $backendLog) {
            Get-Content $backendLog -Tail 20 | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
        $backendErrLog = $backendLog -replace '\.log$', '.err.log'
        if (Test-Path $backendErrLog) {
            Get-Content $backendErrLog -Tail 20 | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
        break
    }
    if ($frontendProc.HasExited) {
        Write-ErrorMsg "前端意外退出 (退出码: $($frontendProc.ExitCode))"
        Write-ErrorMsg "查看日志: $frontendLog"
        Write-Host ''
        Write-Host '---- 前端最后 20 行日志 ----' -ForegroundColor DarkGray
        if (Test-Path $frontendLog) {
            Get-Content $frontendLog -Tail 20 | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
        $frontendErrLog = $frontendLog -replace '\.log$', '.err.log'
        if (Test-Path $frontendErrLog) {
            Get-Content $frontendErrLog -Tail 20 | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
        break
    }
}

} catch {
    Write-Host ''
    Write-ErrorMsg "启动失败: $_"
    Write-Host ''
} finally {
    # 清理
    if ($backendProc -and -not $backendProc.HasExited) {
        $backendProc.Kill() | Out-Null
    }
    if ($frontendProc -and -not $frontendProc.HasExited) {
        $frontendProc.Kill() | Out-Null
    }
}

Write-Host ''
Write-Host '按任意键退出...' -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
