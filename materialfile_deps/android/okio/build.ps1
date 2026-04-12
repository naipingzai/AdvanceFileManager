$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = (Resolve-Path "$ScriptDir\..\..\..").Path
$ModuleName = "okio"

$env:JAVA_HOME = "$RootDir\tools\jdk-17.0.12"
$env:ANDROID_HOME = "$RootDir\tools\android-sdk"

# ===== Download source =====
$SrcDir = "$ScriptDir\src"
if (-not (Test-Path "$SrcDir\.git")) {
    Write-Host "Cloning okio source (tag: parent-3.9.0)..." -ForegroundColor Cyan
    & git clone --depth 1 --branch parent-3.9.0 https://github.com/square/okio.git $SrcDir
    if ($LASTEXITCODE -ne 0) { throw "git clone failed" }
    Write-Host "Clone complete." -ForegroundColor Green
}

Write-Host "=== Building $ModuleName ===" -ForegroundColor Cyan
Push-Location "$ScriptDir\src"
try {
    & .\gradlew.bat :okio:jvmJar --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Build failed for $ModuleName" }
} finally {
    Pop-Location
}

$outputDir = "$RootDir\prebuild\android\$ModuleName"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$jar = Get-ChildItem "$ScriptDir\src\okio\build\libs\*.jar" -Exclude "*-sources*","*-javadoc*" | Select-Object -First 1
if ($jar) {
    Copy-Item $jar.FullName "$outputDir\materialfile-$ModuleName.jar" -Force
    Write-Host "Copied -> prebuild\android\$ModuleName\materialfile-$ModuleName.jar" -ForegroundColor Green
} else {
    throw "No JAR output found for $ModuleName"
}
