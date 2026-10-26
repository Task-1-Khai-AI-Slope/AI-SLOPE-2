package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.DefaultSchedule;
import org.vibeinc.schedule.app.DefaultSubject;
import org.vibeinc.schedule.core.EntityNotFoundException;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.ScheduleConflictException;
import org.vibeinc.schedule.core.Subject;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Розклад: додавання, конфлікти, перенесення, скасування. */
class DefaultScheduleTest {
    private static final LocalDateTime AT_10 = LocalDateTime.of(2026, 10, 28, 10, 0);

    private final Subject git = new DefaultSubject("Git", "Кузнецова Ю. А.", 4);
    private final Subject db = new DefaultSubject("Бази даних", "Петренко О. І.", 5);
    private final Subject web = new DefaultSubject("Вебтехнології", "Кузнецова Ю. А.", 3);

    private DefaultSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new DefaultSchedule();
    }

    private static Lesson lesson(Subject subject, LocalDateTime start, String room) {
        return new Lesson(subject, LessonType.LECTURE, start, Lesson.DEFAULT_DURATION, room);
    }

    @Test
    @DisplayName("TC-08 Заняття повертаються відсортованими за часом")
    void lessonsAreSorted() {
        Lesson late = lesson(git, AT_10.plusDays(1), "301");
        Lesson early = lesson(db, AT_10, "302");
        schedule.addLesson(late);
        schedule.addLesson(early);
        assertEquals(List.of(early, late), schedule.getLessons());
    }

    @Test
    @DisplayName("TC-09 Одна аудиторія в той самий час — конфлікт")
    void roomConflict() {
        schedule.addLesson(lesson(git, AT_10, "301"));
        assertThrows(ScheduleConflictException.class,
                () -> schedule.addLesson(lesson(db, AT_10.plusMinutes(30), "301")));
    }

    @Test
    @DisplayName("TC-10 Той самий викладач у той самий час — конфлікт")
    void teacherConflict() {
        schedule.addLesson(lesson(git, AT_10, "301"));
        assertThrows(ScheduleConflictException.class,
                () -> schedule.addLesson(lesson(web, AT_10, "302")));
    }

    @Test
    @DisplayName("TC-11 Перенесення на вільний час змінює лише час початку")
    void rescheduleToFreeTime() {
        Lesson original = lesson(git, AT_10, "301");
        schedule.addLesson(original);
        Lesson moved = schedule.reschedule(original, AT_10.plusHours(4));
        assertEquals(AT_10.plusHours(4), moved.start());
        assertEquals(original.room(), moved.room());
        assertEquals(List.of(moved), schedule.getLessons());
    }

    @Test
    @DisplayName("TC-12 Невдале перенесення не змінює розклад")
    void failedRescheduleKeepsOriginal() {
        Lesson a = lesson(git, AT_10, "301");
        Lesson b = lesson(db, AT_10.plusHours(2), "301");
        schedule.addLesson(a);
        schedule.addLesson(b);
        assertThrows(ScheduleConflictException.class, () -> schedule.reschedule(b, AT_10));
        assertEquals(List.of(a, b), schedule.getLessons());
    }

    @Test
    @DisplayName("TC-13 Скасування видаляє заняття, повторне — помилка")
    void cancelLesson() {
        Lesson a = lesson(git, AT_10, "301");
        schedule.addLesson(a);
        schedule.cancel(a);
        assertTrue(schedule.getLessons().isEmpty());
        assertThrows(EntityNotFoundException.class, () -> schedule.cancel(a));
    }
}
