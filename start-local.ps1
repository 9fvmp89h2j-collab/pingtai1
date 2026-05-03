param(
    [string]$MysqlBin = 'E:\mysql\mysql-8.0.29-winx64\bin',
    [string]$MysqlUser = 'root',
    [string]$MysqlPassword = '',
    [switch]$ImportDb
)

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$springbootDir = Join-Path $repoRoot 'springboot'
$vueDir = Join-Path $repoRoot 'vue3'
$mysqlExe = Join-Path $MysqlBin 'mysql.exe'
$mysqldExe = Join-Path $MysqlBin 'mysqld.exe'
$doctorStoryMigrationSql = Join-Path $springbootDir 'src\main\resources\sql\doctorstory_readingglossary_migration.sql'
$mavenExe = Join-Path $repoRoot 'tools\apache-maven-3.9.9\bin\mvn.cmd'
$jarPath = Join-Path $springbootDir 'target\springboot-0.0.1-SNAPSHOT.jar'
$imageRootDir = Join-Path $repoRoot 'image'
$doctorStoryPreviewTargetDir = Join-Path $vueDir 'public\doctor-story-preview'

function Test-LocalPort {
    param([int]$Port)

    try {
        return (Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue).TcpTestSucceeded
    } catch {
        return $false
    }
}

function Get-MySqlCliArgs {
    if ($MysqlPassword) {
        return @("-u$MysqlUser", "-p$MysqlPassword")
    }

    return @("-u$MysqlUser")
}

function Start-DetachedProcess {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$ArgumentList,
        [Parameter(Mandatory = $true)][string]$WorkingDirectory,
        [Parameter(Mandatory = $true)][string]$StdoutPath,
        [Parameter(Mandatory = $true)][string]$StderrPath
    )

    if (Test-Path $StdoutPath) {
        Remove-Item $StdoutPath -Force
    }

    if (Test-Path $StderrPath) {
        Remove-Item $StderrPath -Force
    }

    Start-Process -FilePath $FilePath `
        -ArgumentList $ArgumentList `
        -WorkingDirectory $WorkingDirectory `
        -RedirectStandardOutput $StdoutPath `
        -RedirectStandardError $StderrPath `
        -PassThru
}

function Invoke-MySqlFile {
    param(
        [Parameter(Mandatory = $true)][string]$SqlPath,
        [Parameter(Mandatory = $true)][string]$DatabaseName
    )

    if (-not (Test-Path $SqlPath)) {
        throw "SQL file not found: $SqlPath"
    }

    $tempSql = Join-Path $MysqlBin ([System.IO.Path]::GetFileName($SqlPath))
    Copy-Item -Path $SqlPath -Destination $tempSql -Force

    $mysqlArgString = ($mysqlArgs -join ' ')
    $importCommand = ('"{0}" --default-character-set=utf8mb4 {1} {2} < "{3}"' -f $mysqlExe, $mysqlArgString, $DatabaseName, $tempSql)
    & cmd /c $importCommand
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute SQL file: $SqlPath"
    }
}

function Find-DoctorStoryPreviewSourceDir {
    param([Parameter(Mandatory = $true)][string]$ImageRootDir)

    if (-not (Test-Path $ImageRootDir)) {
        return ''
    }

    $candidate = Get-ChildItem -LiteralPath $ImageRootDir -Directory -ErrorAction SilentlyContinue |
        ForEach-Object {
            $matchedImages = Get-ChildItem -LiteralPath $_.FullName -File -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -match '^\d+\.(png|jpe?g|webp)$' }

            if ($matchedImages.Count -gt 0) {
                [PSCustomObject]@{
                    FullName = $_.FullName
                    Count = $matchedImages.Count
                }
            }
        } |
        Sort-Object -Property @{ Expression = 'Count'; Descending = $true }, FullName |
        Select-Object -First 1

    return $candidate.FullName
}

function Sync-DoctorStoryPreviewImages {
    param(
        [Parameter(Mandatory = $true)][string]$SourceDir,
        [Parameter(Mandatory = $true)][string]$TargetDir
    )

    if (-not (Test-Path $SourceDir)) {
        return
    }

    if (-not (Test-Path $TargetDir)) {
        New-Item -ItemType Directory -Path $TargetDir -Force | Out-Null
    }

    Get-ChildItem -LiteralPath $TargetDir -File -ErrorAction SilentlyContinue | Remove-Item -Force

    Add-Type -AssemblyName System.Drawing
    $jpegCodec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() |
        Where-Object { $_.MimeType -eq 'image/jpeg' } |
        Select-Object -First 1

    Get-ChildItem -LiteralPath $SourceDir -File | Sort-Object Name | ForEach-Object {
        $sourceFile = $_
        $targetFileName = '{0}.jpg' -f [System.IO.Path]::GetFileNameWithoutExtension($sourceFile.Name)
        $targetPath = Join-Path $TargetDir $targetFileName
        $sourceImage = $null
        $targetBitmap = $null
        $graphics = $null
        $encoderParams = $null

        try {
            $sourceImage = [System.Drawing.Image]::FromFile($sourceFile.FullName)
            $targetWidth = [Math]::Min(1280, $sourceImage.Width)
            $targetHeight = [int][Math]::Max(1, [Math]::Round($sourceImage.Height * $targetWidth / $sourceImage.Width))

            $targetBitmap = New-Object System.Drawing.Bitmap($targetWidth, $targetHeight)
            $graphics = [System.Drawing.Graphics]::FromImage($targetBitmap)
            $graphics.Clear([System.Drawing.Color]::White)
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $graphics.DrawImage($sourceImage, 0, 0, $targetWidth, $targetHeight)

            $encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
            $encoderParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter(
                [System.Drawing.Imaging.Encoder]::Quality,
                82L
            )
            $targetBitmap.Save($targetPath, $jpegCodec, $encoderParams)
        } finally {
            if ($encoderParams) {
                $encoderParams.Dispose()
            }
            if ($graphics) {
                $graphics.Dispose()
            }
            if ($targetBitmap) {
                $targetBitmap.Dispose()
            }
            if ($sourceImage) {
                $sourceImage.Dispose()
            }
        }
    }
}

$doctorStoryPreviewSourceDir = Find-DoctorStoryPreviewSourceDir -ImageRootDir $imageRootDir

if (-not (Test-Path $mysqldExe)) {
    throw "mysqld.exe not found: $mysqldExe"
}

if (-not (Test-Path $mysqlExe)) {
    throw "mysql.exe not found: $mysqlExe"
}

$mysqlArgs = Get-MySqlCliArgs

if (-not (Test-LocalPort 3306)) {
    Start-Process -FilePath $mysqldExe `
        -ArgumentList '--console' `
        -WorkingDirectory $MysqlBin `
        -WindowStyle Hidden | Out-Null

    Start-Sleep -Seconds 8
}

if (-not (Test-LocalPort 3306)) {
    throw 'MySQL did not start on port 3306.'
}

$dbExists = & $mysqlExe @mysqlArgs -N -e "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='heritage_db';"

if ($ImportDb -or -not $dbExists) {
    & $mysqlExe @mysqlArgs -e "DROP DATABASE IF EXISTS heritage_db; CREATE DATABASE heritage_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
    Invoke-MySqlFile -SqlPath (Join-Path $repoRoot 'heritage_db.sql') -DatabaseName 'heritage_db'
}

if (Test-Path $doctorStoryMigrationSql) {
    Invoke-MySqlFile -SqlPath $doctorStoryMigrationSql -DatabaseName 'heritage_db'
}

Sync-DoctorStoryPreviewImages -SourceDir $doctorStoryPreviewSourceDir -TargetDir $doctorStoryPreviewTargetDir

if (-not (Test-Path $jarPath)) {
    if (-not (Test-Path $mavenExe)) {
        throw "Backend jar missing and Maven not found: $mavenExe"
    }

    Push-Location $springbootDir
    try {
        $env:USERPROFILE = $springbootDir
        $env:HOME = $springbootDir
        $env:MAVEN_OPTS = '-Dmaven.repo.local=.m2\repository'
        & $mavenExe -DskipTests package
    } finally {
        Pop-Location
    }
}

if (-not (Test-LocalPort 8889)) {
    Start-DetachedProcess `
        -FilePath 'java' `
        -ArgumentList @('-jar', 'target\springboot-0.0.1-SNAPSHOT.jar', '--spring.main.banner-mode=off') `
        -WorkingDirectory $springbootDir `
        -StdoutPath (Join-Path $springbootDir 'backend.out.log') `
        -StderrPath (Join-Path $springbootDir 'backend.err.log') | Out-Null

    Start-Sleep -Seconds 15
}

if (-not (Test-LocalPort 8889)) {
    throw 'Backend did not start on port 8889.'
}

if (-not (Test-LocalPort 8800)) {
    Start-DetachedProcess `
        -FilePath 'npm.cmd' `
        -ArgumentList @('run', 'dev', '--', '--host', '127.0.0.1', '--port', '8800', '--strictPort') `
        -WorkingDirectory $vueDir `
        -StdoutPath (Join-Path $vueDir 'frontend.out.log') `
        -StderrPath (Join-Path $vueDir 'frontend.err.log') | Out-Null

    Start-Sleep -Seconds 8
}

if (-not (Test-LocalPort 8800)) {
    throw 'Frontend did not start on port 8800.'
}

Write-Host 'System is running.' -ForegroundColor Green
Write-Host 'Frontend: http://127.0.0.1:8800/' -ForegroundColor Cyan
Write-Host 'Backend:  http://localhost:8889/' -ForegroundColor Cyan
Write-Host 'Admin:    admin / 123456' -ForegroundColor Yellow
