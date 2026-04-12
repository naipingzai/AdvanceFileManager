# build.ps1 - 编译 lz4
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src\build\cmake"
$OutputDir = "$ProjectRoot\prebuild\native\lz4"
$SrcBase = "$LibDir\src"

# Download source if not present
if (-not (Test-Path "$SrcBase\*")) {
    Write-Host "Downloading lz4 1.10.0..." -ForegroundColor Cyan
    $archive = "$env:TEMP\lz4.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://github.com/lz4/lz4/archive/refs/tags/v1.10.0.tar.gz"
    if ($LASTEXITCODE -ne 0) { throw "Download failed" }
    New-Item -ItemType Directory -Force -Path $SrcBase | Out-Null
    & tar xf $archive --strip-components=1 -C $SrcBase
    if ($LASTEXITCODE -ne 0) { throw "Extract failed" }
    Remove-Item $archive -Force
    Write-Host "Download complete." -ForegroundColor Green
}

$SdkDir = "$ProjectRoot\tools\android-sdk"
$NdkDir = (Get-ChildItem "$SdkDir\ndk" -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
$CmakeVersionDir = Get-ChildItem "$SdkDir\cmake" -Directory | Sort-Object Name | Select-Object -Last 1
$Cmake = "$($CmakeVersionDir.FullName)\bin\cmake.exe"
$Ninja = "$($CmakeVersionDir.FullName)\bin\ninja.exe"
$Toolchain = "$NdkDir\build\cmake\android.toolchain.cmake"

$MinApi = 21
$BuildDir = "$LibDir\build\$ABI"

if ($Clean) {
    if (Test-Path $BuildDir) { Remove-Item $BuildDir -Recurse -Force }
    if (Test-Path $OutputDir) { Remove-Item $OutputDir -Recurse -Force }
}

New-Item -ItemType Directory -Force -Path "$OutputDir\lib", "$OutputDir\include" | Out-Null

$cmakeArgs = @(
    "-G", "Ninja",
    "-DCMAKE_MAKE_PROGRAM=$Ninja",
    "-DCMAKE_TOOLCHAIN_FILE=$Toolchain",
    "-DANDROID_ABI=$ABI",
    "-DANDROID_PLATFORM=android-$MinApi",
    "-DCMAKE_BUILD_TYPE=Release",
    "-DCMAKE_INSTALL_PREFIX=$OutputDir",
    "-DBUILD_SHARED_LIBS=OFF",
    "-DBUILD_STATIC_LIBS=ON",
    "-DLZ4_BUILD_CLI=OFF",
    "-DLZ4_BUILD_LEGACY_CLI=OFF",
    "-B", $BuildDir,
    "-S", "$SrcDir"
)

& $Cmake @cmakeArgs
if ($LASTEXITCODE -ne 0) { throw "cmake configure failed" }

& $Cmake --build $BuildDir --parallel
if ($LASTEXITCODE -ne 0) { throw "build failed" }

& $Cmake --install $BuildDir
if ($LASTEXITCODE -ne 0) { throw "install failed" }

Move-Item "$OutputDir\lib\liblz4.a" "$OutputDir\lib\libmaterialfile_lz4.a" -Force

Write-Host "lz4 OK -> $OutputDir" -ForegroundColor Green
