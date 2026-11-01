package org.vibeinc.schedule.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public interface ScheduleManagementSystem {

    Subject addSubject(String name, String teacher, int credits);

    Optional<Subject> findSubject(String name);

    List<Subject> getSubjects();

    Student registerStudent(String id, String fullName, String group);

    Optional<Student> findStudent(String id);

    List<Student> getStudents();

    void enroll(String studentId, String subjectName);

    List<Subject> getSubjectsOf(String studentId);

    Lesson addLesson(String subjectName, LessonType type, LocalDateTime start, String room);

    Schedule getSchedule();

    List<Lesson> getStudentSchedule(String studentId);

    List<Lesson> getTeacherSchedule(String teacher);

    void addGrade(String studentId, String subjectName, int grade);

    Map<Subject, List<Integer>> getGrades(String studentId);

    OptionalDouble getAverageGrade(String studentId, String subjectName);
}
