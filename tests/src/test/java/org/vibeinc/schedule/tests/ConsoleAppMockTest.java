package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.vibeinc.schedule.core.DuplicateEntityException;
import org.vibeinc.schedule.core.EntityNotFoundException;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.Schedule;
import org.vibeinc.schedule.core.ScheduleException;
import org.vibeinc.schedule.core.ScheduleManagementSystem;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@Disabled("Тести-пустушки – макети ще не ініціалізовано")
class ConsoleAppMockTest {

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

    static Stream<ScheduleException> enrollErrors() {
        return Stream.of(
                new EntityNotFoundException("Студента з номером \"KB-999\" не знайдено"),
                new DuplicateEntityException("Козир Віталій Андрійович уже записаний на предмет \"Git\""));
    }

    @Test
    @DisplayName("TC-51 Вихід або кінець введення – система не викликається")
    void exitWithoutCalls() {
    }

    @Test
    @DisplayName("TC-52 Пункт 2 – дані студента передаються в систему без пробілів на краях")
    void registerStudentStripsInput() {
    }

    @ParameterizedTest
    @MethodSource("enrollErrors")
    @DisplayName("TC-53 Пункт 3 – помилка системи виводиться, програма продовжує роботу")
    void enrollErrorIsPrinted(ScheduleException error) {
    }

    @ParameterizedTest
    @EnumSource(LessonType.class)
    @DisplayName("TC-54 Пункт 4 – номер виду заняття перетворюється на LessonType")
    void addLessonOfEachType(LessonType type) {
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
    }

    @Test
    @DisplayName("TC-56 Пункт 5 – обране заняття переноситься на введений час")
    void rescheduleChosenLesson() {
    }

    @Test
    @DisplayName("TC-57 Пункт 5 – конфлікт під час перенесення виводиться як помилка")
    void rescheduleConflictIsPrinted() {
    }

    @Test
    @DisplayName("TC-58 Пункт 6 – скасовується саме заняття з обраним номером")
    void cancelChosenLesson() {
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, -1})
    @DisplayName("TC-59 Пункт 6 – номер поза списком занять не скасовує нічого")
    void cancelWithWrongNumber(int number) {
    }

    @Test
    @DisplayName("TC-60 Пункт 7 – загальний розклад нумерується в порядку, який повернув розклад")
    void printWholeSchedule() {
    }

    @Test
    @DisplayName("TC-61 Пункт 8 – порожній розклад студента")
    void printEmptyStudentSchedule() {
    }

    @Test
    @DisplayName("TC-62 Пункт 9 – розклад викладача, отриманий від системи")
    void printTeacherSchedule() {
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 95, 100, 150})
    @DisplayName("TC-63 Пункт 10 – оцінка передається в систему як ціле число")
    void addGradePassesNumber(int grade) {
    }

    @Test
    @DisplayName("TC-64 Пункт 11 – оцінки студента із середнім балом")
    void printGradesWithAverage() {
    }

    @Test
    @DisplayName("TC-65 Пункт 11 – студент без оцінок, середній бал не рахується")
    void printNoGrades() {
    }

    @Test
    @DisplayName("TC-66 Пункт 12 – списки предметів і студентів")
    void printSubjectsAndStudents() {
    }
}
