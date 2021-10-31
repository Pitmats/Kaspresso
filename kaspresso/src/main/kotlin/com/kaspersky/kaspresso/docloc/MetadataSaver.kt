package com.kaspersky.kaspresso.docloc

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import com.kaspersky.kaspresso.device.Device
import com.kaspersky.kaspresso.device.activities.Activities
import com.kaspersky.kaspresso.device.activities.metadata.ActivityMetadata
import com.kaspersky.kaspresso.device.apps.Apps
import com.kaspersky.kaspresso.internal.extensions.other.safeWrite
import com.kaspersky.kaspresso.internal.extensions.other.toXml
import com.kaspersky.kaspresso.logger.UiTestLogger
import java.io.File
import java.io.OutputStream

internal class MetadataSaver(
    private val activities: Activities,
    private val apps: Apps,
    private val logger: UiTestLogger,
    private val device: Device
) {
    private val activityMetadata = ActivityMetadata(logger)

    fun saveScreenshotMetadata(name: String, folderPath: String) {
        val activity = activities.getResumed()
        if (activity == null) {
            logger.e("Activity is null when saving metadata $name")
            return
        }
        runCatching {
            val metadata = activityMetadata.getFromActivity(activity)
                .toXml(apps.targetAppPackageName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "$name.xml")
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Documents/$folderPath")
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "text/xml")
                var uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                uri = device.targetContext.contentResolver.insert(uri, contentValues)
                uri?.let {
                    val outputStream: OutputStream? = device.targetContext.contentResolver.openOutputStream(uri)
                    outputStream.use { outputStream ->
                        outputStream?.let {
                            outputStream.write(metadata.encodeToByteArray())
                            outputStream.flush()
                        }
                    }
                }
            } else {
                File(folderPath).resolve("$name.xml").safeWrite(logger, metadata)
            }
        }
    }
}
