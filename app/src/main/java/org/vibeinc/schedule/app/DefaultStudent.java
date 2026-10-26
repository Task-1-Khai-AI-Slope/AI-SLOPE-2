package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.Student;

/** Реалізація студента. Два студенти рівні, якщо збігаються номери залікових книжок. */
public final class DefaultStudent implements Student {
    private final String id;
    private final String fullName;
    private final String group;

    public DefaultStudent(String id, String fullName, String group) {
        this.id = Texts.requireText(id, "Номер залікової книжки");
        this.fullName = Texts.requireText(fullName, "ПІБ");
        this.group = Texts.requireText(group, "Група");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Student other && Texts.key(id).equals(Texts.key(other.getId()));
    }

    @Override
    public int hashCode() {
        return Texts.key(id).hashCode();
    }

    @Override
    public String toString() {
        return id + " — " + fullName + ", гр. " + group;
    }
}
