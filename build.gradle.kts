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

plugins {
    kotlin("jvm") version "2.2.20"
}

group = "io.github.kolod"
version = "4.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Logging
    implementation("org.apache.logging.log4j:log4j-core:2.25.2")
    implementation("org.apache.logging.log4j:log4j-api:2.25.2")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.2")

    // JNA
    implementation("net.java.dev.jna:jna:5.18.0")
    implementation("net.java.dev.jna:jna-platform:5.18.0")

    // JFA
    implementation("de.jangassen:jfa:1.2.0") {
        exclude(group = "net.java.dev.jna", module = "jna") // different jna version
    }

    // OSHI
    implementation("com.github.oshi:oshi-core:6.9.0")

    // Version compare
    implementation("io.github.g00fy2:versioncompare:1.5.0")

    // Annotations
    implementation("org.jetbrains:annotations:26.0.2-1")

    // Test
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runGuiDemo") {
    group = "application"
    description = "Runs the GuiDemo class"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("GuiDemo") // Use the fully qualified class name if in a package
}

tasks.register<JavaExec>("runConsoleDemo") {
    group = "application"
    description = "Runs the ConsoleDemo class"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("OsThemeDetectorDemoKt") // Use the fully qualified class name if in a package
}