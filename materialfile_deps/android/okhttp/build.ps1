$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = (Resolve-Path "$ScriptDir\..\..\..").Path
$ModuleName = "okhttp"

$env:JAVA_HOME = "$RootDir\tools\jdk-17.0.12"
$env:ANDROID_HOME = "$RootDir\tools\android-sdk"

# ===== Download source =====
$SrcDir = "$ScriptDir\src"
if (-not (Test-Path "$SrcDir\.git")) {
    Write-Host "Cloning okhttp source (tag: parent-4.12.0)..." -ForegroundColor Cyan
    & git clone --depth 1 --branch parent-4.12.0 https://github.com/square/okhttp.git $SrcDir
    if ($LASTEXITCODE -ne 0) { throw "git clone failed" }
    Write-Host "Clone complete." -ForegroundColor Green
}

Write-Host "=== Building $ModuleName ===" -ForegroundColor Cyan
Push-Location "$ScriptDir\src"
try {
    & .\gradlew.bat :okhttp:jar --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Build failed for $ModuleName" }
} finally {
    Pop-Location
}

$outputDir = "$RootDir\prebuild\android\$ModuleName"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$jar = Get-ChildItem "$ScriptDir\src\okhttp\build\libs\*.jar" -Exclude "*-sources*","*-javadoc*" | Select-Object -First 1
if ($jar) {
    Copy-Item $jar.FullName "$outputDir\materialfile-$ModuleName.jar" -Force
    Write-Host "Copied -> prebuild\android\$ModuleName\materialfile-$ModuleName.jar" -ForegroundColor Green
} else {
    throw "No JAR output found for $ModuleName"
}
