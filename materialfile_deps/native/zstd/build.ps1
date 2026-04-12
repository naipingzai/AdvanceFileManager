# build.ps1 - 编译 zstd
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src\build\cmake"
$OutputDir = "$ProjectRoot\prebuild\native\zstd"
$SrcBase = "$LibDir\src"

# Download source if not present
if (-not (Test-Path "$SrcBase\*")) {
    Write-Host "Downloading zstd 1.5.6..." -ForegroundColor Cyan
    $archive = "$env:TEMP\zstd.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://github.com/facebook/zstd/archive/refs/tags/v1.5.6.tar.gz"
    if ($LASTEXITCODE -ne 0) { throw "Download failed" }
    New-Item -ItemType Directory -Force -Path $SrcBase | Out-Null
    $ErrorActionPreference = "Continue"
    & tar xf $archive --strip-components=1 -C $SrcBase 2>&1 | Out-Null
    $ErrorActionPreference = "Stop"
    if (-not (Test-Path "$SrcBase\build")) { throw "Extract failed" }
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
    "-DZSTD_BUILD_PROGRAMS=OFF",
    "-DZSTD_BUILD_CONTRIB=OFF",
    "-DZSTD_BUILD_SHARED=OFF",
    "-DZSTD_BUILD_STATIC=ON",
    "-DZSTD_BUILD_TESTS=OFF",
    "-DZSTD_LEGACY_SUPPORT=OFF",
    "-B", $BuildDir,
    "-S", "$SrcDir"
)

& $Cmake @cmakeArgs
if ($LASTEXITCODE -ne 0) { throw "cmake configure failed" }

& $Cmake --build $BuildDir --parallel
if ($LASTEXITCODE -ne 0) { throw "build failed" }

& $Cmake --install $BuildDir
if ($LASTEXITCODE -ne 0) { throw "install failed" }

Move-Item "$OutputDir\lib\libzstd.a" "$OutputDir\lib\libmaterialfile_zstd.a" -Force

Write-Host "zstd OK -> $OutputDir" -ForegroundColor Green
