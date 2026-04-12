# build.ps1 - 编译 pcre2
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src"
$OutputDir = "$ProjectRoot\prebuild\native\pcre2"

# Download source if not present
if (-not (Test-Path "$LibDir\src\*")) {
    Write-Host "Downloading pcre2 10.44..." -ForegroundColor Cyan
    $archive = "$env:TEMP\pcre2.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://github.com/PCRE2Project/pcre2/archive/refs/tags/pcre2-10.44.tar.gz"
    if ($LASTEXITCODE -ne 0) { throw "Download failed" }
    New-Item -ItemType Directory -Force -Path "$LibDir\src" | Out-Null
    & tar xf $archive --strip-components=1 -C "$LibDir\src"
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
    "-DPCRE2_BUILD_TESTS=OFF",
    "-DPCRE2_BUILD_PCRE2GREP=OFF",
    "-DBUILD_SHARED_LIBS=OFF",
    "-DBUILD_STATIC_LIBS=ON",
    "-B", $BuildDir,
    "-S", "$SrcDir"
)

& $Cmake @cmakeArgs
if ($LASTEXITCODE -ne 0) { throw "cmake configure failed" }

& $Cmake --build $BuildDir --parallel
if ($LASTEXITCODE -ne 0) { throw "build failed" }

& $Cmake --install $BuildDir
if ($LASTEXITCODE -ne 0) { throw "install failed" }

Move-Item "$OutputDir\lib\libpcre2-8.a" "$OutputDir\lib\libmaterialfile_pcre2.a" -Force

Write-Host "pcre2 OK -> $OutputDir" -ForegroundColor Green
