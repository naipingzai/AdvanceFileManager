/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class ToolItem(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val intent: Intent
)
