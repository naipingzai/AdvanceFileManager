#!/usr/bin/env bash
# ============================================================================
# 质感文件 (MaterialFile) 一体化构建脚本 (Bash)
# ============================================================================
#
# 用法:
#   ./build.sh              # 默认: 编译 Debug
#   ./build.sh setup        # 仅下载/安装工具
#   ./build.sh build        # 编译 Debug APK
#   ./build.sh release      # 编译 Release APK
#   ./build.sh install      # 编译并安装到设备
#   ./build.sh clean        # 清理构建产物
#   ./build.sh full         # 完整流程: setup + clean + build + install
#   ./build.sh help         # 显示帮助
#
# 作者: naipingzai (奶瓶仔)
# 项目: 质感文件 MaterialFile
# ============================================================================

set -euo pipefail

# ============================================================================
# 配置区 (根据需要修改)
# ============================================================================

# JDK
JDK_VERSION="17.0.12"
JDK_BUILD="7"
JDK_DIR_NAME="jdk-17.0.12"

# Android SDK 组件
COMPILE_SDK="36"
BUILD_TOOLS="36.0.0"
NDK_VERSION="28.1.13356709"
CMAKE_VERSION="3.22.1"

# 路径 (相对于项目根目录)
TOOLS_DIR="tools"
JDK_DIR="tools/${JDK_DIR_NAME}"
SDK_DIR="tools/android-sdk"
GIT_DIR="tools/git"

# ============================================================================
# 全局变量
# ============================================================================

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
STEP_COUNT=0
TOTAL_STEPS=0

# 自动检测操作系统和架构
detect_platform() {
    local os arch
    os="$(uname -s)"
    arch="$(uname -m)"

    case "$os" in
        Linux*)   OS_TYPE="linux" ;;
        Darwin*)  OS_TYPE="mac" ;;
        CYGWIN*|MINGW*|MSYS*) OS_TYPE="windows" ;;
        *)        OS_TYPE="linux" ;;
    esac

    case "$arch" in
        x86_64|amd64)  ARCH_TYPE="x64" ;;
        aarch64|arm64) ARCH_TYPE="aarch64" ;;
        *)             ARCH_TYPE="x64" ;;
    esac
}

# 根据平台生成下载 URL
get_jdk_url() {
    local os_name arch_name
    case "$OS_TYPE" in
        linux)   os_name="linux" ;;
        mac)     os_name="mac" ;;
        windows) os_name="windows" ;;
    esac
    arch_name="$ARCH_TYPE"
    # macOS aarch64
    if [[ "$OS_TYPE" == "mac" && "$ARCH_TYPE" == "aarch64" ]]; then
        arch_name="aarch64"
    fi
    echo "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VERSION}%2B${JDK_BUILD}/OpenJDK17U-jdk_${arch_name}_${os_name}_hotspot_${JDK_VERSION}_${JDK_BUILD}.tar.gz"
}

get_sdk_tools_url() {
    case "$OS_TYPE" in
        linux)   echo "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" ;;
        mac)     echo "https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip" ;;
        windows) echo "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" ;;
    esac
}

# ============================================================================
# 工具函数
# ============================================================================

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

print_banner() {
    local text="$1"
    local color="${2:-$CYAN}"
    echo ""
    echo -e "${color}============================================================${NC}"
    echo -e "${color}  ${text}${NC}"
    echo -e "${color}============================================================${NC}"
    echo ""
}

print_step() {
    STEP_COUNT=$((STEP_COUNT + 1))
    echo -e "${YELLOW}[${STEP_COUNT}/${TOTAL_STEPS}] $1${NC}"
}

print_ok() {
    echo -e "  ${GREEN}[OK] $1${NC}"
}

print_warn() {
    echo -e "  ${YELLOW}[!!] $1${NC}"
}

print_err() {
    echo -e "  ${RED}[ERR] $1${NC}"
}

print_info() {
    echo -e "  ${GRAY}$1${NC}"
}

get_full_path() {
    echo "${PROJECT_ROOT}/$1"
}

set_env_vars() {
    export JAVA_HOME="$(get_full_path "$JDK_DIR")"
    export ANDROID_HOME="$(get_full_path "$SDK_DIR")"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"

    # 将工具加入 PATH (不重复)
    local paths=(
        "$JAVA_HOME/bin"
        "$ANDROID_HOME/cmdline-tools/latest/bin"
        "$ANDROID_HOME/platform-tools"
    )
    for p in "${paths[@]}"; do
        case ":$PATH:" in
            *":$p:"*) ;;
            *) export PATH="$p:$PATH" ;;
        esac
    done
}

# 下载文件
download_file() {
    local url="$1" outfile="$2"
    print_info "下载: $url"
    print_info "保存: $outfile"

    if command -v curl &>/dev/null; then
        curl -fSL --progress-bar -o "$outfile" "$url"
    elif command -v wget &>/dev/null; then
        wget -q --show-progress -O "$outfile" "$url"
    else
        print_err "需要 curl 或 wget"
        return 1
    fi
}

# 检查命令是否存在
require_cmd() {
    if ! command -v "$1" &>/dev/null; then
        print_err "需要安装 $1"
        return 1
    fi
}

# ============================================================================
# 核心功能
# ============================================================================

install_jdk() {
    local jdk_path="$(get_full_path "$JDK_DIR")"
    local java_bin="$jdk_path/bin/java"

    if [[ -x "$java_bin" ]]; then
        local ver
        ver=$("$java_bin" -version 2>&1 | head -1)
        print_ok "JDK 已安装: $ver"
        return 0
    fi

    print_info "JDK 未找到，开始下载安装..."
    local tools_dir="$(get_full_path "$TOOLS_DIR")"
    mkdir -p "$tools_dir"

    local jdk_url
    jdk_url="$(get_jdk_url)"
    local tarball="$tools_dir/jdk-download.tar.gz"

    download_file "$jdk_url" "$tarball" || return 1

    print_info "解压: $tarball -> $tools_dir"
    local temp_dir="$tools_dir/_jdk_temp"
    rm -rf "$temp_dir"
    mkdir -p "$temp_dir"
    tar xzf "$tarball" -C "$temp_dir"

    # Adoptium tar.gz 内有一层目录 jdk-17.0.12+7
    local inner_dir
    inner_dir=$(find "$temp_dir" -maxdepth 1 -mindepth 1 -type d | head -1)
    if [[ -n "$inner_dir" ]]; then
        rm -rf "$jdk_path"
        mv "$inner_dir" "$jdk_path"
    fi
    rm -rf "$temp_dir" "$tarball"

    if [[ -x "$java_bin" ]]; then
        print_ok "JDK 安装完成"
        return 0
    else
        print_err "JDK 安装失败"
        return 1
    fi
}

install_android_sdk() {
    local sdk_path="$(get_full_path "$SDK_DIR")"
    local sdkmanager="$sdk_path/cmdline-tools/latest/bin/sdkmanager"

    # 1. 安装 cmdline-tools
    if [[ ! -f "$sdkmanager" ]]; then
        print_info "Android SDK cmdline-tools 未找到，开始下载安装..."
        local tools_dir="$(get_full_path "$TOOLS_DIR")"
        local sdk_url
        sdk_url="$(get_sdk_tools_url)"
        local zipfile="$tools_dir/sdk-tools-download.zip"

        require_cmd unzip || return 1
        download_file "$sdk_url" "$zipfile" || return 1

        local temp_dir="$tools_dir/_sdk_temp"
        rm -rf "$temp_dir"
        mkdir -p "$temp_dir"
        unzip -q "$zipfile" -d "$temp_dir"

        # 移动到正确位置
        local dest_dir="$sdk_path/cmdline-tools/latest"
        mkdir -p "$(dirname "$dest_dir")"
        if [[ -d "$temp_dir/cmdline-tools" ]]; then
            rm -rf "$dest_dir"
            mv "$temp_dir/cmdline-tools" "$dest_dir"
        fi
        rm -rf "$temp_dir" "$zipfile"

        # 确保可执行
        chmod +x "$sdkmanager" 2>/dev/null || true
    fi

    if [[ ! -f "$sdkmanager" ]]; then
        print_err "sdkmanager 未找到"
        return 1
    fi

    # 2. 接受许可证
    print_info "接受 Android SDK 许可证..."
    yes | "$sdkmanager" --licenses --sdk_root="$sdk_path" >/dev/null 2>&1 || true

    # 3. 安装必需的 SDK 组件
    local components=(
        "platforms;android-${COMPILE_SDK}"
        "build-tools;${BUILD_TOOLS}"
        "ndk;${NDK_VERSION}"
        "cmake;${CMAKE_VERSION}"
        "platform-tools"
    )

    for comp in "${components[@]}"; do
        local comp_name="${comp//;/\/}"
        local installed=false

        case "$comp" in
            platforms*)     [[ -d "$sdk_path/platforms/android-${COMPILE_SDK}" ]] && installed=true ;;
            build-tools*)   [[ -d "$sdk_path/build-tools/${BUILD_TOOLS}" ]] && installed=true ;;
            ndk*)           [[ -d "$sdk_path/ndk/${NDK_VERSION}" ]] && installed=true ;;
            cmake*)         [[ -d "$sdk_path/cmake/${CMAKE_VERSION}" ]] && installed=true ;;
            platform-tools) [[ -f "$sdk_path/platform-tools/adb" ]] && installed=true ;;
        esac

        if $installed; then
            print_ok "$comp_name 已安装"
        else
            print_info "安装 $comp_name ..."
            "$sdkmanager" "$comp" --sdk_root="$sdk_path" 2>&1 | grep -E '^\[|done' || true
            print_ok "$comp_name 安装完成"
        fi
    done

    return 0
}

write_local_properties() {
    local props_file="$(get_full_path "local.properties")"
    local sdk_path="$(get_full_path "$SDK_DIR")"

    cat > "$props_file" <<EOF
sdk.dir=${sdk_path}
EOF
    print_ok "已生成 local.properties"
}

do_setup() {
    TOTAL_STEPS=4
    STEP_COUNT=0

    print_banner "环境配置" "$CYAN"

    print_step "安装 JDK ${JDK_VERSION}..."
    install_jdk || { print_err "JDK 安装失败，终止"; return 1; }

    set_env_vars

    print_step "安装 Android SDK..."
    install_android_sdk || { print_err "Android SDK 安装失败，终止"; return 1; }

    print_step "生成 local.properties..."
    write_local_properties

    print_step "验证环境..."
    local java_bin="$(get_full_path "$JDK_DIR")/bin/java"
    local adb="$(get_full_path "$SDK_DIR")/platform-tools/adb"
    local ok=true
    if [[ -x "$java_bin" ]]; then print_ok "java:  $java_bin"; else print_err "java 不存在"; ok=false; fi
    if [[ -x "$adb" ]];     then print_ok "adb:   $adb"; else print_warn "adb 不存在 (不影响构建)"; fi

    if $ok; then
        print_banner "环境配置完成" "$GREEN"
    fi
    return 0
}

do_build() {
    local build_type="${1:-Debug}"

    set_env_vars
    TOTAL_STEPS=3
    STEP_COUNT=0

    local task_name="assemble${build_type}"
    print_banner "编译 ${build_type}" "$CYAN"

    print_step "检查环境..."
    local java_bin="$(get_full_path "$JDK_DIR")/bin/java"
    if [[ ! -x "$java_bin" ]]; then
        print_err "JDK 未安装，请先运行: ./build.sh setup"
        return 1
    fi
    print_ok "环境就绪"

    print_step "执行 Gradle ${task_name} ..."
    print_info "这可能需要几分钟，请耐心等待..."
    echo ""

    local gradlew="$PROJECT_ROOT/gradlew"
    chmod +x "$gradlew" 2>/dev/null || true

    local build_result=0
    (cd "$PROJECT_ROOT" && ./gradlew "$task_name" --no-daemon) || build_result=$?

    echo ""
    print_step "检查构建结果..."

    if [[ $build_result -eq 0 ]]; then
        local apk_dir
        if [[ "$build_type" == "Debug" ]]; then
            apk_dir="$(get_full_path "app/build/outputs/apk/debug")"
        else
            apk_dir="$(get_full_path "app/build/outputs/apk/release")"
        fi

        if [[ -d "$apk_dir" ]]; then
            print_banner "编译成功!" "$GREEN"
            for apk in "$apk_dir"/*.apk; do
                if [[ -f "$apk" ]]; then
                    local size
                    if [[ "$OS_TYPE" == "mac" ]]; then
                        size=$(stat -f%z "$apk" 2>/dev/null || echo 0)
                    else
                        size=$(stat -c%s "$apk" 2>/dev/null || echo 0)
                    fi
                    size=$(echo "scale=2; $size / 1048576" | bc 2>/dev/null || echo "?")
                    print_ok "$(basename "$apk")  ($size MB)"
                    print_info "路径: $apk"
                fi
            done
        fi
        return 0
    else
        print_banner "编译失败" "$RED"
        print_err "请检查上面的错误信息"
        return 1
    fi
}

do_install() {
    set_env_vars

    # 先编译
    do_build "Debug" || return 1

    echo ""
    print_banner "安装到设备" "$CYAN"

    local adb="$(get_full_path "$SDK_DIR")/platform-tools/adb"
    if [[ ! -x "$adb" ]]; then
        print_err "adb 未找到，请先运行: ./build.sh setup"
        return 1
    fi

    local apk_file="$(get_full_path "app/build/outputs/apk/debug/app-debug.apk")"
    if [[ ! -f "$apk_file" ]]; then
        print_err "APK 文件未找到"
        return 1
    fi

    print_info "正在安装..."
    "$adb" install -r "$apk_file" 2>&1 | sed 's/^/  /'

    if [[ ${PIPESTATUS[0]} -eq 0 ]]; then
        print_ok "安装成功!"
        print_info "包名: naipingzai.materialfile"
        return 0
    else
        print_err "安装失败，请检查设备连接"
        return 1
    fi
}

do_clean() {
    set_env_vars
    print_banner "清理构建" "$CYAN"

    # 1. Gradle clean
    print_info "执行 Gradle clean..."
    local gradlew="$PROJECT_ROOT/gradlew"
    chmod +x "$gradlew" 2>/dev/null || true
    (cd "$PROJECT_ROOT" && ./gradlew clean --no-daemon) || true

    # 2. 清理额外文件
    local clean_dirs=("app/build" "app/.cxx" "build" ".gradle" ".kotlin")
    local clean_files=("build_err.txt" "build_err2.txt" "build_output.txt")

    for d in "${clean_dirs[@]}"; do
        local full_path="$(get_full_path "$d")"
        if [[ -d "$full_path" ]]; then
            rm -rf "$full_path"
            print_ok "已删除: $d"
        fi
    done
    for f in "${clean_files[@]}"; do
        local full_path="$(get_full_path "$f")"
        if [[ -f "$full_path" ]]; then
            rm -f "$full_path"
            print_ok "已删除: $f"
        fi
    done

    print_banner "清理完成" "$GREEN"
    return 0
}

do_full() {
    print_banner "完整构建流程" "$MAGENTA"
    print_info "setup -> clean -> build -> install"
    echo ""

    do_setup || return 1
    do_clean || true
    do_install || return 1

    echo ""
    print_banner "全部完成!" "$GREEN"
    return 0
}

show_help() {
    print_banner "质感文件 - 构建脚本帮助" "$CYAN"
    cat <<EOF
  用法: ./build.sh [命令]

  命令:
    setup     下载并安装构建工具 (JDK, Android SDK)
    build     编译 Debug APK (默认)
    release   编译 Release APK (需要签名配置)
    install   编译 Debug 并安装到连接的设备
    clean     清理所有构建产物
    full      完整流程: setup + clean + build + install
    help      显示此帮助

  工具版本:
    JDK:         ${JDK_VERSION}
    Gradle:      8.13 (wrapper)
    AGP:         8.11.1
    Kotlin:      2.1.21
    CompileSDK:  ${COMPILE_SDK}
    BuildTools:  ${BUILD_TOOLS}
    NDK:         ${NDK_VERSION}
    CMake:       ${CMAKE_VERSION}

  目录结构:
    tools/${JDK_DIR_NAME}     JDK
    tools/android-sdk          Android SDK
    prebuild/android            预编译 Android 库 (AAR/JAR)
    prebuild/native             预编译 Native 库 (静态库)

  支持平台:
    Linux (x64, aarch64)
    macOS (x64, aarch64/Apple Silicon)

  示例:
    ./build.sh                 首次编译 (自动检查工具)
    ./build.sh full            全新环境一步到位
    ./build.sh clean && ./build.sh build  清理后重新编译

EOF
}

# ============================================================================
# 入口
# ============================================================================

detect_platform

echo ""
echo -e "${MAGENTA}  质感文件 MaterialFile${NC}"
echo -e "${GRAY}  by naipingzai (奶瓶仔)${NC}"
echo -e "${GRAY}  平台: ${OS_TYPE} / ${ARCH_TYPE}${NC}"
echo ""

COMMAND="${1:-build}"

case "$COMMAND" in
    setup)   do_setup ;;
    build)   do_build "Debug" ;;
    release) do_build "Release" ;;
    install) do_install ;;
    clean)   do_clean ;;
    full)    do_full ;;
    help|-h|--help) show_help ;;
    *)
        print_err "未知命令: $COMMAND"
        echo "  运行 './build.sh help' 查看帮助"
        exit 1
        ;;
esac
