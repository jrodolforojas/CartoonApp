package com.tec.cartoonapp.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ImageModel (
        val uri: Uri,
): Parcelable


