$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = (Resolve-Path "$ScriptDir\..\..\..").Path
$ModuleName = Split-Path -Leaf $ScriptDir

$env:JAVA_HOME = "$RootDir\tools\jdk-17.0.12"
$env:ANDROID_HOME = "$RootDir\tools\android-sdk"

$GradlewDir = (Resolve-Path "$ScriptDir\..\gradle").Path

Write-Host "=== Building $ModuleName ===" -ForegroundColor Cyan
& "$GradlewDir\gradlew.bat" --project-dir "$ScriptDir" assembleRelease --no-daemon
if ($LASTEXITCODE -ne 0) { throw "Build failed for $ModuleName" }

$outputDir = "$RootDir\prebuild\android\$ModuleName"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$aar = Get-ChildItem "$ScriptDir\build\outputs\aar\*-release.aar" | Select-Object -First 1
if ($aar) {
    Copy-Item $aar.FullName "$outputDir\materialfile-$ModuleName.aar" -Force
    Write-Host "Copied -> prebuild\android\$ModuleName\materialfile-$ModuleName.aar" -ForegroundColor Green
} else {
    throw "No AAR output found for $ModuleName"
}