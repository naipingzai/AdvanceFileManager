# build.ps1 - 编译 xz (liblzma)
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src"
$OutputDir = "$ProjectRoot\prebuild\native\xz"

# Download source if not present
if (-not (Test-Path "$SrcDir\*")) {
    Write-Host "Downloading xz 5.6.4..." -ForegroundColor Cyan
    $archive = "$env:TEMP\xz.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://github.com/tukaani-project/xz/archive/refs/tags/v5.6.4.tar.gz"
    if ($LASTEXITCODE -ne 0) { throw "Download failed" }
    New-Item -ItemType Directory -Force -Path $SrcDir | Out-Null
    & tar xf $archive --strip-components=1 -C $SrcDir
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
    "-DCREATE_XZ_SYMLINKS=OFF",
    "-DCREATE_LZMA_SYMLINKS=OFF",
    "-B", $BuildDir,
    "-S", "$SrcDir"
)

& $Cmake @cmakeArgs
if ($LASTEXITCODE -ne 0) { throw "cmake configure failed" }

& $Cmake --build $BuildDir --parallel
if ($LASTEXITCODE -ne 0) { throw "build failed" }

& $Cmake --install $BuildDir --component liblzma_Runtime
if ($LASTEXITCODE -ne 0) { throw "install liblzma_Runtime failed" }

& $Cmake --install $BuildDir --component liblzma_Development
if ($LASTEXITCODE -ne 0) { throw "install liblzma_Development failed" }

Move-Item "$OutputDir\lib\liblzma.a" "$OutputDir\lib\libmaterialfile_lzma.a" -Force

Write-Host "xz (liblzma) OK -> $OutputDir" -ForegroundColor Green
