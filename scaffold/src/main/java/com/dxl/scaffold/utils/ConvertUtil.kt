package com.dxl.scaffold.utils

import android.content.res.Resources
import android.util.TypedValue

/**
 * @author duxiaolong
 */
val Number.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()