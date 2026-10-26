package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.EntityNotFoundException;
import org.vibeinc.schedule.core.Lesson;
import org.vibeinc.schedule.core.Schedule;
import org.vibeinc.schedule.core.ScheduleConflictException;
import org.vibeinc.schedule.core.Subject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Розклад у пам'яті з перевіркою конфліктів за аудиторією та викладачем. */
public final class DefaultSchedule implements Schedule {
    private static final Comparator<Lesson> BY_START = Comparator.comparing(Lesson::start);

    private final List<Lesson> lessons = new ArrayList<>();

    @Override
    public void addLesson(Lesson lesson) {
        Objects.requireNonNull(lesson, "Заняття не задано");
        checkConflicts(lesson, lessons);
        lessons.add(lesson);
    }

    @Override
    public Lesson reschedule(Lesson lesson, LocalDateTime newStart) {
        Objects.requireNonNull(newStart, "Новий час не задано");
        int index = indexOf(lesson);
        Lesson moved = lesson.withStart(newStart);
        // Перевіряємо конфлікти з усіма заняттями, крім того, що переносимо.
        List<Lesson> others = new ArrayList<>(lessons);
        others.remove(index);
        checkConflicts(moved, others);
        lessons.set(index, moved);
        return moved;
    }

    @Override
    public void cancel(Lesson lesson) {
        lessons.remove(indexOf(lesson));
    }

    @Override
    public List<Lesson> getLessons() {
        return select(l -> true);
    }

    @Override
    public List<Lesson> getLessonsOn(LocalDate date) {
        Objects.requireNonNull(date, "Дату не задано");
        return select(l -> l.start().toLocalDate().equals(date));
    }

    @Override
    public List<Lesson> getLessonsOf(Subject subject) {
        Objects.requireNonNull(subject, "Предмет не задано");
        return select(l -> l.subject().equals(subject));
    }

    private List<Lesson> select(Predicate<Lesson> filter) {
        return lessons.stream().filter(filter).sorted(BY_START).toList();
    }

    private int indexOf(Lesson lesson) {
        Objects.requireNonNull(lesson, "Заняття не задано");
        int index = lessons.indexOf(lesson);
        if (index < 0) {
            throw new EntityNotFoundException("Заняття не знайдено в розкладі");
        }
        return index;
    }

    /**
     * Заняття конфліктує з іншим, якщо вони перетинаються в часі й при цьому
     * збігається аудиторія або викладач.
     */
    private static void checkConflicts(Lesson candidate, List<Lesson> existing) {
        for (Lesson other : existing) {
            if (!candidate.overlaps(other)) {
                continue;
            }
            if (Texts.key(candidate.room()).equals(Texts.key(other.room()))) {
                throw new ScheduleConflictException("Аудиторія " + other.room()
                        + " зайнята: " + other.subject().getName() + " о " + Formats.dateTime(other.start()));
            }
            if (Texts.key(candidate.subject().getTeacher()).equals(Texts.key(other.subject().getTeacher()))) {
                throw new ScheduleConflictException("Викладач " + other.subject().getTeacher()
                        + " зайнятий: " + other.subject().getName() + " о " + Formats.dateTime(other.start()));
            }
        }
    }
}
