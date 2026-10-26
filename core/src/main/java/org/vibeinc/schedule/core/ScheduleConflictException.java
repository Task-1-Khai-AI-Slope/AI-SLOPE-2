package org.vibeinc.schedule.core;

/** Заняття перетинається в часі з іншим у тій самій аудиторії або в того самого викладача. */
public class ScheduleConflictException extends ScheduleException {
    public ScheduleConflictException(String message) {
        super(message);
    }
}
