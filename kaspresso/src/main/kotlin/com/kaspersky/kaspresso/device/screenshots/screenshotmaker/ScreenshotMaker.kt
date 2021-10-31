package com.kaspersky.kaspresso.device.screenshots.screenshotmaker

/**
 * Creates and saves a screenshot
 */
interface ScreenshotMaker {

    fun takeScreenshot(screenshotName: String, parentDirPath: String)
}
