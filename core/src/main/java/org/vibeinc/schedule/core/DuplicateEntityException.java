package org.vibeinc.schedule.core;

/** Спроба додати сутність, яка вже є: предмет, студента, повторний запис. */
public class DuplicateEntityException extends ScheduleException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
