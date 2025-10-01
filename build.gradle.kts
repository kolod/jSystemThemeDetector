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
version = "3.8"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.slf4j:slf4j-api:1.7.32")

    implementation("org.apache.logging.log4j:log4j-core:2.25.2")
    implementation("org.apache.logging.log4j:log4j-api:2.25.2")

    testImplementation("ch.qos.logback:logback-classic:1.5.13")
    testImplementation("ch.qos.logback:logback-core:1.5.13")

    // JNA
    implementation("net.java.dev.jna:jna:5.15.0")
    implementation("net.java.dev.jna:jna-platform:5.15.0")

    // JFA
    implementation("de.jangassen:jfa:1.2.0") {
        exclude(group = "net.java.dev.jna", module = "jna") // different jna version
    }

    // OSHI
    implementation("com.github.oshi:oshi-core:5.8.6")

    implementation("io.github.g00fy2:versioncompare:1.4.1")

    implementation("org.jetbrains:annotations:22.0.0")
}

tasks.named<JavaCompile>("compileJava") {
    // placing dependencies to the module path
    // https://discuss.gradle.org/t/gradle-doesnt-add-modules-to-module-path-during-compile/27382
    inputs.property("moduleName", "com.jthemedetector")
    doFirst {
        options.compilerArgs = listOf(
            "--module-path", classpath.asPath
        )
        classpath = files()
    }
}
