# build.ps1 - ?? selinux (libselinux)
# ?? NDK clang ???????? CMake
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$SrcDir = "$LibDir\src\libselinux\src"
$IncludeDir = "$LibDir\src\libselinux\include"
$OutputDir = "$ProjectRoot\prebuild\native\selinux"
$PrebuildNative = "$ProjectRoot\prebuild\native"

# Download source if not present
if (-not (Test-Path "$LibDir\src\*")) {
    Write-Host "Downloading selinux 3.7..." -ForegroundColor Cyan
    $archive = "$env:TEMP\selinux.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://github.com/SELinuxProject/selinux/archive/refs/tags/3.7.tar.gz"
    if ($LASTEXITCODE -ne 0) { throw "Download failed" }
    New-Item -ItemType Directory -Force -Path "$LibDir\src" | Out-Null
    $ErrorActionPreference = "Continue"
    & tar xf $archive --strip-components=1 -C "$LibDir\src" 2>&1 | Out-Null
    $ErrorActionPreference = "Stop"
    if (-not (Test-Path "$LibDir\src\libselinux")) { throw "Extract failed" }
    Remove-Item $archive -Force
    Write-Host "Download complete." -ForegroundColor Green
}

$SdkDir = "$ProjectRoot\tools\android-sdk"
$NdkDir = (Get-ChildItem "$SdkDir\ndk" -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName

$MinApi = 21
$BuildDir = "$LibDir\build\$ABI"

# ?? ABI ?? target triple
switch ($ABI) {
    "arm64-v8a"   { $Target = "aarch64-linux-android$MinApi" }
    "armeabi-v7a" { $Target = "armv7a-linux-androideabi$MinApi" }
    "x86"         { $Target = "i686-linux-android$MinApi" }
    "x86_64"      { $Target = "x86_64-linux-android$MinApi" }
    default       { throw "Unsupported ABI: $ABI" }
}

# ?? toolchain bin ??
$ToolchainBin = "$NdkDir\toolchains\llvm\prebuilt\windows-x86_64\bin"
$CC = "$ToolchainBin\clang.exe"
$AR = "$ToolchainBin\llvm-ar.exe"

if ($Clean) {
    if (Test-Path $BuildDir) { Remove-Item $BuildDir -Recurse -Force }
    if (Test-Path $OutputDir) { Remove-Item $OutputDir -Recurse -Force }
}

New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null
New-Item -ItemType Directory -Force -Path "$OutputDir\lib", "$OutputDir\include\selinux" | Out-Null

# ????�?
$Sources = @(
    "avc.c", "avc_internal.c", "avc_sidtab.c", "booleans.c", "callbacks.c",
    "canonicalize_context.c", "checkAccess.c", "check_context.c",
    "compute_av.c", "compute_create.c", "compute_member.c", "context.c",
    "deny_unknown.c", "disable.c", "enabled.c",
    "fgetfilecon.c", "freecon.c", "fsetfilecon.c", "get_initial_context.c",
    "getenforce.c", "getfilecon.c", "getpeercon.c", "hashtab.c", "init.c",
    "label.c", "label_backends_android.c", "label_file.c", "label_support.c",
    "lgetfilecon.c", "load_policy.c", "lsetfilecon.c", "mapping.c",
    "matchpathcon.c", "policyvers.c", "procattr.c", "regex.c",
    "reject_unknown.c", "selinux_internal.c", "sestatus.c",
    "setenforce.c", "setfilecon.c", "setrans_client.c", "sha1.c", "stringrep.c"
)

# ????
$Defines = @(
    "-DNO_PERSISTENTLY_STORED_PATTERNS",
    "-DDISABLE_SETRANS",
    "-DDISABLE_BOOL",
    "-D_GNU_SOURCE",
    "-DNO_MEDIA_BACKEND",
    "-DNO_X_BACKEND",
    "-DNO_DB_BACKEND",
    "-DUSE_PCRE2",
    "-DPCRE2_CODE_UNIT_WIDTH=8",
    "-DAUDITD_LOG_TAG=1003"
)

$Includes = @(
    "-I$IncludeDir",
    "-I$SrcDir",
    "-I$PrebuildNative\pcre2\include"
)

$ExtraFlags = @(
    "-Wno-error=missing-noreturn",
    "-Wno-error=unused-function",
    "-Wno-error=unused-variable",
    "-Wno-error=unused-but-set-variable",
    "-D__fsetlocking(fp,type)=0"
)

$CommonFlags = @("--target=$Target", "-DANDROID", "-fPIC", "-O2") + $Defines + $Includes + $ExtraFlags

# ???? .c ??
$Objects = @()
foreach ($src in $Sources) {
    $srcPath = "$SrcDir\$src"
    $objName = [System.IO.Path]::GetFileNameWithoutExtension($src) + ".o"
    $objPath = "$BuildDir\$objName"
    $Objects += $objPath

    Write-Host "CC $src"
    & $CC -c $srcPath -o $objPath @CommonFlags
    if ($LASTEXITCODE -ne 0) { throw "Failed to compile $src" }
}

# ?????
Write-Host "AR libmaterialfile_selinux.a"
& $AR rcs "$BuildDir\libmaterialfile_selinux.a" @Objects
if ($LASTEXITCODE -ne 0) { throw "Failed to create archive" }

# ????
Copy-Item "$BuildDir\libmaterialfile_selinux.a" "$OutputDir\lib\libmaterialfile_selinux.a" -Force
Copy-Item "$IncludeDir\selinux\selinux.h" "$OutputDir\include\selinux\selinux.h" -Force

Write-Host "selinux OK -> $OutputDir" -ForegroundColor Green
