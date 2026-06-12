/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 *
 * Shared state, logging macros, and utility declarations for FFmpeg JNI.
 */

#ifndef FFMPEG_COMMON_H
#define FFMPEG_COMMON_H

#include <jni.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/opt.h>
#include <libavutil/channel_layout.h>
#include <libavutil/samplefmt.h>
#include <libswresample/swresample.h>
#include <libswscale/swscale.h>
#include <libavutil/hwcontext.h>
#include <libavutil/imgutils.h>
#include <libavfilter/avfilter.h>
#include <libavfilter/buffersink.h>
#include <libavfilter/buffersrc.h>

#define LOG_TAG "ffmpeg-jni"
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern volatile int g_cancel;
extern char g_last_error[512];

void set_last_error(const char *fmt, ...);
void clear_last_error(void);
void throw_runtime(JNIEnv *env, const char *msg);
int write_header_faststart(AVFormatContext *ofmt_ctx);

#endif /* FFMPEG_COMMON_H */
