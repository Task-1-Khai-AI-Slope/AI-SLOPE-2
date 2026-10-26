package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.Subject;

/** Реалізація предмета. Два предмети рівні, якщо збігаються назви без урахування регістру. */
public final class DefaultSubject implements Subject {
    private final String name;
    private final String teacher;
    private final int credits;

    public DefaultSubject(String name, String teacher, int credits) {
        this.name = Texts.requireText(name, "Назва предмета");
        this.teacher = Texts.requireText(teacher, "Викладач");
        if (credits <= 0) {
            throw new IllegalArgumentException("Кількість кредитів має бути більшою за нуль");
        }
        this.credits = credits;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTeacher() {
        return teacher;
    }

    @Override
    public int getCredits() {
        return credits;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Subject other && Texts.key(name).equals(Texts.key(other.getName()));
    }

    @Override
    public int hashCode() {
        return Texts.key(name).hashCode();
    }

    @Override
    public String toString() {
        return name + " — " + teacher + ", " + credits + " кред.";
    }
}
