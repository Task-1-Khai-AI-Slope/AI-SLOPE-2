// Модуль app: реалізація інтерфейсів core і консольний інтерфейс.
plugins {
    application
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("org.vibeinc.schedule.app.Main")
}

// Щоб "gradlew :app:run" міг читати введення з клавіатури.
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
