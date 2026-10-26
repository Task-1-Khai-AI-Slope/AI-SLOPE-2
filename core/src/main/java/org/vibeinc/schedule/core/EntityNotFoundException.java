package org.vibeinc.schedule.core;

/** Потрібну сутність не знайдено: предмет, студента, заняття або запис на предмет. */
public class EntityNotFoundException extends ScheduleException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
