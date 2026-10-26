package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.ScheduleException;
import org.vibeinc.schedule.core.ScheduleManagementSystem;
import org.vibeinc.schedule.core.Subject;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Консольний інтерфейс — ФВ-6. Потоки введення й виводу передаються ззовні,
 * щоб інтерфейс можна було перевірити тестом без клавіатури.
 */
public final class ConsoleApp {
    private static final String MENU = """

            ===== Система керування студентським розкладом =====
             1  Додати предмет
             2  Зареєструвати студента
             3  Записати студента на предмет
             4  Додати заняття
             5  Перенести заняття
             6  Скасувати заняття
             7  Загальний розклад
             8  Розклад студента
             9  Розклад викладача
            10  Виставити оцінку
            11  Оцінки студента
            12  Списки предметів і студентів
             0  Вихід""";

    private final ScheduleManagementSystem system;
    private final Scanner in;
    private final PrintStream out;

    public ConsoleApp(ScheduleManagementSystem system, Scanner in, PrintStream out) {
        this.system = system;
        this.in = in;
        this.out = out;
    }

    /** Головний цикл меню. Завершується пунктом 0 або кінцем вхідного потоку. */
    public void run() {
        while (true) {
            out.println(MENU);
            String choice = ask("Ваш вибір");
            if (choice == null || choice.equals("0")) {
                out.println("До побачення!");
                return;
            }
            try {
                handle(choice);
            } catch (ScheduleException | IllegalArgumentException | DateTimeParseException e) {
                // ФВ-7: помилка не завершує програму, користувач бачить зрозуміле повідомлення.
                out.println("Помилка: " + e.getMessage());
            }
        }
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> {
                Subject s = system.addSubject(ask("Назва предмета"), ask("Викладач"),
                        askInt("Кредити ЄКТС"));
                out.println("Додано предмет: " + s);
            }
            case "2" -> out.println("Зареєстровано: "
                    + system.registerStudent(ask("Номер залікової книжки"), ask("ПІБ"), ask("Група")));
            case "3" -> {
                system.enroll(ask("Номер залікової книжки"), ask("Назва предмета"));
                out.println("Студента записано на предмет");
            }
            case "4" -> {
                Lesson l = system.addLesson(ask("Назва предмета"), askType(), askDateTime("Початок"),
                        ask("Аудиторія"));
                out.println("Додано: " + Formats.lesson(l));
            }
            case "5" -> {
                Lesson l = chooseLesson();
                Lesson moved = system.getSchedule().reschedule(l, askDateTime("Новий початок"));
                out.println("Перенесено: " + Formats.lesson(moved));
            }
            case "6" -> {
                system.getSchedule().cancel(chooseLesson());
                out.println("Заняття скасовано");
            }
            case "7" -> print(system.getSchedule().getLessons());
            case "8" -> print(system.getStudentSchedule(ask("Номер залікової книжки")));
            case "9" -> print(system.getTeacherSchedule(ask("Викладач")));
            case "10" -> {
                system.addGrade(ask("Номер залікової книжки"), ask("Назва предмета"),
                        askInt("Оцінка 0–100"));
                out.println("Оцінку виставлено");
            }
            case "11" -> printGrades(ask("Номер залікової книжки"));
            case "12" -> {
                out.println("Предмети:");
                system.getSubjects().forEach(s -> out.println("  " + s));
                out.println("Студенти:");
                system.getStudents().forEach(s -> out.println("  " + s));
            }
            default -> out.println("Невідомий пункт меню: " + choice);
        }
    }

    private void printGrades(String studentId) {
        Map<Subject, List<Integer>> grades = system.getGrades(studentId);
        if (grades.isEmpty()) {
            out.println("Оцінок немає");
            return;
        }
        grades.forEach((subject, values) -> out.printf("  %s: %s, середній бал %.1f%n",
                subject.getName(), values,
                system.getAverageGrade(studentId, subject.getName()).orElse(0)));
    }

    private void print(List<Lesson> lessons) {
        if (lessons.isEmpty()) {
            out.println("Занять немає");
            return;
        }
        for (int i = 0; i < lessons.size(); i++) {
            out.printf("%2d. %s%n", i + 1, Formats.lesson(lessons.get(i)));
        }
    }

    /** Показує загальний розклад і повертає заняття, яке вибрав користувач. */
    private Lesson chooseLesson() {
        List<Lesson> lessons = system.getSchedule().getLessons();
        print(lessons);
        int number = askInt("Номер заняття");
        if (number < 1 || number > lessons.size()) {
            throw new IllegalArgumentException("Немає заняття з номером " + number);
        }
        return lessons.get(number - 1);
    }

    private LessonType askType() {
        LessonType[] types = LessonType.values();
        for (int i = 0; i < types.length; i++) {
            out.printf("  %d — %s%n", i + 1, types[i].getTitle());
        }
        int n = askInt("Вид заняття");
        if (n < 1 || n > types.length) {
            throw new IllegalArgumentException("Немає виду заняття з номером " + n);
        }
        return types[n - 1];
    }

    private LocalDateTime askDateTime(String prompt) {
        return LocalDateTime.parse(ask(prompt + " у форматі РРРР-ММ-ДД ГГ:ХХ"), Formats.DATE_TIME);
    }

    /** Читає ціле число. Нечислове введення дає зрозуміле повідомлення замість NumberFormatException. */
    private int askInt(String prompt) {
        String value = ask(prompt);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Очікувалося ціле число, отримано \"" + value + "\"");
        }
    }

    /** Виводить підказку й читає рядок. Повертає null, якщо вхідний потік закінчився. */
    private String ask(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        return in.hasNextLine() ? in.nextLine().strip() : null;
    }
}
