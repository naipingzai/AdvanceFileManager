# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve the line number information for
# debugging stack traces.

# Hide the original source file name.

# Native methods
# https://www.guardsquare.com/en/products/proguard/manual/examples#native
    native <methods>;
}

# App
# For Class.getEnumConstants()
    public static **[] values();
}

# 保证JNI依赖的StructStat/StructDirent不被裁剪
-keep class com.advancefilemanager.provider.linux.syscall.StructStat { *; }
-keep class com.advancefilemanager.provider.linux.syscall.StructDirent { *; }

# Apache FtpServer
    public <init>(java.util.concurrent.ExecutorService);
    public <init>(java.util.concurrent.Executor);
    public <init>();
}

# Bouncy Castle

# SMBJ

# SMBJ-RPC
