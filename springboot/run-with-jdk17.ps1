# 使用 JDK 17 运行 Spring Boot 项目
$jdk17 = "C:\Program Files\Java\jdk-17.0.2"
if (-not (Test-Path "$jdk17\bin\java.exe")) {
    Write-Host "未找到 JDK 17，请确认路径: $jdk17" -ForegroundColor Red
    exit 1
}
$env:JAVA_HOME = $jdk17
$env:Path = "$jdk17\bin;" + $env:Path
Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
java -version
Write-Host "`n启动 Spring Boot..." -ForegroundColor Cyan
$mvnw = Join-Path $PSScriptRoot 'mvnw.cmd'
if (-not (Test-Path -LiteralPath $mvnw -PathType Leaf)) {
    Write-Host "未找到 Maven Wrapper: $mvnw" -ForegroundColor Red
    exit 1
}
& $mvnw -DskipTests spring-boot:run
exit $LASTEXITCODE
