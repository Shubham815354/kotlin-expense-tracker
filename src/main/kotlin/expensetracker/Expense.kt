package expensetracker

import java.math.BigDecimal

/** A single expense. Money uses BigDecimal to avoid floating-point rounding errors. */
data class Expense(
    val id: Int,
    val title: String,
    val amount: BigDecimal,
    val category: Category,
)
