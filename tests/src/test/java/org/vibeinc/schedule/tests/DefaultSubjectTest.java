package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource(delimiter = '|', nullValues = "null", value = {
            "null  | Викладач | 3           | Поле \"Назва предмета\" не може бути порожнім",
            "'   ' | Викладач | 3           | Поле \"Назва предмета\" не може бути порожнім",
            "Git   | null     | 3           | Поле \"Викладач\" не може бути порожнім",
            "Git   | '   '    | 3           | Поле \"Викладач\" не може бути порожнім",
            "Git   | Викладач | 0           | Кількість кредитів має бути більшою за нуль",
            "Git   | Викладач | -1          | Кількість кредитів має бути більшою за нуль",
            "Git   | Викладач | -2147483648 | Кількість кредитів має бути більшою за нуль"
    })
    @DisplayName("TC-32 Порожні поля і кількість кредитів не більше нуля відхиляються")
    void rejectsInvalidData(String name, String teacher, int credits, String message) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DefaultSubject(name, teacher, credits));
        assertEquals(message, e.getMessage());
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
