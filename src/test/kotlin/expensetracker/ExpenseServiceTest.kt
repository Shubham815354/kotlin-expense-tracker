package expensetracker

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ExpenseServiceTest {

    private val service = ExpenseService(InMemoryExpenseRepository())

    @Test
    fun `adding a valid expense stores it with a trimmed title`() {
        val result = service.add("  Lunch  ", BigDecimal("180"), Category.FOOD)
        val added = assertIs<ExpenseResult.Success<Expense>>(result).value
        assertEquals("Lunch", added.title)
        assertEquals(listOf(added), service.list())
    }

    @Test
    fun `empty title is rejected`() {
        val result = service.add("   ", BigDecimal("10"), Category.OTHER)
        assertEquals(ExpenseError.EmptyTitle, assertIs<ExpenseResult.Failure>(result).error)
        assertTrue(service.list().isEmpty())
    }

    @Test
    fun `negative amount is rejected`() {
        val result = service.add("Refund?", BigDecimal("-5"), Category.OTHER)
        assertIs<ExpenseError.NegativeAmount>(assertIs<ExpenseResult.Failure>(result).error)
        assertTrue(service.list().isEmpty())
    }

    // Regression test for #2: amounts with more than 2 decimals made the listed items
    // (rounded for display) not add up to the displayed total.
    @Test
    fun `amounts with more than two decimals are rejected`() {
        val result = service.add("Coffee", BigDecimal("10.555"), Category.FOOD)
        assertIs<ExpenseError.TooManyDecimals>(assertIs<ExpenseResult.Failure>(result).error)
        assertTrue(service.list().isEmpty())
    }

    @Test
    fun `amounts with up to two decimals or trailing zeros are accepted`() {
        assertIs<ExpenseResult.Success<Expense>>(service.add("Tea", BigDecimal("10.5"), Category.FOOD))
        assertIs<ExpenseResult.Success<Expense>>(service.add("Juice", BigDecimal("10.55"), Category.FOOD))
        assertIs<ExpenseResult.Success<Expense>>(service.add("Water", BigDecimal("10.500"), Category.FOOD))
    }

    @Test
    fun `zero amount is allowed`() {
        assertIs<ExpenseResult.Success<Expense>>(service.add("Free sample", BigDecimal.ZERO, Category.FOOD))
    }

    @Test
    fun `console input is parsed into an expense`() {
        val result = service.addFromInput("Bus ticket", "45.50", "transport")
        val added = assertIs<ExpenseResult.Success<Expense>>(result).value
        assertEquals(BigDecimal("45.50"), added.amount)
        assertEquals(Category.TRANSPORT, added.category)
    }

    @Test
    fun `invalid amount text and unknown category are reported`() {
        assertIs<ExpenseError.InvalidAmount>(assertIs<ExpenseResult.Failure>(service.addFromInput("Tea", "abc", "1")).error)
        assertIs<ExpenseError.UnknownCategory>(assertIs<ExpenseResult.Failure>(service.addFromInput("Tea", "20", "groceries")).error)
    }

    @Test
    fun `deleting removes only the chosen expense`() {
        service.add("Rent", BigDecimal("12000"), Category.RENT)
        service.add("Movie", BigDecimal("300"), Category.ENTERTAINMENT)
        val deleted = assertIs<ExpenseResult.Success<Expense>>(service.delete(1)).value
        assertEquals("Rent", deleted.title)
        assertEquals(listOf("Movie"), service.list().map { it.title })
    }

    @Test
    fun `deleting a missing id fails without changing the list`() {
        service.add("Rent", BigDecimal("12000"), Category.RENT)
        assertEquals(ExpenseError.NotFound(99), assertIs<ExpenseResult.Failure>(service.delete(99)).error)
        assertEquals(1, service.list().size)
    }

    @Test
    fun `ids are not reused after deletion`() {
        service.add("A", BigDecimal("1"), Category.OTHER)
        service.delete(1)
        val second = assertIs<ExpenseResult.Success<Expense>>(service.add("B", BigDecimal("2"), Category.OTHER)).value
        assertEquals(2, second.id)
    }

    @Test
    fun `service totals reflect added and deleted expenses`() {
        service.add("Groceries", BigDecimal("500"), Category.FOOD)
        service.add("Snacks", BigDecimal("50"), Category.FOOD)
        service.add("Metro", BigDecimal("60"), Category.TRANSPORT)
        service.delete(2)
        assertEquals(mapOf(Category.FOOD to BigDecimal("500"), Category.TRANSPORT to BigDecimal("60")), service.totalsByCategory())
        assertEquals(BigDecimal("560"), service.total())
    }
}
