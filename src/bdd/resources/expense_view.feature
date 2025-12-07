@ExpenseView
Feature: Expense Application Frame Specification of the behavior of the Expense Application Frame
	@ShowExpenses
	Scenario: The initial state of the Expense view
	Given The database contains the expense with the following values
		| Date | Amount | Description | Category |
		| 2024-01-15 | 100.50 | Lunch | Food |
		| 2024-01-16 | 200.00 | Dinner | Food |
	When The Main Window is shown
	Then The expense table contains an element with the following values
		| ID | Date | Amount | Description | Category |
		| 1 | 2024-01-15 | 100.50 | Lunch | Food |
		| 2 | 2024-01-16 | 200.00 | Dinner | Food |
	@AddNewExpense
	Scenario: Add a new Expense
	Given The database contains the category with the following values
		| Name |
		| Food |
	When The Main Window is shown
	When The user clicks the "Add Expense" button
	Then The user enters the following values in the expense dialog
		| Date | Amount | Description | Category |
		| 2024-01-17 | 50.00 | Breakfast | Food |
	When The user clicks the dialog "Save" button
	Then The expense table contains an element with the following values
		| ID | Date | Amount | Description | Category |
		| 1 | 2024-01-17 | 50.00 | Breakfast | Food |
	@DeleteExpense
	Scenario: Delete an existing expense
	Given The database contains the expense with the following values
		| Date | Amount | Description | Category |
		| 2024-01-15 | 100.50 | Lunch | Food |
		| 2024-01-16 | 200.00 | Dinner | Food |
	When The Main Window is shown
	Then The user selects expense from the table
		| Lunch |
	When The user clicks the "Delete Expense" button
	When The user confirms deletion
	Then The expense table does not contain an element with the following values
		| ID | Date | Amount | Description | Category |
		| 1 | 2024-01-15 | 100.50 | Lunch | Food |

