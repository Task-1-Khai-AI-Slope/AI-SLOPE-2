package org.vibeinc.schedule.core;

public enum LessonType {
    LECTURE("Лекція"),
    PRACTICE("Практичне"),
    LAB("Лабораторна");

    private final String title;

    LessonType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
