package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.ConsoleApp;
import org.vibeinc.schedule.app.DefaultScheduleManagementSystem;
import org.vibeinc.schedule.core.ScheduleManagementSystem;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Консольний інтерфейс: введення подається рядком замість клавіатури. */
class ConsoleAppTest {

    private static String run(ScheduleManagementSystem system, String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleApp(system, new Scanner(input), out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("TC-22 Додавання предмета через меню")
    void addSubjectViaMenu() {
        ScheduleManagementSystem system = new DefaultScheduleManagementSystem();
        String output = run(system, "1\nGit\nКузнецова Ю. А.\n4\n0\n");
        assertTrue(output.contains("Додано предмет"));
        assertEquals(1, system.getSubjects().size());
    }

    @Test
    @DisplayName("TC-23 Помилкове введення не завершує програму")
    void wrongInputDoesNotCrash() {
        String output = run(new DefaultScheduleManagementSystem(),
                "1\nGit\nКузнецова Ю. А.\nчотири\n99\n0\n");
        assertTrue(output.contains("Помилка: Очікувалося ціле число"));
        assertTrue(output.contains("Невідомий пункт меню: 99"));
        assertTrue(output.contains("До побачення!"));
    }
}
