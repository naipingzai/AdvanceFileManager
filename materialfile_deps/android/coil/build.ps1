$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = (Resolve-Path "$ScriptDir\..\..\..").Path
$ModuleName = Split-Path -Leaf $ScriptDir

$env:JAVA_HOME = "$RootDir\tools\jdk-17.0.12"
$env:ANDROID_HOME = "$RootDir\tools\android-sdk"

$GradlewDir = (Resolve-Path "$ScriptDir\..\gradle").Path

# ===== Download source =====
$UpstreamUrl = "https://github.com/coil-kt/coil.git"
$UpstreamTag = "2.7.0"
$ModulePath = "coil-singleton"
$SrcBase = "$ScriptDir\src"
if (-not (Test-Path "$SrcBase\java")) {
    Write-Host "Downloading $ModulePath source..." -ForegroundColor Cyan
    $tmpDir = Join-Path $env:TEMP "mf_dl_$(Get-Random)"
    & git clone --depth 1 --branch $UpstreamTag $UpstreamUrl $tmpDir
    if ($LASTEXITCODE -ne 0) { throw "git clone failed" }
    New-Item -ItemType Directory -Force -Path $SrcBase | Out-Null
    $mainSrc = "$tmpDir\$ModulePath\src\main"
    if (Test-Path "$mainSrc\java") { Copy-Item "$mainSrc\java" "$SrcBase\java" -Recurse }
    if (Test-Path "$mainSrc\kotlin") {
        if (-not (Test-Path "$SrcBase\java")) { New-Item -ItemType Directory -Force -Path "$SrcBase\java" | Out-Null }
        Copy-Item "$mainSrc\kotlin\*" "$SrcBase\java" -Recurse -Force
    }
    if (Test-Path "$mainSrc\res") { Copy-Item "$mainSrc\res" "$SrcBase\res" -Recurse }
    if (Test-Path "$mainSrc\aidl") { Copy-Item "$mainSrc\aidl" "$SrcBase\aidl" -Recurse }
    if (Test-Path "$mainSrc\AndroidManifest.xml") { Copy-Item "$mainSrc\AndroidManifest.xml" "$SrcBase\AndroidManifest.xml" }
    Remove-Item $tmpDir -Recurse -Force
    Write-Host "Download complete." -ForegroundColor Green
}

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