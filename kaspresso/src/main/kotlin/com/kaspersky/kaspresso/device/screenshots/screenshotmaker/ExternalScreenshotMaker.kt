package com.kaspersky.kaspresso.device.screenshots.screenshotmaker

import android.app.UiAutomation
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.test.uiautomator.UiDevice
import com.kaspersky.kaspresso.instrumental.InstrumentalDependencyProvider
import com.kaspersky.kaspresso.params.ScreenshotParams
import java.io.File
import java.io.FileNotFoundException
import java.io.OutputStream

/**
 * Captures spoon-compatible screenshots by uiautomator.
 */
class ExternalScreenshotMaker(
    private val instrumentalDependencyProvider: InstrumentalDependencyProvider,
    private val contentResolver: ContentResolver,
    private val params: ScreenshotParams = ScreenshotParams(),
) : ScreenshotMaker {

    private val device: UiDevice
        get() = instrumentalDependencyProvider.uiDevice

    // Somehow scale param is not used in UiDevice#takeScreenshot method,
    // so just using default here
    private val scale: Float = 1.0f

    override fun takeScreenshot(screenshotName: String, parentDirPath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uiAutomation = instrumentalDependencyProvider.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
            val bitmap = uiAutomation.takeScreenshot()
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, screenshotName)
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$parentDirPath")
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.Images.Media.WIDTH, bitmap.width)
            contentValues.put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            try {
                uri?.let {
                    val outputStream: OutputStream? = contentResolver.openOutputStream(it)
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
            device.takeScreenshot(File(parentDirPath, screenshotName), scale, params.quality)
        }
    }
}
