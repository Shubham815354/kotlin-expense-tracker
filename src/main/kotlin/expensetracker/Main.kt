package expensetracker

import java.math.BigDecimal
import java.math.RoundingMode

/** Console interface. All logic lives in ExpenseService; this file only reads and prints. */
class ConsoleApp(
    private val service: ExpenseService = ExpenseService(),
    private val readLine: () -> String? = ::readlnOrNull,
    private val print: (String) -> Unit = ::println,
) {
    fun run() {
        print("Expense Tracker - type a number and press Enter.")
        while (true) {
            print(MENU)
            when (prompt("Choose")?.trim()) {
                "1" -> addExpense()
                "2" -> listExpenses()
                "3" -> deleteExpense()
                "4" -> showTotals()
                "5", null -> {
                    print("Goodbye!")
                    return
                }
                else -> print("Please choose 1-5.")
            }
        }
    }

    private fun prompt(label: String): String? {
        kotlin.io.print("$label: ")
        return readLine()
    }

    private fun addExpense() {
        val title = prompt("Title") ?: return
        val amount = prompt("Amount") ?: return
        val categories = Category.entries.mapIndexed { i, c -> "${i + 1}. ${c.label}" }.joinToString("  ")
        print(categories)
        val category = prompt("Category (number or name)") ?: return
        when (val result = service.addFromInput(title, amount, category)) {
            is ExpenseResult.Success -> print("Added #${result.value.id}: ${describe(result.value)}")
            is ExpenseResult.Failure -> print("Not added: ${result.error.message}")
        }
    }

    private fun listExpenses() {
        val expenses = service.list()
        if (expenses.isEmpty()) {
            print("No expenses yet.")
            return
        }
        expenses.forEach { print("#${it.id}  ${describe(it)}") }
    }

    private fun deleteExpense() {
        val id = prompt("Id to delete")?.trim()?.toIntOrNull()
        if (id == null) {
            print("Please enter a numeric id.")
            return
        }
        when (val result = service.delete(id)) {
            is ExpenseResult.Success -> print("Deleted #${result.value.id}.")
            is ExpenseResult.Failure -> print(result.error.message)
        }
    }

    private fun showTotals() {
        val totals = service.totalsByCategory()
        if (totals.isEmpty()) {
            print("No expenses yet.")
            return
        }
        totals.entries
            .sortedByDescending { it.value }
            .forEach { (category, amount) -> print("${category.label.padEnd(14)} ${money(amount)}") }
        print("${"Total".padEnd(14)} ${money(service.total())}")
    }

    private fun describe(e: Expense) = "${e.title} - ${money(e.amount)} (${e.category.label})"

    private fun money(amount: BigDecimal) = amount.setScale(2, RoundingMode.HALF_UP).toPlainString()

    private companion object {
        val MENU = """
            |
            |1. Add expense
            |2. List expenses
            |3. Delete expense
            |4. Totals by category
            |5. Exit
        """.trimMargin()
    }
}

fun main() {
    ConsoleApp().run()
}
