package com.kaspersky.kaspresso.files.resources.impl

import com.kaspersky.kaspresso.files.dirs.DirsProvider
import com.kaspersky.kaspresso.files.models.TestMethod
import com.kaspersky.kaspresso.files.extensions.findTestMethod
import com.kaspersky.kaspresso.files.resources.ResourcesDirNameProvider
import com.kaspersky.kaspresso.files.resources.ResourcesDirsProvider
import java.io.File

class DefaultResourcesDirsProvider(
    private val dirsProvider: DirsProvider,
    private val resourcesDirNameProvider: ResourcesDirNameProvider,
) : ResourcesDirsProvider {

    override fun provide(dest: File, subDir: String?): File {
        val rootDir: File = dirsProvider.provideNew(dest)
        val resourcesDest: File = resolveResourcesDirDest(rootDir, subDir)
        return dirsProvider.provideCleared(resourcesDest)
    }

    override fun provideParentPath(dest: String, subDir: String?): String {
        val testMethod: TestMethod = Thread.currentThread().stackTrace.findTestMethod()
        val resourcesDirName: String = resourcesDirNameProvider.provideResourcesDirName(testMethod)
        val destWithSubDir = when {
            subDir == null -> dest
            dest.endsWith(File.separatorChar) -> dest + subDir
            else -> dest + File.separatorChar + subDir
        }
        return when {
            destWithSubDir.endsWith(File.separatorChar) -> destWithSubDir + resourcesDirName
            else -> destWithSubDir + File.separatorChar + resourcesDirName
        }
    }

    private fun resolveResourcesDirDest(rootDir: File, subDir: String? = null): File {
        val testMethod: TestMethod = Thread.currentThread().stackTrace.findTestMethod()
        val resourcesDirName: String = resourcesDirNameProvider.provideResourcesDirName(testMethod)
        return when (subDir) {
            null -> rootDir.resolve(resourcesDirName)
            else -> rootDir.resolve(subDir).resolve(resourcesDirName)
        }
    }
}
