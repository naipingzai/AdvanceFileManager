# build.ps1 - 编译 bzip2
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"
$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src"
$OutputDir = "$ProjectRoot\prebuild\native\bzip2"

# Download source if not present
if (-not (Test-Path "$SrcDir\*")) {
    Write-Host "Downloading bzip2 1.0.8..." -ForegroundColor Cyan
    $archive = "$env:TEMP\bzip2.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://sourceware.org/pub/bzip2/bzip2-1.0.8.tar.gz"
    if ($LASTEXITCODE -ne 0) { throw "Download failed" }
    New-Item -ItemType Directory -Force -Path $SrcDir | Out-Null
    & tar xf $archive --strip-components=1 -C $SrcDir
    if ($LASTEXITCODE -ne 0) { throw "Extract failed" }
    Remove-Item $archive -Force
    Write-Host "Download complete." -ForegroundColor Green
}
$SdkDir = "$ProjectRoot\tools\android-sdk"
$NdkDir = (Get-ChildItem "$SdkDir\ndk" -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
$NdkBin = "$NdkDir\toolchains\llvm\prebuilt\windows-x86_64\bin"
$Clang = "$NdkBin\clang.exe"; $Ar = "$NdkBin\llvm-ar.exe"
$MinApi = 21
$Target = switch ($ABI) { "arm64-v8a" { "aarch64-linux-android$MinApi" } "armeabi-v7a" { "armv7a-linux-androideabi$MinApi" } "x86_64" { "x86_64-linux-android$MinApi" } "x86" { "i686-linux-android$MinApi" } }
$BuildDir = "$LibDir\build\$ABI"
if ($Clean) { if(Test-Path $BuildDir){Remove-Item $BuildDir -Recurse -Force}; if(Test-Path $OutputDir){Remove-Item $OutputDir -Recurse -Force} }
New-Item -ItemType Directory -Force -Path "$OutputDir\lib","$OutputDir\include",$BuildDir | Out-Null
$cflags = @("--target=$Target","-DANDROID","-O2","-fPIC","-fdata-sections","-ffunction-sections","-I$SrcDir","-DUSE_MMAP","-Wno-unused-parameter")
$objs = @()
foreach($f in "blocksort","huffman","crctable","randtable","compress","decompress","bzlib") {
    $obj = "$BuildDir\$f.o"
    & $Clang @cflags -c "$SrcDir\$f.c" -o $obj; if($LASTEXITCODE -ne 0){throw "compile $f failed"}
    $objs += $obj
}
& $Ar rcs "$BuildDir\libmaterialfile_bz2.a" @objs; if($LASTEXITCODE -ne 0){throw "archive failed"}
Copy-Item "$BuildDir\libmaterialfile_bz2.a" "$OutputDir\lib\" -Force
Copy-Item "$SrcDir\bzlib.h" "$OutputDir\include\" -Force
Write-Host "bzip2 OK -> $OutputDir" -ForegroundColor Green