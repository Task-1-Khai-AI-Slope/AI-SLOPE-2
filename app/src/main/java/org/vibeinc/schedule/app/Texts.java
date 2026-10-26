package org.vibeinc.schedule.app;

import java.util.Locale;

/** Допоміжні перевірки текстових полів. */
final class Texts {

    private Texts() {
    }

    /**
     * Перевіряє, що текст не порожній, і прибирає пробіли на краях.
     *
     * @param field назва поля для повідомлення про помилку
     */
    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле \"" + field + "\" не може бути порожнім");
        }
        return value.strip();
    }

    /** Ключ для пошуку без урахування регістру й пробілів на краях. */
    static String key(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
