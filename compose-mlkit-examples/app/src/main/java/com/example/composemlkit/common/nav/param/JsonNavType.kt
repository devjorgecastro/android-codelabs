package com.example.composemlkit.common.nav.param

import android.os.Build
import android.os.Bundle
import androidx.navigation.NavType
import com.example.composemlkit.common.models.FileData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

abstract class JsonNavType<T> : NavType<T>(isNullableAllowed = false) {
    abstract fun fromJsonParse(value: String): T
    abstract fun T.getJsonParse(): String

    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let { parseValue(it) }

    override fun parseValue(value: String): T = fromJsonParse(value)

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, value.getJsonParse())
    }
}

class FileDataNavType : NavType<FileData>(isNullableAllowed = true) {
    override fun get(bundle: Bundle, key: String): FileData? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, FileData::class.java)
        } else {
            bundle.getParcelable(key)
        }
    }

    override fun parseValue(value: String): FileData {
        return Gson().fromJson(value, object : TypeToken<FileData>() {}.type)
    }

    override fun put(bundle: Bundle, key: String, value: FileData) {
        bundle.putParcelable(key, value)
    }

}