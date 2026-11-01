package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.DefaultSubject;
import org.vibeinc.schedule.core.Subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultSubjectTest {

    @Test
    @DisplayName("TC-31 Пробіли на краях назви й прізвища викладача прибираються")
    void stripsText() {
        Subject subject = new DefaultSubject("  Git  ", " Кузнецова Ю. А. ", 4);
        assertEquals("Git", subject.getName());
        assertEquals("Кузнецова Ю. А.", subject.getTeacher());
        assertEquals(4, subject.getCredits());
    }

    @Test
    @DisplayName("TC-32 Порожні поля і кількість кредитів не більше нуля відхиляються")
    void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultSubject(null, "Викладач", 3));
        assertThrows(IllegalArgumentException.class, () -> new DefaultSubject("Git", "   ", 3));
        assertThrows(IllegalArgumentException.class, () -> new DefaultSubject("Git", "Викладач", 0));
        assertThrows(IllegalArgumentException.class, () -> new DefaultSubject("Git", "Викладач", -1));
    }

    @Test
    @DisplayName("TC-33 Предмети з однаковою назвою рівні без урахування регістру")
    void equalityByName() {
        Subject git = new DefaultSubject("Git", "Кузнецова Ю. А.", 4);
        Subject sameName = new DefaultSubject("GIT ", "Інший викладач", 2);
        assertEquals(git, sameName);
        assertEquals(git.hashCode(), sameName.hashCode());
        assertNotEquals(git, new DefaultSubject("Бази даних", "Кузнецова Ю. А.", 4));
        assertEquals("Git — Кузнецова Ю. А., 4 кред.", git.toString());
    }
}
