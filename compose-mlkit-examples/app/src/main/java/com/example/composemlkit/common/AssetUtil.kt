package com.example.composemlkit.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Environment
import androidx.activity.ComponentActivity
import com.example.composemlkit.R
import java.io.File
import java.io.FileOutputStream

object AssetUtil {
    private const val MAX_QUALITY = 100
    private const val DEFAULT_FILE_NAME = "demo-photo-file.jpg"

    private fun getOutputDirectory(context: Context): File? {
        val activityContext = context as? ComponentActivity
        val mediaDir =
            activityContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let { mFile ->
                File(mFile, context.getString(R.string.app_name)).apply {
                    mkdirs()
                }
            } ?: return null

        return if (mediaDir.exists()) mediaDir else context.filesDir
    }

    fun createDefaultFile(context: Context): File? {
        val activityContext = context as? ComponentActivity
        activityContext ?: return null
        return File(getOutputDirectory(activityContext), DEFAULT_FILE_NAME)
    }

    fun addWaterMark(currentFile: File, context: Context) {
        val currentBitmap = BitmapFactory.decodeFile(currentFile.path)
        val newBitmap = mergeBitmapWithDevFestBadge(context, currentBitmap)
        val stream = FileOutputStream(currentFile)
        newBitmap?.compress(Bitmap.CompressFormat.JPEG, MAX_QUALITY, stream)
        stream.run {
            flush()
            close()
        }
    }

    private fun mergeBitmapWithDevFestBadge(context: Context, src: Bitmap): Bitmap? {
        val margin = 50f
        val result = Bitmap.createBitmap(src.width, src.height, src.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)
        val waterMark = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.badge_devfest2022_opacity_blue
        )
        val badgeX = src.width - waterMark.width.toFloat() - margin
        canvas.drawBitmap(waterMark, badgeX, margin, null)
        return result
    }
}
