<#
.SYNOPSIS
    质感文件 (MaterialFile) 一体化构建脚本 (PowerShell)
.DESCRIPTION
    从工具下载到编译到清理，一站式完成。
    支持命令: setup, build, release, install, clean, full, help
.EXAMPLE
    .\build.ps1              # 默认: 编译 Debug
    .\build.ps1 setup        # 仅下载/安装工具
    .\build.ps1 build        # 编译 Debug APK
    .\build.ps1 release      # 编译 Release APK
    .\build.ps1 install      # 编译并安装到设备
    .\build.ps1 clean        # 清理构建产物
    .\build.ps1 full         # 完整流程: setup + clean + build + install
    .\build.ps1 help         # 显示帮助
.NOTES
    作者: naipingzai (奶瓶仔)
    项目: 质感文件 MaterialFile
#>

param(
    [Parameter(Position = 0)]
    [ValidateSet("setup", "build", "release", "install", "clean", "full", "help", "")]
    [string]$Command = "build"
)

# ============================================================================
# 配置区
# ============================================================================

$Script:Config = @{
    JdkVersion       = "17.0.12"
    JdkBuildNumber   = "7"
    JdkDirName       = "jdk-17.0.12"
    CompileSdk       = "36"
    BuildTools       = "36.0.0"
    NdkVersion       = "28.1.13356709"
    CmakeVersion     = "3.22.1"
    ToolsDir         = "tools"
    JdkDir           = "tools\jdk-17.0.12"
    SdkDir           = "tools\android-sdk"
    GitDir           = "tools\git"
    JdkUrl           = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.12_7.zip"
    SdkToolsUrl      = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
    GitUrl           = "https://github.com/git-for-windows/git/releases/download/v2.47.1.windows.1/MinGit-2.47.1-64-bit.zip"
}

# ============================================================================
# 全局变量
# ============================================================================

$Script:ProjectRoot = $PSScriptRoot
$Script:StepCount   = 0
$Script:TotalSteps  = 0

# ============================================================================
# 工具函数
# ============================================================================

function Write-Banner {
    param([string]$Text, [ConsoleColor]$Color = "Cyan")
    $line = "=" * 60
    Write-Host ""
    Write-Host $line -ForegroundColor $Color
    Write-Host "  $Text" -ForegroundColor $Color
    Write-Host $line -ForegroundColor $Color
    Write-Host ""
}

function Write-Step {
    param([string]$Text)
    $Script:StepCount++
    Write-Host "[$Script:StepCount/$Script:TotalSteps] $Text" -ForegroundColor Yellow
}

function Write-Ok {
    param([string]$Text)
    Write-Host "  [OK] $Text" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Text)
    Write-Host "  [!!] $Text" -ForegroundColor DarkYellow
}

function Write-Err {
    param([string]$Text)
    Write-Host "  [ERR] $Text" -ForegroundColor Red
}

function Write-Info {
    param([string]$Text)
    Write-Host "  $Text" -ForegroundColor Gray
}

function Get-FullPath {
    param([string]$RelPath)
    return Join-Path $Script:ProjectRoot $RelPath
}

function Set-EnvVars {
    $env:JAVA_HOME    = Get-FullPath $Config.JdkDir
    $env:ANDROID_HOME = Get-FullPath $Config.SdkDir
    $env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
    $paths = @(
        "$env:JAVA_HOME\bin",
        "$env:ANDROID_HOME\cmdline-tools\latest\bin",
        "$env:ANDROID_HOME\platform-tools"
    )
    $gitCmd = Get-FullPath "$($Config.GitDir)\cmd"
    if (Test-Path $gitCmd) { $paths += $gitCmd }
    foreach ($p in $paths) {
        if ($env:PATH -notlike "*$p*") {
            $env:PATH = "$p;$env:PATH"
        }
    }
}

function Download-File {
    param([string]$Url, [string]$OutFile)
    Write-Info "URL: $Url"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $Url -OutFile $OutFile -UseBasicParsing
        $ProgressPreference = 'Continue'
        return $true
    }
    catch {
        Write-Err "Download failed: $_"
        return $false
    }
}

function Extract-Zip {
    param([string]$ZipPath, [string]$DestDir)
    if (-not (Test-Path $DestDir)) {
        New-Item -ItemType Directory -Path $DestDir -Force | Out-Null
    }
    try {
        Expand-Archive -Path $ZipPath -DestinationPath $DestDir -Force
        return $true
    }
    catch {
        Write-Err "Extract failed: $_"
        return $false
    }
}

# ============================================================================
# Install functions
# ============================================================================

function Install-Jdk {
    $jdkPath = Get-FullPath $Config.JdkDir
    $javaBin = Join-Path $jdkPath "bin\java.exe"
    if (Test-Path $javaBin) {
        $ver = & $javaBin -version 2>&1 | Select-Object -First 1
        Write-Ok "JDK: $ver"
        return $true
    }
    Write-Info "JDK not found, downloading..."
    $toolsDir = Get-FullPath $Config.ToolsDir
    if (-not (Test-Path $toolsDir)) { New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null }
    $zipFile = Join-Path $toolsDir "jdk-download.zip"
    if (-not (Download-File $Config.JdkUrl $zipFile)) { return $false }
    $tempDir = Join-Path $toolsDir "_jdk_temp"
    if (Test-Path $tempDir) { Remove-Item -Recurse -Force $tempDir }
    if (-not (Extract-Zip $zipFile $tempDir)) { return $false }
    $innerDir = Get-ChildItem $tempDir -Directory | Select-Object -First 1
    if ($innerDir) {
        if (Test-Path $jdkPath) { Remove-Item -Recurse -Force $jdkPath }
        Move-Item $innerDir.FullName $jdkPath
    }
    Remove-Item -Recurse -Force $tempDir -ErrorAction SilentlyContinue
    Remove-Item $zipFile -ErrorAction SilentlyContinue
    if (Test-Path $javaBin) { Write-Ok "JDK installed"; return $true }
    else { Write-Err "JDK install failed"; return $false }
}

function Install-AndroidSdk {
    $sdkPath = Get-FullPath $Config.SdkDir
    $sdkmanager = Join-Path $sdkPath "cmdline-tools\latest\bin\sdkmanager.bat"
    if (-not (Test-Path $sdkmanager)) {
        Write-Info "SDK cmdline-tools not found, downloading..."
        $toolsDir = Get-FullPath $Config.ToolsDir
        $zipFile = Join-Path $toolsDir "sdk-tools-download.zip"
        if (-not (Download-File $Config.SdkToolsUrl $zipFile)) { return $false }
        $tempDir = Join-Path $toolsDir "_sdk_temp"
        if (Test-Path $tempDir) { Remove-Item -Recurse -Force $tempDir }
        if (-not (Extract-Zip $zipFile $tempDir)) { return $false }
        $destDir = Join-Path $sdkPath "cmdline-tools\latest"
        if (-not (Test-Path (Split-Path $destDir))) {
            New-Item -ItemType Directory -Path (Split-Path $destDir) -Force | Out-Null
        }
        $innerDir = Join-Path $tempDir "cmdline-tools"
        if (Test-Path $innerDir) {
            if (Test-Path $destDir) { Remove-Item -Recurse -Force $destDir }
            Move-Item $innerDir $destDir
        }
        Remove-Item -Recurse -Force $tempDir -ErrorAction SilentlyContinue
        Remove-Item $zipFile -ErrorAction SilentlyContinue
    }
    if (-not (Test-Path $sdkmanager)) { Write-Err "sdkmanager not found"; return $false }

    Write-Info "Accepting licenses..."
    $yesInput = "y`ny`ny`ny`ny`ny`ny`ny`ny`n"
    $yesInput | & $sdkmanager --licenses --sdk_root="$sdkPath" 2>&1 | Out-Null

    $sdkComponents = @{
        "platforms;android-$($Config.CompileSdk)" = "platforms\android-$($Config.CompileSdk)"
        "build-tools;$($Config.BuildTools)"       = "build-tools\$($Config.BuildTools)"
        "ndk;$($Config.NdkVersion)"               = "ndk\$($Config.NdkVersion)"
        "cmake;$($Config.CmakeVersion)"           = "cmake\$($Config.CmakeVersion)"
        "platform-tools"                           = "platform-tools\adb.exe"
    }

    foreach ($entry in $sdkComponents.GetEnumerator()) {
        $comp = $entry.Key
        $checkPath = Join-Path $sdkPath $entry.Value
        $compName = $comp -replace ';', '/'
        if (Test-Path $checkPath) {
            Write-Ok "$compName installed"
        }
        else {
            Write-Info "Installing $compName ..."
            & $sdkmanager $comp --sdk_root="$sdkPath" 2>&1 | Out-Null
            Write-Ok "$compName done"
        }
    }
    return $true
}

function Install-Git {
    $gitPath = Get-FullPath $Config.GitDir
    $gitExe = Join-Path $gitPath "cmd\git.exe"
    if (Test-Path $gitExe) {
        $ver = & $gitExe --version 2>&1
        Write-Ok "Git: $ver"
        return $true
    }
    Write-Info "Git not found, downloading..."
    $toolsDir = Get-FullPath $Config.ToolsDir
    $zipFile = Join-Path $toolsDir "git-download.zip"
    if (-not (Download-File $Config.GitUrl $zipFile)) { return $false }
    if (Test-Path $gitPath) { Remove-Item -Recurse -Force $gitPath }
    if (-not (Extract-Zip $zipFile $gitPath)) { return $false }
    Remove-Item $zipFile -ErrorAction SilentlyContinue
    if (Test-Path $gitExe) { Write-Ok "Git installed"; return $true }
    else { Write-Err "Git install failed"; return $false }
}

function Write-LocalProperties {
    $propsFile = Get-FullPath "local.properties"
    $sdkEscaped = (Get-FullPath $Config.SdkDir) -replace '\\', '\\\\'
    @"
sdk.dir=$sdkEscaped
"@ | Out-File -FilePath $propsFile -Encoding utf8 -Force -NoNewline
    Write-Ok "local.properties generated"
}

# ============================================================================
# Commands
# ============================================================================

function Invoke-Setup {
    $Script:TotalSteps = 5; $Script:StepCount = 0
    Write-Banner "Setup" "Cyan"
    Write-Step "Install JDK $($Config.JdkVersion)..."
    if (-not (Install-Jdk)) { Write-Err "JDK failed"; return $false }
    Set-EnvVars
    Write-Step "Install Android SDK..."
    if (-not (Install-AndroidSdk)) { Write-Err "SDK failed"; return $false }
    Write-Step "Install Git..."
    if (-not (Install-Git)) { Write-Warn "Git failed (optional)" }
    Write-Step "Generate local.properties..."
    Write-LocalProperties
    Write-Step "Verify..."
    $javaBin = Join-Path (Get-FullPath $Config.JdkDir) "bin\java.exe"
    $adb = Join-Path (Get-FullPath $Config.SdkDir) "platform-tools\adb.exe"
    if (Test-Path $javaBin) { Write-Ok "java: $javaBin" } else { Write-Err "java missing"; return $false }
    if (Test-Path $adb) { Write-Ok "adb: $adb" } else { Write-Warn "adb missing" }
    Write-Banner "Setup Complete" "Green"
    return $true
}

function Invoke-Build {
    param([string]$BuildType = "Debug")
    Set-EnvVars
    $Script:TotalSteps = 3; $Script:StepCount = 0
    $taskName = "assemble$BuildType"
    Write-Banner "Build $BuildType" "Cyan"
    Write-Step "Check environment..."
    $javaBin = Join-Path (Get-FullPath $Config.JdkDir) "bin\java.exe"
    if (-not (Test-Path $javaBin)) { Write-Err "JDK missing. Run: .\build.ps1 setup"; return $false }
    Write-Ok "Ready"
    Write-Step "Gradle $taskName ..."
    Write-Info "This may take a few minutes..."
    Write-Host ""
    Push-Location $Script:ProjectRoot
    try { & .\gradlew.bat $taskName --no-daemon 2>&1 | ForEach-Object { Write-Host $_ }; $buildResult = $LASTEXITCODE }
    finally { Pop-Location }
    Write-Host ""
    Write-Step "Check result..."
    if ($buildResult -eq 0) {
        $apkDir = if ($BuildType -eq "Debug") { Get-FullPath "app\build\outputs\apk\debug" } else { Get-FullPath "app\build\outputs\apk\release" }
        if (Test-Path $apkDir) {
            Write-Banner "BUILD SUCCESS" "Green"
            Get-ChildItem $apkDir -Filter "*.apk" | ForEach-Object {
                $sizeMB = [math]::Round($_.Length / 1MB, 2)
                Write-Ok "$($_.Name)  ${sizeMB}MB"
                Write-Info "Path: $($_.FullName)"
            }
        }
        return $true
    }
    else { Write-Banner "BUILD FAILED" "Red"; Write-Err "Check errors above"; return $false }
}

function Invoke-Install {
    Set-EnvVars
    if (-not (Invoke-Build "Debug")) { return $false }
    Write-Host ""
    Write-Banner "Install to device" "Cyan"
    $adb = Join-Path (Get-FullPath $Config.SdkDir) "platform-tools\adb.exe"
    if (-not (Test-Path $adb)) { Write-Err "adb missing. Run: .\build.ps1 setup"; return $false }
    $apkFile = Get-FullPath "app\build\outputs\apk\debug\app-debug.apk"
    if (-not (Test-Path $apkFile)) { Write-Err "APK not found"; return $false }
    Write-Info "Installing..."
    & $adb install -r $apkFile 2>&1 | ForEach-Object { Write-Host "  $_" }
    if ($LASTEXITCODE -eq 0) { Write-Ok "Install success!"; return $true }
    else { Write-Err "Install failed"; return $false }
}

function Invoke-Clean {
    Set-EnvVars
    Write-Banner "Clean" "Cyan"
    Write-Info "Gradle clean..."
    Push-Location $Script:ProjectRoot
    try { & .\gradlew.bat clean --no-daemon 2>&1 | ForEach-Object { Write-Host $_ } }
    finally { Pop-Location }
    foreach ($d in @("app\build", "app\.cxx", "build", ".gradle", ".kotlin")) {
        $p = Get-FullPath $d
        if (Test-Path $p) { Remove-Item -Recurse -Force $p -ErrorAction SilentlyContinue; Write-Ok "Deleted: $d" }
    }
    foreach ($f in @("build_err.txt", "build_err2.txt", "build_output.txt")) {
        $p = Get-FullPath $f
        if (Test-Path $p) { Remove-Item -Force $p -ErrorAction SilentlyContinue; Write-Ok "Deleted: $f" }
    }
    Write-Banner "Clean Complete" "Green"
    return $true
}

function Invoke-Full {
    Write-Banner "Full Pipeline" "Magenta"
    Write-Info "setup -> clean -> build -> install"
    if (-not (Invoke-Setup)) { return $false }
    Invoke-Clean | Out-Null
    if (-not (Invoke-Install)) { return $false }
    Write-Banner "All Done!" "Green"
    return $true
}

function Show-Help {
    Write-Banner "MaterialFile Build Script" "Cyan"
    $helpText = @"
  Usage: .\build.ps1 [command]

  Commands:
    setup     Download and install build tools (JDK, SDK, Git)
    build     Build Debug APK (default)
    release   Build Release APK (requires signing config)
    install   Build Debug and install to connected device
    clean     Clean all build artifacts
    full      Full pipeline: setup + clean + build + install
    help      Show this help

  Tool Versions:
    JDK:         $($Config.JdkVersion)
    Gradle:      8.13 (wrapper)
    AGP:         8.11.1
    Kotlin:      2.1.21
    CompileSDK:  $($Config.CompileSdk)
    BuildTools:  $($Config.BuildTools)
    NDK:         $($Config.NdkVersion)
    CMake:       $($Config.CmakeVersion)

  Examples:
    .\build.ps1                Build Debug
    .\build.ps1 full           Fresh environment one-step
    .\build.ps1 clean          Clean outputs
"@
    Write-Host $helpText -ForegroundColor White
}

# ============================================================================
# Entry
# ============================================================================

Write-Host ""
Write-Host "  MaterialFile" -ForegroundColor Magenta
Write-Host "  by naipingzai" -ForegroundColor DarkGray
Write-Host ""

$exitCode = 0
switch ($Command) {
    "setup"   { if (-not (Invoke-Setup))          { $exitCode = 1 } }
    "build"   { if (-not (Invoke-Build "Debug"))  { $exitCode = 1 } }
    "release" { if (-not (Invoke-Build "Release")){ $exitCode = 1 } }
    "install" { if (-not (Invoke-Install))        { $exitCode = 1 } }
    "clean"   { if (-not (Invoke-Clean))          { $exitCode = 1 } }
    "full"    { if (-not (Invoke-Full))           { $exitCode = 1 } }
    "help"    { Show-Help }
    default   { if (-not (Invoke-Build "Debug"))  { $exitCode = 1 } }
}
exit $exitCode
