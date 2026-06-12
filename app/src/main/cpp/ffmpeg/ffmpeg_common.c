/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 *
 * Shared state and utility implementations for FFmpeg JNI.
 */

#include "ffmpeg_common.h"

volatile int g_cancel = 0;
char g_last_error[512] = {0};

void set_last_error(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    vsnprintf(g_last_error, sizeof(g_last_error), fmt, args);
    va_end(args);
    ALOGE("LastError: %s", g_last_error);
}

void clear_last_error(void) {
    g_last_error[0] = '\0';
}

void throw_runtime(JNIEnv *env, const char *msg) {
    jclass cls = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (cls) (*env)->ThrowNew(env, cls, msg);
}

int write_header_faststart(AVFormatContext *ofmt_ctx) {
    AVDictionary *opts = NULL;
    if (ofmt_ctx->oformat && ofmt_ctx->oformat->name) {
        const char *name = ofmt_ctx->oformat->name;
        if (strcmp(name, "mp4") == 0 || strcmp(name, "mov") == 0 ||
            strcmp(name, "3gp") == 0 || strcmp(name, "3g2") == 0 ||
            strcmp(name, "ipod") == 0) {
            av_dict_set(&opts, "movflags", "+faststart", 0);
        }
    }
    int ret = avformat_write_header(ofmt_ctx, &opts);
    av_dict_free(&opts);
    return ret;
}
