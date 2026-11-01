package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vibeinc.schedule.app.DefaultStudent;
import org.vibeinc.schedule.app.DefaultSubject;
import org.vibeinc.schedule.core.DuplicateEntityException;
import org.vibeinc.schedule.core.EntityNotFoundException;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.Schedule;
import org.vibeinc.schedule.core.ScheduleConflictException;
import org.vibeinc.schedule.core.ScheduleException;
import org.vibeinc.schedule.core.ScheduleManagementSystem;
import org.vibeinc.schedule.core.Student;
import org.vibeinc.schedule.core.Subject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsoleAppMockTest {
    private static final Subject GIT = new DefaultSubject("Git", "Кузнецова Ю. А.", 4);
    private static final Student KOZYR = new DefaultStudent("KB-001", "Козир Віталій Андрійович", "658п");
    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 4, 10, 25);
    private static final Lesson LECTURE = new Lesson(GIT, LessonType.LECTURE, START, Lesson.DEFAULT_DURATION, "301");
    private static final Lesson LAB = new Lesson(GIT, LessonType.LAB, START.plusHours(2), Lesson.DEFAULT_DURATION, "online");

    @Mock
    private ScheduleManagementSystem system;

    @Mock
    private Schedule schedule;

    @Captor
    private ArgumentCaptor<Lesson> lessonCaptor;

    private ConsoleSession console;

    @BeforeEach
    void setUp() {
        console = new ConsoleSession(system);
    }

    private void lessonsInSchedule(Lesson... lessons) {
        when(system.getSchedule()).thenReturn(schedule);
        when(schedule.getLessons()).thenReturn(List.of(lessons));
    }

    static Stream<ScheduleException> enrollErrors() {
        return Stream.of(
                new EntityNotFoundException("Студента з номером \"KB-999\" не знайдено"),
                new DuplicateEntityException("Козир Віталій Андрійович уже записаний на предмет \"Git\""));
    }

    @Test
    @DisplayName("TC-51 Вихід або кінець введення – система не викликається")
    void exitWithoutCalls() {
        assertTrue(console.run("0").contains("До побачення!"));
        assertTrue(console.run().contains("До побачення!"));
        verifyNoInteractions(system);
    }

    @Test
    @DisplayName("TC-52 Пункт 2 – дані студента передаються в систему без пробілів на краях")
    void registerStudentStripsInput() {
        when(system.registerStudent("KB-003", "Іваненко Іван", "658п"))
                .thenReturn(new DefaultStudent("KB-003", "Іваненко Іван", "658п"));

        String output = console.run("2", "  KB-003 ", "Іваненко Іван", " 658п ", "0");

        assertTrue(output.contains("Зареєстровано: KB-003 — Іваненко Іван, гр. 658п"));
    }

    @ParameterizedTest
    @MethodSource("enrollErrors")
    @DisplayName("TC-53 Пункт 3 – помилка системи виводиться, програма продовжує роботу")
    void enrollErrorIsPrinted(ScheduleException error) {
        doThrow(error).when(system).enroll("KB-999", "Git");

        String output = console.run("3", "KB-999", "Git", "0");

        assertTrue(output.contains("Помилка: " + error.getMessage()));
        assertFalse(output.contains("Студента записано на предмет"));
        assertTrue(output.contains("До побачення!"));
    }

    @ParameterizedTest
    @EnumSource(LessonType.class)
    @DisplayName("TC-54 Пункт 4 – номер виду заняття перетворюється на LessonType")
    void addLessonOfEachType(LessonType type) {
        when(system.addLesson("Git", type, START, "301"))
                .thenReturn(new Lesson(GIT, type, START, Lesson.DEFAULT_DURATION, "301"));

        String output = console.run("4", "Git", String.valueOf(type.ordinal() + 1), "2026-11-04 10:25", "301", "0");

        assertTrue(output.contains("Додано: 2026-11-04 ср 10:25–12:00 | Git | " + type.getTitle() + " | ауд. 301"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "0   | Немає виду заняття з номером 0",
            "4   | Немає виду заняття з номером 4",
            "-1  | Немає виду заняття з номером -1",
            "два | Очікувалося ціле число, отримано \"два\""
    })
    @DisplayName("TC-55 Пункт 4 – некоректний вид заняття не доходить до системи")
    void addLessonWithWrongType(String typeNumber, String message) {
        String output = console.run("4", "Git", typeNumber, "0");

        assertTrue(output.contains("Помилка: " + message));
        verify(system, never()).addLesson(anyString(), any(), any(), anyString());
    }

    @Test
    @DisplayName("TC-56 Пункт 5 – обране заняття переноситься на введений час")
    void rescheduleChosenLesson() {
        lessonsInSchedule(LECTURE);
        LocalDateTime newStart = LocalDateTime.of(2026, 11, 5, 12, 20);
        when(schedule.reschedule(LECTURE, newStart)).thenReturn(LECTURE.withStart(newStart));

        String output = console.run("5", "1", "2026-11-05 12:20", "0");

        assertTrue(output.contains(" 1. 2026-11-04 ср 10:25–12:00 | Git"));
        assertTrue(output.contains("Перенесено: 2026-11-05 чт 12:20–13:55 | Git"));
    }

    @Test
    @DisplayName("TC-57 Пункт 5 – конфлікт під час перенесення виводиться як помилка")
    void rescheduleConflictIsPrinted() {
        lessonsInSchedule(LECTURE);
        LocalDateTime newStart = LocalDateTime.of(2026, 11, 4, 11, 0);
        when(schedule.reschedule(LECTURE, newStart))
                .thenThrow(new ScheduleConflictException("Аудиторія 301 зайнята: Бази даних о 2026-11-04 10:30"));

        String output = console.run("5", "1", "2026-11-04 11:00", "0");

        assertTrue(output.contains("Помилка: Аудиторія 301 зайнята: Бази даних о 2026-11-04 10:30"));
        assertFalse(output.contains("Перенесено"));
    }

    @Test
    @DisplayName("TC-58 Пункт 6 – скасовується саме заняття з обраним номером")
    void cancelChosenLesson() {
        lessonsInSchedule(LECTURE, LAB);

        String output = console.run("6", "2", "0");

        verify(schedule).cancel(lessonCaptor.capture());
        assertEquals(LAB, lessonCaptor.getValue());
        assertTrue(output.contains("Заняття скасовано"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, -1})
    @DisplayName("TC-59 Пункт 6 – номер поза списком занять не скасовує нічого")
    void cancelWithWrongNumber(int number) {
        lessonsInSchedule(LECTURE, LAB);

        String output = console.run("6", String.valueOf(number), "0");

        assertTrue(output.contains("Помилка: Немає заняття з номером " + number));
        verify(schedule, never()).cancel(any());
    }

    @Test
    @DisplayName("TC-60 Пункт 7 – загальний розклад нумерується в порядку, який повернув розклад")
    void printWholeSchedule() {
        lessonsInSchedule(LECTURE, LAB);

        String output = console.run("7", "0");

        int first = output.indexOf(" 1. 2026-11-04 ср 10:25–12:00 | Git | Лекція");
        int second = output.indexOf(" 2. 2026-11-04 ср 12:25–14:00 | Git | Лабораторна");
        assertTrue(first >= 0 && second > first);
    }

    @Test
    @DisplayName("TC-61 Пункт 8 – порожній розклад студента")
    void printEmptyStudentSchedule() {
        when(system.getStudentSchedule("KB-001")).thenReturn(List.of());

        String output = console.run("8", " KB-001 ", "0");

        assertTrue(output.contains("Занять немає"));
    }

    @Test
    @DisplayName("TC-62 Пункт 9 – розклад викладача, отриманий від системи")
    void printTeacherSchedule() {
        when(system.getTeacherSchedule("Кузнецова Ю. А.")).thenReturn(List.of(LAB));

        String output = console.run("9", "Кузнецова Ю. А.", "0");

        assertTrue(output.contains(" 1. 2026-11-04 ср 12:25–14:00 | Git | Лабораторна | ауд. online"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 95, 100, 150})
    @DisplayName("TC-63 Пункт 10 – оцінка передається в систему як ціле число")
    void addGradePassesNumber(int grade) {
        String output = console.run("10", "KB-001", "Git", String.valueOf(grade), "0");

        verify(system).addGrade("KB-001", "Git", grade);
        assertTrue(output.contains("Оцінку виставлено"));
    }

    @Test
    @DisplayName("TC-64 Пункт 11 – оцінки студента із середнім балом")
    void printGradesWithAverage() {
        when(system.getGrades("KB-001")).thenReturn(Map.of(GIT, List.of(90, 75)));
        when(system.getAverageGrade("KB-001", "Git")).thenReturn(OptionalDouble.of(82.5));

        String output = console.run("11", "KB-001", "0");

        assertTrue(output.matches("(?s).*Git: \\[90, 75], середній бал 82[.,]5.*"));
    }

    @Test
    @DisplayName("TC-65 Пункт 11 – студент без оцінок, середній бал не рахується")
    void printNoGrades() {
        when(system.getGrades("KB-001")).thenReturn(Map.of());

        String output = console.run("11", "KB-001", "0");

        assertTrue(output.contains("Оцінок немає"));
        verify(system, never()).getAverageGrade(anyString(), anyString());
    }

    @Test
    @DisplayName("TC-66 Пункт 12 – списки предметів і студентів")
    void printSubjectsAndStudents() {
        when(system.getSubjects()).thenReturn(List.of(GIT));
        when(system.getStudents()).thenReturn(List.of(KOZYR));

        String output = console.run("12", "0");

        int subjects = output.indexOf("Предмети:");
        int git = output.indexOf("  Git — Кузнецова Ю. А., 4 кред.");
        int students = output.indexOf("Студенти:");
        int kozyr = output.indexOf("  KB-001 — Козир Віталій Андрійович, гр. 658п");
        assertTrue(subjects >= 0 && subjects < git && git < students && students < kozyr);
    }
}
