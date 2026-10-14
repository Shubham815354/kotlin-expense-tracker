package expensetracker

import java.math.BigDecimal

/** Validates input and coordinates the repository and calculations. */
class ExpenseService(private val repository: ExpenseRepository = InMemoryExpenseRepository()) {

    fun add(title: String, amount: BigDecimal, category: Category): ExpenseResult<Expense> {
        val cleanTitle = title.trim()
        return when {
            cleanTitle.isEmpty() -> ExpenseResult.Failure(ExpenseError.EmptyTitle)
            amount < BigDecimal.ZERO -> ExpenseResult.Failure(ExpenseError.NegativeAmount(amount))
            else -> ExpenseResult.Success(repository.add(cleanTitle, amount, category))
        }
    }

    /** Parses raw text from the console, then validates it like [add]. */
    fun addFromInput(title: String, amountText: String, categoryText: String): ExpenseResult<Expense> {
        val amount = amountText.trim().toBigDecimalOrNull()
            ?: return ExpenseResult.Failure(ExpenseError.InvalidAmount(amountText))
        val category = Category.fromInput(categoryText)
            ?: return ExpenseResult.Failure(ExpenseError.UnknownCategory(categoryText))
        return add(title, amount, category)
    }

    fun list(): List<Expense> = repository.all()

    fun delete(id: Int): ExpenseResult<Expense> =
        repository.remove(id)?.let { ExpenseResult.Success(it) }
            ?: ExpenseResult.Failure(ExpenseError.NotFound(id))

    fun totalsByCategory(): Map<Category, BigDecimal> = ExpenseCalculator.totalsByCategory(list())

    fun total(): BigDecimal = ExpenseCalculator.total(list())
}
