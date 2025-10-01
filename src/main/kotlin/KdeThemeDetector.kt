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

import io.github.kolod.jthemedetecor.util.ConcurrentHashSet
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Objects
import java.util.regex.Pattern

/**
 * Used for detecting the dark theme on a Linux KDE desktop environment.
 * Tested on Ubuntu KDE Plasma (kde-plasma-desktop).
 *
 * @author Thomas Sartre
 * @see GnomeThemeDetector
 */
class KdeThemeDetector : OsThemeDetector() {
    companion object {
                private val logger = LogManager.getLogger(KdeThemeDetector::class.java)
        private const val GET_THEME_CMD = "kreadconfig5 --file kdeglobals --group General --key ColorScheme"
    }

    private val listeners = ConcurrentHashSet<(Boolean) -> Unit>()
    private val darkThemeNamePattern = Pattern.compile(".*dark.*", Pattern.CASE_INSENSITIVE)
    @Volatile
    private var detectorThread: DetectorThread? = null

    override fun isDark(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(GET_THEME_CMD)
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                val theme = reader.readLine()
                theme != null && isDarkTheme(theme)
            }
        } catch (e: IOException) {
            logger.error("Couldn't detect KDE OS theme", e)
            false
        }
    }

    private fun isDarkTheme(theme: String): Boolean {
        return darkThemeNamePattern.matcher(theme).matches()
    }

    @Synchronized
    override fun registerListener(darkThemeListener: (Boolean) -> Unit) {
        Objects.requireNonNull(darkThemeListener)
        val listenerAdded = listeners.add(darkThemeListener)
        val singleListener = listenerAdded && listeners.size == 1
        val threadInterrupted = detectorThread?.isInterrupted == true
        if (singleListener || threadInterrupted) {
            val newThread = DetectorThread(this)
            detectorThread = newThread
            newThread.start()
        }
    }

    @Synchronized
    override fun removeListener(darkThemeListener: ((Boolean) -> Unit)?) {
        listeners.remove(darkThemeListener)
        if (listeners.isEmpty()) {
            detectorThread?.interrupt()
            detectorThread = null
        }
    }

    /**
     * Thread implementation for detecting the actually changed theme.
     */
    private class DetectorThread(val detector: KdeThemeDetector) : Thread() {
        private var lastValue: Boolean = detector.isDark()
        init {
            name = "KDE Theme Detector Thread"
            isDaemon = true
            priority = NORM_PRIORITY - 1
        }
        override fun run() {
            while (!isInterrupted) {
                val currentDetection = detector.isDark()
                if (currentDetection != lastValue) {
                    lastValue = currentDetection
                    for (listener in detector.listeners) {
                        try {
                            listener(currentDetection)
                        } catch (e: RuntimeException) {
                            logger.error("Caught exception during listener notification", e)
                        }
                    }
                }
            }
        }
        companion object {
                        private val logger = LogManager.getLogger(DetectorThread::class.java)
        }
    }
}
