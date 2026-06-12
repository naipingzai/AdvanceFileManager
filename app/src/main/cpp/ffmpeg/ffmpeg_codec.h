/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 *
 * Hardware/software codec helper declarations.
 */

#ifndef FFMPEG_CODEC_H
#define FFMPEG_CODEC_H

#include "ffmpeg_common.h"

const AVCodec *find_hw_decoder(enum AVCodecID codec_id);
const AVCodec *find_hw_encoder(enum AVCodecID codec_id);
int open_video_decoder_hw(AVFormatContext *ifmt_ctx, int video_idx,
                          AVCodecContext **out_dec_ctx);
int open_video_encoder_with_fallback(
        enum AVCodecID codec_id,
        AVCodecContext *dec_ctx,
        AVFormatContext *ifmt_ctx, int video_idx,
        const AVOutputFormat *oformat,
        int64_t bit_rate,
        AVCodecContext **out_enc_ctx, int *out_is_hw);
int open_audio_encoder_with_resampler(
        enum AVCodecID codec_id,
        AVCodecContext *dec_ctx,
        int global_header,
        AVCodecContext **out_enc_ctx,
        SwrContext **out_swr_ctx);

#endif /* FFMPEG_CODEC_H */
