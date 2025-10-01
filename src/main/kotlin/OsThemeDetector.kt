/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.github.kolod.jthemedetecor

import io.github.kolod.jthemedetecor.util.OsInfo
import org.slf4j.LoggerFactory
import oshi.annotation.concurrent.ThreadSafe

/**
 * For detecting the theme (dark/light) used by the Operating System.
 *
 * @author Daniel Gyorffy
 */
abstract class OsThemeDetector {
    companion object {
        private val logger = LoggerFactory.getLogger(OsThemeDetector::class.java)
        @Volatile
        private var osThemeDetector: OsThemeDetector? = null

        @JvmStatic
        @ThreadSafe
        fun getDetector(): OsThemeDetector {
            var instance = osThemeDetector
            if (instance == null) {
                synchronized(this) {
                    instance = osThemeDetector
                    if (instance == null) {
                        osThemeDetector = createDetector()
                        instance = osThemeDetector
                    }
                }
            }
            return instance!!
        }

        private fun createDetector(): OsThemeDetector {
            return when {
                OsInfo.isWindows10OrLater() -> {
                    logDetection("Windows 10", WindowsThemeDetector::class.java)
                    WindowsThemeDetector()
                }
                OsInfo.isGnome() -> {
                    logDetection("Gnome", GnomeThemeDetector::class.java)
                    GnomeThemeDetector()
                }
                OsInfo.isKde() -> {
                    logDetection("KDE", KdeThemeDetector::class.java)
                    KdeThemeDetector()
                }
                OsInfo.isMacOsMojaveOrLater() -> {
                    logDetection("MacOS", MacOSThemeDetector::class.java)
                    MacOSThemeDetector()
                }
                else -> {
                    logger.debug("Theme detection is not supported on the system: {} {}", OsInfo.getFamily(), OsInfo.getVersion())
                    logger.debug("Creating empty detector...")
                    EmptyDetector()
                }
            }
        }

        private fun logDetection(desktop: String, detectorClass: Class<out OsThemeDetector>) {
            logger.debug("Supported Desktop detected: {}", desktop)
            logger.debug("Creating {}...", detectorClass.name)
        }

        @Suppress("unused")
        @JvmStatic
        @ThreadSafe
        fun isSupported(): Boolean {
            return OsInfo.isWindows10OrLater() || OsInfo.isMacOsMojaveOrLater() || OsInfo.isGnome() || OsInfo.isKde()
        }
    }

    @ThreadSafe
    abstract fun isDark(): Boolean

    @ThreadSafe
    abstract fun registerListener(darkThemeListener: (Boolean) -> Unit)

    @ThreadSafe
    abstract fun removeListener(darkThemeListener: ((Boolean) -> Unit)?)

    private class EmptyDetector : OsThemeDetector() {
        override fun isDark(): Boolean = false
        override fun registerListener(darkThemeListener: (Boolean) -> Unit) {}
        override fun removeListener(darkThemeListener: ((Boolean) -> Unit)?) {}
    }
}

