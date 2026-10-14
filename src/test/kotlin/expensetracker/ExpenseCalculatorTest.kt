package expensetracker

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExpenseCalculatorTest {

    private fun expense(id: Int, amount: String, category: Category) =
        Expense(id, "Item $id", BigDecimal(amount), category)

    @Test
    fun `total of an empty list is zero`() {
        assertEquals(BigDecimal.ZERO, ExpenseCalculator.total(emptyList()))
    }

    @Test
    fun `total adds all amounts exactly`() {
        val expenses = listOf(
            expense(1, "0.10", Category.FOOD),
            expense(2, "0.20", Category.FOOD),
            expense(3, "100.00", Category.RENT),
        )
        assertEquals(BigDecimal("100.30"), ExpenseCalculator.total(expenses))
    }

    @Test
    fun `totals are grouped per category`() {
        val expenses = listOf(
            expense(1, "250", Category.FOOD),
            expense(2, "120.50", Category.TRANSPORT),
            expense(3, "80", Category.FOOD),
        )
        val totals = ExpenseCalculator.totalsByCategory(expenses)
        assertEquals(BigDecimal("330"), totals[Category.FOOD])
        assertEquals(BigDecimal("120.50"), totals[Category.TRANSPORT])
    }

    @Test
    fun `categories without expenses are not in the totals`() {
        val totals = ExpenseCalculator.totalsByCategory(listOf(expense(1, "10", Category.HEALTH)))
        assertEquals(setOf(Category.HEALTH), totals.keys)
    }

    @Test
    fun `totals of an empty list are empty`() {
        assertTrue(ExpenseCalculator.totalsByCategory(emptyList()).isEmpty())
    }

    @Test
    fun `category totals add up to the overall total`() {
        val expenses = listOf(
            expense(1, "15.75", Category.FOOD),
            expense(2, "40", Category.ENTERTAINMENT),
            expense(3, "9.25", Category.FOOD),
            expense(4, "0", Category.OTHER),
        )
        val sumOfCategories = ExpenseCalculator.totalsByCategory(expenses).values.fold(BigDecimal.ZERO, BigDecimal::add)
        assertEquals(ExpenseCalculator.total(expenses), sumOfCategories)
    }

    @Test
    fun `top category is the one with the highest total`() {
        val expenses = listOf(
            expense(1, "30", Category.FOOD),
            expense(2, "50", Category.TRANSPORT),
            expense(3, "25", Category.FOOD),
        )
        assertEquals(Category.FOOD, ExpenseCalculator.topCategory(expenses))
        assertNull(ExpenseCalculator.topCategory(emptyList()))
    }
}
