# build.ps1 - 编译 mbedtls
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src"
$OutputDir = "$ProjectRoot\prebuild\native\mbedtls"

# Download source if not present (needs git clone for submodules)
if (-not (Test-Path "$SrcDir\CMakeLists.txt")) {
    Write-Host "Cloning mbedtls 3.6.5 (with submodules)..." -ForegroundColor Cyan
    if (Test-Path $SrcDir) { Remove-Item $SrcDir -Recurse -Force }
    & git clone --depth 1 --branch v3.6.5 --recurse-submodules https://github.com/Mbed-TLS/mbedtls.git $SrcDir
    if ($LASTEXITCODE -ne 0) { throw "git clone failed" }
    Write-Host "Clone complete." -ForegroundColor Green
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
    "-DENABLE_PROGRAMS=OFF",
    "-DENABLE_TESTING=OFF",
    "-DUSE_SHARED_MBEDTLS_LIBRARY=OFF",
    "-DUSE_STATIC_MBEDTLS_LIBRARY=ON",
    "-DMBEDTLS_FATAL_WARNINGS=OFF",
    "-B", $BuildDir,
    "-S", "$SrcDir"
)

& $Cmake @cmakeArgs
if ($LASTEXITCODE -ne 0) { throw "cmake configure failed" }

& $Cmake --build $BuildDir --parallel
if ($LASTEXITCODE -ne 0) { throw "build failed" }

& $Cmake --install $BuildDir
if ($LASTEXITCODE -ne 0) { throw "install failed" }

Move-Item "$OutputDir\lib\libmbedcrypto.a" "$OutputDir\lib\libmaterialfile_mbedcrypto.a" -Force

Write-Host "mbedtls OK -> $OutputDir" -ForegroundColor Green
