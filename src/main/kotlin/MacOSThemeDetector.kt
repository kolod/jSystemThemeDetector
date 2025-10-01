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
import com.sun.jna.Callback
import de.jangassen.jfa.foundation.Foundation
import de.jangassen.jfa.foundation.ID
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

/**
 * Determines the dark/light theme on a MacOS System through the Apple Foundation framework.
 *
 * @author Daniel Gyorffy
 */
class MacOSThemeDetector : OsThemeDetector() {
    companion object {
        private val logger = LoggerFactory.getLogger(MacOSThemeDetector::class.java)
    }

    private val listeners = ConcurrentHashSet<(Boolean) -> Unit>()
    private val themeNamePattern = Pattern.compile(".*dark.*", Pattern.CASE_INSENSITIVE)
    private val callbackExecutor: ExecutorService = Executors.newSingleThreadExecutor { DetectorThread(it) }

    private val themeChangedCallback = object : Callback {
        @Suppress("unused")
        fun callback() {
            callbackExecutor.execute { notifyListeners(isDark()) }
        }
    }

    init {
        initObserver()
    }

    private fun initObserver() {
        val pool = Foundation.NSAutoreleasePool()
        try {
            val delegateClass = Foundation.allocateObjcClassPair(Foundation.getObjcClass("NSObject"), "NSColorChangesObserver")
            if (delegateClass != ID.NIL) {
                if (!Foundation.addMethod(delegateClass, Foundation.createSelector("handleAppleThemeChanged:"), themeChangedCallback, "v@")) {
                    logger.error("Observer method cannot be added")
                }
                Foundation.registerObjcClassPair(delegateClass)
            }
            val delegate = Foundation.invoke("NSColorChangesObserver", "new")
            Foundation.invoke(
                Foundation.invoke("NSDistributedNotificationCenter", "defaultCenter"),
                "addObserver:selector:name:object:",
                delegate,
                Foundation.createSelector("handleAppleThemeChanged:"),
                Foundation.nsString("AppleInterfaceThemeChangedNotification"),
                ID.NIL
            )
        } finally {
            pool.drain()
        }
    }

    override fun isDark(): Boolean {
        val pool = Foundation.NSAutoreleasePool()
        try {
            val userDefaults = Foundation.invoke("NSUserDefaults", "standardUserDefaults")
            val appleInterfaceStyle = Foundation.toStringViaUTF8(
                Foundation.invoke(userDefaults, "objectForKey:", Foundation.nsString("AppleInterfaceStyle"))
            )
            return isDarkTheme(appleInterfaceStyle)
        } catch (e: RuntimeException) {
            logger.error("Couldn't execute theme name query with the Os", e)
        } finally {
            pool.drain()
        }
        return false
    }

    private fun isDarkTheme(themeName: String?): Boolean {
        return themeName != null && themeNamePattern.matcher(themeName).matches()
    }

    override fun registerListener(darkThemeListener: (Boolean) -> Unit) {
        listeners.add(darkThemeListener)
    }

    override fun removeListener(darkThemeListener: ((Boolean) -> Unit)?) {
        listeners.remove(darkThemeListener)
    }

    private fun notifyListeners(isDark: Boolean) {
        listeners.forEach { listener ->
            try {
                listener(isDark)
            } catch (e: RuntimeException) {
                logger.error("Caught exception during listener notifying ", e)
            }
        }
    }

    private class DetectorThread(runnable: Runnable) : Thread(runnable) {
        init {
            name = "MacOS Theme Detector Thread"
            isDaemon = true
        }
    }
}

