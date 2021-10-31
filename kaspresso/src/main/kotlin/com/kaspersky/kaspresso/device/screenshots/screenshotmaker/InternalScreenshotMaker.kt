package com.kaspersky.kaspresso.device.screenshots.screenshotmaker

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import com.kaspersky.kaspresso.device.activities.Activities
import com.kaspersky.kaspresso.params.ScreenshotParams
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch

/**
 * Captures the view of a current activity
 */
class InternalScreenshotMaker(
    private val activities: Activities,
    private val params: ScreenshotParams
) : ScreenshotMaker {

    override fun takeScreenshot(screenshotName: String, parentDirPath: String) {
        val activity = activities.getResumed() ?: throw RuntimeException("There is no resumed activity.")
        val view = activity.window.decorView
        if (view.width == 0 || view.height == 0) {
            throw RuntimeException(
                "The view of ${activity.javaClass.simpleName} has no height or width."
            )
        }

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, screenshotName)
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$parentDirPath")
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.Images.Media.WIDTH, bitmap.width)
            contentValues.put(MediaStore.Images.Media.HEIGHT, bitmap.height)

            val uri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            try {
                uri?.let {
                    val outputStream: OutputStream? = activity.contentResolver.openOutputStream(uri)
                    outputStream.use { outputStream ->
                        outputStream?.let {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream)
                            outputStream.flush()
                            contentValues.clear()
                        }
                    }
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            val file = File(parentDirPath, screenshotName)
            fillBitmap(activity, bitmap, file)

            BufferedOutputStream(FileOutputStream(file)).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, params.quality, outputStream)
                file.setReadable(true, false)
            }
            bitmap.recycle()
        }
    }

    private fun fillBitmap(activity: Activity, bitmap: Bitmap, file: File) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            activity.drawToBitmap(bitmap)
            return
        }

        val latch = CountDownLatch(1)
        activity.runOnUiThread {
            try {
                activity.drawToBitmap(bitmap)
            } finally {
                latch.countDown()
            }
        }
        latch.runCatching { await() }
            .onFailure { e -> throw RuntimeException("Unable to get screenshot ${file.absolutePath}", e) }
    }

    private fun Activity.drawToBitmap(bitmap: Bitmap) {
        window.decorView.draw(Canvas(bitmap))
    }
}
