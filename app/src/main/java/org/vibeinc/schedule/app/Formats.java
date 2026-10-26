package org.vibeinc.schedule.app;

import org.vibeinc.schedule.core.Lesson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Формати дат і рядкове подання занять для консолі. */
public final class Formats {
    /** Формат введення й виводу дати та часу, наприклад 2026-10-28 10:25. */
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale UK = Locale.forLanguageTag("uk");

    private Formats() {
    }

    public static String dateTime(LocalDateTime value) {
        return DATE_TIME.format(value);
    }

    /** Наприклад: "2026-10-28 ср 10:25–12:00 | Git-технологія | Лекція | ауд. 301 | Кузнецова Ю. А.". */
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
