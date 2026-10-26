// Спільні налаштування для всіх модулів Solution.
subprojects {
    group = "org.vibeinc.schedule"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }

    // Застосовується до кожного модуля, у якому підключено плагін java.
    plugins.withType<JavaPlugin> {
        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(21)
        }
    }
}
