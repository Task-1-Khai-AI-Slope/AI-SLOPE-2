// Модуль tests: модульні тести для core і app на JUnit Jupiter.
plugins {
    java
}

dependencies {
    testImplementation(project(":core"))
    testImplementation(project(":app"))

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
