package org.vibeinc.schedule.core;

/**
 * Студент. Ідентифікується номером залікової книжки — ФВ-2.1.
 */
public interface Student {

    /** Номер залікової книжки, унікальний у системі. */
    String getId();

    /** Прізвище, ім'я та по батькові. */
    String getFullName();

    /** Академічна група, наприклад "658п". */
    String getGroup();
}
