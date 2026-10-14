package expensetracker

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CategoryTest {

    @Test
    fun `category is found by menu number or name in any case`() {
        assertEquals(Category.FOOD, Category.fromInput("1"))
        assertEquals(Category.HEALTH, Category.fromInput(" health "))
        assertEquals(Category.ENTERTAINMENT, Category.fromInput("ENTERTAINMENT"))
    }

    @Test
    fun `unknown or out-of-range input returns null`() {
        assertNull(Category.fromInput("0"))
        assertNull(Category.fromInput("99"))
        assertNull(Category.fromInput("shopping"))
    }
}
