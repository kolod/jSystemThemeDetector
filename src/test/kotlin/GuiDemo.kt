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
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.text.Font
import javafx.stage.Stage

class GuiDemo : Application() {
    companion object {
        private const val WINDOW_TITLE = "Dark Theme Detection"
        private const val WINDOW_WIDTH = 800.0
        private const val WINDOW_HEIGHT = 600.0
        private const val LIGHT_THEME_LABEL = "The OS uses LIGHT THEME"
        private const val DARK_THEME_LABEL = "The OS uses DARK THEME"
        private const val FONT_SIZE = 50.0
        private const val DARK_STYLE = "-fx-base: #000000"
        private const val LIGHT_STYLE = "-fx-base: #d7d7d7"
        @JvmStatic
        fun main(args: Array<String>) {
            launch(GuiDemo::class.java, *args)
        }
    }

    override fun start(stage: Stage) {
        val labelValue = SimpleStringProperty()
        stage.scene = Scene(buildRoot(labelValue))
        stage.width = WINDOW_WIDTH
        stage.height = WINDOW_HEIGHT
        stage.title = WINDOW_TITLE
        stage.show()

        val detector = OsThemeDetector.getDetector()
        val darkThemeListener: (Boolean) -> Unit = { isDark ->
            Platform.runLater {
                if (isDark) {
                    stage.scene.root.style = DARK_STYLE
                    labelValue.set(DARK_THEME_LABEL)
                } else {
                    stage.scene.root.style = LIGHT_STYLE
                    labelValue.set(LIGHT_THEME_LABEL)
                }
            }
        }
        darkThemeListener(detector.isDark())
        detector.registerListener(darkThemeListener)
    }

    private fun buildRoot(labelValue: StringProperty): Parent {
        val label = Label()
        label.textProperty().bind(labelValue)
        label.font = Font.font(FONT_SIZE)
        return StackPane(label)
    }
}

