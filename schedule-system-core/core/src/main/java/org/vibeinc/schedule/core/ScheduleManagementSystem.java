package org.vibeinc.schedule.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Система керування студентським розкладом — головний фасад, через який
 * інтерфейс користувача працює з предметами, студентами, розкладом і оцінками.
 * Номери ФВ відповідають технічному завданню.
 */
public interface ScheduleManagementSystem {

    // ---------- ФВ-1. Предмети ----------

    /** @throws DuplicateEntityException якщо предмет з такою назвою вже є */
    Subject addSubject(String name, String teacher, int credits);

    Optional<Subject> findSubject(String name);

    List<Subject> getSubjects();

    // ---------- ФВ-2. Студенти ----------

    /** @throws DuplicateEntityException якщо студент з таким номером вже є */
    Student registerStudent(String id, String fullName, String group);

    Optional<Student> findStudent(String id);

    List<Student> getStudents();

    // ---------- ФВ-3. Запис на предмети ----------

    /**
     * Записує студента на предмет.
     *
     * @throws EntityNotFoundException  якщо немає студента або предмета
     * @throws DuplicateEntityException якщо студент уже записаний
     */
    void enroll(String studentId, String subjectName);

    /** Предмети, на які записаний студент. */
    List<Subject> getSubjectsOf(String studentId);

    // ---------- ФВ-4. Розклад ----------

    /** Додає заняття з предмета, знайденого за назвою. */
    Lesson addLesson(String subjectName, LessonType type, LocalDateTime start, String room);

    /** Загальний розклад. */
    Schedule getSchedule();

    /** ФВ-4.5. Особистий розклад студента — лише заняття з предметів, на які він записаний. */
    List<Lesson> getStudentSchedule(String studentId);

    /** ФВ-4.6. Розклад викладача. */
    List<Lesson> getTeacherSchedule(String teacher);

    // ---------- ФВ-5. Оцінки ----------

    /**
     * Виставляє оцінку за 100-бальною шкалою.
     *
     * @throws IllegalArgumentException якщо оцінка поза межами 0–100
     * @throws EntityNotFoundException  якщо студент не записаний на предмет
     */
    void addGrade(String studentId, String subjectName, int grade);

    /** Усі оцінки студента за предметами. */
    Map<Subject, List<Integer>> getGrades(String studentId);

    /** Середній бал студента з предмета, порожній, якщо оцінок немає. */
    OptionalDouble getAverageGrade(String studentId, String subjectName);
}
