package expensetracker

import java.math.BigDecimal

/** Pure calculation functions: no I/O, easy to unit-test. */
object ExpenseCalculator {

    /** Sum of all amounts; zero for an empty list. */
    fun total(expenses: List<Expense>): BigDecimal =
        expenses.fold(BigDecimal.ZERO) { sum, expense -> sum + expense.amount }

    /** Totals per category using groupBy. Categories without expenses are not included. */
    fun totalsByCategory(expenses: List<Expense>): Map<Category, BigDecimal> =
        expenses
            .groupBy { it.category }
            .mapValues { (_, inCategory) -> total(inCategory) }

    /** The category with the highest total, or null when there are no expenses. */
    fun topCategory(expenses: List<Expense>): Category? =
        totalsByCategory(expenses).maxByOrNull { it.value }?.key
}
