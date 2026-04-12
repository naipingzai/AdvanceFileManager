$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = (Resolve-Path "$ScriptDir\..").Path

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  Third-party Library Build-All Script" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

$failed = @()
$succeeded = @()

function Run-Build {
    param([string]$Category, [string]$Name)
    $script = "$ScriptDir\$Category\$Name\build.ps1"
    if (-not (Test-Path $script)) {
        Write-Host "SKIP: $Category/$Name (no build.ps1)" -ForegroundColor DarkGray
        return
    }
    Write-Host ""
    Write-Host ">>> Building $Category/$Name <<<" -ForegroundColor Cyan
    try {
        & powershell -NoProfile -ExecutionPolicy Bypass -File $script
        if ($LASTEXITCODE -ne 0) { throw "Non-zero exit code" }
        $script:succeeded += "$Category/$Name"
        Write-Host "<<< $Category/$Name SUCCESS >>>" -ForegroundColor Green
    } catch {
        $script:failed += "$Category/$Name"
        Write-Host "<<< $Category/$Name FAILED: $_ >>>" -ForegroundColor Red
    }
}

# ============================================================
# Phase 1: Native libraries (independent)
# ============================================================
Write-Host "Phase 1: Native libraries (independent)" -ForegroundColor Yellow
$nativeIndependent = @("bzip2", "xz", "lz4", "zstd", "mbedtls", "pcre2", "ffmpeg")
foreach ($lib in $nativeIndependent) {
    Run-Build "native" $lib
}

# Phase 2: Native libraries (dependent)
Write-Host ""
Write-Host "Phase 2: Native libraries (dependent)" -ForegroundColor Yellow
Run-Build "native" "libarchive"   # depends on bzip2, xz, lz4, zstd, mbedtls
Run-Build "native" "selinux"      # depends on pcre2

# ============================================================
# Phase 3: Android JAR modules (independent)
# ============================================================
Write-Host ""
Write-Host "Phase 3: Android JAR modules" -ForegroundColor Yellow
$jarModules = @("okio", "okhttp", "dav4jvm")
foreach ($lib in $jarModules) {
    Run-Build "android" $lib
}

# ============================================================
# Phase 4: Android AAR modules (no inter-module deps)
# ============================================================
Write-Host ""
Write-Host "Phase 4: Android AAR modules (independent)" -ForegroundColor Yellow
$aarIndependent = @(
    "advrecyclerview", "androidsvg", "coil-base", "drawer", "insetter",
    "libsu-core", "licensesdialog", "materialshadownp", "photoview",
    "preferencex", "shizuku-aidl", "shizuku-shared", "simplemenu",
    "speed-dial", "subsampling"
)
foreach ($lib in $aarIndependent) {
    Run-Build "android" $lib
}

# ============================================================
# Phase 5: Android AAR modules (with inter-module deps)
# ============================================================
Write-Host ""
Write-Host "Phase 5: Android AAR modules (dependent)" -ForegroundColor Yellow
$aarDependent = @(
    "coil",          # depends on coil-base
    "coil-gif",      # depends on coil-base
    "coil-svg",      # depends on coil-base, androidsvg
    "coil-video",    # depends on coil-base
    "insetter-ktx",  # depends on insetter
    "libsu-service", # depends on libsu-core
    "shizuku-api"    # depends on shizuku-shared, shizuku-aidl
)
foreach ($lib in $aarDependent) {
    Run-Build "android" $lib
}

# ============================================================
# Summary
# ============================================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  Build Summary" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Succeeded: $($succeeded.Count)" -ForegroundColor Green
foreach ($s in $succeeded) { Write-Host "  OK: $s" -ForegroundColor Green }
if ($failed.Count -gt 0) {
    Write-Host "Failed: $($failed.Count)" -ForegroundColor Red
    foreach ($f in $failed) { Write-Host "  FAIL: $f" -ForegroundColor Red }
    throw "$($failed.Count) build(s) failed"
} else {
    Write-Host "All builds succeeded!" -ForegroundColor Green
}
