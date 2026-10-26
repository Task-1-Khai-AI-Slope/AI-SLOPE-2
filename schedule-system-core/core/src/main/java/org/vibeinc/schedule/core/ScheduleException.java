package org.vibeinc.schedule.core;

/**
 * Базовий виняток предметної області. Інтерфейс користувача перехоплює
 * його й показує повідомлення, не завершуючи програму — ФВ-7.
 */
public class ScheduleException extends RuntimeException {
    public ScheduleException(String message) {
        super(message);
    }
}
