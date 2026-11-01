plugins {
    application
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("org.vibeinc.schedule.app.Main")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
