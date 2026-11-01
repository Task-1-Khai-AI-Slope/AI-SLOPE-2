package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.Lesson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public final class Formats {
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale UK = Locale.forLanguageTag("uk");

    private Formats() {
    }

    public static String dateTime(LocalDateTime value) {
        return DATE_TIME.format(value);
    }

    public static String date(LocalDate value) {
        return DATE.format(value) + ", " + value.getDayOfWeek().getDisplayName(TextStyle.SHORT, UK);
    }

    public static String lesson(Lesson l) {
        String day = l.start().getDayOfWeek().getDisplayName(TextStyle.SHORT, UK);
        return l.start().toLocalDate() + " " + day + " "
                + TIME.format(l.start()) + "–" + TIME.format(l.end())
                + " | " + l.subject().getName()
                + " | " + l.type().getTitle()
                + " | ауд. " + l.room()
                + " | " + l.subject().getTeacher();
    }
}
