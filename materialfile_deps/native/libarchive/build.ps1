# build.ps1 - ?? libarchive
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src"
$OutputDir = "$ProjectRoot\prebuild\native\libarchive"
$PrebuildNative = "$ProjectRoot\prebuild\native"

# Download source if not present
if (-not (Test-Path "$LibDir\src\*")) {
    Write-Host "Downloading libarchive 3.7.7..." -ForegroundColor Cyan
    $archive = "$env:TEMP\libarchive.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://github.com/libarchive/libarchive/archive/refs/tags/v3.7.7.tar.gz"
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
    # ???�?
    "-DBUILD_SHARED_LIBS=OFF",
    "-DENABLE_TEST=OFF",
    "-DENABLE_TAR=OFF",
    "-DENABLE_CPIO=OFF",
    "-DENABLE_CAT=OFF",
    "-DENABLE_UNZIP=OFF",
    "-DENABLE_XATTR=OFF",
    "-DENABLE_ACL=OFF",
    "-DENABLE_ICONV=OFF",
    "-DENABLE_EXPAT=OFF",
    "-DENABLE_LIBXML2=OFF",
    "-DENABLE_OPENSSL=OFF",
    "-DENABLE_LIBB2=OFF",
    # ??????
    "-DENABLE_LZ4=ON",
    "-DENABLE_LZMA=ON",
    "-DENABLE_ZSTD=ON",
    "-DENABLE_ZLIB=ON",
    "-DENABLE_BZip2=ON",
    "-DENABLE_MBEDTLS=ON",
    # bzip2 ????
    "-DBZIP2_INCLUDE_DIR=$PrebuildNative\bzip2\include",
    "-DBZIP2_LIBRARIES=$PrebuildNative\bzip2\lib\libmaterialfile_bz2.a",
    "-DHAVE_LIBBZ2:BOOL=TRUE",
    # xz (liblzma) ????
    "-DLIBLZMA_INCLUDE_DIR=$PrebuildNative\xz\include",
    "-DLIBLZMA_LIBRARY=$PrebuildNative\xz\lib\libmaterialfile_lzma.a",
    "-DLIBLZMA_HAS_AUTO_DECODER:BOOL=TRUE",
    "-DLIBLZMA_HAS_EASY_ENCODER:BOOL=TRUE",
    "-DLIBLZMA_HAS_LZMA_PRESET:BOOL=TRUE",
    "-DHAVE_LIBLZMA:BOOL=TRUE",
    # lz4 ????
    "-DLZ4_INCLUDE_DIR=$PrebuildNative\lz4\include",
    "-DLZ4_LIBRARY=$PrebuildNative\lz4\lib\libmaterialfile_lz4.a",
    "-DHAVE_LIBLZ4:BOOL=TRUE",
    # zstd ????
    "-DZSTD_INCLUDE_DIR=$PrebuildNative\zstd\include",
    "-DZSTD_LIBRARY=$PrebuildNative\zstd\lib\libmaterialfile_zstd.a",
    "-DHAVE_ZSTD:BOOL=TRUE",
    # mbedtls ????
    "-DMBEDTLS_INCLUDE_DIRS=$PrebuildNative\mbedtls\include",
    "-DMBEDTLS_LIBRARY=$PrebuildNative\mbedtls\lib\libmbedtls.a",
    "-DMBEDX509_LIBRARY=$PrebuildNative\mbedtls\lib\libmbedx509.a",
    "-DMBEDCRYPTO_LIBRARY=$PrebuildNative\mbedtls\lib\libmaterialfile_mbedcrypto.a",
    "-B", $BuildDir,
    "-S", "$SrcDir"
)

& $Cmake @cmakeArgs
if ($LASTEXITCODE -ne 0) { throw "cmake configure failed" }

& $Cmake --build $BuildDir --parallel
if ($LASTEXITCODE -ne 0) { throw "build failed" }

& $Cmake --install $BuildDir
if ($LASTEXITCODE -ne 0) { throw "install failed" }

Move-Item "$OutputDir\lib\libarchive.a" "$OutputDir\lib\libmaterialfile_archive.a" -Force

Write-Host "libarchive OK -> $OutputDir" -ForegroundColor Green
