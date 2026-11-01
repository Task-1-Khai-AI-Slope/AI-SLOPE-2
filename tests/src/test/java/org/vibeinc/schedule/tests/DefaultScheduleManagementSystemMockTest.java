package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vibeinc.schedule.app.DefaultScheduleManagementSystem;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.Schedule;
import org.vibeinc.schedule.core.ScheduleManagementSystem;
import org.vibeinc.schedule.core.Subject;

@ExtendWith(MockitoExtension.class)
@Disabled("Тести-пустушки – макети ще не ініціалізовано")
class DefaultScheduleManagementSystemMockTest {

    @Mock
    private Schedule schedule;

    @Captor
    private ArgumentCaptor<Lesson> lessonCaptor;

    private ScheduleManagementSystem system;
    private Subject git;
    private Subject databases;

    @BeforeEach
    void setUp() {
        system = new DefaultScheduleManagementSystem(schedule);
        git = system.addSubject("Git", "Кузнецова Ю. А.", 4);
        databases = system.addSubject("Бази даних", "Петренко О. І.", 5);
        system.registerStudent("KB-001", "Козир В. А.", "658п");
        system.registerStudent("KB-002", "Єременко М. Л.", "658п");
        system.enroll("KB-001", "Git");
    }

    @ParameterizedTest
    @CsvSource({
            "Git,        LECTURE,  301",
            "' git ',    LAB,      ' online '",
            "БАЗИ ДАНИХ, PRACTICE, 302 ГАК"
    })
    @DisplayName("TC-43 Нове заняття передається в розклад з даними з параметрів")
    void addLessonPassesLessonToSchedule(String subjectName, LessonType type, String room) {
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Хімія", "   ", "Gi t"})
    @DisplayName("TC-44 Заняття з неіснуючого предмета не доходить до розкладу")
    void addLessonForUnknownSubject(String subjectName) {
    }

    @Test
    @DisplayName("TC-45 Конфлікт, знайдений розкладом, передається без змін")
    void addLessonRethrowsConflict() {
    }

    @Test
    @DisplayName("TC-46 Розклад студента – лише заняття з його предметів у порядку розкладу")
    void studentScheduleFiltersLessons() {
    }

    @ParameterizedTest
    @ValueSource(strings = {"KB-999", "", "658п"})
    @DisplayName("TC-47 Розклад незареєстрованого студента не запитується з розкладу")
    void studentScheduleForUnknownStudent(String studentId) {
    }

    @ParameterizedTest
    @ValueSource(strings = {"Кузнецова Ю. А.", "  кузнецова ю. а.  ", "КУЗНЕЦОВА Ю. А."})
    @DisplayName("TC-48 Розклад викладача не залежить від регістру й пробілів у прізвищі")
    void teacherScheduleIgnoresCase(String teacher) {
    }

    @Test
    @DisplayName("TC-49 Операції з предметами, студентами й оцінками не звертаються до розкладу")
    void gradesDoNotUseSchedule() {
    }

    @Test
    @DisplayName("TC-50 Обгортка над справжнім розкладом – реальна перевірка конфлікту і облік викликів")
    void wrapperOverRealSchedule() {
    }
}
