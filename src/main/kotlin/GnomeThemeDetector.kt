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
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Objects
import java.util.regex.Pattern

/**
 * Used for detecting the dark theme on a Linux (GNOME/GTK) system.
 * Tested on Ubuntu.
 *
 * @author Daniel Gyorffy
 */
class GnomeThemeDetector : OsThemeDetector() {
    companion object {
        private val logger = LoggerFactory.getLogger(GnomeThemeDetector::class.java)
        private const val MONITORING_CMD = "gsettings monitor org.gnome.desktop.interface"
        private val GET_CMD = arrayOf(
            "gsettings get org.gnome.desktop.interface gtk-theme",
            "gsettings get org.gnome.desktop.interface color-scheme"
        )
    }

    private val listeners = ConcurrentHashSet<(Boolean) -> Unit>()
    private val darkThemeNamePattern = Pattern.compile(".*dark.*", Pattern.CASE_INSENSITIVE)
    @Volatile
    private var detectorThread: DetectorThread? = null

    override fun isDark(): Boolean {
        try {
            val runtime = Runtime.getRuntime()
            for (cmd in GET_CMD) {
                val process = runtime.exec(cmd)
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val readLine = reader.readLine()
                    if (readLine != null && isDarkTheme(readLine)) {
                        return true
                    }
                }
            }
        } catch (e: IOException) {
            logger.error("Couldn't detect Linux OS theme", e)
        }
        return false
    }

    private fun isDarkTheme(gtkTheme: String): Boolean {
        return darkThemeNamePattern.matcher(gtkTheme).matches()
    }

    @Synchronized
    override fun registerListener(darkThemeListener: (Boolean) -> Unit) {
        Objects.requireNonNull(darkThemeListener)
        val listenerAdded = listeners.add(darkThemeListener)
        val singleListener = listenerAdded && listeners.size == 1
        val threadInterrupted = detectorThread?.isInterrupted == true
        if (singleListener || threadInterrupted) {
            val newDetectorThread = DetectorThread(this)
            detectorThread = newDetectorThread
            newDetectorThread.start()
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
     * Thread implementation for detecting the actually changed theme
     */
    private class DetectorThread(val detector: GnomeThemeDetector) : Thread() {
        private val outputPattern = Pattern.compile("(gtk-theme|color-scheme).*", Pattern.CASE_INSENSITIVE)
        private var lastValue: Boolean = detector.isDark()
        init {
            name = "GTK Theme Detector Thread"
            isDaemon = true
            priority = NORM_PRIORITY - 1
        }
        override fun run() {
            try {
                val runtime = Runtime.getRuntime()
                val monitoringProcess = runtime.exec(MONITORING_CMD)
                BufferedReader(InputStreamReader(monitoringProcess.inputStream)).use { reader ->
                    while (!isInterrupted) {
                        val readLine = reader.readLine() ?: continue
                        if (!outputPattern.matcher(readLine).matches()) continue
                        val keyValue = readLine.split("\\s".toRegex(), 2)
                        if (keyValue.size < 2) continue
                        val value = keyValue[1]
                        val currentDetection = detector.isDarkTheme(value)
                        logger.debug("Theme changed detection, dark: {}", currentDetection)
                        if (currentDetection != lastValue) {
                            lastValue = currentDetection
                            for (listener in detector.listeners) {
                                try {
                                    listener(currentDetection)
                                } catch (e: RuntimeException) {
                                    logger.error("Caught exception during listener notifying ", e)
                                }
                            }
                        }
                    }
                    logger.debug("ThemeDetectorThread has been interrupted!")
                    if (monitoringProcess.isAlive) {
                        monitoringProcess.destroy()
                        logger.debug("Monitoring process has been destroyed!")
                    }
                }
            } catch (e: IOException) {
                logger.error("Couldn't start monitoring process ", e)
            } catch (e: ArrayIndexOutOfBoundsException) {
                logger.error("Couldn't parse command line output", e)
            }
        }
        companion object {
            private val logger = LoggerFactory.getLogger(DetectorThread::class.java)
        }
    }
}

