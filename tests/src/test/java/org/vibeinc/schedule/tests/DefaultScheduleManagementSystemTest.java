package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.DefaultScheduleManagementSystem;
import org.vibeinc.schedule.core.DuplicateEntityException;
import org.vibeinc.schedule.core.EntityNotFoundException;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.ScheduleManagementSystem;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Фасад системи: предмети, студенти, запис, особистий розклад, оцінки. */
class DefaultScheduleManagementSystemTest {
    private static final LocalDateTime AT_10 = LocalDateTime.of(2026, 10, 28, 10, 0);

    private ScheduleManagementSystem system;

    @BeforeEach
    void setUp() {
        system = new DefaultScheduleManagementSystem();
        system.addSubject("Git", "Кузнецова Ю. А.", 4);
        system.addSubject("Бази даних", "Петренко О. І.", 5);
        system.registerStudent("KB-001", "Козир В. А.", "658п");
    }

    @Test
    @DisplayName("TC-01 Новий предмет додається і знаходиться без урахування регістру")
    void addAndFindSubject() {
        system.addSubject("Вебтехнології", "Іваненко І. І.", 3);
        assertEquals(3, system.getSubjects().size());
        assertTrue(system.findSubject("  вебТЕХНОЛОГІЇ ").isPresent());
    }

    @Test
    @DisplayName("TC-02 Повторний предмет з тією ж назвою відхиляється")
    void duplicateSubject() {
        assertThrows(DuplicateEntityException.class, () -> system.addSubject("GIT", "Інший", 2));
    }

    @Test
    @DisplayName("TC-03 Порожня назва або нуль кредитів відхиляються")
    void invalidSubject() {
        assertThrows(IllegalArgumentException.class, () -> system.addSubject(" ", "Хтось", 3));
        assertThrows(IllegalArgumentException.class, () -> system.addSubject("ОС", "Хтось", 0));
    }

    @Test
    @DisplayName("TC-04 Повторна реєстрація студента з тим самим номером відхиляється")
    void duplicateStudent() {
        assertThrows(DuplicateEntityException.class,
                () -> system.registerStudent("kb-001", "Інший", "658п"));
    }

    @Test
    @DisplayName("TC-05 Студента записано на предмет")
    void enroll() {
        system.enroll("KB-001", "Git");
        assertEquals(List.of("Git"),
                system.getSubjectsOf("KB-001").stream().map(s -> s.getName()).toList());
    }

    @Test
    @DisplayName("TC-06 Повторний запис на той самий предмет відхиляється")
    void enrollTwice() {
        system.enroll("KB-001", "Git");
        assertThrows(DuplicateEntityException.class, () -> system.enroll("KB-001", "git"));
    }

    @Test
    @DisplayName("TC-07 Запис неіснуючого студента або на неіснуючий предмет відхиляється")
    void enrollUnknown() {
        assertThrows(EntityNotFoundException.class, () -> system.enroll("KB-999", "Git"));
        assertThrows(EntityNotFoundException.class, () -> system.enroll("KB-001", "Хімія"));
    }

    @Test
    @DisplayName("TC-14 Розклад студента містить лише предмети, на які він записаний")
    void studentSchedule() {
        system.enroll("KB-001", "Git");
        Lesson git = system.addLesson("Git", LessonType.LECTURE, AT_10, "301");
        system.addLesson("Бази даних", LessonType.LECTURE, AT_10, "302");
        assertEquals(List.of(git), system.getStudentSchedule("KB-001"));
    }

    @Test
    @DisplayName("TC-19 Оцінка поза межами 0–100 відхиляється")
    void gradeOutOfRange() {
        system.enroll("KB-001", "Git");
        assertThrows(IllegalArgumentException.class, () -> system.addGrade("KB-001", "Git", 101));
        assertThrows(IllegalArgumentException.class, () -> system.addGrade("KB-001", "Git", -1));
    }

    @Test
    @DisplayName("TC-20 Оцінку з предмета, на який студент не записаний, виставити не можна")
    void gradeWithoutEnrollment() {
        assertThrows(EntityNotFoundException.class, () -> system.addGrade("KB-001", "Git", 90));
    }

    @Test
    @DisplayName("TC-21 Середній бал рахується за всіма оцінками з предмета")
    void averageGrade() {
        system.enroll("KB-001", "Git");
        system.addGrade("KB-001", "Git", 90);
        system.addGrade("KB-001", "Git", 75);
        assertEquals(82.5, system.getAverageGrade("KB-001", "Git").getAsDouble(), 0.001);
        assertTrue(system.getAverageGrade("KB-001", "Бази даних").isEmpty());
    }
}
