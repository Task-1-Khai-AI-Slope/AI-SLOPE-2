package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.DuplicateEntityException;
import org.vibeinc.schedule.core.EntityNotFoundException;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.LessonType;
import org.vibeinc.schedule.core.Schedule;
import org.vibeinc.schedule.core.ScheduleManagementSystem;
import org.vibeinc.schedule.core.Student;
import org.vibeinc.schedule.core.Subject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Прототип системи керування розкладом. Дані зберігаються в пам'яті.
 * Ключі словників — рядки, нормалізовані через {@link Texts#key(String)},
 * тому пошук не залежить від регістру.
 */
public final class DefaultScheduleManagementSystem implements ScheduleManagementSystem {
    private final Map<String, Subject> subjects = new LinkedHashMap<>();
    private final Map<String, Student> students = new LinkedHashMap<>();
    /** Номер залікової книжки -> предмети, на які записаний студент. */
    private final Map<String, Set<Subject>> enrollments = new LinkedHashMap<>();
    /** Номер залікової книжки -> предмет -> оцінки в порядку виставлення. */
    private final Map<String, Map<Subject, List<Integer>>> grades = new LinkedHashMap<>();
    private final Schedule schedule = new DefaultSchedule();

    // ---------- ФВ-1. Предмети ----------

    @Override
    public Subject addSubject(String name, String teacher, int credits) {
        Subject subject = new DefaultSubject(name, teacher, credits);
        String key = Texts.key(subject.getName());
        if (subjects.containsKey(key)) {
            throw new DuplicateEntityException("Предмет \"" + subject.getName() + "\" уже існує");
        }
        subjects.put(key, subject);
        return subject;
    }

    @Override
    public Optional<Subject> findSubject(String name) {
        return Optional.ofNullable(subjects.get(Texts.key(name)));
    }

    @Override
    public List<Subject> getSubjects() {
        return List.copyOf(subjects.values());
    }

    // ---------- ФВ-2. Студенти ----------

    @Override
    public Student registerStudent(String id, String fullName, String group) {
        Student student = new DefaultStudent(id, fullName, group);
        String key = Texts.key(student.getId());
        if (students.containsKey(key)) {
            throw new DuplicateEntityException("Студент з номером " + student.getId() + " уже зареєстрований");
        }
        students.put(key, student);
        return student;
    }

    @Override
    public Optional<Student> findStudent(String id) {
        return Optional.ofNullable(students.get(Texts.key(id)));
    }

    @Override
    public List<Student> getStudents() {
        return List.copyOf(students.values());
    }

    // ---------- ФВ-3. Запис на предмети ----------

    @Override
    public void enroll(String studentId, String subjectName) {
        Student student = requireStudent(studentId);
        Subject subject = requireSubject(subjectName);
        Set<Subject> set = enrollments.computeIfAbsent(Texts.key(student.getId()), k -> new LinkedHashSet<>());
        if (!set.add(subject)) {
            throw new DuplicateEntityException(student.getFullName()
                    + " уже записаний на предмет \"" + subject.getName() + "\"");
        }
    }

    @Override
    public List<Subject> getSubjectsOf(String studentId) {
        Student student = requireStudent(studentId);
        return List.copyOf(enrollments.getOrDefault(Texts.key(student.getId()), Set.of()));
    }

    // ---------- ФВ-4. Розклад ----------

    @Override
    public Lesson addLesson(String subjectName, LessonType type, LocalDateTime start, String room) {
        Lesson lesson = new Lesson(requireSubject(subjectName), type, start, Lesson.DEFAULT_DURATION, room);
        schedule.addLesson(lesson);
        return lesson;
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public List<Lesson> getStudentSchedule(String studentId) {
        Set<Subject> own = Set.copyOf(getSubjectsOf(studentId));
        return schedule.getLessons().stream().filter(l -> own.contains(l.subject())).toList();
    }

    @Override
    public List<Lesson> getTeacherSchedule(String teacher) {
        String key = Texts.key(teacher);
        return schedule.getLessons().stream()
                .filter(l -> Texts.key(l.subject().getTeacher()).equals(key))
                .toList();
    }

    // ---------- ФВ-5. Оцінки ----------

    @Override
    public void addGrade(String studentId, String subjectName, int grade) {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Оцінка має бути в межах від 0 до 100");
        }
        Subject subject = requireSubject(subjectName);
        if (!getSubjectsOf(studentId).contains(subject)) {
            throw new EntityNotFoundException("Студент не записаний на предмет \"" + subject.getName() + "\"");
        }
        grades.computeIfAbsent(Texts.key(studentId), k -> new LinkedHashMap<>())
                .computeIfAbsent(subject, k -> new ArrayList<>())
                .add(grade);
    }

    @Override
    public Map<Subject, List<Integer>> getGrades(String studentId) {
        requireStudent(studentId);
        Map<Subject, List<Integer>> copy = new LinkedHashMap<>();
        grades.getOrDefault(Texts.key(studentId), Map.of())
                .forEach((subject, values) -> copy.put(subject, List.copyOf(values)));
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public OptionalDouble getAverageGrade(String studentId, String subjectName) {
        Subject subject = requireSubject(subjectName);
        return getGrades(studentId).getOrDefault(subject, List.of()).stream()
                .mapToInt(Integer::intValue)
                .average();
    }

    // ---------- Допоміжні ----------

    private Student requireStudent(String id) {
        return findStudent(id).orElseThrow(() ->
                new EntityNotFoundException("Студента з номером \"" + id + "\" не знайдено"));
    }

    private Subject requireSubject(String name) {
        return findSubject(name).orElseThrow(() ->
                new EntityNotFoundException("Предмет \"" + name + "\" не знайдено"));
    }
}
