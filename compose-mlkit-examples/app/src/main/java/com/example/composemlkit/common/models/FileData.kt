package com.example.composemlkit.common.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FileData(val absolutePath: String) : Parcelable
