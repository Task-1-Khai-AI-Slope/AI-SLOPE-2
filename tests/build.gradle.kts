plugins {
    java
    jacoco
}

val mockitoVersion = "5.24.0"
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    testImplementation(project(":core"))
    testImplementation(project(":app"))

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    mockitoAgent("org.mockito:mockito-core:$mockitoVersion") {
        isTransitive = false
    }
}

jacoco {
    toolVersion = "0.8.16"
}

val coveredProjects = listOf(project(":core"), project(":app"))

tasks.test {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, coveredProjects.map { "${it.path}:classes" })
    sourceDirectories.setFrom(coveredProjects.map { it.file("src/main/java") })
    classDirectories.setFrom(coveredProjects.map { it.layout.buildDirectory.dir("classes/java/main") })
    reports {
        html.required = true
        csv.required = true
        xml.required = false
    }
}
