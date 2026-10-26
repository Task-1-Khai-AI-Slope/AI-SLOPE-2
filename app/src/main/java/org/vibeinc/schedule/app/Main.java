package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.ScheduleManagementSystem;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Точка входу. За замовчуванням система заповнюється демонстраційними даними,
 * з аргументом --empty запускається порожньою.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ScheduleManagementSystem system = new DefaultScheduleManagementSystem();
        if (!Arrays.asList(args).contains("--empty")) {
            fillDemoData(system);
        }
        new ConsoleApp(system, new Scanner(System.in), System.out).run();
    }

    /** Демонстраційні дані: два предмети, два студенти, кілька занять і оцінок. */
    static void fillDemoData(ScheduleManagementSystem system) {
        system.addSubject("Git-технологія", "Кузнецова Ю. А.", 4);
        system.addSubject("Бази даних", "Петренко О. І.", 5);

        system.registerStudent("KB-001", "Козир Віталій Андрійович", "658п");
        system.registerStudent("KB-002", "Єременко Максим", "658п");

        system.enroll("KB-001", "Git-технологія");
        system.enroll("KB-001", "Бази даних");
        system.enroll("KB-002", "Git-технологія");

        // Середа, 28.10.2026: лекція 2-ю парою, лабораторна 3-ю.
        system.addLesson("Git-технологія", LessonType.LECTURE, LocalDateTime.of(2026, 10, 28, 10, 25), "online");
        system.addLesson("Git-технологія", LessonType.LAB, LocalDateTime.of(2026, 10, 28, 12, 20), "online");
        system.addLesson("Бази даних", LessonType.LECTURE, LocalDateTime.of(2026, 10, 29, 8, 0), "301 ГАК");

        system.addGrade("KB-001", "Git-технологія", 95);
    }
}
