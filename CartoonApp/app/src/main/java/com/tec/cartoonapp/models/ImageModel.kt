package com.tec.cartoonapp.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [ImageModel] is used to parcelable objects to other fragments
 * @param uri image uri from take photo
 */
@Parcelize
class ImageModel (
        val uri: Uri,
): Parcelable


