package org.vibeinc.schedule.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vibeinc.schedule.app.DefaultStudent;
import org.vibeinc.schedule.core.Student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultStudentTest {

    @Test
    @DisplayName("TC-34 Пробіли на краях номера, ПІБ і групи прибираються")
    void stripsText() {
        Student student = new DefaultStudent(" KB-001 ", "  Козир Віталій Андрійович", "658п  ");
        assertEquals("KB-001", student.getId());
        assertEquals("Козир Віталій Андрійович", student.getFullName());
        assertEquals("658п", student.getGroup());
    }

    @Test
    @DisplayName("TC-35 Студента з порожнім номером, ПІБ або групою створити не можна")
    void rejectsBlankFields() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultStudent("", "Козир В. А.", "658п"));
        assertThrows(IllegalArgumentException.class, () -> new DefaultStudent("KB-001", null, "658п"));
        assertThrows(IllegalArgumentException.class, () -> new DefaultStudent("KB-001", "Козир В. А.", " "));
    }

    @Test
    @DisplayName("TC-36 Студенти рівні, якщо збігаються номери залікових книжок без урахування регістру")
    void equalityById() {
        Student first = new DefaultStudent("KB-001", "Козир В. А.", "658п");
        Student sameId = new DefaultStudent("kb-001", "Інше ПІБ", "659п");
        assertEquals(first, sameId);
        assertEquals(first.hashCode(), sameId.hashCode());
        assertNotEquals(first, new DefaultStudent("KB-002", "Козир В. А.", "658п"));
        assertEquals("KB-001 — Козир В. А., гр. 658п", first.toString());
    }
}
