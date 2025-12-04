# Personal Expense Tracker

A comprehensive Personal Expense Tracker application developed following Test-Driven Development (TDD) principles, with full build automation and continuous integration.

## Project Overview

This application provides a simple yet complete solution for managing personal expenses with the following features:

- **Add, edit, and delete expenses**
- **Manage expense categories** (e.g., Food, Travel, Bills)
- **Monthly expense summary**
- **Total spending per category**
- **Simple and clear Swing interface** (form and table views)
- **Persistent data storage using MongoDB**

## Technologies & Tools

- **Java 17**
- **MongoDB** - Managed through Docker/Testcontainers for reproducible integration testing
- **Maven** - Build automation
- **JUnit 4** - Unit testing
- **Mockito** - Mocking framework
- **Testcontainers** - Integration testing with Docker
- **PIT** - Mutation testing
- **JaCoCo** - Code coverage analysis
- **Coveralls** - Coverage reporting
- **SonarCloud** - Code quality analysis
- **GitHub Actions** - Continuous integration
- **Swing** - Graphical user interface

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MongoDB 4.11 (or Docker for Testcontainers)
- Eclipse IDE (recommended)


### Directory Structure

```
personal-expense-tracker/
├── pom.xml                          # Maven Project Object Model
├── README.md                         # Project documentation
├── .gitignore                        # Git ignore rules
├── src/
│   ├── main/
│   │   ├── java/                     # ✅ Main source code
│   │   │   └── com/
│   │   │       └── mycompany/
│   │   │           └── pet/
│   │   │               ├── model/    # Domain models
│   │   │               └── dao/      # Data Access Objects (empty, for future)
│   │   └── resources/                # ✅ Main resources (config files, etc.)
│   └── test/
│       ├── java/                     # ✅ Test source code
│       │   └── com/
│       │       └── mycompany/
│       │           └── pet/
│       └── resources/                # ✅ Test resources
└── target/                           # Build output (ignored by git)
```

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd pem
```

## Features

### Expense Management

- Create new expenses with date, amount, description, and category
- Edit existing expenses
- Delete expenses
- View all expenses in a table
- Filter expenses by month and year

### Category Management

- Create expense categories
- Edit category names
- Delete categories (with cascade delete of associated expenses)
- View all categories

### Reporting

- Monthly expense summary
- Total spending per category
- Filter expenses by month/year

## License

This project is developed for educational purposes as part of a course on Test-Driven Development, Build Automation, and Continuous Integration with Java.

## Contributing

This is an educational project. Contributions and suggestions are welcome!

## Author

Abdul Kader
