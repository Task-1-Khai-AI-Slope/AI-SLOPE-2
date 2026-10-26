package org.vibeinc.schedule.core;

/** Вид заняття. */
public enum LessonType {
    LECTURE("Лекція"),
    PRACTICE("Практичне"),
    LAB("Лабораторна");

    private final String title;

    LessonType(String title) {
        this.title = title;
    }

    /** Назва виду заняття українською для виводу користувачу. */
    public String getTitle() {
        return title;
    }
}
