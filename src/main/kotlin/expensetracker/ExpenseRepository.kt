package expensetracker

/** Storage abstraction so the service can be tested and later backed by a file or database. */
interface ExpenseRepository {
    fun add(title: String, amount: java.math.BigDecimal, category: Category): Expense
    fun all(): List<Expense>
    fun remove(id: Int): Expense?
}

/** Keeps expenses in memory for the lifetime of the program. */
class InMemoryExpenseRepository : ExpenseRepository {
    private val expenses = mutableListOf<Expense>()
    private var nextId = 1

    override fun add(title: String, amount: java.math.BigDecimal, category: Category): Expense {
        val expense = Expense(nextId++, title, amount, category)
        expenses += expense
        return expense
    }

    override fun all(): List<Expense> = expenses.toList()

    override fun remove(id: Int): Expense? {
        val expense = expenses.firstOrNull { it.id == id } ?: return null
        expenses.remove(expense)
        return expense
    }
}
