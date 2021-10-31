package com.kaspersky.kaspresso.docloc

import android.os.Build
import com.kaspersky.kaspresso.device.screenshots.screenshotmaker.ScreenshotMaker
import com.kaspersky.kaspresso.files.extensions.FileExtension
import com.kaspersky.kaspresso.files.resources.ResourceFileNamesProvider
import com.kaspersky.kaspresso.files.resources.ResourceFilesProvider
import com.kaspersky.kaspresso.internal.wait.wait
import com.kaspersky.kaspresso.logger.UiTestLogger

/**
 * The class for saving and clearing docloc screenshots with metadata.
 * @param screenshotRootDir directory to save screenshots and metadata.
 */
internal class DocLocScreenshotCapturer(
    private val logger: UiTestLogger,
    private val resourceFilesProvider: ResourceFilesProvider,
    private val screenshotMaker: ScreenshotMaker,
    private val metadataSaver: MetadataSaver,
    private val resourceFileNamesProvider: ResourceFileNamesProvider
) {
    private companion object {
        private const val SCREENSHOT_CAPTURE_DELAY_MS: Long = 500
        private const val SCREENSHOT_DEFAULT_FAILS_SUB_DIR = "fails"
    }

    /**
     * Captures a screenshot and save it with metadata to [screenshotRootDir].
     * @param screenshotName name of screenshot. Must match [a-zA-Z0-9_-]+
     */
    fun captureScreenshot(screenshotName: String) {
        wait(timeoutMs = SCREENSHOT_CAPTURE_DELAY_MS, logger = logger) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resFileName: String = resourceFileNamesProvider.getFileName(
                    screenshotName,
                    FileExtension.PNG.toString()
                )
                val parentPath = resourceFilesProvider.provideScreenshotParentPath()
                screenshotMaker.takeScreenshot(resFileName, parentPath)
                metadataSaver.saveScreenshotMetadata(screenshotName, parentPath)
            } else {
                val screenshotFile = resourceFilesProvider.provideScreenshotFile(screenshotName)
                screenshotFile.parentFile?.let {
                    screenshotMaker.takeScreenshot(screenshotFile.name, it.path)
                    metadataSaver.saveScreenshotMetadata(screenshotName, it.path)
                }
            }
        }
    }

    /**
     * Captures a screenshot and save it to [screenshotRootDir]/fails.
     * @param screenshotName name of screenshot. Must match [a-zA-Z0-9_-]+
     */
    fun captureScreenshotOnFail(screenshotName: String) {
        wait(timeoutMs = SCREENSHOT_CAPTURE_DELAY_MS, logger = logger) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resFileName: String = resourceFileNamesProvider.getFileName(
                    screenshotName,
                    FileExtension.PNG.toString()
                )
                val parentPath = resourceFilesProvider.provideScreenshotParentPath(SCREENSHOT_DEFAULT_FAILS_SUB_DIR)
                screenshotMaker.takeScreenshot(resFileName, parentPath)
            } else {
                val screenshotFile = resourceFilesProvider.provideScreenshotFile(screenshotName, SCREENSHOT_DEFAULT_FAILS_SUB_DIR)
                screenshotFile.parentFile?.let {
                    screenshotMaker.takeScreenshot(screenshotFile.name, it.path)
                }
            }
        }
    }
}
