package org.vibeinc.schedule.core;

/**
 * Навчальний предмет. Предмет однозначно визначається назвою
 * без урахування регістру — ФВ-1.1.
 */
public interface Subject {

    /** Назва предмета, наприклад "Git-технологія". */
    String getName();

    /** ПІБ викладача, який веде предмет. */
    String getTeacher();

    /** Кількість кредитів ЄКТС, більше нуля. */
    int getCredits();
}
