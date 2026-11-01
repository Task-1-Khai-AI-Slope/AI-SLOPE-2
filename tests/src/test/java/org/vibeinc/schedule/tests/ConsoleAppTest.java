package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.ConsoleApp;
import org.vibeinc.schedule.app.DefaultSchedule;
import org.vibeinc.schedule.app.DefaultScheduleManagementSystem;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.ScheduleManagementSystem;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleAppTest {

    private static String run(ScheduleManagementSystem system, String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleApp(system, new Scanner(input), out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static ScheduleManagementSystem groupsAndLessons() {
        ScheduleManagementSystem system = new DefaultScheduleManagementSystem(new DefaultSchedule());
        system.addSubject("Git", "Кузнецова Ю. А.", 4);
        system.addSubject("Бази даних", "Петренко О. І.", 5);
        system.registerStudent("KB-001", "Козир Віталій Андрійович", "658п");
        system.registerStudent("KB-002", "Єременко Максим", "658п");
        system.registerStudent("KB-101", "Студент Іншої Групи", "659п");
        system.addLesson("Git", LessonType.LAB, LocalDateTime.of(2026, 10, 28, 12, 20), "online");
        system.addLesson("Git", LessonType.LECTURE, LocalDateTime.of(2026, 10, 28, 10, 25), "online");
        system.addLesson("Бази даних", LessonType.LECTURE, LocalDateTime.of(2026, 10, 29, 8, 0), "301");
        return system;
    }

    @Test
    @DisplayName("TC-22 Додавання предмета через меню")
    void addSubjectViaMenu() {
        ScheduleManagementSystem system = new DefaultScheduleManagementSystem(new DefaultSchedule());
        String output = run(system, "1\nGit\nКузнецова Ю. А.\n4\n0\n");
        assertTrue(output.contains("Додано предмет"));
        assertEquals(1, system.getSubjects().size());
    }

    @Test
    @DisplayName("TC-23 Помилкове введення не завершує програму")
    void wrongInputDoesNotCrash() {
        String output = run(new DefaultScheduleManagementSystem(new DefaultSchedule()),
                "1\nGit\nКузнецова Ю. А.\nчотири\n99\n0\n");
        assertTrue(output.contains("Помилка: Очікувалося ціле число"));
        assertTrue(output.contains("Невідомий пункт меню: 99"));
        assertTrue(output.contains("До побачення!"));
    }

    @Test
    @DisplayName("TC-24 Некоректна дата заняття – повідомлення українською, дефект #27")
    void wrongDateTimeMessageInUkrainian() {
        String output = run(new DefaultScheduleManagementSystem(new DefaultSchedule()),
                "4\nGit\n1\n28.10.2026\n0\n");
        assertTrue(output.contains("Помилка: Некоректні дата й час \"28.10.2026\""));
        assertTrue(output.contains("До побачення!"));
    }

    @Test
    @DisplayName("TC-25 Студенти групи – назва групи без урахування регістру")
    void groupStudentsIgnoreCase() {
        String output = run(groupsAndLessons(), "13\n658П\n0\n");
        assertTrue(output.contains("Група 658П, студентів: 2"));
        assertTrue(output.contains("KB-001"));
        assertTrue(output.contains("KB-002"));
        assertFalse(output.contains("KB-101"));
    }

    @Test
    @DisplayName("TC-26 Студенти групи – група без студентів")
    void emptyGroup() {
        String output = run(groupsAndLessons(), "13\n000\n0\n");
        assertTrue(output.contains("У групі 000 студентів немає"));
    }

    @Test
    @DisplayName("TC-27 Розклад на дату – лише заняття цього дня за часом")
    void scheduleForDate() {
        String output = run(groupsAndLessons(), "14\n2026-10-28\n0\n");
        assertTrue(output.contains("Розклад на 2026-10-28, ср:"));
        assertTrue(output.indexOf(" 1. 2026-10-28 ср 10:25") < output.indexOf(" 2. 2026-10-28 ср 12:20"));
        assertTrue(output.contains(" 1. 2026-10-28 ср 10:25"));
        assertFalse(output.contains("2026-10-29 чт"));
    }

    @Test
    @DisplayName("TC-28 Розклад на дату – некоректний формат дати")
    void scheduleForWrongDate() {
        String output = run(groupsAndLessons(), "14\n28.10.2026\n0\n");
        assertTrue(output.contains("Помилка: Некоректна дата \"28.10.2026\", очікується формат РРРР-ММ-ДД"));
        assertTrue(output.contains("До побачення!"));
    }

    @Test
    @DisplayName("TC-29 Розклад на дату – день без занять")
    void scheduleForFreeDate() {
        String output = run(groupsAndLessons(), "14\n2026-11-01\n0\n");
        assertTrue(output.contains("Розклад на 2026-11-01, нд:"));
        assertTrue(output.contains("Занять немає"));
    }
}
