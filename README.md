# Kotlin Expense Tracker (console)

A small command-line app for tracking expenses in memory: add, list and delete expenses, and see totals per category.

## Requirements

- JDK 17 or newer (`java -version` to check)
- No Gradle installation needed: the project includes the Gradle wrapper

## Run

```bash
# macOS / Linux
./gradlew run --console=plain -q

# Windows
gradlew.bat run --console=plain -q
```

Example session:

```
Expense Tracker - type a number and press Enter.

1. Add expense
2. List expenses
3. Delete expense
4. Totals by category
5. Exit
Choose: 1
Title: Groceries
Amount: 850.50
1. Food  2. Transport  3. Rent  4. Utilities  5. Entertainment  6. Health  7. Other
Category (number or name): 1
Added #1: Groceries - 850.50 (Food)
```

Invalid input is rejected with a message, and nothing is saved:

```
Not added: Title must not be empty.
Not added: Amount must not be negative (got -20).
Not added: "abc" is not a valid amount.
```

## Test

```bash
./gradlew test        # Windows: gradlew.bat test
```

19 JUnit 5 tests cover:

| Area | Tests |
|---|---|
| Totals | Empty list, exact decimal sums, totals per category, categories with no expenses, category totals add up to the overall total, top category |
| Validation | Empty title, negative amount, zero amount allowed, invalid amount text, unknown category |
| Repository and service | Add, delete, deleting a missing id, ids not reused after deletion, totals after delete |
| Category input | Category accepted by menu number or by name |

## Design

```
src/main/kotlin/expensetracker/
├── Category.kt            enum of categories + parsing from menu input
├── Expense.kt             data class (amount is BigDecimal)
├── ExpenseResult.kt       sealed Result type (Success / Failure) + sealed ExpenseError
├── ExpenseRepository.kt   storage interface + in-memory implementation
├── ExpenseCalculator.kt   pure calculations: total, totalsByCategory (groupBy), topCategory
├── ExpenseService.kt      validation + coordination
└── Main.kt                console UI only (reads input, prints results)
```

- **Validation without exceptions.** Operations that can fail return `ExpenseResult.Success` or `ExpenseResult.Failure(ExpenseError)`. The UI handles both with an exhaustive `when`, so every error case must be handled.
- **Money uses `BigDecimal`**, so `0.10 + 0.20` is exactly `0.30`.
- **Layers are separated.** Calculations are pure functions, and the service depends on a repository interface, so logic is tested without the console. Storage could later move to a file or database without changing the service.
- **No `!!`.** Nullable values are handled with `?.`, `?:` and `let`.

## Known limitations

- Data is kept in memory only and is lost when the program exits.
- Amounts are not tied to a currency.
