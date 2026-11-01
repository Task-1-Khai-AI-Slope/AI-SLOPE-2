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
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public final class DefaultScheduleManagementSystem implements ScheduleManagementSystem {
    private final Map<String, Subject> subjects = new LinkedHashMap<>();
    private final Map<String, Student> students = new LinkedHashMap<>();
    private final Map<String, Set<Subject>> enrollments = new LinkedHashMap<>();
    private final Map<String, Map<Subject, List<Integer>>> grades = new LinkedHashMap<>();
    private final Schedule schedule;

    /**
     * Тестові набори: макет {@code Schedule}, з яким система має працювати без підміни, і {@code null},
     * що дає {@code NullPointerException} з текстом “Розклад не задано”.
     */
    public DefaultScheduleManagementSystem(Schedule schedule) {
        this.schedule = Objects.requireNonNull(schedule, "Розклад не задано");
    }

    /**
     * Тестові набори: коректні назва, викладач і кредити. Дублікат назви в іншому регістрі й з пробілами.
     * Порожня або {@code null} назва чи викладач, кредити 0, від’ємні та {@code Integer.MIN_VALUE}.
     * Макет {@code Schedule} не повинен отримати жодного виклику.
     */
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

    /**
     * Тестові набори: назва точно як при додаванні, в іншому регістрі, з пробілами на краях,
     * неіснуюча назва, порожній рядок і {@code null} – для двох останніх результат порожній.
     */
    @Override
    public Optional<Subject> findSubject(String name) {
        return Optional.ofNullable(subjects.get(Texts.key(name)));
    }

    /**
     * Тестові набори: порожня система і кілька предметів – перевіряється порядок додавання
     * і те, що список не можна змінити ззовні.
     */
    @Override
    public List<Subject> getSubjects() {
        return List.copyOf(subjects.values());
    }

    /**
     * Тестові набори: коректні номер, ПІБ і група. Повторний номер в іншому регістрі.
     * Порожні або {@code null} номер, ПІБ чи група. Розклад не використовується.
     */
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

    /**
     * Тестові набори: номер точно як при реєстрації, в іншому регістрі, з пробілами,
     * неіснуючий номер і {@code null}.
     */
    @Override
    public Optional<Student> findStudent(String id) {
        return Optional.ofNullable(students.get(Texts.key(id)));
    }

    /**
     * Тестові набори: порожня система і кілька студентів – порядок реєстрації, незмінний список.
     */
    @Override
    public List<Student> getStudents() {
        return List.copyOf(students.values());
    }

    /**
     * Тестові набори: існуючі студент і предмет, повторний запис на той самий предмет в іншому регістрі,
     * неіснуючий студент, неіснуючий предмет. Розклад не використовується.
     */
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

    /**
     * Тестові набори: студент без записів, студент з кількома предметами, неіснуючий студент.
     */
    @Override
    public List<Subject> getSubjectsOf(String studentId) {
        Student student = requireStudent(studentId);
        return List.copyOf(enrollments.getOrDefault(Texts.key(student.getId()), Set.of()));
    }

    /**
     * Тестові набори: назва предмета в різному регістрі й з пробілами, кожен {@code LessonType},
     * аудиторія з пробілами на краях. Неіснуючий, порожній і {@code null} предмет.
     * Макет {@code Schedule}: перевірити переданий в {@code addLesson} об’єкт через {@code ArgumentCaptor},
     * відсутність виклику для неіснуючого предмета і передачу {@code ScheduleConflictException} без змін.
     */
    @Override
    public Lesson addLesson(String subjectName, LessonType type, LocalDateTime start, String room) {
        Lesson lesson = new Lesson(requireSubject(subjectName), type, start, Lesson.DEFAULT_DURATION, room);
        schedule.addLesson(lesson);
        return lesson;
    }

    /**
     * Тестові набори: макет {@code Schedule}, переданий у конструктор, – повертається той самий об’єкт,
     * жодних викликів на ньому немає.
     */
    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Тестові набори: макет {@code Schedule} повертає заняття з предметів студента і з чужих предметів,
     * студент без записів, незареєстрований студент – тоді розклад не запитується.
     */
    @Override
    public List<Lesson> getStudentSchedule(String studentId) {
        Set<Subject> own = Set.copyOf(getSubjectsOf(studentId));
        return schedule.getLessons().stream().filter(l -> own.contains(l.subject())).toList();
    }

    /**
     * Тестові набори: прізвище викладача точно, в іншому регістрі, з пробілами на краях,
     * викладач без занять. Заняття повертає макет {@code Schedule}.
     */
    @Override
    public List<Lesson> getTeacherSchedule(String teacher) {
        String key = Texts.key(teacher);
        return schedule.getLessons().stream()
                .filter(l -> Texts.key(l.subject().getTeacher()).equals(key))
                .toList();
    }

    /**
     * Тестові набори: граничні оцінки 0, 1, 99, 100, оцінки поза межами −1, 101,
     * {@code Integer.MIN_VALUE}, {@code Integer.MAX_VALUE}, предмет, на який студент не записаний.
     */
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

    /**
     * Тестові набори: студент без оцінок, кілька оцінок з одного предмета, неіснуючий студент,
     * спроба змінити повернений словник.
     */
    @Override
    public Map<Subject, List<Integer>> getGrades(String studentId) {
        requireStudent(studentId);
        Map<Subject, List<Integer>> copy = new LinkedHashMap<>();
        grades.getOrDefault(Texts.key(studentId), Map.of())
                .forEach((subject, values) -> copy.put(subject, List.copyOf(values)));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Тестові набори: кілька оцінок – середнє з точністю 0,001, предмет без оцінок – порожній результат,
     * неіснуючий предмет.
     */
    @Override
    public OptionalDouble getAverageGrade(String studentId, String subjectName) {
        Subject subject = requireSubject(subjectName);
        return getGrades(studentId).getOrDefault(subject, List.of()).stream()
                .mapToInt(Integer::intValue)
                .average();
    }

    /**
     * Тестові набори: існуючий номер у різному регістрі і неіснуючий номер – перевіряється
     * через публічні методи, що приймають номер студента.
     */
    private Student requireStudent(String id) {
        return findStudent(id).orElseThrow(() ->
                new EntityNotFoundException("Студента з номером \"" + id + "\" не знайдено"));
    }

    /**
     * Тестові набори: існуюча назва в різному регістрі і неіснуюча назва – перевіряється
     * через публічні методи, що приймають назву предмета.
     */
    private Subject requireSubject(String name) {
        return findSubject(name).orElseThrow(() ->
                new EntityNotFoundException("Предмет \"" + name + "\" не знайдено"));
    }
}
