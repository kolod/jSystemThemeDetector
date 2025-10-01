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

package io.github.kolod.jthemedetecor.util

import io.github.kolod.jthemedetecor.OsThemeDetector
import org.slf4j.LoggerFactory
import oshi.PlatformEnum
import oshi.SystemInfo
import io.github.g00fy2.versioncompare.Version
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object OsInfo {
    private val logger = LoggerFactory.getLogger(OsThemeDetector::class.java)

    private val platformType: PlatformEnum
    private val family: String
    private val version: String

    init {
        val systemInfo = SystemInfo()
        val osInfo = systemInfo.operatingSystem
        val osVersionInfo = osInfo.versionInfo
        platformType = SystemInfo.getCurrentPlatform()
        family = osInfo.family
        version = osVersionInfo.version
    }

    fun isWindows10OrLater(): Boolean = hasTypeAndVersionOrHigher(PlatformEnum.WINDOWS, "10")
    fun isLinux(): Boolean = hasType(PlatformEnum.LINUX)
    fun isMacOsMojaveOrLater(): Boolean = hasTypeAndVersionOrHigher(PlatformEnum.MACOS, "10.14")

    fun getCurrentLinuxDesktopEnvironmentName(): String = System.getenv("XDG_CURRENT_DESKTOP") ?: ""

    fun isGnome(): Boolean = isLinux() && getCurrentLinuxDesktopEnvironmentName().lowercase().contains("gnome")
    fun isKde(): Boolean = isLinux() && getCurrentLinuxDesktopEnvironmentName().lowercase().contains("kde")

    fun hasType(platformType: PlatformEnum): Boolean = OsInfo.platformType == platformType
    fun isVersionAtLeast(version: String): Boolean = Version(OsInfo.version).isAtLeast(version)
    fun hasTypeAndVersionOrHigher(platformType: PlatformEnum, version: String): Boolean = hasType(platformType) && isVersionAtLeast(version)
    fun getVersion(): String = version
    fun getFamily(): String = family

    @Suppress("unused")
    private fun queryResultContains(cmd: String, subResult: String): Boolean = query(cmd).lowercase().contains(subResult)

    private fun query(cmd: String): String {
        return try {
            val process = Runtime.getRuntime().exec(cmd)
            val stringBuilder = StringBuilder()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var actualReadLine: String?
                while (reader.readLine().also { actualReadLine = it } != null) {
                    if (stringBuilder.isNotEmpty()) stringBuilder.append('\n')
                    stringBuilder.append(actualReadLine)
                }
            }
            stringBuilder.toString()
        } catch (e: IOException) {
            logger.error("Exception caught while querying the OS", e)
            ""
        }
    }
}

