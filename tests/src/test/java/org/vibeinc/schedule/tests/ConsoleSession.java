package org.vibeinc.schedule.tests;

import org.vibeinc.schedule.app.ConsoleApp;
import org.vibeinc.schedule.core.ScheduleManagementSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

final class ConsoleSession {
    private final ScheduleManagementSystem system;

    ConsoleSession(ScheduleManagementSystem system) {
        this.system = system;
    }

    String run(String... lines) {
        String input = lines.length == 0 ? "" : String.join("\n", lines) + "\n";
        Scanner in = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleApp(system, in, out).run();
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
