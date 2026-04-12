# build.ps1 - 编译 FFmpeg (精简版，仅保留格式转换所需组件)
# 使用 NDK 交叉编译，输出静态库�?prebuild/native/ffmpeg
param([string]$ABI = "arm64-v8a", [switch]$Clean)
$ErrorActionPreference = "Stop"

$LibDir = $PSScriptRoot
$SrcDir = "$LibDir\src"
$ProjectRoot = Resolve-Path "$LibDir\..\..\..\"
$OutputDir = "$ProjectRoot\prebuild\native\ffmpeg"

# Download source if not present
if (-not (Test-Path "$LibDir\src\*")) {
    Write-Host "Downloading ffmpeg 7.1..." -ForegroundColor Cyan
    $archive = "$env:TEMP\ffmpeg.tar.gz"
    & curl.exe -L --progress-bar -o $archive "https://github.com/FFmpeg/FFmpeg/archive/refs/tags/n7.1.tar.gz"
    if ($LASTEXITCODE -ne 0) { throw "Download failed" }
    New-Item -ItemType Directory -Force -Path "$LibDir\src" | Out-Null
    & tar xf $archive --strip-components=1 -C "$LibDir\src"
    if ($LASTEXITCODE -ne 0) { throw "Extract failed" }
    Remove-Item $archive -Force
    Write-Host "Download complete." -ForegroundColor Green
}

$SdkDir = "$ProjectRoot\tools\android-sdk"
$NdkDir = (Get-ChildItem "$SdkDir\ndk" -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
$ToolchainBin = "$NdkDir\toolchains\llvm\prebuilt\windows-x86_64\bin"
$Sysroot = "$NdkDir\toolchains\llvm\prebuilt\windows-x86_64\sysroot"
$Make = "$NdkDir\prebuilt\windows-x86_64\bin\make.exe"
$Shell = "$ProjectRoot\tools\git\usr\bin\sh.exe"
$MsysBin = "$ProjectRoot\tools\git\usr\bin"

$MinApi = 21

switch ($ABI) {
    "arm64-v8a" {
        $Arch = "aarch64"; $Cpu = "armv8-a"
        $CrossPrefix = "aarch64-linux-android"
    }
    "armeabi-v7a" {
        $Arch = "arm"; $Cpu = "armv7-a"
        $CrossPrefix = "armv7a-linux-androideabi"
    }
    "x86_64" {
        $Arch = "x86_64"; $Cpu = "x86-64"
        $CrossPrefix = "x86_64-linux-android"
    }
    "x86" {
        $Arch = "x86"; $Cpu = "i686"
        $CrossPrefix = "i686-linux-android"
    }
    default { throw "Unsupported ABI: $ABI" }
}

$CC = "$ToolchainBin\${CrossPrefix}${MinApi}-clang.cmd"
$CXX = "$ToolchainBin\${CrossPrefix}${MinApi}-clang++.cmd"
$BuildDir = "$LibDir\build\$ABI"

if ($Clean) {
    if (Test-Path $BuildDir) { Remove-Item $BuildDir -Recurse -Force }
    if (Test-Path $OutputDir) { Remove-Item $OutputDir -Recurse -Force }
}

New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null
New-Item -ItemType Directory -Force -Path "$OutputDir\lib", "$OutputDir\include" | Out-Null

function ConvertTo-MsysPath($WinPath) {
    $resolved = $WinPath
    if (Test-Path $WinPath) { $resolved = (Resolve-Path $WinPath).Path }
    $p = $resolved.Replace('\', '/')
    if ($p -match '^([A-Za-z]):(.*)') { return "/$($Matches[1].ToLower())$($Matches[2])" }
    return $p
}

$m = @{
    src      = ConvertTo-MsysPath $SrcDir
    build    = ConvertTo-MsysPath $BuildDir
    output   = ConvertTo-MsysPath $OutputDir
    cc       = ConvertTo-MsysPath $CC
    cxx      = ConvertTo-MsysPath $CXX
    sysroot  = ConvertTo-MsysPath $Sysroot
    tcbin    = ConvertTo-MsysPath $ToolchainBin
    make     = ConvertTo-MsysPath $Make
    msysbin  = ConvertTo-MsysPath $MsysBin
}

# Write shell scripts using .NET to avoid BOM
function Write-ShellScript($path, $lines) {
    $content = ($lines -join "`n") + "`n"
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($path, $content, $utf8NoBom)
}

# Compute MSYS/Windows path prefixes for post-configure path fixup
$msysPrefix = ConvertTo-MsysPath $ProjectRoot
$winPrefix = ($ProjectRoot.Path).Replace('\', '/')

$envSetup = "export PATH=`"$($m.msysbin)`":`"$($m.tcbin)`":`$PATH"

$configureLines = @(
    '#!/bin/sh'
    'set -e'
    "cd `"$($m.build)`""
    $envSetup
    "`"$($m.src)/configure`" \"
    "    --prefix=`"$($m.output)`" \"
    '    --target-os=android \'
    "    --arch=$Arch \"
    "    --cpu=$Cpu \"
    "    --cc=`"$($m.cc)`" \"
    "    --cxx=`"$($m.cxx)`" \"
    "    --sysroot=`"$($m.sysroot)`" \"
    "    --cross-prefix=`"$($m.tcbin)/${CrossPrefix}-`" \"
    "    --nm=`"$($m.tcbin)/llvm-nm`" \"
    "    --ar=`"$($m.tcbin)/llvm-ar`" \"
    "    --ranlib=`"$($m.tcbin)/llvm-ranlib`" \"
    "    --strip=`"$($m.tcbin)/llvm-strip`" \"
    "    --host-cc=`"$($m.cc)`" \"
    '    --enable-cross-compile \'
    '    --enable-pic \'
    '    --enable-small \'
    '    --enable-static \'
    '    --disable-shared \'
    '    --disable-programs \'
    '    --disable-doc \'
    '    --disable-htmlpages \'
    '    --disable-manpages \'
    '    --disable-podpages \'
    '    --disable-txtpages \'
    '    --disable-avdevice \'
    '    --disable-postproc \'
    '    --disable-network \'
    '    --disable-debug \'
    '    --disable-symver \'
    '    --disable-asm \'
    '    --disable-vulkan \'
    '    --disable-v4l2-m2m \'
    '    --disable-everything \'
    '    --enable-jni \'
    '    --enable-mediacodec \'
    '    --enable-decoder=aac,mp3,flac,opus,vorbis,pcm_s16le,pcm_s24le,pcm_s32le,pcm_f32le \'
    '    --enable-decoder=h264,hevc,vp8,vp9,av1,mpeg4,mjpeg \'
    '    --enable-decoder=h264_mediacodec,hevc_mediacodec,vp8_mediacodec,vp9_mediacodec,av1_mediacodec \'
    '    --enable-decoder=png,mjpeg,bmp,webp,gif,tiff \'
    '    --enable-decoder=flv,vp6,vp6f,vp6a \'
    '    --enable-decoder=wmv1,wmv2,wmv3,wmav1,wmav2 \'
    '    --enable-decoder=mpeg1video,mpeg2video,mpegvideo \'
    '    --enable-decoder=rv10,rv20,rv30,rv40,cook,ra_288 \'
    '    --enable-encoder=aac,opus,pcm_s16le,flac \'
    '    --enable-encoder=png,mjpeg,gif \'
    '    --enable-encoder=h264_mediacodec,hevc_mediacodec \'
    '    --enable-encoder=vp8_mediacodec,vp9_mediacodec \'
    '    --enable-encoder=mpeg4 \'
    '    --enable-demuxer=aac,mp3,flac,ogg,wav,mov,matroska,avi,mpegts,concat \'
    '    --enable-demuxer=flv,asf,mpegps,mpegvideo,rm \'
    '    --enable-demuxer=image2,image_png_pipe,image_jpeg_pipe,image_webp_pipe,image_bmp_pipe,gif \'
    '    --enable-muxer=mp4,ipod,adts,mp3,flac,ogg,wav,matroska,webm,avi,mpegts,mov,image2,webp,png,mjpeg,gif \'
    '    --enable-parser=aac,h264,hevc,vp8,vp9,av1,mpegaudio,opus,flac,png,mjpeg,bmp,webp \'
    '    --enable-parser=mpeg4video,mpegvideo \'
    '    --enable-protocol=file \'
    '    --enable-filter=aresample,scale,format,aformat,anull,null,unsharp \'
    '    --enable-bsf=aac_adtstoasc,h264_mp4toannexb,hevc_mp4toannexb,h264_metadata,hevc_metadata,extract_extradata \'
    '    --enable-swresample \'
    '    --enable-swscale \'
    '    --extra-cflags="-Os -fPIC -DANDROID" \'
    '    --extra-ldflags="-lm -llog"'
    ''
    '# Fix MSYS paths to Windows paths for NDK make.exe'
    "sed -i 's|${msysPrefix}|${winPrefix}|g' config.mak ffbuild/config.mak Makefile 2>/dev/null || true"
    "sed -i 's|${msysPrefix}|${winPrefix}|g' libavcodec/Makefile libavformat/Makefile libavutil/Makefile libavfilter/Makefile libswresample/Makefile libswscale/Makefile 2>/dev/null || true"
    'echo "Configure OK"'
)

$buildLines = @(
    '#!/bin/sh'
    'set -e'
    "cd `"$($m.build)`""
    $envSetup
    "`"$($m.make)`" -j4"
    'echo "Build OK"'
)

$installLines = @(
    '#!/bin/sh'
    'set -e'
    "cd `"$($m.build)`""
    $envSetup
    "`"$($m.make)`" install"
    'echo "Install OK"'
)

Write-ShellScript "$BuildDir\do_configure.sh" $configureLines
Write-ShellScript "$BuildDir\do_build.sh" $buildLines
Write-ShellScript "$BuildDir\do_install.sh" $installLines

Write-Host "=== FFmpeg Configure ($ABI) ===" -ForegroundColor Cyan
& $Shell "$BuildDir\do_configure.sh"
if ($LASTEXITCODE -ne 0) { throw "FFmpeg configure failed" }

Write-Host "=== FFmpeg Build ($ABI) ===" -ForegroundColor Cyan
& $Shell "$BuildDir\do_build.sh"
if ($LASTEXITCODE -ne 0) { throw "FFmpeg build failed" }

Write-Host "=== FFmpeg Install ($ABI) ===" -ForegroundColor Cyan
& $Shell "$BuildDir\do_install.sh"
if ($LASTEXITCODE -ne 0) { throw "FFmpeg install failed" }

# Workaround: make install may occasionally skip libavutil headers.
# Ensure all public headers are present by copying from source + build dirs.
$avutilIncDir = "$OutputDir\include\libavutil"
if ((Get-ChildItem $avutilIncDir -ErrorAction SilentlyContinue).Count -lt 10) {
    Write-Host "  [fix] Copying missing libavutil headers..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $avutilIncDir | Out-Null
    Get-ChildItem "$SrcDir\libavutil\*.h" | Where-Object {
        # Only copy public headers (skip internal ones with underscore prefix)
        $_.Name -notmatch '^_'
    } | ForEach-Object { Copy-Item $_.FullName "$avutilIncDir\$($_.Name)" -Force }
    # Also copy build-generated headers (avconfig.h, ffversion.h)
    @("avconfig.h", "ffversion.h") | ForEach-Object {
        $genHdr = "$BuildDir\libavutil\$_"
        if (Test-Path $genHdr) { Copy-Item $genHdr "$avutilIncDir\$_" -Force }
    }
    Write-Host "  [fix] libavutil headers: $((Get-ChildItem $avutilIncDir).Count) files" -ForegroundColor Green
}

Move-Item "$OutputDir\lib\libavcodec.a" "$OutputDir\lib\libmaterialfile_avcodec.a" -Force
Move-Item "$OutputDir\lib\libavformat.a" "$OutputDir\lib\libmaterialfile_avformat.a" -Force
Move-Item "$OutputDir\lib\libavutil.a" "$OutputDir\lib\libmaterialfile_avutil.a" -Force
Move-Item "$OutputDir\lib\libavfilter.a" "$OutputDir\lib\libmaterialfile_avfilter.a" -Force
Move-Item "$OutputDir\lib\libswresample.a" "$OutputDir\lib\libmaterialfile_swresample.a" -Force
Move-Item "$OutputDir\lib\libswscale.a" "$OutputDir\lib\libmaterialfile_swscale.a" -Force

Write-Host "FFmpeg OK -> $OutputDir" -ForegroundColor Green
