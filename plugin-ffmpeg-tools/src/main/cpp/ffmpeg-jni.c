/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdarg.h>
#include <inttypes.h>
#include <unistd.h>
#include <android/log.h>

#include <libavcodec/avcodec.h>
#include <libavcodec/jni.h>
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

/* ====================================================================
 *  JNI_OnLoad: pass JavaVM to FFmpeg so MediaCodec codecs can work.
 * ==================================================================== */
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)reserved;
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    /* Register JavaVM with FFmpeg for MediaCodec hardware codec access */
    int ret = av_jni_set_java_vm(vm, NULL);
    if (ret < 0) {
        ALOGE("av_jni_set_java_vm failed: %d", ret);
    } else {
        ALOGI("av_jni_set_java_vm OK - MediaCodec HW codecs enabled");
    }
    return JNI_VERSION_1_6;
}

/* ====================================================================
 *  Cancel flag - set from Java thread to abort running conversion.
 * ==================================================================== */
static volatile int g_cancel = 0;

/* ====================================================================
 *  Last error message - saved for Java to retrieve after failure.
 * ==================================================================== */
static char g_last_error[512] = {0};

static void set_last_error(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    vsnprintf(g_last_error, sizeof(g_last_error), fmt, args);
    va_end(args);
    ALOGE("LastError: %s", g_last_error);
}

static void clear_last_error(void) {
    g_last_error[0] = '\0';
}

/* ====================================================================
 *  Hardware codec helpers: try MediaCodec first, fallback to software.
 * ==================================================================== */

/**
 * Try to find a HW (MediaCodec) decoder for the given codec_id.
 * Returns the HW decoder, or the SW decoder if HW is not available.
 */
static const AVCodec *find_hw_decoder(enum AVCodecID codec_id) {
    const char *mc_name = NULL;
    switch (codec_id) {
        case AV_CODEC_ID_H264:  mc_name = "h264_mediacodec";  break;
        case AV_CODEC_ID_HEVC:  mc_name = "hevc_mediacodec";  break;
        case AV_CODEC_ID_VP8:   mc_name = "vp8_mediacodec";   break;
        case AV_CODEC_ID_VP9:   mc_name = "vp9_mediacodec";   break;
        case AV_CODEC_ID_AV1:   mc_name = "av1_mediacodec";   break;
        default: break;
    }
    if (mc_name) {
        const AVCodec *hw = avcodec_find_decoder_by_name(mc_name);
        if (hw) {
            ALOGI("Using HW decoder: %s", mc_name);
            return hw;
        }
        ALOGI("HW decoder %s not available, using SW", mc_name);
    }
    return avcodec_find_decoder(codec_id);
}

/**
 * Try to find a HW (MediaCodec) encoder for the given codec_id.
 * Returns the HW encoder, or the SW encoder if HW is not available.
 */
static const AVCodec *find_hw_encoder(enum AVCodecID codec_id) {
    const char *mc_name = NULL;
    switch (codec_id) {
        case AV_CODEC_ID_H264:  mc_name = "h264_mediacodec";  break;
        case AV_CODEC_ID_HEVC:  mc_name = "hevc_mediacodec";  break;
        case AV_CODEC_ID_VP8:   mc_name = "vp8_mediacodec";   break;
        case AV_CODEC_ID_VP9:   mc_name = "vp9_mediacodec";   break;
        default: break;
    }
    if (mc_name) {
        const AVCodec *hw = avcodec_find_encoder_by_name(mc_name);
        if (hw) {
            ALOGI("Using HW encoder: %s", mc_name);
            return hw;
        }
        ALOGI("HW encoder %s not available, falling back to SW", mc_name);
    }
    return avcodec_find_encoder(codec_id);
}

/**
 * Open a video decoder context with HW acceleration if possible.
 * Caller must free dec_ctx on success.
 */
static int open_video_decoder_hw(AVFormatContext *ifmt_ctx, int video_idx,
                                 AVCodecContext **out_dec_ctx) {
    AVCodecParameters *par = ifmt_ctx->streams[video_idx]->codecpar;
    const AVCodec *dec = find_hw_decoder(par->codec_id);
    if (!dec) return AVERROR_DECODER_NOT_FOUND;

    AVCodecContext *dec_ctx = avcodec_alloc_context3(dec);
    if (!dec_ctx) return AVERROR(ENOMEM);

    int ret = avcodec_parameters_to_context(dec_ctx, par);
    if (ret < 0) { avcodec_free_context(&dec_ctx); return ret; }

    ret = avcodec_open2(dec_ctx, dec, NULL);
    if (ret < 0) {
        avcodec_free_context(&dec_ctx);
        /* If HW decoder failed, try SW fallback */
        const AVCodec *sw = avcodec_find_decoder(par->codec_id);
        if (sw && sw != dec) {
            ALOGI("HW decoder open failed, trying SW decoder: %s", sw->name);
            dec_ctx = avcodec_alloc_context3(sw);
            if (!dec_ctx) return AVERROR(ENOMEM);
            ret = avcodec_parameters_to_context(dec_ctx, par);
            if (ret < 0) { avcodec_free_context(&dec_ctx); return ret; }
            ret = avcodec_open2(dec_ctx, sw, NULL);
            if (ret < 0) { avcodec_free_context(&dec_ctx); return ret; }
        } else {
            return ret;
        }
    }
    *out_dec_ctx = dec_ctx;
    return 0;
}

/**
 * Open a video encoder with HW (MediaCodec) first, SW fallback.
 * Configures dimensions, bitrate, time_base, framerate, pix_fmt, and codec options.
 * Caller must free *out_enc_ctx on success.
 * @param out_is_hw  set to 1 if HW encoder was used, 0 for SW.
 */
static int open_video_encoder_with_fallback(
        enum AVCodecID codec_id,
        AVCodecContext *dec_ctx,
        AVFormatContext *ifmt_ctx, int video_idx,
        const AVOutputFormat *oformat,
        int64_t bit_rate,
        AVCodecContext **out_enc_ctx, int *out_is_hw) {
    const AVCodec *enc = find_hw_encoder(codec_id);
    if (!enc) return AVERROR_ENCODER_NOT_FOUND;

    int is_hw = (strstr(enc->name, "mediacodec") != NULL);
    int ret = AVERROR_ENCODER_NOT_FOUND;

    /* Build a list of encoders to try: HW first, then mpeg4 SW fallback */
    const AVCodec *try_list[3] = {NULL};
    int try_is_hw[3] = {0};
    int try_count = 0;

    /* 1) HW encoder */
    try_list[try_count] = enc;
    try_is_hw[try_count] = is_hw;
    try_count++;

    /* 2) mpeg4 SW fallback (always compiled in this build) */
    if (codec_id != AV_CODEC_ID_MPEG4) {
        const AVCodec *mp4 = avcodec_find_encoder(AV_CODEC_ID_MPEG4);
        if (mp4) {
            try_list[try_count] = mp4;
            try_is_hw[try_count] = 0;
            try_count++;
        }
    }

    int out_w = (dec_ctx->width  + 1) & ~1;
    int out_h = (dec_ctx->height + 1) & ~1;
    if (out_w <= 0) out_w = 640;
    if (out_h <= 0) out_h = 480;

    AVRational tb = ifmt_ctx->streams[video_idx]->time_base;
    if (tb.num <= 0 || tb.den <= 0) tb = (AVRational){1, 30};
    AVRational fr = av_guess_frame_rate(ifmt_ctx, ifmt_ctx->streams[video_idx], NULL);
    if (fr.num <= 0 || fr.den <= 0) fr = (AVRational){30, 1};

    for (int attempt = 0; attempt < try_count; attempt++) {
        enc = try_list[attempt];
        is_hw = try_is_hw[attempt];

        AVCodecContext *ctx = avcodec_alloc_context3(enc);
        if (!ctx) return AVERROR(ENOMEM);

        ctx->width  = out_w;
        ctx->height = out_h;
        ctx->sample_aspect_ratio = dec_ctx->sample_aspect_ratio;
        ctx->bit_rate  = bit_rate;
        /* Use inverse framerate as encoder time_base, NOT the container
         * time_base (1/90000) which exceeds mpeg4's VOP increment limit */
        ctx->time_base = av_inv_q(fr);
        ctx->framerate = fr;
        ctx->gop_size  = 12;
        ctx->max_b_frames = 0;

        if (is_hw) {
            ctx->pix_fmt = AV_PIX_FMT_NV12;
        } else {
            ctx->pix_fmt = AV_PIX_FMT_YUV420P;
        }

        if (oformat->flags & AVFMT_GLOBALHEADER)
            ctx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;

        ALOGI("Trying encoder: %s HW=%d %dx%d pix=%d br=%lld tb=%d/%d fr=%d/%d",
              enc->name, is_hw, ctx->width, ctx->height, ctx->pix_fmt,
              (long long)ctx->bit_rate, ctx->time_base.num, ctx->time_base.den,
              ctx->framerate.num, ctx->framerate.den);

        AVDictionary *opts = NULL;
        ret = avcodec_open2(ctx, enc, &opts);
        av_dict_free(&opts);

        if (ret >= 0) {
            *out_enc_ctx = ctx;
            *out_is_hw = is_hw;
            ALOGI("Video encoder OK: %s (HW=%d) pix_fmt=%d", enc->name, is_hw, ctx->pix_fmt);
            return 0;
        }

        ALOGI("Encoder %s open failed (%s), trying next", enc->name, av_err2str(ret));
        avcodec_free_context(&ctx);
    }
    return ret;
}

/**
 * Open an audio encoder and set up resampler for the given decoder context.
 * Finds encoder with AAC fallback, configures 128kbps stereo/mono.
 * Caller must free *out_enc_ctx and *out_swr_ctx on success.
 */
static int open_audio_encoder_with_resampler(
        enum AVCodecID codec_id,
        AVCodecContext *dec_ctx,
        int global_header,
        AVCodecContext **out_enc_ctx,
        SwrContext **out_swr_ctx) {
    const AVCodec *enc = avcodec_find_encoder(codec_id);
    if (!enc) {
        ALOGI("Encoder %s not found, trying AAC fallback", avcodec_get_name(codec_id));
        enc = avcodec_find_encoder(AV_CODEC_ID_AAC);
    }
    if (!enc) return AVERROR_ENCODER_NOT_FOUND;

    AVCodecContext *enc_ctx = avcodec_alloc_context3(enc);
    if (!enc_ctx) return AVERROR(ENOMEM);

    enc_ctx->sample_rate = dec_ctx->sample_rate;
    enc_ctx->sample_fmt  = enc->sample_fmts ? enc->sample_fmts[0] : AV_SAMPLE_FMT_FLTP;
    AVChannelLayout stereo = AV_CHANNEL_LAYOUT_STEREO;
    AVChannelLayout mono   = AV_CHANNEL_LAYOUT_MONO;
    av_channel_layout_copy(&enc_ctx->ch_layout,
        dec_ctx->ch_layout.nb_channels >= 2 ? &stereo : &mono);
    enc_ctx->bit_rate  = 128000;
    enc_ctx->time_base = (AVRational){1, enc_ctx->sample_rate};
    if (global_header)
        enc_ctx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;

    int ret = avcodec_open2(enc_ctx, enc, NULL);
    if (ret < 0) { avcodec_free_context(&enc_ctx); return ret; }

    SwrContext *swr = NULL;
    ret = swr_alloc_set_opts2(&swr,
        &enc_ctx->ch_layout, enc_ctx->sample_fmt, enc_ctx->sample_rate,
        &dec_ctx->ch_layout, dec_ctx->sample_fmt, dec_ctx->sample_rate,
        0, NULL);
    if (ret < 0 || swr_init(swr) < 0) {
        swr_free(&swr);
        avcodec_free_context(&enc_ctx);
        return AVERROR_UNKNOWN;
    }

    *out_enc_ctx = enc_ctx;
    *out_swr_ctx = swr;
    ALOGI("Audio encoder: %s, sample_rate=%d", enc->name, enc_ctx->sample_rate);
    return 0;
}

/* ====================================================================
 *  JNI helper: throw RuntimeException
 * ==================================================================== */
static void throw_runtime(JNIEnv *env, const char *msg) {
    jclass cls = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (cls) (*env)->ThrowNew(env, cls, msg);
}

/* ====================================================================
 *  Helper: write header with movflags +faststart for MP4/MOV formats.
 *  This ensures the moov atom is at the beginning of the file,
 *  enabling proper seeking in players.
 * ==================================================================== */
static int write_header_faststart(AVFormatContext *ofmt_ctx) {
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

/* ====================================================================
 *  Remux-only conversion (no re-encoding, just container change).
 *  Returns 0 on success, negative on error.
 * ==================================================================== */
static int remux(const char *in_path, const char *out_path,
                 JNIEnv *env, jobject callback, jmethodID onProgress) {
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket *pkt = NULL;
    int ret = 0;
    int *stream_mapping = NULL;

    if ((ret = avformat_open_input(&ifmt_ctx, in_path, NULL, NULL)) < 0) {
        ALOGE("Cannot open input: %s", av_err2str(ret));
        return ret;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) {
        ALOGE("Cannot find stream info: %s", av_err2str(ret));
        goto end;
    }

    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_path);
    if (!ofmt_ctx) { ret = AVERROR_UNKNOWN; goto end; }

    int nb = ifmt_ctx->nb_streams;
    stream_mapping = av_calloc(nb, sizeof(int));
    if (!stream_mapping) { ret = AVERROR(ENOMEM); goto end; }

    int stream_idx = 0;
    for (int i = 0; i < nb; i++) {
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVCodecParameters *par = in_stream->codecpar;
        if (par->codec_type != AVMEDIA_TYPE_AUDIO &&
            par->codec_type != AVMEDIA_TYPE_VIDEO &&
            par->codec_type != AVMEDIA_TYPE_SUBTITLE) {
            stream_mapping[i] = -1;
            continue;
        }
        stream_mapping[i] = stream_idx++;
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
        if (!out_stream) { ret = AVERROR(ENOMEM); goto end; }
        ret = avcodec_parameters_copy(out_stream->codecpar, par);
        if (ret < 0) goto end;
        out_stream->codecpar->codec_tag = 0;
    }

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, out_path, AVIO_FLAG_WRITE);
        if (ret < 0) { ALOGE("avio_open failed: %s", av_err2str(ret)); goto end; }
    }

    ret = write_header_faststart(ofmt_ctx);
    if (ret < 0) { ALOGE("write_header failed: %s", av_err2str(ret)); goto end; }

    pkt = av_packet_alloc();
    if (!pkt) { ret = AVERROR(ENOMEM); goto end; }

    int64_t duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 1;
    int last_percent = -1;

    /* Collect per-stream start_time so we can subtract it and output
     * timestamps that start from 0 — required for concat demuxer to
     * correctly calculate offsets at file boundaries. */
    int64_t *start_ts = av_calloc(nb, sizeof(int64_t));
    if (!start_ts) { ret = AVERROR(ENOMEM); goto end; }
    for (int i = 0; i < nb; i++) {
        AVStream *st = ifmt_ctx->streams[i];
        start_ts[i] = (st->start_time != AV_NOPTS_VALUE) ? st->start_time : 0;
    }

    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;

        if (pkt->stream_index >= nb || stream_mapping[pkt->stream_index] < 0) {
            av_packet_unref(pkt);
            continue;
        }

        int in_idx = pkt->stream_index;
        int out_idx = stream_mapping[in_idx];
        AVStream *in_s = ifmt_ctx->streams[in_idx];
        AVStream *out_s = ofmt_ctx->streams[out_idx];

        /* Subtract start_time so output starts from 0 */
        if (start_ts[in_idx] != 0) {
            if (pkt->pts != AV_NOPTS_VALUE) pkt->pts -= start_ts[in_idx];
            if (pkt->dts != AV_NOPTS_VALUE) pkt->dts -= start_ts[in_idx];
        }

        /* Save progress info before rescaling (rescale changes pts) */
        int64_t saved_pts_us = -1;
        if (callback && onProgress && in_s->codecpar->codec_type == AVMEDIA_TYPE_VIDEO
            && pkt->pts != AV_NOPTS_VALUE) {
            saved_pts_us = av_rescale_q(pkt->pts, in_s->time_base,
                                        (AVRational){1, AV_TIME_BASE});
        }

        pkt->stream_index = out_idx;
        av_packet_rescale_ts(pkt, in_s->time_base, out_s->time_base);
        pkt->pos = -1;

        ret = av_interleaved_write_frame(ofmt_ctx, pkt);
        if (ret < 0) { ALOGE("write_frame error: %s", av_err2str(ret)); break; }

        /* Progress callback */
        if (saved_pts_us >= 0) {
            int pct = (int)(saved_pts_us * 100 / duration);
            if (pct < 0) pct = 0;
            if (pct > 100) pct = 100;
            if (pct != last_percent) {
                last_percent = pct;
                (*env)->CallVoidMethod(env, callback, onProgress, pct);
                if ((*env)->ExceptionCheck(env)) {
                    (*env)->ExceptionClear(env);
                    ret = AVERROR_EXIT; break;
                }
            }
        }
    }

    av_free(start_ts);

    if (g_cancel) { ret = AVERROR_EXIT; goto end; }
    if (ret == AVERROR_EOF || ret >= 0) {
        av_write_trailer(ofmt_ctx);
        ret = 0;
    }
    /* else: keep ret as the error code */

end:
    av_packet_free(&pkt);
    av_free(stream_mapping);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    return ret;
}

/* ====================================================================
 *  Full transcode (audio re-encoding for format conversion).
 *  Simplified: single audio stream, decode → resample → encode.
 * ==================================================================== */
static int transcode_audio(const char *in_path, const char *out_path,
                           JNIEnv *env, jobject callback, jmethodID onProgress) {
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    const AVCodec *dec = NULL;
    AVCodecContext *dec_ctx = NULL, *enc_ctx = NULL;
    SwrContext *swr_ctx = NULL;
    AVPacket *pkt = NULL, *out_pkt = NULL;
    AVFrame *frame = NULL, *filt_frame = NULL;
    int ret = 0, audio_idx = -1;

    /* Open input */
    if ((ret = avformat_open_input(&ifmt_ctx, in_path, NULL, NULL)) < 0) goto end;
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) goto end;

    audio_idx = av_find_best_stream(ifmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, &dec, 0);
    if (audio_idx < 0) { ret = audio_idx; goto end; }

    /* Decoder */
    dec_ctx = avcodec_alloc_context3(dec);
    if (!dec_ctx) { ret = AVERROR(ENOMEM); goto end; }
    if ((ret = avcodec_parameters_to_context(dec_ctx, ifmt_ctx->streams[audio_idx]->codecpar)) < 0) goto end;
    if ((ret = avcodec_open2(dec_ctx, dec, NULL)) < 0) goto end;

    /* Output */
    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_path);
    if (!ofmt_ctx) {
        /* Muxer for extension not found (e.g. .m4a needs ipod); try mp4 */
        avformat_alloc_output_context2(&ofmt_ctx, NULL, "mp4", out_path);
    }
    if (!ofmt_ctx) { ret = AVERROR_UNKNOWN; goto end; }

    ret = open_audio_encoder_with_resampler(
        ofmt_ctx->oformat->audio_codec, dec_ctx,
        ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER,
        &enc_ctx, &swr_ctx);
    if (ret < 0) goto end;

    AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
    if (!out_stream) { ret = AVERROR(ENOMEM); goto end; }
    avcodec_parameters_from_context(out_stream->codecpar, enc_ctx);
    out_stream->time_base = enc_ctx->time_base;

    /* Open output file */
    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, out_path, AVIO_FLAG_WRITE);
        if (ret < 0) goto end;
    }
    if ((ret = write_header_faststart(ofmt_ctx)) < 0) goto end;

    pkt = av_packet_alloc();
    out_pkt = av_packet_alloc();
    frame = av_frame_alloc();
    filt_frame = av_frame_alloc();
    if (!pkt || !out_pkt || !frame || !filt_frame) { ret = AVERROR(ENOMEM); goto end; }

    int64_t duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 1;
    int last_percent = -1;
    int64_t pts = 0;

    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;
        if (pkt->stream_index != audio_idx) { av_packet_unref(pkt); continue; }

        ret = avcodec_send_packet(dec_ctx, pkt);
        av_packet_unref(pkt);
        if (ret < 0) break;

        while (ret >= 0 && !g_cancel) {
            ret = avcodec_receive_frame(dec_ctx, frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
            if (ret < 0) break;

            /* Resample */
            filt_frame->sample_rate = enc_ctx->sample_rate;
            filt_frame->format = enc_ctx->sample_fmt;
            av_channel_layout_copy(&filt_frame->ch_layout, &enc_ctx->ch_layout);
            filt_frame->nb_samples = swr_get_out_samples(swr_ctx, frame->nb_samples);
            av_frame_get_buffer(filt_frame, 0);

            int converted = swr_convert(swr_ctx,
                filt_frame->data, filt_frame->nb_samples,
                (const uint8_t **)frame->data, frame->nb_samples);
            filt_frame->nb_samples = converted;
            filt_frame->pts = pts;
            pts += converted;

            /* Encode */
            ret = avcodec_send_frame(enc_ctx, filt_frame);
            av_frame_unref(filt_frame);
            if (ret < 0) break;

            while (ret >= 0) {
                ret = avcodec_receive_packet(enc_ctx, out_pkt);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                if (ret < 0) break;
                out_pkt->stream_index = 0;
                av_packet_rescale_ts(out_pkt, enc_ctx->time_base, out_stream->time_base);
                av_interleaved_write_frame(ofmt_ctx, out_pkt);
            }

            /* Progress */
            if (callback && onProgress && frame->pts != AV_NOPTS_VALUE) {
                int64_t pts_us = av_rescale_q(frame->pts, ifmt_ctx->streams[audio_idx]->time_base,
                                              (AVRational){1, AV_TIME_BASE});
                int pct = (int)(pts_us * 100 / duration);
                if (pct < 0) pct = 0; if (pct > 100) pct = 100;
                if (pct != last_percent) {
                    last_percent = pct;
                    (*env)->CallVoidMethod(env, callback, onProgress, pct);
                    if ((*env)->ExceptionCheck(env)) {
                        (*env)->ExceptionClear(env);
                        ret = AVERROR_EXIT; break;
                    }
                }
            }
            av_frame_unref(frame);
        }
        if (ret < 0 && ret != AVERROR_EOF) break;
    }

    /* Flush encoder */
    if (!g_cancel) {
        avcodec_send_frame(enc_ctx, NULL);
        while (1) {
            ret = avcodec_receive_packet(enc_ctx, out_pkt);
            if (ret == AVERROR_EOF || ret < 0) break;
            out_pkt->stream_index = 0;
            av_packet_rescale_ts(out_pkt, enc_ctx->time_base, out_stream->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }

    if (g_cancel) ret = AVERROR_EXIT;
    else if (ret == AVERROR_EOF || ret >= 0) { av_write_trailer(ofmt_ctx); ret = 0; }
    /* else: keep ret as the error code */

end:
    av_frame_free(&frame);
    av_frame_free(&filt_frame);
    av_packet_free(&pkt);
    av_packet_free(&out_pkt);
    swr_free(&swr_ctx);
    avcodec_free_context(&dec_ctx);
    avcodec_free_context(&enc_ctx);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    return ret;
}

/* ====================================================================
 *  Full video transcode: decode video+audio, re-encode to target
 *  format codecs.  Handles e.g. MP4(H264+AAC) -> WebM(VP9+Opus).
 * ==================================================================== */
static int transcode_video(const char *in_path, const char *out_path,
                           JNIEnv *env, jobject callback, jmethodID onProgress) {
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVCodecContext *vdec_ctx = NULL, *venc_ctx = NULL;
    AVCodecContext *adec_ctx = NULL, *aenc_ctx = NULL;
    SwrContext *swr_ctx = NULL;
    struct SwsContext *sws_ctx = NULL;
    AVPacket *pkt = NULL, *out_pkt = NULL;
    AVFrame *frame = NULL, *aframe = NULL, *afilt_frame = NULL;
    int ret = 0;
    int video_idx = -1, audio_idx = -1;
    int vout_idx = 0, aout_idx = -1;
    int is_hw_encoder = 0;
    int need_audio_transcode = 0;
    int64_t audio_pts = 0;

    /* Open input */
    if ((ret = avformat_open_input(&ifmt_ctx, in_path, NULL, NULL)) < 0) goto tv_end;
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) goto tv_end;

    for (unsigned i = 0; i < ifmt_ctx->nb_streams; i++) {
        if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && video_idx < 0)
            video_idx = i;
        else if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO && audio_idx < 0)
            audio_idx = i;
    }
    if (video_idx < 0) { ret = AVERROR_STREAM_NOT_FOUND; goto tv_end; }

    /* Output format */
    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_path);
    if (!ofmt_ctx) { ret = AVERROR_UNKNOWN; goto tv_end; }

    /* ---- Video decoder (HW if possible) ---- */
    if ((ret = open_video_decoder_hw(ifmt_ctx, video_idx, &vdec_ctx)) < 0) goto tv_end;

    /* Target video codec from output format */
    enum AVCodecID target_vcodec = ofmt_ctx->oformat->video_codec;
    if (target_vcodec == AV_CODEC_ID_NONE) target_vcodec = AV_CODEC_ID_H264;
    ALOGI("transcode_video: target video codec = %s", avcodec_get_name(target_vcodec));

    int64_t src_vbitrate = ifmt_ctx->streams[video_idx]->codecpar->bit_rate;
    if (src_vbitrate <= 0) {
        src_vbitrate = (int64_t)vdec_ctx->width * vdec_ctx->height * 3;
        if (src_vbitrate < 500000)  src_vbitrate = 500000;
        if (src_vbitrate > 8000000) src_vbitrate = 8000000;
    }

    ret = open_video_encoder_with_fallback(target_vcodec, vdec_ctx, ifmt_ctx, video_idx,
                                           ofmt_ctx->oformat, src_vbitrate,
                                           &venc_ctx, &is_hw_encoder);
    if (ret < 0) goto tv_end;

    AVStream *vout_stream = avformat_new_stream(ofmt_ctx, NULL);
    if (!vout_stream) { ret = AVERROR(ENOMEM); goto tv_end; }
    avcodec_parameters_from_context(vout_stream->codecpar, venc_ctx);
    vout_stream->time_base = venc_ctx->time_base;
    vout_idx = vout_stream->index;

    /* ---- Audio setup ---- */
    if (audio_idx >= 0) {
        enum AVCodecID target_acodec = ofmt_ctx->oformat->audio_codec;
        if (target_acodec == AV_CODEC_ID_NONE) target_acodec = AV_CODEC_ID_AAC;
        enum AVCodecID src_acodec = ifmt_ctx->streams[audio_idx]->codecpar->codec_id;
        ALOGI("Audio: src=%s target=%s", avcodec_get_name(src_acodec), avcodec_get_name(target_acodec));

        if (src_acodec != target_acodec) {
            /* Need to transcode audio */
            const AVCodec *adec = avcodec_find_decoder(src_acodec);
            if (adec) {
                adec_ctx = avcodec_alloc_context3(adec);
                avcodec_parameters_to_context(adec_ctx, ifmt_ctx->streams[audio_idx]->codecpar);
                if (avcodec_open2(adec_ctx, adec, NULL) < 0) {
                    avcodec_free_context(&adec_ctx); adec_ctx = NULL;
                }
            }
            if (adec_ctx) {
                int ar = open_audio_encoder_with_resampler(
                    target_acodec, adec_ctx,
                    ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER,
                    &aenc_ctx, &swr_ctx);
                if (ar < 0) {
                    ALOGI("Audio encoder setup failed, dropping audio");
                } else {
                    AVStream *aout = avformat_new_stream(ofmt_ctx, NULL);
                    if (aout) {
                        avcodec_parameters_from_context(aout->codecpar, aenc_ctx);
                        aout->time_base = aenc_ctx->time_base;
                        aout_idx = aout->index;
                        need_audio_transcode = 1;
                    }
                }
            }
        } else {
            /* Codec compatible, just copy */
            AVStream *aout = avformat_new_stream(ofmt_ctx, NULL);
            if (aout) {
                avcodec_parameters_copy(aout->codecpar, ifmt_ctx->streams[audio_idx]->codecpar);
                aout->codecpar->codec_tag = 0;
                aout_idx = aout->index;
            }
        }
    }

    /* Open output file */
    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, out_path, AVIO_FLAG_WRITE);
        if (ret < 0) goto tv_end;
    }
    if ((ret = write_header_faststart(ofmt_ctx)) < 0) goto tv_end;

    /* Pixel format converter */
    if (vdec_ctx->pix_fmt != venc_ctx->pix_fmt) {
        sws_ctx = sws_getContext(vdec_ctx->width, vdec_ctx->height, vdec_ctx->pix_fmt,
                                venc_ctx->width, venc_ctx->height, venc_ctx->pix_fmt,
                                SWS_BILINEAR, NULL, NULL, NULL);
        if (!sws_ctx) { ret = AVERROR_UNKNOWN; ALOGE("sws_getContext failed"); goto tv_end; }
    }

    pkt      = av_packet_alloc();
    out_pkt  = av_packet_alloc();
    frame    = av_frame_alloc();
    aframe   = av_frame_alloc();
    afilt_frame = av_frame_alloc();
    if (!pkt || !out_pkt || !frame || !aframe || !afilt_frame) { ret = AVERROR(ENOMEM); goto tv_end; }

    int64_t duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 1;
    int last_percent = -1;

    /* ==== Main decode / encode loop ==== */
    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;

        /* ---- Video packet ---- */
        if (pkt->stream_index == video_idx) {
            ret = avcodec_send_packet(vdec_ctx, pkt);
            av_packet_unref(pkt);
            if (ret < 0) {
                if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
                break;
            }

            while (ret >= 0 && !g_cancel) {
                ret = avcodec_receive_frame(vdec_ctx, frame);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                if (ret < 0) {
                    if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
                    break;
                }

                int64_t saved_pts = frame->pts; /* save before potential unref */

                AVFrame *enc_frame = frame;
                AVFrame *conv = NULL;
                if (sws_ctx) {
                    conv = av_frame_alloc();
                    if (!conv) { av_frame_unref(frame); ret = AVERROR(ENOMEM); break; }
                    conv->width  = venc_ctx->width;
                    conv->height = venc_ctx->height;
                    conv->format = venc_ctx->pix_fmt;
                    av_frame_get_buffer(conv, 0);
                    sws_scale(sws_ctx, (const uint8_t *const *)frame->data, frame->linesize,
                              0, vdec_ctx->height, conv->data, conv->linesize);
                    conv->pts = frame->pts;
                    enc_frame = conv;
                }

                /* Rescale pts from input stream time_base to encoder time_base */
                enc_frame->pts = av_rescale_q(enc_frame->pts,
                    ifmt_ctx->streams[video_idx]->time_base, venc_ctx->time_base);
                ret = avcodec_send_frame(venc_ctx, enc_frame);
                if (conv) av_frame_free(&conv);
                av_frame_unref(frame);
                if (ret < 0) break;

                while (ret >= 0) {
                    ret = avcodec_receive_packet(venc_ctx, out_pkt);
                    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                    if (ret < 0) break;
                    out_pkt->stream_index = vout_idx;
                    av_packet_rescale_ts(out_pkt, venc_ctx->time_base,
                                         ofmt_ctx->streams[vout_idx]->time_base);
                    av_interleaved_write_frame(ofmt_ctx, out_pkt);
                }

                /* Progress */
                if (callback && onProgress && saved_pts != AV_NOPTS_VALUE) {
                    int64_t pts_us = av_rescale_q(saved_pts,
                        ifmt_ctx->streams[video_idx]->time_base,
                        (AVRational){1, AV_TIME_BASE});
                    int pct = (int)(pts_us * 100 / duration);
                    if (pct < 0) pct = 0; if (pct > 100) pct = 100;
                    if (pct != last_percent) {
                        last_percent = pct;
                        (*env)->CallVoidMethod(env, callback, onProgress, pct);
                        if ((*env)->ExceptionCheck(env)) {
                            (*env)->ExceptionClear(env);
                            ret = AVERROR_EXIT; break;
                        }
                    }
                }
            }
        }
        /* ---- Audio packet ---- */
        else if (pkt->stream_index == audio_idx && aout_idx >= 0) {
            if (need_audio_transcode && adec_ctx && aenc_ctx && swr_ctx) {
                ret = avcodec_send_packet(adec_ctx, pkt);
                av_packet_unref(pkt);
                if (ret < 0) { ret = 0; continue; }

                while (ret >= 0 && !g_cancel) {
                    ret = avcodec_receive_frame(adec_ctx, aframe);
                    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                    if (ret < 0) break;

                    afilt_frame->sample_rate = aenc_ctx->sample_rate;
                    av_channel_layout_copy(&afilt_frame->ch_layout, &aenc_ctx->ch_layout);
                    afilt_frame->format     = aenc_ctx->sample_fmt;
                    afilt_frame->nb_samples = swr_get_out_samples(swr_ctx, aframe->nb_samples);
                    av_frame_get_buffer(afilt_frame, 0);

                    int converted = swr_convert(swr_ctx,
                                afilt_frame->data, afilt_frame->nb_samples,
                                (const uint8_t **)aframe->data, aframe->nb_samples);
                    afilt_frame->nb_samples = converted;
                    afilt_frame->pts = audio_pts;
                    audio_pts += converted;
                    av_frame_unref(aframe);

                    ret = avcodec_send_frame(aenc_ctx, afilt_frame);
                    av_frame_unref(afilt_frame);
                    if (ret < 0) break;

                    while (ret >= 0) {
                        ret = avcodec_receive_packet(aenc_ctx, out_pkt);
                        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                        if (ret < 0) break;
                        out_pkt->stream_index = aout_idx;
                        av_packet_rescale_ts(out_pkt, aenc_ctx->time_base,
                                             ofmt_ctx->streams[aout_idx]->time_base);
                        av_interleaved_write_frame(ofmt_ctx, out_pkt);
                    }
                }
            } else {
                /* Audio copy */
                AVStream *in_s  = ifmt_ctx->streams[audio_idx];
                AVStream *out_s = ofmt_ctx->streams[aout_idx];
                pkt->stream_index = aout_idx;
                av_packet_rescale_ts(pkt, in_s->time_base, out_s->time_base);
                pkt->pos = -1;
                av_interleaved_write_frame(ofmt_ctx, pkt);
            }
        } else {
            av_packet_unref(pkt);
        }
    }

    /* Flush video encoder */
    if (!g_cancel && venc_ctx) {
        avcodec_send_frame(venc_ctx, NULL);
        while (1) {
            ret = avcodec_receive_packet(venc_ctx, out_pkt);
            if (ret == AVERROR_EOF || ret < 0) break;
            out_pkt->stream_index = vout_idx;
            av_packet_rescale_ts(out_pkt, venc_ctx->time_base,
                                 ofmt_ctx->streams[vout_idx]->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }
    /* Flush audio encoder */
    if (!g_cancel && aenc_ctx && need_audio_transcode) {
        avcodec_send_frame(aenc_ctx, NULL);
        while (1) {
            ret = avcodec_receive_packet(aenc_ctx, out_pkt);
            if (ret == AVERROR_EOF || ret < 0) break;
            out_pkt->stream_index = aout_idx;
            av_packet_rescale_ts(out_pkt, aenc_ctx->time_base,
                                 ofmt_ctx->streams[aout_idx]->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }

    if (g_cancel) ret = AVERROR_EXIT;
    else if (ret == AVERROR_EOF || ret >= 0) { av_write_trailer(ofmt_ctx); ret = 0; }
    /* else: keep ret as the error code */

tv_end:
    if (sws_ctx) sws_freeContext(sws_ctx);
    swr_free(&swr_ctx);
    av_frame_free(&frame);
    av_frame_free(&aframe);
    av_frame_free(&afilt_frame);
    av_packet_free(&pkt);
    av_packet_free(&out_pkt);
    avcodec_free_context(&vdec_ctx);
    avcodec_free_context(&venc_ctx);
    avcodec_free_context(&adec_ctx);
    avcodec_free_context(&aenc_ctx);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    return ret;
}

/* ====================================================================
 *  Smart conversion: try remux first, fallback to transcode.
 * ==================================================================== */
static int smart_convert(const char *in_path, const char *out_path,
                         JNIEnv *env, jobject callback, jmethodID onProgress) {
    /* Probe input to decide strategy */
    AVFormatContext *probe = NULL;
    int ret = avformat_open_input(&probe, in_path, NULL, NULL);
    if (ret < 0) {
        set_last_error("Cannot open input file: %s", av_err2str(ret));
        return ret;
    }
    ret = avformat_find_stream_info(probe, NULL);
    if (ret < 0) {
        set_last_error("Cannot read stream info: %s", av_err2str(ret));
        avformat_close_input(&probe);
        return ret;
    }

    int has_video = 0;
    for (unsigned i = 0; i < probe->nb_streams; i++) {
        if (probe->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) has_video = 1;
    }
    avformat_close_input(&probe);

    if (has_video) {
        /* Video file: try remux first (fast, no quality loss).
         * If remux fails (codec/container incompatibility), do full transcode. */
        ret = remux(in_path, out_path, env, callback, onProgress);
        if (ret < 0) {
            ALOGI("Remux failed (%s), trying full video transcode", av_err2str(ret));
            ret = transcode_video(in_path, out_path, env, callback, onProgress);
        }
        return ret;
    }

    /* Audio-only: try remux, fallback to audio transcode */
    ret = remux(in_path, out_path, env, callback, onProgress);
    if (ret < 0) {
        ALOGI("Remux failed (%s), trying audio transcode", av_err2str(ret));
        ret = transcode_audio(in_path, out_path, env, callback, onProgress);
    }
    return ret;
}

/* ====================================================================
 *  JNI Methods
 * ==================================================================== */

JNIEXPORT jstring JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_getVersion(
    JNIEnv *env, jclass clazz) {
    (void)clazz;
    char buf[128];
    unsigned v = avcodec_version();
    snprintf(buf, sizeof(buf), "FFmpeg libavcodec %d.%d.%d",
             (v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF);
    return (*env)->NewStringUTF(env, buf);
}

JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_convert(
    JNIEnv *env, jclass clazz,
    jstring j_input, jstring j_output, jobject callback) {
    (void)clazz;

    const char *input = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!input || !output) {
        if (input) (*env)->ReleaseStringUTFChars(env, j_input, input);
        if (output) (*env)->ReleaseStringUTFChars(env, j_output, output);
        throw_runtime(env, "Failed to get path strings");
        return -1;
    }

    ALOGI("Convert: %s -> %s", input, output);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    int ret = smart_convert(input, output, env, callback, onProgress);

    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);

    if (ret < 0 && ret != AVERROR_EXIT) {
        ALOGE("Conversion failed: %s", av_err2str(ret));
        if (g_last_error[0] == '\0') {
            set_last_error("Conversion failed: %s", av_err2str(ret));
        }
    }
    return ret;
}

JNIEXPORT void JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_cancel(
    JNIEnv *env, jclass clazz) {
    (void)env; (void)clazz;
    g_cancel = 1;
}

JNIEXPORT jstring JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_getLastError(
    JNIEnv *env, jclass clazz) {
    (void)clazz;
    return (*env)->NewStringUTF(env, g_last_error);
}

JNIEXPORT void JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_getMediaInfo(
    JNIEnv *env, jclass clazz, jstring j_path, jobject info) {
    (void)clazz;

    const char *path = (*env)->GetStringUTFChars(env, j_path, NULL);
    if (!path) return;

    AVFormatContext *fmt_ctx = NULL;
    int ret = avformat_open_input(&fmt_ctx, path, NULL, NULL);
    if (ret < 0) {
        (*env)->ReleaseStringUTFChars(env, j_path, path);
        return;
    }
    avformat_find_stream_info(fmt_ctx, NULL);

    jclass cls = (*env)->GetObjectClass(env, info);

    /* Duration in milliseconds */
    jfieldID fDuration = (*env)->GetFieldID(env, cls, "durationMs", "J");
    if (fDuration) {
        (*env)->SetLongField(env, info, fDuration,
            fmt_ctx->duration > 0 ? fmt_ctx->duration / 1000 : 0);
    }

    /* Format name */
    jfieldID fFormat = (*env)->GetFieldID(env, cls, "formatName", "Ljava/lang/String;");
    if (fFormat && fmt_ctx->iformat) {
        jstring name = (*env)->NewStringUTF(env, fmt_ctx->iformat->long_name ?
            fmt_ctx->iformat->long_name : fmt_ctx->iformat->name);
        (*env)->SetObjectField(env, info, fFormat, name);
        (*env)->DeleteLocalRef(env, name);
    }

    /* Codec info */
    for (unsigned i = 0; i < fmt_ctx->nb_streams; i++) {
        AVCodecParameters *par = fmt_ctx->streams[i]->codecpar;
        if (par->codec_type == AVMEDIA_TYPE_AUDIO) {
            jfieldID f = (*env)->GetFieldID(env, cls, "audioCodec", "Ljava/lang/String;");
            if (f) {
                const AVCodecDescriptor *desc = avcodec_descriptor_get(par->codec_id);
                jstring s = (*env)->NewStringUTF(env, desc ? desc->name : "unknown");
                (*env)->SetObjectField(env, info, f, s);
                (*env)->DeleteLocalRef(env, s);
            }
            jfieldID fsr = (*env)->GetFieldID(env, cls, "sampleRate", "I");
            if (fsr) (*env)->SetIntField(env, info, fsr, par->sample_rate);
            jfieldID fch = (*env)->GetFieldID(env, cls, "channels", "I");
            if (fch) (*env)->SetIntField(env, info, fch, par->ch_layout.nb_channels);
            jfieldID fbr = (*env)->GetFieldID(env, cls, "audioBitrate", "I");
            if (fbr) (*env)->SetIntField(env, info, fbr, (int)(par->bit_rate / 1000));
        } else if (par->codec_type == AVMEDIA_TYPE_VIDEO) {
            jfieldID f = (*env)->GetFieldID(env, cls, "videoCodec", "Ljava/lang/String;");
            if (f) {
                const AVCodecDescriptor *desc = avcodec_descriptor_get(par->codec_id);
                jstring s = (*env)->NewStringUTF(env, desc ? desc->name : "unknown");
                (*env)->SetObjectField(env, info, f, s);
                (*env)->DeleteLocalRef(env, s);
            }
            jfieldID fw = (*env)->GetFieldID(env, cls, "width", "I");
            if (fw) (*env)->SetIntField(env, info, fw, par->width);
            jfieldID fh = (*env)->GetFieldID(env, cls, "height", "I");
            if (fh) (*env)->SetIntField(env, info, fh, par->height);
            jfieldID fbr = (*env)->GetFieldID(env, cls, "videoBitrate", "I");
            if (fbr) (*env)->SetIntField(env, info, fbr, (int)(par->bit_rate / 1000));
        }
    }

    (*env)->DeleteLocalRef(env, cls);
    avformat_close_input(&fmt_ctx);
    (*env)->ReleaseStringUTFChars(env, j_path, path);
}

/* ====================================================================
 *  Extract Audio: remux audio stream only (no re-encoding).
 *  Faster and preserves original quality.
 * ==================================================================== */
static int extract_audio_remux(const char *in_path, const char *out_path,
                               JNIEnv *env, jobject callback, jmethodID onProgress) {
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket *pkt = NULL;
    int ret = 0, audio_idx = -1;

    if ((ret = avformat_open_input(&ifmt_ctx, in_path, NULL, NULL)) < 0) return ret;
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) goto ear_end;

    audio_idx = av_find_best_stream(ifmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
    if (audio_idx < 0) { ret = audio_idx; goto ear_end; }

    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_path);
    if (!ofmt_ctx) {
        /* .m4a needs ipod muxer; fallback to mp4 */
        avformat_alloc_output_context2(&ofmt_ctx, NULL, "mp4", out_path);
    }
    if (!ofmt_ctx) { ret = AVERROR_UNKNOWN; goto ear_end; }

    AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
    if (!out_stream) { ret = AVERROR(ENOMEM); goto ear_end; }
    ret = avcodec_parameters_copy(out_stream->codecpar, ifmt_ctx->streams[audio_idx]->codecpar);
    if (ret < 0) goto ear_end;
    out_stream->codecpar->codec_tag = 0;

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, out_path, AVIO_FLAG_WRITE);
        if (ret < 0) goto ear_end;
    }
    ret = write_header_faststart(ofmt_ctx);
    if (ret < 0) goto ear_end;

    pkt = av_packet_alloc();
    if (!pkt) { ret = AVERROR(ENOMEM); goto ear_end; }

    int64_t duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 1;
    int last_percent = -1;

    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;
        if (pkt->stream_index != audio_idx) { av_packet_unref(pkt); continue; }

        AVStream *in_s = ifmt_ctx->streams[audio_idx];
        if (pkt->pts == AV_NOPTS_VALUE) { av_packet_unref(pkt); continue; }
        int64_t pts_us = av_rescale_q(pkt->pts, in_s->time_base,
                                      (AVRational){1, AV_TIME_BASE});
        pkt->stream_index = 0;
        av_packet_rescale_ts(pkt, in_s->time_base, out_stream->time_base);
        pkt->pos = -1;

        ret = av_interleaved_write_frame(ofmt_ctx, pkt);
        if (ret < 0) break;

        if (callback && onProgress) {
            int pct = (int)(pts_us * 100 / duration);
            if (pct < 0) pct = 0; if (pct > 100) pct = 100;
            if (pct != last_percent) {
                last_percent = pct;
                (*env)->CallVoidMethod(env, callback, onProgress, pct);
                if ((*env)->ExceptionCheck(env)) {
                    (*env)->ExceptionClear(env);
                    ret = AVERROR_EXIT; break;
                }
            }
        }
    }

    if (g_cancel) { ret = AVERROR_EXIT; goto ear_end; }
    if (ret == AVERROR_EOF) ret = 0;
    if (ret >= 0) av_write_trailer(ofmt_ctx);

ear_end:
    av_packet_free(&pkt);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    return ret;
}

/* ====================================================================
 *  Extract Audio JNI: try remux first (fast), fallback to transcode.
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_extractAudio(
    JNIEnv *env, jclass clazz,
    jstring j_input, jstring j_output, jobject callback) {
    (void)clazz;

    const char *input = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!input || !output) {
        if (input) (*env)->ReleaseStringUTFChars(env, j_input, input);
        if (output) (*env)->ReleaseStringUTFChars(env, j_output, output);
        return -1;
    }

    ALOGI("ExtractAudio: %s -> %s", input, output);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    /* Try remux first (fast, no quality loss) */
    int ret = extract_audio_remux(input, output, env, callback, onProgress);
    if (ret < 0) {
        ALOGI("Audio remux failed (%s), trying transcode", av_err2str(ret));
        clear_last_error();
        ret = transcode_audio(input, output, env, callback, onProgress);
    }
    if (ret < 0 && g_last_error[0] == '\0')
        set_last_error("Audio extraction failed: %s", av_err2str(ret));

    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  Trim: remux with time range (no re-encoding).
 * ==================================================================== */
static int trim_remux(const char *in_path, const char *out_path,
                      int64_t start_ms, int64_t end_ms,
                      JNIEnv *env, jobject callback, jmethodID onProgress) {
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket *pkt = NULL;
    int ret = 0;
    int *stream_mapping = NULL;

    if ((ret = avformat_open_input(&ifmt_ctx, in_path, NULL, NULL)) < 0) return ret;
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) goto end;

    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_path);
    if (!ofmt_ctx) { ret = AVERROR_UNKNOWN; goto end; }

    int nb = ifmt_ctx->nb_streams;
    stream_mapping = av_calloc(nb, sizeof(int));
    if (!stream_mapping) { ret = AVERROR(ENOMEM); goto end; }

    int stream_idx = 0;
    for (int i = 0; i < nb; i++) {
        AVCodecParameters *par = ifmt_ctx->streams[i]->codecpar;
        if (par->codec_type != AVMEDIA_TYPE_AUDIO &&
            par->codec_type != AVMEDIA_TYPE_VIDEO &&
            par->codec_type != AVMEDIA_TYPE_SUBTITLE) {
            stream_mapping[i] = -1;
            continue;
        }
        stream_mapping[i] = stream_idx++;
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
        if (!out_stream) { ret = AVERROR(ENOMEM); goto end; }
        ret = avcodec_parameters_copy(out_stream->codecpar, ifmt_ctx->streams[i]->codecpar);
        if (ret < 0) goto end;
        out_stream->codecpar->codec_tag = 0;
    }

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, out_path, AVIO_FLAG_WRITE);
        if (ret < 0) goto end;
    }

    /* Seek to start position */
    int64_t start_ts = start_ms * 1000; /* ms -> us (AV_TIME_BASE) */
    if (start_ts > 0) {
        ret = av_seek_frame(ifmt_ctx, -1, start_ts, AVSEEK_FLAG_BACKWARD);
        if (ret < 0) ALOGI("Seek failed, starting from beginning");
    }

    ret = write_header_faststart(ofmt_ctx);
    if (ret < 0) goto end;

    pkt = av_packet_alloc();
    if (!pkt) { ret = AVERROR(ENOMEM); goto end; }

    int64_t end_ts = end_ms * 1000;
    int64_t duration = end_ts - start_ts;
    if (duration <= 0) duration = 1;
    int last_percent = -1;

    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;

        if (pkt->stream_index >= nb || stream_mapping[pkt->stream_index] < 0) {
            av_packet_unref(pkt);
            continue;
        }

        AVStream *in_s = ifmt_ctx->streams[pkt->stream_index];
        if (pkt->pts == AV_NOPTS_VALUE) { av_packet_unref(pkt); continue; }
        int64_t pts_us = av_rescale_q(pkt->pts, in_s->time_base,
                                      (AVRational){1, AV_TIME_BASE});

        /* Skip packets before start */
        if (pts_us < start_ts) {
            av_packet_unref(pkt);
            continue;
        }
        /* Stop at end */
        if (pts_us >= end_ts) {
            av_packet_unref(pkt);
            break;
        }

        int out_idx = stream_mapping[pkt->stream_index];
        AVStream *out_s = ofmt_ctx->streams[out_idx];

        /* Offset timestamps so output starts at 0 */
        pkt->pts = av_rescale_q(pts_us - start_ts,
                                (AVRational){1, AV_TIME_BASE}, out_s->time_base);
        int64_t dts_us = pkt->dts != AV_NOPTS_VALUE
            ? av_rescale_q(pkt->dts, in_s->time_base, (AVRational){1, AV_TIME_BASE})
            : pts_us;
        /* Clamp DTS offset to >= 0: for B-frames DTS can precede start_ts */
        int64_t dts_offset = dts_us - start_ts;
        if (dts_offset < 0) dts_offset = 0;
        pkt->dts = av_rescale_q(dts_offset,
                                (AVRational){1, AV_TIME_BASE}, out_s->time_base);
        pkt->stream_index = out_idx;
        pkt->pos = -1;

        ret = av_interleaved_write_frame(ofmt_ctx, pkt);
        if (ret < 0) break;

        /* Progress */
        if (callback && onProgress) {
            int pct = (int)((pts_us - start_ts) * 100 / duration);
            if (pct < 0) pct = 0; if (pct > 100) pct = 100;
            if (pct != last_percent) {
                last_percent = pct;
                (*env)->CallVoidMethod(env, callback, onProgress, pct);
                if ((*env)->ExceptionCheck(env)) {
                    (*env)->ExceptionClear(env);
                    ret = AVERROR_EXIT; break;
                }
            }
        }
    }

    if (g_cancel) { ret = AVERROR_EXIT; goto end; }
    if (ret == AVERROR_EOF || ret >= 0) {
        av_write_trailer(ofmt_ctx);
        ret = 0;
    }
    /* else: keep ret as the error code */

end:
    av_packet_free(&pkt);
    av_free(stream_mapping);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_trim(
    JNIEnv *env, jclass clazz,
    jstring j_input, jstring j_output, jlong startMs, jlong endMs, jobject callback) {
    (void)clazz;

    const char *input = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!input || !output) {
        if (input) (*env)->ReleaseStringUTFChars(env, j_input, input);
        if (output) (*env)->ReleaseStringUTFChars(env, j_output, output);
        return -1;
    }

    ALOGI("Trim: %s -> %s [%lld-%lld ms]", input, output, (long long)startMs, (long long)endMs);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    int ret = trim_remux(input, output, (int64_t)startMs, (int64_t)endMs,
                         env, callback, onProgress);
    if (ret < 0 && g_last_error[0] == '\0')
        set_last_error("Trim failed: %s", av_err2str(ret));

    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  Video Compress: re-encode video at target bitrate, resolution, framerate.
 *  targetWidth/targetHeight = 0 means keep original.
 *  targetFps = 0 means keep original.
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_videoCompress(
    JNIEnv *env, jclass clazz,
    jstring j_input, jstring j_output, jint targetBitrateKbps,
    jint targetWidth, jint targetHeight, jint targetFps,
    jobject callback) {
    (void)clazz;

    const char *input = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!input || !output) {
        if (input) (*env)->ReleaseStringUTFChars(env, j_input, input);
        if (output) (*env)->ReleaseStringUTFChars(env, j_output, output);
        return -1;
    }

    ALOGI("VideoCompress: %s -> %s @ %d kbps, res=%dx%d, fps=%d",
          input, output, targetBitrateKbps, targetWidth, targetHeight, targetFps);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVCodecContext *dec_ctx = NULL, *enc_ctx = NULL;
    AVPacket *pkt = NULL, *out_pkt = NULL;
    AVFrame *frame = NULL;
    struct SwsContext *sws_ctx = NULL;
    int ret = 0, video_idx = -1;
    int is_hw_encoder = 0;

    if ((ret = avformat_open_input(&ifmt_ctx, input, NULL, NULL)) < 0) {
        set_last_error("Cannot open input: %s", av_err2str(ret)); goto vc_end;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) {
        set_last_error("Cannot read stream info: %s", av_err2str(ret)); goto vc_end;
    }

    video_idx = av_find_best_stream(ifmt_ctx, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
    if (video_idx < 0) { ret = video_idx; set_last_error("No video stream found"); goto vc_end; }

    /* Use HW decoder if available */
    if ((ret = open_video_decoder_hw(ifmt_ctx, video_idx, &dec_ctx)) < 0) {
        set_last_error("Video decoder init failed: %s", av_err2str(ret)); goto vc_end;
    }

    /* Apply target resolution (0 = keep original, preserve aspect ratio) */
    int orig_dec_w = dec_ctx->width;
    int orig_dec_h = dec_ctx->height;
    if (targetWidth > 0 && targetHeight > 0) {
        dec_ctx->width  = ((int)targetWidth  + 1) & ~1;
        dec_ctx->height = ((int)targetHeight + 1) & ~1;
    } else if (targetWidth > 0) {
        int tw = ((int)targetWidth + 1) & ~1;
        int th = ((int)((double)orig_dec_h / orig_dec_w * tw) + 1) & ~1;
        if (th < 2) th = 2;
        dec_ctx->width = tw;
        dec_ctx->height = th;
    } else if (targetHeight > 0) {
        int th = ((int)targetHeight + 1) & ~1;
        int tw = ((int)((double)orig_dec_w / orig_dec_h * th) + 1) & ~1;
        if (tw < 2) tw = 2;
        dec_ctx->width = tw;
        dec_ctx->height = th;
    }

    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, output);
    if (!ofmt_ctx) { ret = AVERROR_UNKNOWN; set_last_error("Cannot create output context"); goto vc_end; }

    /* Open encoder with HW fallback */
    ret = open_video_encoder_with_fallback(AV_CODEC_ID_H264, dec_ctx, ifmt_ctx, video_idx,
                                           ofmt_ctx->oformat,
                                           (int64_t)targetBitrateKbps * 1000,
                                           &enc_ctx, &is_hw_encoder);
    /* Restore original decoder dimensions for sws scaling */
    dec_ctx->width  = orig_dec_w;
    dec_ctx->height = orig_dec_h;
    if (ret < 0) { set_last_error("H.264 encoder init failed: %s", av_err2str(ret)); goto vc_end; }

    AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
    if (!out_stream) { ret = AVERROR(ENOMEM); goto vc_end; }

    avcodec_parameters_from_context(out_stream->codecpar, enc_ctx);
    out_stream->time_base = enc_ctx->time_base;

    /* Copy audio stream if present */
    int audio_idx = -1;
    int audio_out_idx = -1;
    for (unsigned i = 0; i < ifmt_ctx->nb_streams; i++) {
        if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_idx = i;
            AVStream *a_out = avformat_new_stream(ofmt_ctx, NULL);
            if (a_out) {
                avcodec_parameters_copy(a_out->codecpar, ifmt_ctx->streams[i]->codecpar);
                a_out->codecpar->codec_tag = 0;
                audio_out_idx = a_out->index;
            }
            break;
        }
    }

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, output, AVIO_FLAG_WRITE);
        if (ret < 0) goto vc_end;
    }
    if ((ret = write_header_faststart(ofmt_ctx)) < 0) goto vc_end;

    pkt = av_packet_alloc();
    out_pkt = av_packet_alloc();
    frame = av_frame_alloc();
    if (!pkt || !out_pkt || !frame) { ret = AVERROR(ENOMEM); goto vc_end; }

    /* sws_ctx lazily initialized from first decoded frame to handle HW pixel formats */

    int64_t duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 1;
    int last_percent = -1;
    int sws_needs_init = 1;

    /* Frame dropping for target FPS */
    AVRational src_fps = av_guess_frame_rate(ifmt_ctx, ifmt_ctx->streams[video_idx], NULL);
    double src_fps_d = (src_fps.num > 0 && src_fps.den > 0) ? av_q2d(src_fps) : 30.0;
    double target_fps_d = (targetFps > 0) ? (double)targetFps : src_fps_d;
    double frame_interval_us = 1000000.0 / target_fps_d;  /* target interval in microseconds */
    int64_t next_output_us = 0;
    int64_t output_frame_count = 0;

    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;

        if (pkt->stream_index == audio_idx && audio_out_idx >= 0) {
            /* Pass through audio */
            AVStream *in_s = ifmt_ctx->streams[audio_idx];
            AVStream *out_s = ofmt_ctx->streams[audio_out_idx];
            pkt->stream_index = audio_out_idx;
            av_packet_rescale_ts(pkt, in_s->time_base, out_s->time_base);
            pkt->pos = -1;
            int wr = av_interleaved_write_frame(ofmt_ctx, pkt);
            if (wr < 0) {
                set_last_error("Audio passthrough write error: %s", av_err2str(wr));
                ret = wr; break;
            }
            continue;
        }

        if (pkt->stream_index != video_idx) { av_packet_unref(pkt); continue; }

        ret = avcodec_send_packet(dec_ctx, pkt);
        av_packet_unref(pkt);
        if (ret < 0) {
            /* Skip corrupt packets from HW decoder instead of aborting */
            if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
            break;
        }

        while (ret >= 0 && !g_cancel) {
            ret = avcodec_receive_frame(dec_ctx, frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
            if (ret < 0) {
                /* Skip corrupt frames instead of aborting */
                if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
                break;
            }

            /* Frame dropping for target FPS */
            if (targetFps > 0 && frame->pts != AV_NOPTS_VALUE) {
                int64_t frame_us = av_rescale_q(frame->pts,
                    ifmt_ctx->streams[video_idx]->time_base,
                    (AVRational){1, AV_TIME_BASE});
                if (frame_us < next_output_us) {
                    av_frame_unref(frame);
                    continue;  /* drop this frame */
                }
                next_output_us = frame_us + (int64_t)frame_interval_us;
            }

            AVFrame *src_frame = frame;
            AVFrame *sw_frame = NULL;

            /* Transfer HW frames to CPU */
            if (frame->format == AV_PIX_FMT_MEDIACODEC || frame->hw_frames_ctx != NULL) {
                sw_frame = av_frame_alloc();
                if (sw_frame && av_hwframe_transfer_data(sw_frame, frame, 0) >= 0) {
                    sw_frame->pts = frame->pts;
                    src_frame = sw_frame;
                } else {
                    av_frame_free(&sw_frame);
                    sw_frame = NULL;
                }
            }

            /* Lazy sws_ctx init from first actual decoded frame */
            if (sws_needs_init) {
                enum AVPixelFormat actual_pf = (enum AVPixelFormat)src_frame->format;
                if (actual_pf == AV_PIX_FMT_NONE || actual_pf == AV_PIX_FMT_MEDIACODEC)
                    actual_pf = AV_PIX_FMT_NV12;
                /* Need sws if pixel format differs OR dimensions differ */
                if (actual_pf != enc_ctx->pix_fmt ||
                    src_frame->width != enc_ctx->width ||
                    src_frame->height != enc_ctx->height) {
                    sws_ctx = sws_getContext(src_frame->width, src_frame->height, actual_pf,
                                            enc_ctx->width, enc_ctx->height, enc_ctx->pix_fmt,
                                            SWS_BILINEAR, NULL, NULL, NULL);
                    if (!sws_ctx) {
                        set_last_error("sws_getContext failed (src=%d dst=%d)", actual_pf, enc_ctx->pix_fmt);
                        if (sw_frame) av_frame_free(&sw_frame);
                        av_frame_unref(frame);
                        ret = AVERROR_UNKNOWN; break;
                    }
                }
                sws_needs_init = 0;
            }

            AVFrame *enc_frame = src_frame;
            AVFrame *conv_frame = NULL;
            if (sws_ctx) {
                conv_frame = av_frame_alloc();
                if (!conv_frame) {
                    if (sw_frame) av_frame_free(&sw_frame);
                    av_frame_unref(frame);
                    ret = AVERROR(ENOMEM); break;
                }
                conv_frame->width = enc_ctx->width;
                conv_frame->height = enc_ctx->height;
                conv_frame->format = enc_ctx->pix_fmt;
                av_frame_get_buffer(conv_frame, 0);
                sws_scale(sws_ctx, (const uint8_t *const *)src_frame->data, src_frame->linesize,
                          0, src_frame->height, conv_frame->data, conv_frame->linesize);
                conv_frame->pts = frame->pts;
                enc_frame = conv_frame;
            }

            int64_t saved_pts = frame->pts;
            /* Rescale pts from input stream time_base to encoder time_base */
            enc_frame->pts = av_rescale_q(enc_frame->pts,
                ifmt_ctx->streams[video_idx]->time_base, enc_ctx->time_base);
            ret = avcodec_send_frame(enc_ctx, enc_frame);
            if (conv_frame) av_frame_free(&conv_frame);
            if (sw_frame) av_frame_free(&sw_frame);
            av_frame_unref(frame);
            if (ret < 0) break;

            while (ret >= 0) {
                ret = avcodec_receive_packet(enc_ctx, out_pkt);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                if (ret < 0) break;
                out_pkt->stream_index = 0;
                av_packet_rescale_ts(out_pkt, enc_ctx->time_base, out_stream->time_base);
                int wr = av_interleaved_write_frame(ofmt_ctx, out_pkt);
                if (wr < 0) {
                    set_last_error("Video write error: %s", av_err2str(wr));
                    ret = wr; break;
                }
            }

            if (callback && onProgress && saved_pts != AV_NOPTS_VALUE) {
                int64_t pts_us = av_rescale_q(saved_pts,
                    ifmt_ctx->streams[video_idx]->time_base,
                    (AVRational){1, AV_TIME_BASE});
                int pct = (int)(pts_us * 100 / duration);
                if (pct < 0) pct = 0; if (pct > 100) pct = 100;
                if (pct != last_percent) {
                    last_percent = pct;
                    (*env)->CallVoidMethod(env, callback, onProgress, pct);
                    if ((*env)->ExceptionCheck(env)) {
                        (*env)->ExceptionClear(env);
                        ret = AVERROR_EXIT; break;
                    }
                }
            }
        }
    }

    /* Flush encoder */
    if (!g_cancel && ret >= 0) {
        avcodec_send_frame(enc_ctx, NULL);
        while (1) {
            int fr = avcodec_receive_packet(enc_ctx, out_pkt);
            if (fr == AVERROR_EOF || fr < 0) break;
            out_pkt->stream_index = 0;
            av_packet_rescale_ts(out_pkt, enc_ctx->time_base, out_stream->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }

    if (g_cancel) { ret = AVERROR_EXIT; set_last_error("Operation cancelled"); }
    else if (ret == AVERROR_EOF || ret >= 0) { av_write_trailer(ofmt_ctx); ret = 0; }
    else if (g_last_error[0] == '\0') { set_last_error("Video compress failed: %s", av_err2str(ret)); }

vc_end:
    if (sws_ctx) sws_freeContext(sws_ctx);
    av_frame_free(&frame);
    av_packet_free(&pkt);
    av_packet_free(&out_pkt);
    avcodec_free_context(&dec_ctx);
    avcodec_free_context(&enc_ctx);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);

    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  Normalize Video: re-encode to H.264+AAC at target resolution/bitrate.
 *  Used to prepare files with different codecs/resolutions for merging.
 *  targetWidth/targetHeight of 0 means keep original resolution.
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_normalizeVideo(
    JNIEnv *env, jclass clazz,
    jstring j_input, jstring j_output,
    jint targetWidth, jint targetHeight, jint targetBitrateKbps,
    jobject callback) {
    (void)clazz;

    const char *input = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!input || !output) {
        if (input) (*env)->ReleaseStringUTFChars(env, j_input, input);
        if (output) (*env)->ReleaseStringUTFChars(env, j_output, output);
        return -1;
    }

    ALOGI("NormalizeVideo: %s -> %s [%dx%d @ %d kbps]", input, output,
          targetWidth, targetHeight, targetBitrateKbps);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVCodecContext *vdec_ctx = NULL, *venc_ctx = NULL;
    AVCodecContext *adec_ctx = NULL, *aenc_ctx = NULL;
    SwrContext *swr_ctx = NULL;
    struct SwsContext *sws_ctx = NULL;
    AVPacket *pkt = NULL, *out_pkt = NULL;
    AVFrame *frame = NULL, *aframe = NULL, *afilt_frame = NULL;
    int ret = 0;
    int video_idx = -1, audio_idx = -1;
    int vout_idx = -1, aout_idx = -1;
    int is_hw_encoder = 0;
    int64_t audio_pts = 0;
    int out_w = 0, out_h = 0;   /* target output canvas dimensions */
    int orig_w = 0, orig_h = 0; /* original input dimensions */

    if ((ret = avformat_open_input(&ifmt_ctx, input, NULL, NULL)) < 0) { set_last_error("Cannot open input: %s", av_err2str(ret)); goto nv_end; }
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) { set_last_error("Cannot read stream info: %s", av_err2str(ret)); goto nv_end; }

    for (unsigned i = 0; i < ifmt_ctx->nb_streams; i++) {
        if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && video_idx < 0)
            video_idx = i;
        else if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO && audio_idx < 0)
            audio_idx = i;
    }

    /* Output as MP4 */
    avformat_alloc_output_context2(&ofmt_ctx, NULL, "mp4", output);
    if (!ofmt_ctx) { set_last_error("Cannot create MP4 output context"); ret = AVERROR_UNKNOWN; goto nv_end; }

    /* ---- Video setup ---- */
    if (video_idx >= 0) {
        if ((ret = open_video_decoder_hw(ifmt_ctx, video_idx, &vdec_ctx)) < 0) {
            ALOGE("NormalizeVideo: video decoder init failed: %s", av_err2str(ret));
            set_last_error("Video decoder init failed: %s", av_err2str(ret)); goto nv_end;
        }
        int out_w_tmp = targetWidth > 0 ? targetWidth : vdec_ctx->width;
        int out_h_tmp = targetHeight > 0 ? targetHeight : vdec_ctx->height;
        /* Ensure even dimensions */
        out_w = (out_w_tmp + 1) & ~1;
        out_h = (out_h_tmp + 1) & ~1;

        int64_t vbitrate = targetBitrateKbps > 0
            ? (int64_t)targetBitrateKbps * 1000
            : (int64_t)out_w * out_h * 3;
        if (vbitrate < 500000) vbitrate = 500000;
        if (vbitrate > 8000000) vbitrate = 8000000;

        /* Temporarily override dec_ctx dimensions for encoder setup */
        orig_w = vdec_ctx->width;
        orig_h = vdec_ctx->height;
        vdec_ctx->width = out_w;
        vdec_ctx->height = out_h;

        ret = open_video_encoder_with_fallback(AV_CODEC_ID_H264, vdec_ctx,
            ifmt_ctx, video_idx, ofmt_ctx->oformat, vbitrate,
            &venc_ctx, &is_hw_encoder);
        vdec_ctx->width = orig_w;
        vdec_ctx->height = orig_h;
        if (ret < 0) {
            ALOGE("NormalizeVideo: H.264 encoder init FAILED: %s (target %dx%d)",
                  av_err2str(ret), out_w, out_h);
            set_last_error("H.264 encoder init failed (%dx%d): %s", out_w, out_h, av_err2str(ret)); goto nv_end;
        }
        AVStream *vs = avformat_new_stream(ofmt_ctx, NULL);
        if (!vs) { ret = AVERROR(ENOMEM); goto nv_end; }
        avcodec_parameters_from_context(vs->codecpar, venc_ctx);
        vs->time_base = venc_ctx->time_base;
        vout_idx = vs->index;

        /*
         * Deferred sws_ctx creation: will be created on first decoded frame
         * using the actual frame pixel format (not dec_ctx->pix_fmt which may
         * be opaque MEDIACODEC format or uninitialized).
         *
         * Letterbox/pillarbox: we compute fit dimensions preserving aspect ratio
         * and center the scaled image on a black canvas.
         */
        /* sws_ctx = NULL here – created lazily in the decode loop */
    }

    /* ---- Audio setup: always transcode to AAC ---- */
    if (audio_idx >= 0) {
        const AVCodec *adec = avcodec_find_decoder(
            ifmt_ctx->streams[audio_idx]->codecpar->codec_id);
        if (adec) {
            adec_ctx = avcodec_alloc_context3(adec);
            avcodec_parameters_to_context(adec_ctx, ifmt_ctx->streams[audio_idx]->codecpar);
            if (avcodec_open2(adec_ctx, adec, NULL) < 0) {
                avcodec_free_context(&adec_ctx); adec_ctx = NULL;
            }
        }
        if (adec_ctx) {
            int ar = open_audio_encoder_with_resampler(
                AV_CODEC_ID_AAC, adec_ctx,
                ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER,
                &aenc_ctx, &swr_ctx);
            if (ar >= 0) {
                AVStream *as = avformat_new_stream(ofmt_ctx, NULL);
                if (as) {
                    avcodec_parameters_from_context(as->codecpar, aenc_ctx);
                    as->time_base = aenc_ctx->time_base;
                    aout_idx = as->index;
                }
            } else {
                ALOGI("Audio encoder setup failed, dropping audio");
            }
        }
    }

    if (vout_idx < 0 && aout_idx < 0) { ret = AVERROR_STREAM_NOT_FOUND; goto nv_end; }

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, output, AVIO_FLAG_WRITE);
        if (ret < 0) goto nv_end;
    }
    if ((ret = write_header_faststart(ofmt_ctx)) < 0) goto nv_end;

    pkt = av_packet_alloc();
    out_pkt = av_packet_alloc();
    frame = av_frame_alloc();
    aframe = av_frame_alloc();
    afilt_frame = av_frame_alloc();
    if (!pkt || !out_pkt || !frame || !aframe || !afilt_frame) {
        ret = AVERROR(ENOMEM); goto nv_end;
    }

    int64_t duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 1;
    int last_percent = -1;

    /* Letterbox geometry – computed on first frame */
    int fit_w = 0, fit_h = 0;     /* scaled image size (aspect-preserved) */
    int pad_x = 0, pad_y = 0;     /* offset to center on canvas */
    int sws_initialized = 0;

    /* ==== Main loop ==== */
    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;

        /* --- Video --- */
        if (pkt->stream_index == video_idx && vout_idx >= 0 && vdec_ctx && venc_ctx) {
            ret = avcodec_send_packet(vdec_ctx, pkt);
            av_packet_unref(pkt);
            if (ret < 0) {
                if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
                break;
            }

            while (ret >= 0 && !g_cancel) {
                ret = avcodec_receive_frame(vdec_ctx, frame);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                if (ret < 0) {
                    if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
                    break;
                }

                int64_t saved_pts = frame->pts;
                AVFrame *enc_frame = frame;
                AVFrame *conv = NULL;
                AVFrame *sw_frame = NULL;

                /* --- Handle HW (opaque) frames: transfer to CPU --- */
                AVFrame *src_frame = frame;
                if (frame->format == AV_PIX_FMT_MEDIACODEC ||
                    frame->hw_frames_ctx != NULL) {
                    sw_frame = av_frame_alloc();
                    if (!sw_frame) { av_frame_unref(frame); ret = AVERROR(ENOMEM); break; }
                    ret = av_hwframe_transfer_data(sw_frame, frame, 0);
                    if (ret < 0) {
                        ALOGE("NormalizeVideo: hw transfer failed: %s, falling back to raw frame",
                              av_err2str(ret));
                        av_frame_free(&sw_frame);
                        sw_frame = NULL;
                        /* fall through – try the raw frame anyway */
                    } else {
                        sw_frame->pts = frame->pts;
                        sw_frame->pkt_dts = frame->pkt_dts;
                        src_frame = sw_frame;
                    }
                }

                /* --- Lazy sws_ctx init on first decoded frame --- */
                int src_w = src_frame->width;
                int src_h = src_frame->height;
                enum AVPixelFormat src_pix_fmt = (enum AVPixelFormat)src_frame->format;

                if (!sws_initialized && venc_ctx) {
                    /* Compute letterbox/pillarbox geometry:
                     * Scale src as large as possible within out_w×out_h,
                     * preserving aspect ratio. */
                    double scale_w = (double)out_w / src_w;
                    double scale_h = (double)out_h / src_h;
                    double scale = scale_w < scale_h ? scale_w : scale_h;
                    fit_w = ((int)(src_w * scale + 0.5)) & ~1;  /* ensure even */
                    fit_h = ((int)(src_h * scale + 0.5)) & ~1;
                    if (fit_w > out_w) fit_w = out_w;
                    if (fit_h > out_h) fit_h = out_h;
                    pad_x = ((out_w - fit_w) / 2) & ~1;  /* even offset for YUV */
                    pad_y = ((out_h - fit_h) / 2) & ~1;

                    /* Handle unknown/invalid pixel format from HW decoder fallback */
                    if (src_pix_fmt == AV_PIX_FMT_NONE || src_pix_fmt == AV_PIX_FMT_MEDIACODEC) {
                        src_pix_fmt = AV_PIX_FMT_NV12;
                    }

                    sws_ctx = sws_getContext(src_w, src_h, src_pix_fmt,
                                            fit_w, fit_h, venc_ctx->pix_fmt,
                                            SWS_BILINEAR, NULL, NULL, NULL);
                    if (!sws_ctx) {
                        ALOGE("NormalizeVideo: sws_getContext failed for %d->%d %dx%d->%dx%d",
                              src_pix_fmt, venc_ctx->pix_fmt, src_w, src_h, fit_w, fit_h);
                        if (sw_frame) av_frame_free(&sw_frame);
                        av_frame_unref(frame);
                        ret = AVERROR_UNKNOWN;
                        break;
                    }
                    sws_initialized = 1;
                }

                /* --- Scale + Letterbox --- */
                if (sws_ctx) {
                    conv = av_frame_alloc();
                    if (!conv) {
                        if (sw_frame) av_frame_free(&sw_frame);
                        av_frame_unref(frame); ret = AVERROR(ENOMEM); break;
                    }
                    conv->width = out_w;
                    conv->height = out_h;
                    conv->format = venc_ctx->pix_fmt;
                    av_frame_get_buffer(conv, 0);

                    /* Fill canvas with black (Y=0, U=V=128 for YUV) */
                    memset(conv->data[0], 0, (size_t)conv->linesize[0] * out_h);    /* Y plane */
                    if (conv->data[1] && conv->data[2]) {
                        /* Planar YUV (YUV420P etc.) */
                        int chroma_h = out_h / 2;
                        memset(conv->data[1], 128, (size_t)conv->linesize[1] * chroma_h); /* U */
                        memset(conv->data[2], 128, (size_t)conv->linesize[2] * chroma_h); /* V */
                    } else if (conv->data[1]) {
                        /* Semi-planar (NV12/NV21): interleaved UV */
                        int chroma_h = out_h / 2;
                        memset(conv->data[1], 128, (size_t)conv->linesize[1] * chroma_h); /* UV */
                    }

                    /* Scale into the centered region */
                    uint8_t *dst_data[4];
                    int dst_linesize[4];
                    for (int p = 0; p < 4; p++) {
                        dst_data[p] = conv->data[p];
                        dst_linesize[p] = conv->linesize[p];
                    }
                    /* Offset Y plane */
                    if (dst_data[0])
                        dst_data[0] += pad_y * dst_linesize[0] + pad_x;
                    /* Offset chroma planes (half resolution for 4:2:0) */
                    if (dst_data[1] && dst_data[2]) {
                        /* Planar YUV */
                        dst_data[1] += (pad_y / 2) * dst_linesize[1] + (pad_x / 2);
                        dst_data[2] += (pad_y / 2) * dst_linesize[2] + (pad_x / 2);
                    } else if (dst_data[1]) {
                        /* Semi-planar NV12: U/V interleaved, so x offset is pad_x (not /2) */
                        dst_data[1] += (pad_y / 2) * dst_linesize[1] + (pad_x & ~1);
                    }

                    sws_scale(sws_ctx,
                        (const uint8_t *const *)src_frame->data, src_frame->linesize,
                        0, src_h, dst_data, dst_linesize);
                    conv->pts = frame->pts;
                    enc_frame = conv;
                }

                if (sw_frame) av_frame_free(&sw_frame);

                /* Rescale pts from input stream time_base to encoder time_base */
                enc_frame->pts = av_rescale_q(enc_frame->pts,
                    ifmt_ctx->streams[video_idx]->time_base, venc_ctx->time_base);
                ret = avcodec_send_frame(venc_ctx, enc_frame);
                if (conv) av_frame_free(&conv);
                av_frame_unref(frame);
                if (ret < 0) break;

                while (ret >= 0) {
                    ret = avcodec_receive_packet(venc_ctx, out_pkt);
                    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                    if (ret < 0) break;
                    out_pkt->stream_index = vout_idx;
                    av_packet_rescale_ts(out_pkt, venc_ctx->time_base,
                        ofmt_ctx->streams[vout_idx]->time_base);
                    av_interleaved_write_frame(ofmt_ctx, out_pkt);
                }

                if (callback && onProgress && saved_pts != AV_NOPTS_VALUE) {
                    int64_t pts_us = av_rescale_q(saved_pts,
                        ifmt_ctx->streams[video_idx]->time_base,
                        (AVRational){1, AV_TIME_BASE});
                    int pct = (int)(pts_us * 100 / duration);
                    if (pct < 0) pct = 0; if (pct > 100) pct = 100;
                    if (pct != last_percent) {
                        last_percent = pct;
                        (*env)->CallVoidMethod(env, callback, onProgress, pct);
                        if ((*env)->ExceptionCheck(env)) {
                            (*env)->ExceptionClear(env); ret = AVERROR_EXIT; break;
                        }
                    }
                }
            }
        }
        /* --- Audio --- */
        else if (pkt->stream_index == audio_idx && aout_idx >= 0 && adec_ctx && aenc_ctx) {
            ret = avcodec_send_packet(adec_ctx, pkt);
            av_packet_unref(pkt);
            if (ret < 0) { ret = 0; continue; }

            while (ret >= 0 && !g_cancel) {
                ret = avcodec_receive_frame(adec_ctx, aframe);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                if (ret < 0) break;

                afilt_frame->sample_rate = aenc_ctx->sample_rate;
                av_channel_layout_copy(&afilt_frame->ch_layout, &aenc_ctx->ch_layout);
                afilt_frame->format = aenc_ctx->sample_fmt;
                afilt_frame->nb_samples = swr_get_out_samples(swr_ctx, aframe->nb_samples);
                av_frame_get_buffer(afilt_frame, 0);

                int converted = swr_convert(swr_ctx,
                    afilt_frame->data, afilt_frame->nb_samples,
                    (const uint8_t **)aframe->data, aframe->nb_samples);
                afilt_frame->nb_samples = converted;
                afilt_frame->pts = audio_pts;
                audio_pts += converted;
                av_frame_unref(aframe);

                ret = avcodec_send_frame(aenc_ctx, afilt_frame);
                av_frame_unref(afilt_frame);
                if (ret < 0) break;

                while (ret >= 0) {
                    ret = avcodec_receive_packet(aenc_ctx, out_pkt);
                    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                    if (ret < 0) break;
                    out_pkt->stream_index = aout_idx;
                    av_packet_rescale_ts(out_pkt, aenc_ctx->time_base,
                        ofmt_ctx->streams[aout_idx]->time_base);
                    av_interleaved_write_frame(ofmt_ctx, out_pkt);
                }
            }
        } else {
            av_packet_unref(pkt);
        }
    }

    /* Flush video encoder */
    if (!g_cancel && venc_ctx) {
        avcodec_send_frame(venc_ctx, NULL);
        while (1) {
            ret = avcodec_receive_packet(venc_ctx, out_pkt);
            if (ret == AVERROR_EOF || ret < 0) break;
            out_pkt->stream_index = vout_idx;
            av_packet_rescale_ts(out_pkt, venc_ctx->time_base,
                ofmt_ctx->streams[vout_idx]->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }
    /* Flush audio encoder */
    if (!g_cancel && aenc_ctx) {
        avcodec_send_frame(aenc_ctx, NULL);
        while (1) {
            ret = avcodec_receive_packet(aenc_ctx, out_pkt);
            if (ret == AVERROR_EOF || ret < 0) break;
            out_pkt->stream_index = aout_idx;
            av_packet_rescale_ts(out_pkt, aenc_ctx->time_base,
                ofmt_ctx->streams[aout_idx]->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }

    if (g_cancel) { ret = AVERROR_EXIT; set_last_error("Operation cancelled"); }
    else if (ret == AVERROR_EOF || ret >= 0) { av_write_trailer(ofmt_ctx); ret = 0; }
    else { set_last_error("Normalize encode error: %s", av_err2str(ret)); }

nv_end:
    if (sws_ctx) sws_freeContext(sws_ctx);
    swr_free(&swr_ctx);
    av_frame_free(&frame);
    av_frame_free(&aframe);
    av_frame_free(&afilt_frame);
    av_packet_free(&pkt);
    av_packet_free(&out_pkt);
    avcodec_free_context(&vdec_ctx);
    avcodec_free_context(&venc_ctx);
    avcodec_free_context(&adec_ctx);
    avcodec_free_context(&aenc_ctx);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);

    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  Video Snapshot: extract single frame at given timestamp.
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_videoSnapshot(
    JNIEnv *env, jclass clazz,
    jstring j_input, jstring j_output, jlong timeMs) {
    (void)clazz;

    const char *input = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!input || !output) {
        if (input) (*env)->ReleaseStringUTFChars(env, j_input, input);
        if (output) (*env)->ReleaseStringUTFChars(env, j_output, output);
        return -1;
    }

    ALOGI("VideoSnapshot: %s @ %lld ms -> %s", input, (long long)timeMs, output);
    g_cancel = 0;

    AVFormatContext *fmt_ctx = NULL;
    AVCodecContext *dec_ctx = NULL;
    AVPacket *pkt = NULL;
    AVFrame *frame = NULL, *rgb_frame = NULL;
    struct SwsContext *sws_ctx = NULL;
    int ret = 0, video_idx = -1;

    if ((ret = avformat_open_input(&fmt_ctx, input, NULL, NULL)) < 0) goto vs_end;
    if ((ret = avformat_find_stream_info(fmt_ctx, NULL)) < 0) goto vs_end;

    video_idx = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
    if (video_idx < 0) { ret = video_idx; goto vs_end; }

    /* Use HW decoder if available */
    if ((ret = open_video_decoder_hw(fmt_ctx, video_idx, &dec_ctx)) < 0) goto vs_end;

    /* Seek to target time */
    int64_t target_ts = timeMs * 1000;
    if (target_ts > 0) {
        av_seek_frame(fmt_ctx, -1, target_ts, AVSEEK_FLAG_BACKWARD);
        avcodec_flush_buffers(dec_ctx);
    }

    pkt = av_packet_alloc();
    frame = av_frame_alloc();
    if (!pkt || !frame) { ret = AVERROR(ENOMEM); goto vs_end; }

    /* Decode frame at target timestamp.
     * After seeking backward, skip frames until we reach or pass the target. */
    int got_frame = 0;
    while (!got_frame) {
        ret = av_read_frame(fmt_ctx, pkt);
        if (ret < 0) break;
        if (pkt->stream_index != video_idx) { av_packet_unref(pkt); continue; }

        ret = avcodec_send_packet(dec_ctx, pkt);
        av_packet_unref(pkt);
        if (ret < 0) break;

        while (1) {
            ret = avcodec_receive_frame(dec_ctx, frame);
            if (ret == AVERROR(EAGAIN)) break; /* need more packets */
            if (ret < 0) goto vs_end;

            /* Check if this frame is at or past the target time */
            int64_t frame_us = av_rescale_q(frame->pts,
                fmt_ctx->streams[video_idx]->time_base,
                (AVRational){1, AV_TIME_BASE});
            if (frame_us >= target_ts || target_ts == 0) {
                got_frame = 1;
                break;
            }
            /* Frame is before target, keep decoding */
            av_frame_unref(frame);
        }
    }

    if (!got_frame) { ret = -1; goto vs_end; }

    /* Encode to MJPEG */
    const AVCodec *mjpeg_enc = avcodec_find_encoder(AV_CODEC_ID_MJPEG);
    if (!mjpeg_enc) { ret = AVERROR_ENCODER_NOT_FOUND; goto vs_end; }

    AVCodecContext *jpg_ctx = avcodec_alloc_context3(mjpeg_enc);
    if (!jpg_ctx) { ret = AVERROR(ENOMEM); goto vs_end; }
    jpg_ctx->width = dec_ctx->width;
    jpg_ctx->height = dec_ctx->height;
    jpg_ctx->pix_fmt = AV_PIX_FMT_YUVJ420P;
    jpg_ctx->time_base = (AVRational){1, 1};

    if ((ret = avcodec_open2(jpg_ctx, mjpeg_enc, NULL)) < 0) {
        avcodec_free_context(&jpg_ctx);
        goto vs_end;
    }

    /* Convert pixel format if needed */
    rgb_frame = av_frame_alloc();
    if (!rgb_frame) { ret = AVERROR(ENOMEM); avcodec_free_context(&jpg_ctx); goto vs_end; }
    rgb_frame->width = jpg_ctx->width;
    rgb_frame->height = jpg_ctx->height;
    rgb_frame->format = AV_PIX_FMT_YUVJ420P;
    if ((ret = av_frame_get_buffer(rgb_frame, 0)) < 0) { avcodec_free_context(&jpg_ctx); goto vs_end; }

    sws_ctx = sws_getContext(dec_ctx->width, dec_ctx->height, dec_ctx->pix_fmt,
                             jpg_ctx->width, jpg_ctx->height, AV_PIX_FMT_YUVJ420P,
                             SWS_BILINEAR, NULL, NULL, NULL);
    if (!sws_ctx) {
        avcodec_free_context(&jpg_ctx);
        ret = AVERROR_UNKNOWN; ALOGE("sws_getContext failed");
        goto vs_end;
    }
    sws_scale(sws_ctx, (const uint8_t *const *)frame->data, frame->linesize,
              0, dec_ctx->height, rgb_frame->data, rgb_frame->linesize);
    rgb_frame->pts = 0;

    AVPacket *jpg_pkt = av_packet_alloc();
    ret = avcodec_send_frame(jpg_ctx, rgb_frame);
    if (ret >= 0) {
        ret = avcodec_receive_packet(jpg_ctx, jpg_pkt);
        if (ret >= 0) {
            /* Write JPEG to file */
            FILE *fp = fopen(output, "wb");
            if (fp) {
                fwrite(jpg_pkt->data, 1, jpg_pkt->size, fp);
                fclose(fp);
                ret = 0;
            } else {
                ret = -1;
            }
        }
    }
    av_packet_free(&jpg_pkt);
    avcodec_free_context(&jpg_ctx);

vs_end:
    if (sws_ctx) sws_freeContext(sws_ctx);
    av_frame_free(&frame);
    av_frame_free(&rgb_frame);
    av_packet_free(&pkt);
    avcodec_free_context(&dec_ctx);
    avformat_close_input(&fmt_ctx);

    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  GIF Maker: extract video segment and encode as GIF.
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_gifMake(
    JNIEnv *env, jclass clazz,
    jstring j_input, jstring j_output, jlong startMs, jlong endMs,
    jint width, jint fps, jobject callback) {
    (void)clazz;

    const char *input = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!input || !output) {
        if (input) (*env)->ReleaseStringUTFChars(env, j_input, input);
        if (output) (*env)->ReleaseStringUTFChars(env, j_output, output);
        return -1;
    }

    ALOGI("GIF: %s -> %s [%lld-%lld ms] w=%d fps=%d",
          input, output, (long long)startMs, (long long)endMs, width, fps);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVCodecContext *dec_ctx = NULL, *enc_ctx = NULL;
    const AVCodec *enc = NULL;
    AVPacket *pkt = NULL, *out_pkt = NULL;
    AVFrame *frame = NULL, *scaled = NULL;
    struct SwsContext *sws_ctx = NULL;
    int ret = 0, video_idx = -1;

    if ((ret = avformat_open_input(&ifmt_ctx, input, NULL, NULL)) < 0) {
        set_last_error("Cannot open input: %s", av_err2str(ret)); goto gif_end;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) {
        set_last_error("Cannot read stream info: %s", av_err2str(ret)); goto gif_end;
    }

    video_idx = av_find_best_stream(ifmt_ctx, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
    if (video_idx < 0) { ret = video_idx; set_last_error("No video stream found"); goto gif_end; }

    /* Use HW decoder if available */
    if ((ret = open_video_decoder_hw(ifmt_ctx, video_idx, &dec_ctx)) < 0) {
        set_last_error("Video decoder init failed: %s", av_err2str(ret)); goto gif_end;
    }

    /* Calculate output dimensions maintaining aspect ratio */
    int out_w = width > 0 ? width : 320;
    if (out_w % 2 != 0) out_w++;
    if (dec_ctx->width <= 0 || dec_ctx->height <= 0) {
        ret = AVERROR_INVALIDDATA; set_last_error("Invalid input dimensions"); goto gif_end;
    }
    int out_h = (int)((double)dec_ctx->height / dec_ctx->width * out_w);
    if (out_h <= 0) out_h = 2;
    if (out_h % 2 != 0) out_h++;

    /* GIF output */
    avformat_alloc_output_context2(&ofmt_ctx, NULL, "gif", output);
    if (!ofmt_ctx) { ret = AVERROR_UNKNOWN; goto gif_end; }

    enc = avcodec_find_encoder(AV_CODEC_ID_GIF);
    if (!enc) { ret = AVERROR_ENCODER_NOT_FOUND; goto gif_end; }

    AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
    if (!out_stream) { ret = AVERROR(ENOMEM); goto gif_end; }

    enc_ctx = avcodec_alloc_context3(enc);
    if (!enc_ctx) { ret = AVERROR(ENOMEM); goto gif_end; }
    enc_ctx->width = out_w;
    enc_ctx->height = out_h;
    /* Use encoder's preferred pixel format (typically PAL8 or RGB8) */
    enc_ctx->pix_fmt = enc->pix_fmts ? enc->pix_fmts[0] : AV_PIX_FMT_RGB8;
    enc_ctx->time_base = (AVRational){1, fps > 0 ? fps : 15};
    if ((ret = avcodec_open2(enc_ctx, enc, NULL)) < 0) {
        set_last_error("GIF encoder init failed: %s", av_err2str(ret)); goto gif_end;
    }
    avcodec_parameters_from_context(out_stream->codecpar, enc_ctx);
    out_stream->time_base = enc_ctx->time_base;

    enum AVPixelFormat gif_pix_fmt = enc_ctx->pix_fmt;  /* actual format after open */

    /* sws_ctx initialized lazily from first decoded frame to handle HW pixel formats */

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, output, AVIO_FLAG_WRITE);
        if (ret < 0) goto gif_end;
    }
    /* Set infinite loop for GIF animation via format options */
    {
        AVDictionary *gif_opts = NULL;
        av_dict_set(&gif_opts, "loop", "0", 0);
        ret = avformat_write_header(ofmt_ctx, &gif_opts);
        av_dict_free(&gif_opts);
        if (ret < 0) goto gif_end;
    }

    /* Seek to start */
    int64_t start_ts = startMs * 1000;
    int64_t end_ts = endMs * 1000;
    if (start_ts > 0) {
        av_seek_frame(ifmt_ctx, -1, start_ts, AVSEEK_FLAG_BACKWARD);
        avcodec_flush_buffers(dec_ctx);
    }

    pkt = av_packet_alloc();
    out_pkt = av_packet_alloc();
    frame = av_frame_alloc();
    scaled = av_frame_alloc();
    scaled->width = out_w;
    scaled->height = out_h;
    scaled->format = gif_pix_fmt;
    if (!pkt || !out_pkt || !frame || !scaled) { ret = AVERROR(ENOMEM); goto gif_end; }
    if ((ret = av_frame_get_buffer(scaled, 0)) < 0) goto gif_end;

    int64_t duration = end_ts - start_ts;
    if (duration <= 0) duration = 1;
    int last_percent = -1;
    int64_t frame_count = 0;

    while (!g_cancel) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) break;
        if (pkt->stream_index != video_idx) { av_packet_unref(pkt); continue; }

        AVStream *in_s = ifmt_ctx->streams[video_idx];
        int64_t pkt_us = av_rescale_q(pkt->pts, in_s->time_base,
                                      (AVRational){1, AV_TIME_BASE});
        /* Stop reading once packet is past end time */
        if (pkt_us >= end_ts) { av_packet_unref(pkt); break; }

        /* Send ALL packets to decoder (including before start_ts)
         * so reference frames are available for proper decoding. */
        ret = avcodec_send_packet(dec_ctx, pkt);
        av_packet_unref(pkt);
        if (ret < 0) {
            /* Skip corrupt packets instead of aborting */
            if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
            break;
        }

        while (ret >= 0 && !g_cancel) {
            ret = avcodec_receive_frame(dec_ctx, frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
            if (ret < 0) {
                /* Skip corrupt frames instead of aborting */
                if (ret == AVERROR_INVALIDDATA) { ret = 0; continue; }
                break;
            }

            /* Filter at frame level: skip frames before start_ts */
            int64_t frame_us = av_rescale_q(frame->pts,
                in_s->time_base, (AVRational){1, AV_TIME_BASE});
            if (frame_us < start_ts) { av_frame_unref(frame); continue; }

            AVFrame *src_frame = frame;
            AVFrame *sw_frame = NULL;
            if (frame->format == AV_PIX_FMT_MEDIACODEC || frame->hw_frames_ctx != NULL) {
                sw_frame = av_frame_alloc();
                if (sw_frame && av_hwframe_transfer_data(sw_frame, frame, 0) >= 0) {
                    sw_frame->pts = frame->pts;
                    src_frame = sw_frame;
                } else {
                    av_frame_free(&sw_frame);
                    sw_frame = NULL;
                }
            }

            if (!sws_ctx) {
                enum AVPixelFormat src_pf = (enum AVPixelFormat)src_frame->format;
                if (src_pf == AV_PIX_FMT_NONE || src_pf == AV_PIX_FMT_MEDIACODEC)
                    src_pf = AV_PIX_FMT_NV12;
                sws_ctx = sws_getContext(src_frame->width, src_frame->height, src_pf,
                                         out_w, out_h, gif_pix_fmt,
                                         SWS_BILINEAR, NULL, NULL, NULL);
                if (!sws_ctx) {
                    set_last_error("GIF sws_getContext failed (src=%d dst=%d)", src_pf, gif_pix_fmt);
                    if (sw_frame) av_frame_free(&sw_frame);
                    av_frame_unref(frame);
                    ret = AVERROR_UNKNOWN; break;
                }
            }

            /* Ensure scaled buffer is not shared with encoder's internal reference
             * (GIF encoder keeps previous frame for diff optimization) */
            av_frame_make_writable(scaled);
            sws_scale(sws_ctx, (const uint8_t *const *)src_frame->data, src_frame->linesize,
                      0, src_frame->height, scaled->data, scaled->linesize);
            if (sw_frame) av_frame_free(&sw_frame);
            scaled->pts = frame_count++;

            ret = avcodec_send_frame(enc_ctx, scaled);
            av_frame_unref(frame);
            if (ret < 0) {
                /* Skip frames the encoder rejects, don't abort */
                if (ret == AVERROR_INVALIDDATA || ret == AVERROR(EINVAL)) { ret = 0; continue; }
                break;
            }

            while (ret >= 0) {
                ret = avcodec_receive_packet(enc_ctx, out_pkt);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                if (ret < 0) break;
                out_pkt->stream_index = 0;
                /* Rescale from encoder time_base to output stream time_base
                 * (GIF muxer uses 1/100 centiseconds) */
                av_packet_rescale_ts(out_pkt, enc_ctx->time_base, out_stream->time_base);
                int wr = av_interleaved_write_frame(ofmt_ctx, out_pkt);
                if (wr < 0) {
                    set_last_error("GIF write error: %s", av_err2str(wr));
                    ret = wr; break;
                }
            }

            if (callback && onProgress) {
                int pct = (int)((frame_us - start_ts) * 100 / duration);
                if (pct < 0) pct = 0; if (pct > 100) pct = 100;
                if (pct != last_percent) {
                    last_percent = pct;
                    (*env)->CallVoidMethod(env, callback, onProgress, pct);
                    if ((*env)->ExceptionCheck(env)) {
                        (*env)->ExceptionClear(env);
                        ret = AVERROR_EXIT; break;
                    }
                }
            }
        }
    }

    /* Flush */
    if (!g_cancel && (ret >= 0 || ret == AVERROR_EOF || frame_count > 0)) {
        avcodec_send_frame(enc_ctx, NULL);
        while (1) {
            int fr = avcodec_receive_packet(enc_ctx, out_pkt);
            if (fr == AVERROR_EOF || fr < 0) break;
            out_pkt->stream_index = 0;
            av_packet_rescale_ts(out_pkt, enc_ctx->time_base, out_stream->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }

    ALOGI("GIF encoding done: frame_count=%lld ret=%d cancel=%d",
          (long long)frame_count, ret, g_cancel);
    if (g_cancel) { ret = AVERROR_EXIT; set_last_error("Operation cancelled"); }
    else if (frame_count > 0) { av_write_trailer(ofmt_ctx); ret = 0; }
    else if (ret == AVERROR_EOF || ret >= 0) { av_write_trailer(ofmt_ctx); ret = 0; }
    else if (g_last_error[0] == '\0') { set_last_error("GIF creation failed: %s", av_err2str(ret)); }

gif_end:
    if (sws_ctx) sws_freeContext(sws_ctx);
    av_frame_free(&frame);
    av_frame_free(&scaled);
    av_packet_free(&pkt);
    av_packet_free(&out_pkt);
    avcodec_free_context(&dec_ctx);
    avcodec_free_context(&enc_ctx);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);

    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  Merge Files: concatenate using FFmpeg concat demuxer.
 *  Now that remux() properly resets timestamps to 0, the concat demuxer
 *  can correctly handle file boundary offsets natively.
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_mergeFiles(
    JNIEnv *env, jclass clazz,
    jobjectArray j_inputs, jstring j_output, jobject callback) {
    (void)clazz;

    int count = (*env)->GetArrayLength(env, j_inputs);
    if (count < 2) { ALOGE("Merge: need >= 2 files, got %d", count); return -1; }

    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!output) return -1;

    ALOGI("Merge: %d files -> %s", count, output);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    int ret = 0;
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket *pkt = NULL;
    int *stream_mapping = NULL;
    char concat_path[512] = {0};

    /* ---- Step 1: Write concat list file ---- */
    {
        const char *last_slash = strrchr(output, '/');
        int dir_len = last_slash ? (int)(last_slash - output) : 0;
        snprintf(concat_path, sizeof(concat_path), "%.*s/._concat_%d.txt",
                 dir_len, output, (int)getpid());

        FILE *fp = fopen(concat_path, "w");
        if (!fp) {
            ALOGE("Cannot create concat list: %s", concat_path);
            set_last_error("Cannot create temporary concat list file");
            (*env)->ReleaseStringUTFChars(env, j_output, output);
            return -1;
        }
        for (int i = 0; i < count; i++) {
            jstring j_path = (jstring)(*env)->GetObjectArrayElement(env, j_inputs, i);
            const char *path = (*env)->GetStringUTFChars(env, j_path, NULL);
            if (!path) {
                (*env)->DeleteLocalRef(env, j_path);
                fclose(fp); remove(concat_path);
                (*env)->ReleaseStringUTFChars(env, j_output, output);
                return AVERROR(ENOMEM);
            }
            fprintf(fp, "file '");
            for (const char *c = path; *c; c++) {
                if (*c == '\'') fprintf(fp, "'\\''");
                else fputc(*c, fp);
            }
            fprintf(fp, "'\n");
            (*env)->ReleaseStringUTFChars(env, j_path, path);
            (*env)->DeleteLocalRef(env, j_path);
        }
        fclose(fp);
    }

    /* ---- Step 2: Open concat demuxer ---- */
    {
        const AVInputFormat *concat_fmt = av_find_input_format("concat");
        if (!concat_fmt) {
            ALOGE("concat demuxer not found");
            set_last_error("FFmpeg concat demuxer not available");
            ret = AVERROR_DEMUXER_NOT_FOUND;
            goto merge_end;
        }
        AVDictionary *opts = NULL;
        av_dict_set(&opts, "safe", "0", 0);
        av_dict_set(&opts, "auto_convert", "1", 0);
        ret = avformat_open_input(&ifmt_ctx, concat_path, concat_fmt, &opts);
        av_dict_free(&opts);
        if (ret < 0) {
            ALOGE("Cannot open concat list: %s (%s)", concat_path, av_err2str(ret));
            set_last_error("Cannot open input files for merging: %s", av_err2str(ret));
            goto merge_end;
        }
        if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) {
            ALOGE("Cannot find stream info: %s", av_err2str(ret));
            set_last_error("Cannot read stream info: %s", av_err2str(ret));
            goto merge_end;
        }
    }

    /* ---- Step 3: Setup output and copy streams ---- */
    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, output);
    if (!ofmt_ctx) { set_last_error("Cannot create output context"); ret = AVERROR_UNKNOWN; goto merge_end; }

    {
        int nb = ifmt_ctx->nb_streams;
        stream_mapping = av_calloc(nb, sizeof(int));
        if (!stream_mapping) { ret = AVERROR(ENOMEM); goto merge_end; }

        int out_idx = 0;
        for (int i = 0; i < nb; i++) {
            AVCodecParameters *par = ifmt_ctx->streams[i]->codecpar;
            if (par->codec_type != AVMEDIA_TYPE_VIDEO &&
                par->codec_type != AVMEDIA_TYPE_AUDIO) {
                stream_mapping[i] = -1;
                continue;
            }
            stream_mapping[i] = out_idx++;
            AVStream *os = avformat_new_stream(ofmt_ctx, NULL);
            if (!os) { ret = AVERROR(ENOMEM); goto merge_end; }
            if ((ret = avcodec_parameters_copy(os->codecpar, par)) < 0) goto merge_end;
            os->codecpar->codec_tag = 0;
            os->time_base = ifmt_ctx->streams[i]->time_base;
        }
    }

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, output, AVIO_FLAG_WRITE);
        if (ret < 0) { ALOGE("Merge: avio_open failed: %s", av_err2str(ret)); set_last_error("Cannot open output file: %s", av_err2str(ret)); goto merge_end; }
    }
    if ((ret = write_header_faststart(ofmt_ctx)) < 0) {
        ALOGE("Merge: write_header failed: %s", av_err2str(ret));
        set_last_error("Cannot write output header: %s", av_err2str(ret)); goto merge_end;
    }

    pkt = av_packet_alloc();
    if (!pkt) { ret = AVERROR(ENOMEM); goto merge_end; }

    /* ---- Step 4: Straight packet copy — concat demuxer handles offsets ---- */
    {
        int64_t total_duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 1;
        /* If concat demuxer didn't report duration, estimate from streams */
        if (total_duration <= 1) {
            for (unsigned s = 0; s < ifmt_ctx->nb_streams; s++) {
                AVStream *st = ifmt_ctx->streams[s];
                if (st->duration > 0) {
                    int64_t dur_us = av_rescale_q(st->duration, st->time_base,
                                                  (AVRational){1, AV_TIME_BASE});
                    if (dur_us > total_duration) total_duration = dur_us;
                }
            }
            if (total_duration <= 0) total_duration = 1;
        }
        int last_percent = -1;

        while (!g_cancel) {
            ret = av_read_frame(ifmt_ctx, pkt);
            if (ret < 0) break;

            if (pkt->stream_index >= (int)ifmt_ctx->nb_streams ||
                stream_mapping[pkt->stream_index] < 0) {
                av_packet_unref(pkt);
                continue;
            }

            int in_idx = pkt->stream_index;
            int out_idx = stream_mapping[in_idx];
            AVStream *in_s = ifmt_ctx->streams[in_idx];
            AVStream *out_s = ofmt_ctx->streams[out_idx];

            /* Log first packets + periodic samples */
            int64_t saved_pts = pkt->pts;
            AVRational saved_tb = in_s->time_base;

            pkt->stream_index = out_idx;
            av_packet_rescale_ts(pkt, in_s->time_base, out_s->time_base);
            pkt->pos = -1;

            int write_ret = av_interleaved_write_frame(ofmt_ctx, pkt);
            if (write_ret < 0) {
                ALOGE("Merge: write error stream=%d: %s", out_idx, av_err2str(write_ret));
                set_last_error("Write error on stream %d: %s", out_idx, av_err2str(write_ret));
                ret = write_ret;
                break;
            }

            /* Progress */
            if (callback && onProgress && saved_pts != AV_NOPTS_VALUE) {
                int64_t pos_us = av_rescale_q(saved_pts, saved_tb,
                                              (AVRational){1, AV_TIME_BASE});
                int pct = (int)(pos_us * 100 / total_duration);
                if (pct < 0) pct = 0;
                if (pct > 100) pct = 100;
                if (pct != last_percent) {
                    last_percent = pct;
                    (*env)->CallVoidMethod(env, callback, onProgress, pct);
                    if ((*env)->ExceptionCheck(env)) {
                        (*env)->ExceptionClear(env);
                        ret = AVERROR_EXIT; break;
                    }
                }
            }
        }
    }

    if (g_cancel) { ret = AVERROR_EXIT; set_last_error("Operation cancelled"); }
    else if (ret == AVERROR_EOF || ret >= 0) {
        av_write_trailer(ofmt_ctx);
        ret = 0;
    } else {
        set_last_error("Merge error: %s", av_err2str(ret));
    }

merge_end:
    av_packet_free(&pkt);
    av_free(stream_mapping);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    if (concat_path[0]) remove(concat_path);

    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  Merge Files Transcode: decode all inputs, re-encode to single output.
 *  Handles files with different codecs, resolutions, and sample rates.
 *  Output is always H.264+AAC MP4.
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_mergeFilesTranscode(
    JNIEnv *env, jclass clazz,
    jobjectArray j_inputs, jstring j_output,
    jint targetWidth, jint targetHeight, jint targetBitrateKbps,
    jobject callback) {
    (void)clazz;

    int count = (*env)->GetArrayLength(env, j_inputs);
    if (count < 2) { ALOGE("MergeTranscode: need >= 2 files, got %d", count); return -1; }

    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    if (!output) return -1;

    ALOGI("MergeTranscode: %d files -> %s [%dx%d @ %d kbps]",
          count, output, targetWidth, targetHeight, targetBitrateKbps);
    g_cancel = 0;
    clear_last_error();

    jmethodID onProgress = NULL;
    if (callback) {
        jclass cbClass = (*env)->GetObjectClass(env, callback);
        onProgress = (*env)->GetMethodID(env, cbClass, "onProgress", "(I)V");
    }

    int ret = 0;
    AVFormatContext *ofmt_ctx = NULL;
    AVCodecContext *venc_ctx = NULL, *aenc_ctx = NULL;
    AVPacket *out_pkt = NULL;
    int vout_idx = -1, aout_idx = -1;
    int is_hw_encoder = 0;

    int out_w = (targetWidth + 1) & ~1;
    int out_h = (targetHeight + 1) & ~1;
    int64_t vbitrate = targetBitrateKbps > 0
        ? (int64_t)targetBitrateKbps * 1000
        : (int64_t)out_w * out_h * 3;
    if (vbitrate < 500000) vbitrate = 500000;
    if (vbitrate > 8000000) vbitrate = 8000000;

    /* --- Probe first input to set up encoder parameters --- */
    AVFormatContext *probe_ctx = NULL;
    {
        jstring j_first = (jstring)(*env)->GetObjectArrayElement(env, j_inputs, 0);
        const char *first_path = (*env)->GetStringUTFChars(env, j_first, NULL);
        if (!first_path) { (*env)->ReleaseStringUTFChars(env, j_output, output); return -1; }

        ret = avformat_open_input(&probe_ctx, first_path, NULL, NULL);
        if (ret < 0) {
            set_last_error("Cannot open first input: %s", av_err2str(ret));
            (*env)->ReleaseStringUTFChars(env, j_first, first_path);
            (*env)->DeleteLocalRef(env, j_first);
            (*env)->ReleaseStringUTFChars(env, j_output, output);
            return ret;
        }
        avformat_find_stream_info(probe_ctx, NULL);
        (*env)->ReleaseStringUTFChars(env, j_first, first_path);
        (*env)->DeleteLocalRef(env, j_first);
    }

    int probe_video_idx = -1, probe_audio_idx = -1;
    for (unsigned i = 0; i < probe_ctx->nb_streams; i++) {
        if (probe_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && probe_video_idx < 0)
            probe_video_idx = i;
        else if (probe_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO && probe_audio_idx < 0)
            probe_audio_idx = i;
    }

    /* --- Create output context --- */
    avformat_alloc_output_context2(&ofmt_ctx, NULL, "mp4", output);
    if (!ofmt_ctx) {
        set_last_error("Cannot create MP4 output context");
        ret = AVERROR_UNKNOWN; goto mt_end;
    }

    /* --- Setup video encoder (shared across all inputs) --- */
    int has_any_video = (probe_video_idx >= 0);
    if (has_any_video) {
        /*
         * We need a temporary decoder context just to configure the encoder.
         * Use probe_ctx's video stream parameters.
         */
        AVCodecContext *tmp_dec = avcodec_alloc_context3(NULL);
        if (!tmp_dec) { ret = AVERROR(ENOMEM); goto mt_end; }
        avcodec_parameters_to_context(tmp_dec, probe_ctx->streams[probe_video_idx]->codecpar);
        tmp_dec->width = out_w;
        tmp_dec->height = out_h;
        tmp_dec->pix_fmt = AV_PIX_FMT_YUV420P;
        tmp_dec->time_base = probe_ctx->streams[probe_video_idx]->time_base;
        tmp_dec->framerate = av_guess_frame_rate(probe_ctx, probe_ctx->streams[probe_video_idx], NULL);
        if (tmp_dec->framerate.num <= 0) {
            tmp_dec->framerate = (AVRational){30, 1};
            tmp_dec->time_base = (AVRational){1, 30};
        }

        ret = open_video_encoder_with_fallback(AV_CODEC_ID_H264, tmp_dec,
            probe_ctx, probe_video_idx, ofmt_ctx->oformat, vbitrate,
            &venc_ctx, &is_hw_encoder);
        avcodec_free_context(&tmp_dec);
        if (ret < 0) {
            set_last_error("H.264 encoder init failed (%dx%d): %s", out_w, out_h, av_err2str(ret));
            goto mt_end;
        }
        ALOGI("MergeTranscode: video encoder: %s (HW=%d) %dx%d",
              venc_ctx->codec->name, is_hw_encoder, out_w, out_h);

        AVStream *vs = avformat_new_stream(ofmt_ctx, NULL);
        if (!vs) { ret = AVERROR(ENOMEM); goto mt_end; }
        avcodec_parameters_from_context(vs->codecpar, venc_ctx);
        vs->time_base = venc_ctx->time_base;
        vout_idx = vs->index;
    }

    /* --- Setup audio encoder (shared across all inputs) --- */
    int has_any_audio = (probe_audio_idx >= 0);
    if (has_any_audio) {
        /* Create a minimal decoder context for encoder setup */
        AVCodecParameters *apar = probe_ctx->streams[probe_audio_idx]->codecpar;
        AVCodecContext *tmp_adec = avcodec_alloc_context3(NULL);
        if (!tmp_adec) { ret = AVERROR(ENOMEM); goto mt_end; }
        avcodec_parameters_to_context(tmp_adec, apar);
        /* Force 44100Hz stereo for consistent output */
        tmp_adec->sample_rate = 44100;
        AVChannelLayout stereo = AV_CHANNEL_LAYOUT_STEREO;
        av_channel_layout_copy(&tmp_adec->ch_layout, &stereo);
        tmp_adec->sample_fmt = AV_SAMPLE_FMT_FLTP;

        SwrContext *tmp_swr = NULL;
        int ar = open_audio_encoder_with_resampler(
            AV_CODEC_ID_AAC, tmp_adec,
            ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER,
            &aenc_ctx, &tmp_swr);
        swr_free(&tmp_swr);
        avcodec_free_context(&tmp_adec);

        if (ar >= 0 && aenc_ctx) {
            AVStream *as = avformat_new_stream(ofmt_ctx, NULL);
            if (as) {
                avcodec_parameters_from_context(as->codecpar, aenc_ctx);
                as->time_base = aenc_ctx->time_base;
                aout_idx = as->index;
            }
            ALOGI("MergeTranscode: audio encoder: AAC %dHz",
                  aenc_ctx->sample_rate);
        } else {
            ALOGI("MergeTranscode: audio encoder setup failed, continuing without audio");
        }
    }

    avformat_close_input(&probe_ctx);
    probe_ctx = NULL;

    if (vout_idx < 0 && aout_idx < 0) {
        set_last_error("No output streams created");
        ret = AVERROR_STREAM_NOT_FOUND;
        goto mt_end;
    }

    /* --- Open output file and write header --- */
    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, output, AVIO_FLAG_WRITE);
        if (ret < 0) {
            set_last_error("Cannot open output file: %s", av_err2str(ret));
            goto mt_end;
        }
    }
    if ((ret = write_header_faststart(ofmt_ctx)) < 0) {
        set_last_error("Cannot write output header: %s", av_err2str(ret));
        goto mt_end;
    }

    out_pkt = av_packet_alloc();
    if (!out_pkt) { ret = AVERROR(ENOMEM); goto mt_end; }

    /* --- Estimate total duration for progress --- */
    int64_t total_duration_us = 0;
    {
        for (int fi = 0; fi < count; fi++) {
            jstring j_path = (jstring)(*env)->GetObjectArrayElement(env, j_inputs, fi);
            const char *path = (*env)->GetStringUTFChars(env, j_path, NULL);
            if (path) {
                AVFormatContext *tmp = NULL;
                if (avformat_open_input(&tmp, path, NULL, NULL) >= 0) {
                    avformat_find_stream_info(tmp, NULL);
                    if (tmp->duration > 0) total_duration_us += tmp->duration;
                    avformat_close_input(&tmp);
                }
                (*env)->ReleaseStringUTFChars(env, j_path, path);
            }
            (*env)->DeleteLocalRef(env, j_path);
        }
        if (total_duration_us <= 0) total_duration_us = 1;
    }

    /* --- Process each input file --- */
    int64_t video_pts_offset = 0;
    int64_t audio_pts_offset = 0;
    int64_t progress_us = 0;
    int last_percent = -1;

    for (int fi = 0; fi < count && !g_cancel; fi++) {
        jstring j_path = (jstring)(*env)->GetObjectArrayElement(env, j_inputs, fi);
        const char *path = (*env)->GetStringUTFChars(env, j_path, NULL);
        if (!path) {
            (*env)->DeleteLocalRef(env, j_path);
            ret = AVERROR(ENOMEM);
            break;
        }

        AVFormatContext *ifmt_ctx = NULL;
        AVCodecContext *vdec_ctx = NULL, *adec_ctx = NULL;
        SwrContext *swr_ctx = NULL;
        struct SwsContext *sws_ctx = NULL;
        AVPacket *pkt = NULL;
        AVFrame *frame = NULL, *aframe = NULL, *afilt_frame = NULL;
        int video_idx = -1, audio_idx = -1;
        int64_t file_video_pts_last = 0;
        int64_t file_audio_pts_last = 0;
        int sws_initialized = 0;
        int fit_w = 0, fit_h = 0, pad_x = 0, pad_y = 0;

        ret = avformat_open_input(&ifmt_ctx, path, NULL, NULL);
        if (ret < 0) {
            set_last_error("Cannot open input[%d]: %s", fi, av_err2str(ret));
            (*env)->ReleaseStringUTFChars(env, j_path, path);
            (*env)->DeleteLocalRef(env, j_path);
            break;
        }
        avformat_find_stream_info(ifmt_ctx, NULL);

        for (unsigned s = 0; s < ifmt_ctx->nb_streams; s++) {
            if (ifmt_ctx->streams[s]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && video_idx < 0)
                video_idx = s;
            else if (ifmt_ctx->streams[s]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO && audio_idx < 0)
                audio_idx = s;
        }

        /* Open video decoder for this file */
        if (video_idx >= 0 && vout_idx >= 0) {
            ret = open_video_decoder_hw(ifmt_ctx, video_idx, &vdec_ctx);
            if (ret < 0) {
                ALOGE("MergeTranscode: video decoder failed for file[%d]: %s", fi, av_err2str(ret));
                set_last_error("Video decoder init failed for file %d: %s", fi + 1, av_err2str(ret));
                avformat_close_input(&ifmt_ctx);
                (*env)->ReleaseStringUTFChars(env, j_path, path);
                (*env)->DeleteLocalRef(env, j_path);
                break;
            }
        }

        /* Open audio decoder + resampler for this file */
        if (audio_idx >= 0 && aout_idx >= 0 && aenc_ctx) {
            const AVCodec *adec = avcodec_find_decoder(
                ifmt_ctx->streams[audio_idx]->codecpar->codec_id);
            if (adec) {
                adec_ctx = avcodec_alloc_context3(adec);
                avcodec_parameters_to_context(adec_ctx, ifmt_ctx->streams[audio_idx]->codecpar);
                if (avcodec_open2(adec_ctx, adec, NULL) < 0) {
                    avcodec_free_context(&adec_ctx);
                    adec_ctx = NULL;
                }
            }
            if (adec_ctx) {
                swr_alloc_set_opts2(&swr_ctx,
                    &aenc_ctx->ch_layout, aenc_ctx->sample_fmt, aenc_ctx->sample_rate,
                    &adec_ctx->ch_layout, adec_ctx->sample_fmt, adec_ctx->sample_rate,
                    0, NULL);
                if (!swr_ctx || swr_init(swr_ctx) < 0) {
                    swr_free(&swr_ctx);
                    swr_ctx = NULL;
                    ALOGI("MergeTranscode: audio resampler failed for file[%d], dropping audio", fi);
                }
            }
        }

        pkt = av_packet_alloc();
        frame = av_frame_alloc();
        aframe = av_frame_alloc();
        afilt_frame = av_frame_alloc();
        if (!pkt || !frame || !aframe || !afilt_frame) {
            ret = AVERROR(ENOMEM);
            goto mt_file_end;
        }

        int64_t file_duration = ifmt_ctx->duration > 0 ? ifmt_ctx->duration : 0;

        /* --- Decode loop for this file --- */
        while (!g_cancel) {
            ret = av_read_frame(ifmt_ctx, pkt);
            if (ret < 0) break;

            /* --- VIDEO --- */
            if (pkt->stream_index == video_idx && vout_idx >= 0 && vdec_ctx && venc_ctx) {
                ret = avcodec_send_packet(vdec_ctx, pkt);
                av_packet_unref(pkt);
                if (ret < 0) { ret = 0; continue; }

                while (ret >= 0 && !g_cancel) {
                    ret = avcodec_receive_frame(vdec_ctx, frame);
                    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                    if (ret < 0) break;

                    AVFrame *src_frame = frame;
                    AVFrame *sw_frame = NULL;

                    /* Transfer HW frames to CPU */
                    if (frame->format == AV_PIX_FMT_MEDIACODEC ||
                        frame->hw_frames_ctx != NULL) {
                        sw_frame = av_frame_alloc();
                        if (sw_frame) {
                            int xr = av_hwframe_transfer_data(sw_frame, frame, 0);
                            if (xr >= 0) {
                                sw_frame->pts = frame->pts;
                                src_frame = sw_frame;
                            } else {
                                av_frame_free(&sw_frame);
                                sw_frame = NULL;
                            }
                        }
                    }

                    int src_w = src_frame->width;
                    int src_h = src_frame->height;
                    enum AVPixelFormat src_pix_fmt = (enum AVPixelFormat)src_frame->format;

                    /* Lazy sws_ctx init (per-file, since resolution/format may differ) */
                    if (!sws_initialized) {
                        double scale_w = (double)out_w / src_w;
                        double scale_h = (double)out_h / src_h;
                        double scale = scale_w < scale_h ? scale_w : scale_h;
                        fit_w = ((int)(src_w * scale + 0.5)) & ~1;
                        fit_h = ((int)(src_h * scale + 0.5)) & ~1;
                        if (fit_w > out_w) fit_w = out_w;
                        if (fit_h > out_h) fit_h = out_h;
                        pad_x = ((out_w - fit_w) / 2) & ~1;
                        pad_y = ((out_h - fit_h) / 2) & ~1;

                        if (src_pix_fmt == AV_PIX_FMT_NONE || src_pix_fmt == AV_PIX_FMT_MEDIACODEC)
                            src_pix_fmt = AV_PIX_FMT_NV12;

                        sws_ctx = sws_getContext(src_w, src_h, src_pix_fmt,
                                                fit_w, fit_h, venc_ctx->pix_fmt,
                                                SWS_BILINEAR, NULL, NULL, NULL);
                        if (!sws_ctx) {
                            ALOGE("MergeTranscode: sws_getContext failed file[%d]", fi);
                            if (sw_frame) av_frame_free(&sw_frame);
                            av_frame_unref(frame);
                            ret = AVERROR_UNKNOWN;
                            break;
                        }
                        sws_initialized = 1;
                    }

                    /* Scale + letterbox */
                    AVFrame *conv = av_frame_alloc();
                    if (!conv) {
                        if (sw_frame) av_frame_free(&sw_frame);
                        av_frame_unref(frame);
                        ret = AVERROR(ENOMEM);
                        break;
                    }
                    conv->width = out_w;
                    conv->height = out_h;
                    conv->format = venc_ctx->pix_fmt;
                    av_frame_get_buffer(conv, 0);

                    /* Black canvas */
                    memset(conv->data[0], 0, (size_t)conv->linesize[0] * out_h);
                    if (conv->data[1] && conv->data[2]) {
                        int chroma_h = out_h / 2;
                        memset(conv->data[1], 128, (size_t)conv->linesize[1] * chroma_h);
                        memset(conv->data[2], 128, (size_t)conv->linesize[2] * chroma_h);
                    } else if (conv->data[1]) {
                        int chroma_h = out_h / 2;
                        memset(conv->data[1], 128, (size_t)conv->linesize[1] * chroma_h);
                    }

                    /* Scale into centered region */
                    uint8_t *dst_data[4];
                    int dst_linesize[4];
                    for (int p = 0; p < 4; p++) {
                        dst_data[p] = conv->data[p];
                        dst_linesize[p] = conv->linesize[p];
                    }
                    if (dst_data[0])
                        dst_data[0] += pad_y * dst_linesize[0] + pad_x;
                    if (dst_data[1] && dst_data[2]) {
                        dst_data[1] += (pad_y / 2) * dst_linesize[1] + (pad_x / 2);
                        dst_data[2] += (pad_y / 2) * dst_linesize[2] + (pad_x / 2);
                    } else if (dst_data[1]) {
                        dst_data[1] += (pad_y / 2) * dst_linesize[1] + (pad_x & ~1);
                    }

                    sws_scale(sws_ctx,
                        (const uint8_t *const *)src_frame->data, src_frame->linesize,
                        0, src_h, dst_data, dst_linesize);

                    /* Compute output PTS: rescale source pts then add offset */
                    int64_t src_pts = frame->pts;
                    if (src_pts == AV_NOPTS_VALUE) src_pts = 0;
                    int64_t pts_in_enc_tb = av_rescale_q(src_pts,
                        ifmt_ctx->streams[video_idx]->time_base,
                        venc_ctx->time_base);
                    conv->pts = pts_in_enc_tb + video_pts_offset;
                    file_video_pts_last = pts_in_enc_tb;

                    if (sw_frame) av_frame_free(&sw_frame);

                    int send_ret = avcodec_send_frame(venc_ctx, conv);
                    av_frame_free(&conv);
                    av_frame_unref(frame);
                    if (send_ret < 0) { ret = send_ret; break; }

                    while (1) {
                        int recv_ret = avcodec_receive_packet(venc_ctx, out_pkt);
                        if (recv_ret == AVERROR(EAGAIN) || recv_ret == AVERROR_EOF) break;
                        if (recv_ret < 0) { ret = recv_ret; break; }
                        out_pkt->stream_index = vout_idx;
                        av_packet_rescale_ts(out_pkt, venc_ctx->time_base,
                            ofmt_ctx->streams[vout_idx]->time_base);
                        int wr = av_interleaved_write_frame(ofmt_ctx, out_pkt);
                        if (wr < 0) {
                            set_last_error("Video write error file[%d]: %s", fi, av_err2str(wr));
                            ret = wr;
                            break;
                        }
                    }

                    /* Progress */
                    if (callback && onProgress && src_pts != AV_NOPTS_VALUE) {
                        int64_t pts_us = av_rescale_q(src_pts,
                            ifmt_ctx->streams[video_idx]->time_base,
                            (AVRational){1, AV_TIME_BASE});
                        int pct = (int)((progress_us + pts_us) * 100 / total_duration_us);
                        if (pct < 0) pct = 0;
                        if (pct > 100) pct = 100;
                        if (pct != last_percent) {
                            last_percent = pct;
                            (*env)->CallVoidMethod(env, callback, onProgress, pct);
                            if ((*env)->ExceptionCheck(env)) {
                                (*env)->ExceptionClear(env);
                                ret = AVERROR_EXIT;
                                break;
                            }
                        }
                    }
                }
                if (ret < 0 && ret != AVERROR_EOF) goto mt_file_end;
            }
            /* --- AUDIO --- */
            else if (pkt->stream_index == audio_idx && aout_idx >= 0 &&
                     adec_ctx && aenc_ctx && swr_ctx) {
                ret = avcodec_send_packet(adec_ctx, pkt);
                av_packet_unref(pkt);
                if (ret < 0) { ret = 0; continue; }

                while (ret >= 0 && !g_cancel) {
                    ret = avcodec_receive_frame(adec_ctx, aframe);
                    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) { ret = 0; break; }
                    if (ret < 0) break;

                    afilt_frame->sample_rate = aenc_ctx->sample_rate;
                    av_channel_layout_copy(&afilt_frame->ch_layout, &aenc_ctx->ch_layout);
                    afilt_frame->format = aenc_ctx->sample_fmt;
                    afilt_frame->nb_samples = swr_get_out_samples(swr_ctx, aframe->nb_samples);
                    av_frame_get_buffer(afilt_frame, 0);

                    int converted = swr_convert(swr_ctx,
                        afilt_frame->data, afilt_frame->nb_samples,
                        (const uint8_t **)aframe->data, aframe->nb_samples);
                    afilt_frame->nb_samples = converted;

                    /* Compute audio PTS with offset */
                    int64_t src_apts = aframe->pts;
                    if (src_apts == AV_NOPTS_VALUE) src_apts = 0;
                    int64_t apts_in_enc_tb = av_rescale_q(src_apts,
                        ifmt_ctx->streams[audio_idx]->time_base,
                        aenc_ctx->time_base);
                    afilt_frame->pts = apts_in_enc_tb + audio_pts_offset;
                    file_audio_pts_last = apts_in_enc_tb + converted;

                    av_frame_unref(aframe);

                    int sr = avcodec_send_frame(aenc_ctx, afilt_frame);
                    av_frame_unref(afilt_frame);
                    if (sr < 0) break;

                    while (1) {
                        int rr = avcodec_receive_packet(aenc_ctx, out_pkt);
                        if (rr == AVERROR(EAGAIN) || rr == AVERROR_EOF) break;
                        if (rr < 0) { ret = rr; break; }
                        out_pkt->stream_index = aout_idx;
                        av_packet_rescale_ts(out_pkt, aenc_ctx->time_base,
                            ofmt_ctx->streams[aout_idx]->time_base);
                        int wr = av_interleaved_write_frame(ofmt_ctx, out_pkt);
                        if (wr < 0) {
                            set_last_error("Audio write error file[%d]: %s", fi, av_err2str(wr));
                            ret = wr;
                            break;
                        }
                    }
                }
                if (ret < 0 && ret != AVERROR_EOF) goto mt_file_end;
            } else {
                av_packet_unref(pkt);
            }
        }

        /* Update PTS offsets for next file */
        if (file_video_pts_last > 0) {
            /* Add a small gap (1 frame) to avoid overlap */
            int64_t frame_dur = venc_ctx ? venc_ctx->time_base.den /
                (venc_ctx->framerate.num > 0 ? venc_ctx->framerate.num : 30) : 1;
            video_pts_offset += file_video_pts_last + frame_dur;
        }
        if (file_audio_pts_last > 0) {
            audio_pts_offset += file_audio_pts_last;
        }
        progress_us += file_duration;
        ret = 0; /* reset for next file */

mt_file_end:
        if (sws_ctx) sws_freeContext(sws_ctx);
        swr_free(&swr_ctx);
        av_frame_free(&frame);
        av_frame_free(&aframe);
        av_frame_free(&afilt_frame);
        av_packet_free(&pkt);
        avcodec_free_context(&vdec_ctx);
        avcodec_free_context(&adec_ctx);
        avformat_close_input(&ifmt_ctx);
        (*env)->ReleaseStringUTFChars(env, j_path, path);
        (*env)->DeleteLocalRef(env, j_path);

        if (ret < 0 && ret != AVERROR_EOF) break;
        ret = 0;
    }

    /* Flush video encoder */
    if (!g_cancel && venc_ctx && ret >= 0) {
        avcodec_send_frame(venc_ctx, NULL);
        while (1) {
            int fr = avcodec_receive_packet(venc_ctx, out_pkt);
            if (fr == AVERROR_EOF || fr < 0) break;
            out_pkt->stream_index = vout_idx;
            av_packet_rescale_ts(out_pkt, venc_ctx->time_base,
                ofmt_ctx->streams[vout_idx]->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }
    /* Flush audio encoder */
    if (!g_cancel && aenc_ctx && ret >= 0) {
        avcodec_send_frame(aenc_ctx, NULL);
        while (1) {
            int fr = avcodec_receive_packet(aenc_ctx, out_pkt);
            if (fr == AVERROR_EOF || fr < 0) break;
            out_pkt->stream_index = aout_idx;
            av_packet_rescale_ts(out_pkt, aenc_ctx->time_base,
                ofmt_ctx->streams[aout_idx]->time_base);
            av_interleaved_write_frame(ofmt_ctx, out_pkt);
        }
    }

    if (g_cancel) { ret = AVERROR_EXIT; set_last_error("Operation cancelled"); }
    else if (ret == AVERROR_EOF || ret >= 0) { av_write_trailer(ofmt_ctx); ret = 0; }
    else { set_last_error("Merge transcode error: %s", av_err2str(ret)); }

mt_end:
    avformat_close_input(&probe_ctx);
    av_packet_free(&out_pkt);
    avcodec_free_context(&venc_ctx);
    avcodec_free_context(&aenc_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);

    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  imageCompress: compress/resize image via FFmpeg (replaces Bitmap API)
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_imageCompress(
        JNIEnv *env, jclass clazz,
        jstring j_input, jstring j_output,
        jint quality, jint maxWidth, jint maxHeight) {
    const char *input  = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    clear_last_error();
    int ret = 0;

    AVFormatContext *ifmt = NULL, *ofmt = NULL;
    AVCodecContext *dec_ctx = NULL, *enc_ctx = NULL;
    struct SwsContext *sws = NULL;
    AVFrame *frame = NULL, *scaled = NULL;
    AVPacket *pkt = NULL;
    int stream_idx = -1;

    ret = avformat_open_input(&ifmt, input, NULL, NULL);
    if (ret < 0) { set_last_error("Open input: %s", av_err2str(ret)); goto ic_end; }
    ret = avformat_find_stream_info(ifmt, NULL);
    if (ret < 0) { set_last_error("Find stream: %s", av_err2str(ret)); goto ic_end; }

    for (unsigned i = 0; i < ifmt->nb_streams; i++) {
        if (ifmt->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            stream_idx = (int)i; break;
        }
    }
    if (stream_idx < 0) { set_last_error("No image stream"); ret = -1; goto ic_end; }

    {
        const AVCodec *decoder = avcodec_find_decoder(ifmt->streams[stream_idx]->codecpar->codec_id);
        if (!decoder) { set_last_error("No decoder"); ret = -1; goto ic_end; }
        dec_ctx = avcodec_alloc_context3(decoder);
        avcodec_parameters_to_context(dec_ctx, ifmt->streams[stream_idx]->codecpar);
        ret = avcodec_open2(dec_ctx, decoder, NULL);
        if (ret < 0) { set_last_error("Open decoder: %s", av_err2str(ret)); goto ic_end; }
    }

    pkt = av_packet_alloc();
    frame = av_frame_alloc();
    while (av_read_frame(ifmt, pkt) >= 0) {
        if (pkt->stream_index == stream_idx) {
            avcodec_send_packet(dec_ctx, pkt);
            ret = avcodec_receive_frame(dec_ctx, frame);
            av_packet_unref(pkt);
            if (ret >= 0) break;
        } else { av_packet_unref(pkt); }
    }
    if (ret < 0 || frame->width == 0) { set_last_error("Decode frame failed"); ret = -1; goto ic_end; }

    {
        int tw = frame->width, th = frame->height;
        if (maxWidth > 0 && maxHeight > 0 && (tw > maxWidth || th > maxHeight)) {
            double sc = fmin((double)maxWidth / tw, (double)maxHeight / th);
            tw = (int)(tw * sc); if (tw < 1) tw = 1; tw &= ~1;
            th = (int)(th * sc); if (th < 1) th = 1; th &= ~1;
        }

        scaled = av_frame_alloc();
        scaled->format = AV_PIX_FMT_YUV420P;
        scaled->width = tw; scaled->height = th;
        av_frame_get_buffer(scaled, 0);
        sws = sws_getContext(frame->width, frame->height, frame->format,
            tw, th, AV_PIX_FMT_YUV420P, SWS_LANCZOS, NULL, NULL, NULL);
        if (!sws) { set_last_error("sws_getContext failed"); ret = -1; goto ic_end; }
        sws_scale(sws, (const uint8_t *const *)frame->data, frame->linesize,
                  0, frame->height, scaled->data, scaled->linesize);
        scaled->pts = 0;

        const char *ext = strrchr(output, '.');
        enum AVCodecID enc_id = AV_CODEC_ID_MJPEG;
        enum AVPixelFormat enc_pix = AV_PIX_FMT_YUVJ420P;
        if (ext) {
            if (strcasecmp(ext, ".png") == 0) { enc_id = AV_CODEC_ID_PNG; enc_pix = AV_PIX_FMT_RGB24; }
            else if (strcasecmp(ext, ".webp") == 0) { enc_id = AV_CODEC_ID_WEBP; enc_pix = AV_PIX_FMT_YUV420P; }
        }

        if (enc_pix != AV_PIX_FMT_YUV420P) {
            AVFrame *tmp = av_frame_alloc();
            tmp->format = enc_pix; tmp->width = tw; tmp->height = th;
            av_frame_get_buffer(tmp, 0);
            struct SwsContext *s2 = sws_getContext(tw, th, AV_PIX_FMT_YUV420P,
                tw, th, enc_pix, SWS_BILINEAR, NULL, NULL, NULL);
            sws_scale(s2, (const uint8_t *const *)scaled->data, scaled->linesize,
                      0, th, tmp->data, tmp->linesize);
            sws_freeContext(s2);
            tmp->pts = 0;
            av_frame_free(&scaled); scaled = tmp;
        }

        const AVCodec *encoder = avcodec_find_encoder(enc_id);
        if (!encoder) { set_last_error("No encoder"); ret = -1; goto ic_end; }
        enc_ctx = avcodec_alloc_context3(encoder);
        enc_ctx->width = tw; enc_ctx->height = th;
        enc_ctx->pix_fmt = enc_pix;
        enc_ctx->time_base = (AVRational){1, 25};
        if (enc_id == AV_CODEC_ID_MJPEG) {
            enc_ctx->pix_fmt = AV_PIX_FMT_YUVJ420P;
            int q = 31 - (int)(quality * 30.0 / 100.0);
            if (q < 1) q = 1; if (q > 31) q = 31;
            enc_ctx->global_quality = q * FF_QP2LAMBDA;
            enc_ctx->flags |= AV_CODEC_FLAG_QSCALE;
        } else if (enc_id == AV_CODEC_ID_WEBP) {
            av_opt_set_int(enc_ctx->priv_data, "quality", quality, 0);
        }
        ret = avcodec_open2(enc_ctx, encoder, NULL);
        if (ret < 0) { set_last_error("Open encoder: %s", av_err2str(ret)); goto ic_end; }

        avformat_alloc_output_context2(&ofmt, NULL, NULL, output);
        if (!ofmt) { set_last_error("alloc output ctx"); ret = -1; goto ic_end; }
        AVStream *ost = avformat_new_stream(ofmt, NULL);
        avcodec_parameters_from_context(ost->codecpar, enc_ctx);
        if (!(ofmt->oformat->flags & AVFMT_NOFILE)) {
            ret = avio_open(&ofmt->pb, output, AVIO_FLAG_WRITE);
            if (ret < 0) { set_last_error("avio_open: %s", av_err2str(ret)); goto ic_end; }
        }
        avformat_write_header(ofmt, NULL);
        avcodec_send_frame(enc_ctx, scaled);
        avcodec_send_frame(enc_ctx, NULL);
        while (avcodec_receive_packet(enc_ctx, pkt) >= 0) {
            pkt->stream_index = 0;
            av_interleaved_write_frame(ofmt, pkt);
        }
        av_write_trailer(ofmt);
        ret = 0;
    }

ic_end:
    sws_freeContext(sws);
    av_frame_free(&frame); av_frame_free(&scaled);
    av_packet_free(&pkt);
    avcodec_free_context(&dec_ctx); avcodec_free_context(&enc_ctx);
    avformat_close_input(&ifmt);
    if (ofmt && !(ofmt->oformat->flags & AVFMT_NOFILE)) avio_closep(&ofmt->pb);
    avformat_free_context(ofmt);
    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  imageEnhance: sharpen image using FFmpeg unsharp filter
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_imageEnhance(
        JNIEnv *env, jclass clazz,
        jstring j_input, jstring j_output, jfloat strength) {
    const char *input  = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    clear_last_error();
    int ret = 0;

    AVFormatContext *ifmt = NULL, *ofmt = NULL;
    AVCodecContext *dec_ctx = NULL, *enc_ctx = NULL;
    AVFilterGraph *fgraph = NULL;
    AVFilterContext *src_ctx = NULL, *sink_ctx = NULL;
    AVFrame *frame = NULL, *filt = NULL;
    AVPacket *pkt = NULL;
    int sidx = -1;

    ret = avformat_open_input(&ifmt, input, NULL, NULL);
    if (ret < 0) { set_last_error("Open: %s", av_err2str(ret)); goto ien_end; }
    avformat_find_stream_info(ifmt, NULL);
    for (unsigned i = 0; i < ifmt->nb_streams; i++)
        if (ifmt->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) { sidx = (int)i; break; }
    if (sidx < 0) { set_last_error("No image"); ret = -1; goto ien_end; }

    { const AVCodec *d = avcodec_find_decoder(ifmt->streams[sidx]->codecpar->codec_id);
      dec_ctx = avcodec_alloc_context3(d);
      avcodec_parameters_to_context(dec_ctx, ifmt->streams[sidx]->codecpar);
      avcodec_open2(dec_ctx, d, NULL); }

    frame = av_frame_alloc(); pkt = av_packet_alloc();
    while (av_read_frame(ifmt, pkt) >= 0) {
        if (pkt->stream_index == sidx) {
            avcodec_send_packet(dec_ctx, pkt);
            ret = avcodec_receive_frame(dec_ctx, frame);
            av_packet_unref(pkt);
            if (ret >= 0) break;
        } else av_packet_unref(pkt);
    }
    if (frame->width == 0) { set_last_error("Decode failed"); ret = -1; goto ien_end; }

    fgraph = avfilter_graph_alloc();
    { char args[256];
      snprintf(args, sizeof(args), "video_size=%dx%d:pix_fmt=%d:time_base=1/25:pixel_aspect=1/1",
               frame->width, frame->height, frame->format);
      avfilter_graph_create_filter(&src_ctx, avfilter_get_by_name("buffer"), "in", args, NULL, fgraph);
      avfilter_graph_create_filter(&sink_ctx, avfilter_get_by_name("buffersink"), "out", NULL, NULL, fgraph);
      float amt = strength > 0 ? strength : 1.5f;
      char ua[64]; snprintf(ua, sizeof(ua), "5:5:%.2f:5:5:%.2f", amt, amt);
      AVFilterContext *uc = NULL;
      avfilter_graph_create_filter(&uc, avfilter_get_by_name("unsharp"), "unsharp", ua, NULL, fgraph);
      avfilter_link(src_ctx, 0, uc, 0);
      avfilter_link(uc, 0, sink_ctx, 0);
      avfilter_graph_config(fgraph, NULL); }

    frame->pts = 0;
    av_buffersrc_add_frame(src_ctx, frame);
    av_buffersrc_add_frame(src_ctx, NULL);
    filt = av_frame_alloc();
    ret = av_buffersink_get_frame(sink_ctx, filt);
    if (ret < 0) { set_last_error("filter: %s", av_err2str(ret)); goto ien_end; }

    { const char *ext = strrchr(output, '.');
      enum AVCodecID eid = AV_CODEC_ID_MJPEG;
      enum AVPixelFormat epx = AV_PIX_FMT_YUVJ420P;
      if (ext && strcasecmp(ext, ".png") == 0) { eid = AV_CODEC_ID_PNG; epx = AV_PIX_FMT_RGB24; }
      else if (ext && strcasecmp(ext, ".webp") == 0) { eid = AV_CODEC_ID_WEBP; epx = AV_PIX_FMT_YUV420P; }

      AVFrame *ef = filt;
      AVFrame *cvt = NULL;
      if (filt->format != (int)epx) {
          cvt = av_frame_alloc(); cvt->format = epx;
          cvt->width = filt->width; cvt->height = filt->height;
          av_frame_get_buffer(cvt, 0);
          struct SwsContext *s = sws_getContext(filt->width, filt->height, filt->format,
              cvt->width, cvt->height, epx, SWS_BILINEAR, NULL, NULL, NULL);
          sws_scale(s, (const uint8_t *const *)filt->data, filt->linesize,
                    0, filt->height, cvt->data, cvt->linesize);
          sws_freeContext(s); cvt->pts = 0; ef = cvt;
      }

      const AVCodec *enc = avcodec_find_encoder(eid);
      enc_ctx = avcodec_alloc_context3(enc);
      enc_ctx->width = ef->width; enc_ctx->height = ef->height;
      enc_ctx->pix_fmt = epx; enc_ctx->time_base = (AVRational){1,25};
      if (eid == AV_CODEC_ID_MJPEG) {
          enc_ctx->pix_fmt = AV_PIX_FMT_YUVJ420P;
          enc_ctx->global_quality = 2 * FF_QP2LAMBDA;
          enc_ctx->flags |= AV_CODEC_FLAG_QSCALE;
      }
      avcodec_open2(enc_ctx, enc, NULL);

      avformat_alloc_output_context2(&ofmt, NULL, NULL, output);
      AVStream *os = avformat_new_stream(ofmt, NULL);
      avcodec_parameters_from_context(os->codecpar, enc_ctx);
      if (!(ofmt->oformat->flags & AVFMT_NOFILE))
          avio_open(&ofmt->pb, output, AVIO_FLAG_WRITE);
      avformat_write_header(ofmt, NULL);
      ef->pts = 0;
      avcodec_send_frame(enc_ctx, ef);
      avcodec_send_frame(enc_ctx, NULL);
      while (avcodec_receive_packet(enc_ctx, pkt) >= 0) {
          pkt->stream_index = 0;
          av_interleaved_write_frame(ofmt, pkt);
      }
      av_write_trailer(ofmt);
      if (cvt) av_frame_free(&cvt);
      ret = 0;
    }

ien_end:
    avfilter_graph_free(&fgraph);
    av_frame_free(&frame); av_frame_free(&filt);
    av_packet_free(&pkt);
    avcodec_free_context(&dec_ctx); avcodec_free_context(&enc_ctx);
    avformat_close_input(&ifmt);
    if (ofmt && !(ofmt->oformat->flags & AVFMT_NOFILE)) avio_closep(&ofmt->pb);
    avformat_free_context(ofmt);
    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}

/* ====================================================================
 *  videoEnhance: sharpen video using FFmpeg unsharp filter
 * ==================================================================== */
JNIEXPORT jint JNICALL
Java_com_advancefilemanager_plugin_ffmpegtools_FFmpegJni_videoEnhance(
        JNIEnv *env, jclass clazz,
        jstring j_input, jstring j_output,
        jfloat strength, jint targetBitrateKbps,
        jobject j_callback) {
    const char *input  = (*env)->GetStringUTFChars(env, j_input, NULL);
    const char *output = (*env)->GetStringUTFChars(env, j_output, NULL);
    clear_last_error(); g_cancel = 0;
    int ret = 0;

    AVFormatContext *ifmt = NULL, *ofmt = NULL;
    AVCodecContext *vdec = NULL, *venc = NULL, *adec = NULL, *aenc = NULL;
    AVFilterGraph *fgraph = NULL;
    AVFilterContext *buf_ctx = NULL, *fsink_ctx = NULL;
    struct SwsContext *sws_enc = NULL;
    struct SwrContext *swr = NULL;
    AVFrame *frame = NULL, *filt_frame = NULL, *enc_frame = NULL, *aframe = NULL;
    AVPacket *pkt = NULL;
    int vi = -1, ai = -1, vo = -1, ao = -1;
    int is_hw_encoder = 0;
    int filter_inited = 0;

    jmethodID cb = NULL;
    if (j_callback) { jclass c = (*env)->GetObjectClass(env, j_callback);
      cb = (*env)->GetMethodID(env, c, "onProgress", "(I)V"); }

    float amt = strength > 0 ? strength : 1.5f;

    ret = avformat_open_input(&ifmt, input, NULL, NULL);
    if (ret < 0) { set_last_error("Open: %s", av_err2str(ret)); goto ve_end; }
    avformat_find_stream_info(ifmt, NULL);
    for (unsigned i = 0; i < ifmt->nb_streams; i++) {
        if (ifmt->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && vi < 0) vi = (int)i;
        if (ifmt->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO && ai < 0) ai = (int)i;
    }
    if (vi < 0) { set_last_error("No video"); ret = -1; goto ve_end; }

    /* Decoder with HW acceleration */
    if ((ret = open_video_decoder_hw(ifmt, vi, &vdec)) < 0) {
        set_last_error("Open decoder: %s", av_err2str(ret)); goto ve_end;
    }

    /* Encoder with HW fallback */
    {
        int br = targetBitrateKbps > 0 ? targetBitrateKbps * 1000
               : (vdec->bit_rate > 0 ? (int)vdec->bit_rate : 4000000);
        avformat_alloc_output_context2(&ofmt, NULL, NULL, output);
        if (!ofmt) { set_last_error("Cannot create output context"); ret = -1; goto ve_end; }
        ret = open_video_encoder_with_fallback(AV_CODEC_ID_H264, vdec, ifmt, vi,
                                               ofmt->oformat, (int64_t)br,
                                               &venc, &is_hw_encoder);
        if (ret < 0) { set_last_error("H.264 encoder init failed: %s", av_err2str(ret)); goto ve_end; }
    }

    { AVStream *vs = avformat_new_stream(ofmt, NULL);
      if (!vs) { set_last_error("Cannot create output stream"); ret = AVERROR(ENOMEM); goto ve_end; }
      avcodec_parameters_from_context(vs->codecpar, venc);
      vs->time_base = venc->time_base; vo = vs->index; }

    if (ai >= 0) {
        const AVCodec *ad = avcodec_find_decoder(ifmt->streams[ai]->codecpar->codec_id);
        if (ad) {
            adec = avcodec_alloc_context3(ad);
            avcodec_parameters_to_context(adec, ifmt->streams[ai]->codecpar);
            if (avcodec_open2(adec, ad, NULL) >= 0) {
                ret = open_audio_encoder_with_resampler(
                    ifmt->streams[ai]->codecpar->codec_id,
                    adec, (ofmt->oformat->flags & AVFMT_GLOBALHEADER) ? 1 : 0,
                    &aenc, &swr);
                if (ret >= 0) {
                    AVStream *as = avformat_new_stream(ofmt, NULL);
                    avcodec_parameters_from_context(as->codecpar, aenc);
                    as->time_base = aenc->time_base; ao = as->index;
                } else {
                    /* audio encode setup failed - proceed without audio */
                    avcodec_free_context(&adec);
                    adec = NULL; aenc = NULL; swr = NULL;
                }
            } else {
                avcodec_free_context(&adec);
                adec = NULL;
            }
        }
    }

    if (!(ofmt->oformat->flags & AVFMT_NOFILE))
        avio_open(&ofmt->pb, output, AVIO_FLAG_WRITE);
    write_header_faststart(ofmt);

    int64_t dur = ifmt->duration > 0 ? ifmt->duration : 1;
    int last_pct = -1;
    frame = av_frame_alloc(); aframe = av_frame_alloc(); pkt = av_packet_alloc();
    filt_frame = av_frame_alloc();
    if (!frame || !aframe || !pkt || !filt_frame) { ret = AVERROR(ENOMEM); goto ve_end; }

    while (!g_cancel && av_read_frame(ifmt, pkt) >= 0) {
        if (pkt->stream_index == vi) {
            /* Save progress info from packet before decoding */
            int64_t pkt_pts_us = -1;
            if (cb && pkt->pts != AV_NOPTS_VALUE) {
                pkt_pts_us = av_rescale_q(pkt->pts, ifmt->streams[vi]->time_base,
                                          (AVRational){1, AV_TIME_BASE});
            }

            avcodec_send_packet(vdec, pkt);
            while (avcodec_receive_frame(vdec, frame) >= 0) {
                AVFrame *src_frame = frame;
                AVFrame *sw_frame = NULL;

                /* Transfer HW frames to CPU */
                if (frame->format == AV_PIX_FMT_MEDIACODEC || frame->hw_frames_ctx != NULL) {
                    sw_frame = av_frame_alloc();
                    if (sw_frame && av_hwframe_transfer_data(sw_frame, frame, 0) >= 0) {
                        sw_frame->pts = frame->pts;
                        src_frame = sw_frame;
                    } else {
                        av_frame_free(&sw_frame);
                        sw_frame = NULL;
                    }
                }

                /* Lazy init filter graph on first frame */
                if (!filter_inited) {
                    enum AVPixelFormat src_pf = (enum AVPixelFormat)src_frame->format;
                    if (src_pf == AV_PIX_FMT_NONE || src_pf == AV_PIX_FMT_MEDIACODEC)
                        src_pf = AV_PIX_FMT_NV12;

                    fgraph = avfilter_graph_alloc();
                    char args[256];
                    snprintf(args, sizeof(args),
                        "video_size=%dx%d:pix_fmt=%d:time_base=%d/%d:pixel_aspect=1/1",
                        src_frame->width, src_frame->height, (int)src_pf,
                        ifmt->streams[vi]->time_base.num, ifmt->streams[vi]->time_base.den);
                    avfilter_graph_create_filter(&buf_ctx, avfilter_get_by_name("buffer"), "in", args, NULL, fgraph);
                    avfilter_graph_create_filter(&fsink_ctx, avfilter_get_by_name("buffersink"), "out", NULL, NULL, fgraph);

                    char ua[64]; snprintf(ua, sizeof(ua), "5:5:%.2f:5:5:%.2f", amt, amt);
                    AVFilterContext *uc = NULL;
                    avfilter_graph_create_filter(&uc, avfilter_get_by_name("unsharp"), "unsharp", ua, NULL, fgraph);
                    avfilter_link(buf_ctx, 0, uc, 0);
                    avfilter_link(uc, 0, fsink_ctx, 0);
                    if (avfilter_graph_config(fgraph, NULL) < 0) {
                        set_last_error("Filter graph config failed");
                        if (sw_frame) av_frame_free(&sw_frame);
                        av_frame_unref(frame);
                        ret = AVERROR_UNKNOWN; goto ve_flush;
                    }

                    /* sws for filter output -> encoder pixel format if needed */
                    int filt_pf = av_buffersink_get_format(fsink_ctx);
                    if (filt_pf != (int)venc->pix_fmt) {
                        sws_enc = sws_getContext(venc->width, venc->height, filt_pf,
                                                 venc->width, venc->height, venc->pix_fmt,
                                                 SWS_BILINEAR, NULL, NULL, NULL);
                        enc_frame = av_frame_alloc();
                        enc_frame->format = venc->pix_fmt;
                        enc_frame->width = venc->width;
                        enc_frame->height = venc->height;
                        av_frame_get_buffer(enc_frame, 0);
                    }
                    filter_inited = 1;
                }

                /* Push decoded frame to filter */
                src_frame->pts = frame->best_effort_timestamp;
                av_buffersrc_add_frame(buf_ctx, src_frame);
                if (sw_frame) av_frame_free(&sw_frame);

                /* Get filtered frames and encode */
                while (av_buffersink_get_frame(fsink_ctx, filt_frame) >= 0) {
                    AVFrame *to_encode;
                    if (sws_enc) {
                        av_frame_make_writable(enc_frame);
                        sws_scale(sws_enc,
                            (const uint8_t *const *)filt_frame->data, filt_frame->linesize,
                            0, venc->height, enc_frame->data, enc_frame->linesize);
                        enc_frame->pts = av_rescale_q(filt_frame->pts,
                            ifmt->streams[vi]->time_base, venc->time_base);
                        to_encode = enc_frame;
                    } else {
                        filt_frame->pts = av_rescale_q(filt_frame->pts,
                            ifmt->streams[vi]->time_base, venc->time_base);
                        to_encode = filt_frame;
                    }

                    avcodec_send_frame(venc, to_encode);
                    AVPacket *ep = av_packet_alloc();
                    while (avcodec_receive_packet(venc, ep) >= 0) {
                        ep->stream_index = vo;
                        av_packet_rescale_ts(ep, venc->time_base, ofmt->streams[vo]->time_base);
                        av_interleaved_write_frame(ofmt, ep);
                    }
                    av_packet_free(&ep);
                    av_frame_unref(filt_frame);
                }

                if (pkt_pts_us >= 0) {
                    int pct = (int)(pkt_pts_us * 100 / dur);
                    if (pct < 0) pct = 0;
                    if (pct > 100) pct = 100;
                    if (pct != last_pct) {
                        last_pct = pct;
                        (*env)->CallVoidMethod(env, j_callback, cb, pct);
                        if ((*env)->ExceptionCheck(env)) {
                            (*env)->ExceptionClear(env);
                            ret = AVERROR_EXIT; goto ve_flush;
                        }
                    }
                }
                av_frame_unref(frame);
            }
        } else if (pkt->stream_index == ai && adec && aenc) {
            avcodec_send_packet(adec, pkt);
            while (avcodec_receive_frame(adec, aframe) >= 0) {
                AVFrame *oa = av_frame_alloc();
                oa->format = aenc->sample_fmt;
                av_channel_layout_copy(&oa->ch_layout, &aenc->ch_layout);
                oa->sample_rate = aenc->sample_rate;
                oa->nb_samples = aenc->frame_size > 0 ? aenc->frame_size : aframe->nb_samples;
                av_frame_get_buffer(oa, 0);
                swr_convert(swr, oa->data, oa->nb_samples,
                    (const uint8_t **)aframe->data, aframe->nb_samples);
                oa->pts = av_rescale_q(aframe->pts, adec->time_base, aenc->time_base);
                avcodec_send_frame(aenc, oa); av_frame_free(&oa);
                AVPacket *ap = av_packet_alloc();
                while (avcodec_receive_packet(aenc, ap) >= 0) {
                    ap->stream_index = ao;
                    av_packet_rescale_ts(ap, aenc->time_base, ofmt->streams[ao]->time_base);
                    av_interleaved_write_frame(ofmt, ap);
                }
                av_packet_free(&ap);
            }
        }
        av_packet_unref(pkt);
    }

ve_flush:
    if (!g_cancel && ret >= 0) {
        /* Flush filter graph */
        if (buf_ctx) av_buffersrc_add_frame(buf_ctx, NULL);
        while (fsink_ctx && av_buffersink_get_frame(fsink_ctx, filt_frame) >= 0) {
            AVFrame *to_encode;
            if (sws_enc) {
                av_frame_make_writable(enc_frame);
                sws_scale(sws_enc,
                    (const uint8_t *const *)filt_frame->data, filt_frame->linesize,
                    0, venc->height, enc_frame->data, enc_frame->linesize);
                enc_frame->pts = av_rescale_q(filt_frame->pts,
                    ifmt->streams[vi]->time_base, venc->time_base);
                to_encode = enc_frame;
            } else {
                filt_frame->pts = av_rescale_q(filt_frame->pts,
                    ifmt->streams[vi]->time_base, venc->time_base);
                to_encode = filt_frame;
            }
            avcodec_send_frame(venc, to_encode);
            AVPacket *ep = av_packet_alloc();
            while (avcodec_receive_packet(venc, ep) >= 0) {
                ep->stream_index = vo;
                av_packet_rescale_ts(ep, venc->time_base, ofmt->streams[vo]->time_base);
                av_interleaved_write_frame(ofmt, ep);
            }
            av_packet_free(&ep);
            av_frame_unref(filt_frame);
        }
        /* Flush encoder */
        avcodec_send_frame(venc, NULL);
        while (avcodec_receive_packet(venc, pkt) >= 0) {
            pkt->stream_index = vo;
            av_packet_rescale_ts(pkt, venc->time_base, ofmt->streams[vo]->time_base);
            av_interleaved_write_frame(ofmt, pkt);
        }
        if (aenc) { avcodec_send_frame(aenc, NULL);
            while (avcodec_receive_packet(aenc, pkt) >= 0) {
                pkt->stream_index = ao;
                av_packet_rescale_ts(pkt, aenc->time_base, ofmt->streams[ao]->time_base);
                av_interleaved_write_frame(ofmt, pkt);
            }
        }
        av_write_trailer(ofmt); ret = 0;
    } else if (g_cancel) { ret = AVERROR_EXIT; set_last_error("Cancelled"); }

ve_end:
    avfilter_graph_free(&fgraph);
    if (sws_enc) sws_freeContext(sws_enc);
    swr_free(&swr);
    av_frame_free(&frame); av_frame_free(&filt_frame);
    av_frame_free(&enc_frame); av_frame_free(&aframe);
    av_packet_free(&pkt);
    avcodec_free_context(&vdec); avcodec_free_context(&venc);
    avcodec_free_context(&adec); avcodec_free_context(&aenc);
    avformat_close_input(&ifmt);
    if (ofmt && !(ofmt->oformat->flags & AVFMT_NOFILE)) avio_closep(&ofmt->pb);
    avformat_free_context(ofmt);
    (*env)->ReleaseStringUTFChars(env, j_input, input);
    (*env)->ReleaseStringUTFChars(env, j_output, output);
    return ret;
}
