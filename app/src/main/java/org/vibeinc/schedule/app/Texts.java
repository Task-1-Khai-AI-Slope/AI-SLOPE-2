package org.vibeinc.schedule.app;

import java.util.Locale;

final class Texts {

    private Texts() {
    }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле \"" + field + "\" не може бути порожнім");
        }
        return value.strip();
    }

    static String key(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
