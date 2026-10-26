package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.DefaultSubject;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.Subject;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Модель заняття з модуля core. */
class LessonTest {
    private static final Subject GIT = new DefaultSubject("Git", "Кузнецова Ю. А.", 4);
    private static final LocalDateTime AT_10 = LocalDateTime.of(2026, 10, 28, 10, 0);

    private static Lesson lessonAt(LocalDateTime start) {
        return new Lesson(GIT, LessonType.LECTURE, start, Lesson.DEFAULT_DURATION, "301");
    }

    @Test
    @DisplayName("TC-15 Час завершення = початок + тривалість")
    void endIsStartPlusDuration() {
        assertEquals(LocalDateTime.of(2026, 10, 28, 11, 35), lessonAt(AT_10).end());
    }

    @Test
    @DisplayName("TC-16 Заняття, що частково збігаються в часі, перетинаються")
    void overlappingLessons() {
        assertTrue(lessonAt(AT_10).overlaps(lessonAt(AT_10.plusMinutes(30))));
    }

    @Test
    @DisplayName("TC-17 Заняття, що йде одразу після іншого, не перетинається з ним")
    void adjacentLessonsDoNotOverlap() {
        Lesson first = lessonAt(AT_10);
        assertFalse(first.overlaps(lessonAt(first.end())));
    }

    @Test
    @DisplayName("TC-18 Нульова тривалість і порожня аудиторія відхиляються")
    void invalidLessonRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson(GIT, LessonType.LAB, AT_10, Duration.ZERO, "301"));
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson(GIT, LessonType.LAB, AT_10, Lesson.DEFAULT_DURATION, "  "));
    }
}
