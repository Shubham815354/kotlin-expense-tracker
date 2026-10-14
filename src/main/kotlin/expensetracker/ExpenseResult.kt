package expensetracker

import java.math.BigDecimal

/**
 * Outcome of an operation that can fail validation.
 * Callers handle both branches with an exhaustive `when` instead of catching exceptions.
 */
sealed interface ExpenseResult<out T> {
    data class Success<T>(val value: T) : ExpenseResult<T>
    data class Failure(val error: ExpenseError) : ExpenseResult<Nothing>
}

/** Every reason an operation can be rejected, each with a user-facing message. */
sealed class ExpenseError(val message: String) {
    data object EmptyTitle : ExpenseError("Title must not be empty.")
    data class NegativeAmount(val amount: BigDecimal) : ExpenseError("Amount must not be negative (got $amount).")
    data class TooManyDecimals(val amount: BigDecimal) : ExpenseError("Use at most 2 decimal places (got $amount).")
    data class InvalidAmount(val input: String) : ExpenseError("\"$input\" is not a valid amount.")
    data class UnknownCategory(val input: String) : ExpenseError("\"$input\" is not a known category.")
    data class NotFound(val id: Int) : ExpenseError("No expense with id $id.")
}
