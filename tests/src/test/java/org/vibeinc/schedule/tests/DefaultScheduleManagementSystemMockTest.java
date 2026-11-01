package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.BeforeEach;
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
import org.vibeinc.schedule.app.DefaultSchedule;
import org.vibeinc.schedule.app.DefaultScheduleManagementSystem;
import org.vibeinc.schedule.core.EntityNotFoundException;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.Schedule;
import org.vibeinc.schedule.core.ScheduleConflictException;
import org.vibeinc.schedule.core.ScheduleManagementSystem;
import org.vibeinc.schedule.core.Subject;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultScheduleManagementSystemMockTest {
    private static final LocalDateTime AT_10 = LocalDateTime.of(2026, 11, 4, 10, 25);

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

    private static Lesson lesson(Subject subject, LessonType type, int hour, String room) {
        return new Lesson(subject, type, AT_10.withHour(hour).withMinute(0), Lesson.DEFAULT_DURATION, room);
    }

    @ParameterizedTest
    @CsvSource({
            "Git,        LECTURE,  301",
            "' git ',    LAB,      ' online '",
            "БАЗИ ДАНИХ, PRACTICE, 302 ГАК"
    })
    @DisplayName("TC-43 Нове заняття передається в розклад з даними з параметрів")
    void addLessonPassesLessonToSchedule(String subjectName, LessonType type, String room) {
        Lesson returned = system.addLesson(subjectName, type, AT_10, room);

        verify(schedule).addLesson(lessonCaptor.capture());
        verifyNoMoreInteractions(schedule);
        Lesson passed = lessonCaptor.getValue();
        assertSame(returned, passed);
        assertEquals(system.findSubject(subjectName).orElseThrow(), passed.subject());
        assertEquals(type, passed.type());
        assertEquals(AT_10, passed.start());
        assertEquals(Lesson.DEFAULT_DURATION, passed.duration());
        assertEquals(room.strip(), passed.room());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Хімія", "   ", "Gi t"})
    @DisplayName("TC-44 Заняття з неіснуючого предмета не доходить до розкладу")
    void addLessonForUnknownSubject(String subjectName) {
        assertThrows(EntityNotFoundException.class,
                () -> system.addLesson(subjectName, LessonType.LECTURE, AT_10, "301"));
        verifyNoInteractions(schedule);
    }

    @Test
    @DisplayName("TC-45 Конфлікт, знайдений розкладом, передається без змін")
    void addLessonRethrowsConflict() {
        ScheduleConflictException conflict = new ScheduleConflictException("Аудиторія 301 зайнята");
        doThrow(conflict).when(schedule).addLesson(any(Lesson.class));

        ScheduleConflictException thrown = assertThrows(ScheduleConflictException.class,
                () -> system.addLesson("Git", LessonType.LAB, AT_10, "301"));

        assertSame(conflict, thrown);
    }

    @Test
    @DisplayName("TC-46 Розклад студента – лише заняття з його предметів у порядку розкладу")
    void studentScheduleFiltersLessons() {
        Lesson gitLecture = lesson(git, LessonType.LECTURE, 8, "301");
        Lesson dbLecture = lesson(databases, LessonType.LECTURE, 10, "302");
        Lesson gitLab = lesson(git, LessonType.LAB, 12, "online");
        when(schedule.getLessons()).thenReturn(List.of(gitLecture, dbLecture, gitLab));

        assertEquals(List.of(gitLecture, gitLab), system.getStudentSchedule("KB-001"));
        assertTrue(system.getStudentSchedule("KB-002").isEmpty());
        verify(schedule, times(2)).getLessons();
        verifyNoMoreInteractions(schedule);
    }

    @ParameterizedTest
    @ValueSource(strings = {"KB-999", "", "658п"})
    @DisplayName("TC-47 Розклад незареєстрованого студента не запитується з розкладу")
    void studentScheduleForUnknownStudent(String studentId) {
        assertThrows(EntityNotFoundException.class, () -> system.getStudentSchedule(studentId));
        verify(schedule, never()).getLessons();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Кузнецова Ю. А.", "  кузнецова ю. а.  ", "КУЗНЕЦОВА Ю. А."})
    @DisplayName("TC-48 Розклад викладача не залежить від регістру й пробілів у прізвищі")
    void teacherScheduleIgnoresCase(String teacher) {
        Lesson gitLecture = lesson(git, LessonType.LECTURE, 8, "301");
        Lesson dbLecture = lesson(databases, LessonType.LECTURE, 10, "302");
        when(schedule.getLessons()).thenReturn(List.of(gitLecture, dbLecture));

        assertEquals(List.of(gitLecture), system.getTeacherSchedule(teacher));
    }

    @Test
    @DisplayName("TC-49 Операції з предметами, студентами й оцінками не звертаються до розкладу")
    void gradesDoNotUseSchedule() {
        system.enroll("KB-002", "Бази даних");
        system.addGrade("KB-002", "Бази даних", 90);
        system.addGrade("KB-002", "бази даних", 75);

        assertEquals(List.of(90, 75), system.getGrades("KB-002").get(databases));
        assertEquals(82.5, system.getAverageGrade("KB-002", "Бази даних").orElseThrow(), 0.001);
        assertSame(schedule, system.getSchedule());
        verifyNoInteractions(schedule);
    }

    @Test
    @DisplayName("TC-50 Обгортка над справжнім розкладом – реальна перевірка конфлікту і облік викликів")
    void wrapperOverRealSchedule() {
        Schedule wrapper = mock(Schedule.class, delegatesTo(new DefaultSchedule()));
        ScheduleManagementSystem wrapped = new DefaultScheduleManagementSystem(wrapper);
        wrapped.addSubject("Git", "Кузнецова Ю. А.", 4);
        wrapped.addSubject("Бази даних", "Петренко О. І.", 5);

        Lesson first = wrapped.addLesson("Git", LessonType.LECTURE, AT_10, "301");
        assertThrows(ScheduleConflictException.class,
                () -> wrapped.addLesson("Бази даних", LessonType.LECTURE, AT_10.plusMinutes(30), "301"));

        verify(wrapper, times(2)).addLesson(any(Lesson.class));
        assertEquals(List.of(first), wrapper.getLessons());
    }
}
