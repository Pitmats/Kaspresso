package com.kaspersky.kaspresso.device.screenshots.screenshotmaker

/**
 * Calls [preferredScreenshotMaker] and fallbacks to [fallbackScreenshotMaker] on fail
 */
class CombinedScreenshotMaker(
    private val preferredScreenshotMaker: ScreenshotMaker,
    private val fallbackScreenshotMaker: ScreenshotMaker
) : ScreenshotMaker {

    override fun takeScreenshot(screenshotName: String, parentDirPath: String) {
        runCatching {
            preferredScreenshotMaker.takeScreenshot(screenshotName, parentDirPath)
        }.onFailure {
            fallbackScreenshotMaker.takeScreenshot(screenshotName, parentDirPath)
        }
    }
}
