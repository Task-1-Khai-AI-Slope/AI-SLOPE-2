package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.ScheduleException;
import org.vibeinc.schedule.core.ScheduleManagementSystem;
import org.vibeinc.schedule.core.Student;
import org.vibeinc.schedule.core.Subject;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
            13  Студенти групи
            14  Розклад на дату
             0  Вихід""";

    private static final String DATE_HINT = "РРРР-ММ-ДД";
    private static final String DATE_TIME_HINT = "РРРР-ММ-ДД ГГ:ХХ";

    private final ScheduleManagementSystem system;
    private final Scanner in;
    private final PrintStream out;

    /**
     * Тестові набори: макет {@code ScheduleManagementSystem}, {@code Scanner} над рядком введення
     * і {@code PrintStream} над буфером – обидва потоки обгортає тестовий клас {@code ConsoleSession}.
     */
    public ConsoleApp(ScheduleManagementSystem system, Scanner in, PrintStream out) {
        this.system = system;
        this.in = in;
        this.out = out;
    }

    /**
     * Тестові набори: вихід “0”, кінець введення без жодного рядка – система не викликається.
     * Невідомий пункт меню, помилки {@code ScheduleException}, {@code IllegalArgumentException}
     * і {@code DateTimeParseException} від макета – програма виводить “Помилка: …” і продовжує роботу.
     */
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
                out.println("Помилка: " + e.getMessage());
            }
        }
    }

    /**
     * Тестові набори: кожен пункт меню 1–14 з коректними даними, для пунктів 4–6 і 10 – некоректні
     * номери й дати. Макет системи перевіряє, що в неї передано саме введені значення без пробілів.
     */
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
            case "13" -> printGroup(ask("Група"));
            case "14" -> {
                LocalDate date = askDate("Дата");
                out.println("Розклад на " + Formats.date(date) + ":");
                print(system.getSchedule().getLessonsOn(date));
            }
            default -> out.println("Невідомий пункт меню: " + choice);
        }
    }

    /**
     * Тестові набори: макет повертає порожній словник оцінок – виводиться “Оцінок немає”
     * і середній бал не запитується, або кілька оцінок із середнім 82,5.
     */
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

    /**
     * Тестові набори: група в іншому регістрі, група без студентів, студенти кількох груп.
     */
    private void printGroup(String group) {
        String key = Texts.key(group);
        List<Student> students = system.getStudents().stream()
                .filter(s -> Texts.key(s.getGroup()).equals(key))
                .toList();
        if (students.isEmpty()) {
            out.println("У групі " + group + " студентів немає");
            return;
        }
        out.println("Група " + group + ", студентів: " + students.size());
        students.forEach(s -> out.println("  " + s));
    }

    /**
     * Тестові набори: порожній список – “Занять немає”, кілька занять – нумерація з 1
     * в порядку, у якому їх повернула система.
     */
    private void print(List<Lesson> lessons) {
        if (lessons.isEmpty()) {
            out.println("Занять немає");
            return;
        }
        for (int i = 0; i < lessons.size(); i++) {
            out.printf("%2d. %s%n", i + 1, Formats.lesson(lessons.get(i)));
        }
    }

    /**
     * Тестові набори: номер першого й останнього заняття, 0, −1 і номер, більший за кількість занять.
     */
    private Lesson chooseLesson() {
        List<Lesson> lessons = system.getSchedule().getLessons();
        print(lessons);
        int number = askInt("Номер заняття");
        if (number < 1 || number > lessons.size()) {
            throw new IllegalArgumentException("Немає заняття з номером " + number);
        }
        return lessons.get(number - 1);
    }

    /**
     * Тестові набори: номери 1–3 для кожного {@code LessonType}, 0, 4, −1 і нечислове введення.
     */
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

    /**
     * Тестові набори: “2026-11-04 10:25”, дата без часу, дата у форматі “28.10.2026”.
     */
    private LocalDateTime askDateTime(String prompt) {
        String value = ask(prompt + " у форматі " + DATE_TIME_HINT);
        try {
            return LocalDateTime.parse(value, Formats.DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Некоректні дата й час \"" + value + "\", очікується формат " + DATE_TIME_HINT);
        }
    }

    /**
     * Тестові набори: “2026-10-28”, вільний день без занять, дата у форматі “28.10.2026”.
     */
    private LocalDate askDate(String prompt) {
        String value = ask(prompt + " у форматі " + DATE_HINT);
        try {
            return LocalDate.parse(value, Formats.DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Некоректна дата \"" + value + "\", очікується формат " + DATE_HINT);
        }
    }

    /**
     * Тестові набори: 0, додатні й від’ємні числа, слово замість числа.
     */
    private int askInt(String prompt) {
        String value = ask(prompt);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Очікувалося ціле число, отримано \"" + value + "\"");
        }
    }

    /**
     * Тестові набори: рядок з пробілами на краях, порожній рядок і кінець введення – тоді {@code null}.
     */
    private String ask(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        return in.hasNextLine() ? in.nextLine().strip() : null;
    }
}
