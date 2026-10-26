package org.vibeinc.schedule.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Загальний розклад занять — ФВ-4.
 * Усі методи, що повертають списки, повертають їх відсортованими за часом початку.
 */
public interface Schedule {

    /**
     * Додає заняття.
     *
     * @throws ScheduleConflictException якщо в цей час аудиторія або викладач зайняті
     */
    void addLesson(Lesson lesson);

    /**
     * Переносить заняття на новий час, зберігаючи предмет, вид, тривалість і аудиторію.
     *
     * @return заняття з новим часом
     * @throws EntityNotFoundException   якщо такого заняття немає
     * @throws ScheduleConflictException якщо новий час конфліктує з іншим заняттям
     */
    Lesson reschedule(Lesson lesson, LocalDateTime newStart);

    /**
     * Скасовує заняття.
     *
     * @throws EntityNotFoundException якщо такого заняття немає
     */
    void cancel(Lesson lesson);

    /** Усі заняття. */
    List<Lesson> getLessons();

    /** Заняття за вказану дату. */
    List<Lesson> getLessonsOn(LocalDate date);

    /** Заняття з вказаного предмета. */
    List<Lesson> getLessonsOf(Subject subject);
}
