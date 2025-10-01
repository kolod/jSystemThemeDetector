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

import io.github.kolod.jthemedetecor.OsThemeDetector

fun main() {
    val detector = OsThemeDetector.getDetector()
    println(detector.isDark())
    detector.registerListener { isDark -> println("OS is dark: $isDark") }

    println("Listening to system ui theme change... (Press E for exit)")
    while (readlnOrNull()?.lowercase()?.startsWith("e") == false) {
        // Loop until user enters a line starting with 'e' or 'E'
    }
}
