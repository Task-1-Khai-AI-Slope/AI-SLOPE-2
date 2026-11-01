package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.DefaultSubject;
import org.vibeinc.schedule.app.Formats;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatsTest {
    private static final LocalDateTime WEDNESDAY_10_25 = LocalDateTime.of(2026, 10, 28, 10, 25);

    @Test
    @DisplayName("TC-37 Дата й час виводяться і читаються у форматі РРРР-ММ-ДД ГГ:ХХ")
    void dateTimeFormat() {
        assertEquals("2026-10-28 10:25", Formats.dateTime(WEDNESDAY_10_25));
        assertEquals(WEDNESDAY_10_25, LocalDateTime.parse("2026-10-28 10:25", Formats.DATE_TIME));
        assertThrows(DateTimeParseException.class,
                () -> LocalDateTime.parse("28.10.2026 10:25", Formats.DATE_TIME));
    }

    @Test
    @DisplayName("TC-38 Заняття виводиться одним рядком з днем тижня і часом завершення")
    void lessonLine() {
        Lesson lesson = new Lesson(new DefaultSubject("Git", "Кузнецова Ю. А.", 4),
                LessonType.LECTURE, WEDNESDAY_10_25, Lesson.DEFAULT_DURATION, "301");
        assertEquals("2026-10-28 ср 10:25–12:00 | Git | Лекція | ауд. 301 | Кузнецова Ю. А.",
                Formats.lesson(lesson));
    }
}
