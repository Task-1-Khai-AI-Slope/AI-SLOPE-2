package org.vibeinc.schedule.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface Schedule {

    void addLesson(Lesson lesson);

    Lesson reschedule(Lesson lesson, LocalDateTime newStart);

    void cancel(Lesson lesson);

    List<Lesson> getLessons();

    List<Lesson> getLessonsOn(LocalDate date);

    List<Lesson> getLessonsOf(Subject subject);
}
