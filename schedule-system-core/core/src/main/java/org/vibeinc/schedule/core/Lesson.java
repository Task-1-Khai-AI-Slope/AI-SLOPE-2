package org.vibeinc.schedule.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Одне заняття в розкладі. Незмінний об'єкт-значення: щоб перенести
 * заняття, створюється нова копія через {@link #withStart(LocalDateTime)}.
 *
 * @param subject  предмет
 * @param type     вид заняття
 * @param start    дата й час початку
 * @param duration тривалість, більше нуля
 * @param room     аудиторія, наприклад "301 ГАК" або "online"
 */
public record Lesson(Subject subject, LessonType type, LocalDateTime start,
                     Duration duration, String room) {

    /** Стандартна тривалість пари — 1 год 35 хв. */
    public static final Duration DEFAULT_DURATION = Duration.ofMinutes(95);

    public Lesson {
        Objects.requireNonNull(subject, "Предмет не задано");
        Objects.requireNonNull(type, "Вид заняття не задано");
        Objects.requireNonNull(start, "Час початку не задано");
        Objects.requireNonNull(duration, "Тривалість не задано");
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Тривалість заняття має бути більшою за нуль");
        }
        if (room == null || room.isBlank()) {
            throw new IllegalArgumentException("Аудиторію не задано");
        }
        room = room.strip();
    }

    /** Час завершення заняття. */
    public LocalDateTime end() {
        return start.plus(duration);
    }

    /**
     * Чи перетинаються два заняття в часі. Заняття, що закінчується
     * рівно в момент початку іншого, перетином не вважається.
     */
    public boolean overlaps(Lesson other) {
        return start.isBefore(other.end()) && other.start.isBefore(end());
    }

    /** Копія заняття з іншим часом початку. */
    public Lesson withStart(LocalDateTime newStart) {
        return new Lesson(subject, type, newStart, duration, room);
    }
}
