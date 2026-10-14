package expensetracker

/** Expense categories shown in the menu. */
enum class Category(val label: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    RENT("Rent"),
    UTILITIES("Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    OTHER("Other");

    companion object {
        /**
         * Accepts a menu number (1-based) or a category name in any case.
         * Returns null for unknown input instead of throwing.
         */
        fun fromInput(input: String): Category? {
            val text = input.trim()
            text.toIntOrNull()?.let { number -> return entries.getOrNull(number - 1) }
            return entries.firstOrNull { it.name.equals(text, ignoreCase = true) || it.label.equals(text, ignoreCase = true) }
        }
    }
}
