package org.vibeinc.schedule.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public record Lesson(Subject subject, LessonType type, LocalDateTime start,
                     Duration duration, String room) {

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

    public LocalDateTime end() {
        return start.plus(duration);
    }

    public boolean overlaps(Lesson other) {
        return start.isBefore(other.end()) && other.start.isBefore(end());
    }

    public Lesson withStart(LocalDateTime newStart) {
        return new Lesson(subject, type, newStart, duration, room);
    }
}
