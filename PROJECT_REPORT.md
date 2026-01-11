# Personal Expense Tracker
## Project Report

**Author:** Abdul Kader  
**Course:** Test-Driven Development, Build Automation, and Continuous Integration  
**Date:** January 2026  
**Version:** 1.0.0

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Overview](#2-project-overview)
3. [Architecture & Design](#3-architecture--design)
4. [Technologies & Tools](#4-technologies--tools)
5. [Testing Strategy](#5-testing-strategy)
6. [Build Automation & CI/CD](#6-build-automation--cicd)
7. [Code Quality & Coverage](#7-code-quality--coverage)
8. [Conclusion](#8-conclusion)

---

## 1. Introduction

This report presents the **Personal Expense Tracker** application, a comprehensive desktop application developed following **Test-Driven Development (TDD)** principles with full build automation and continuous integration practices. The project demonstrates modern software engineering practices including dependency injection, comprehensive testing strategies, automated builds, and continuous integration pipelines.

The application provides a user-friendly graphical interface for managing personal expenses, allowing users to track their spending by categories, filter expenses by month and year, and generate summary reports. The project serves as a practical demonstration of professional Java development practices, emphasizing code quality, test coverage, and maintainability.

---

## 2. Project Overview

### 2.1 Application Description

The Personal Expense Tracker is a desktop application built with Java Swing that enables users to manage their personal finances efficiently. The application provides an intuitive graphical user interface for adding, editing, and deleting expenses, organizing them by categories, and analyzing spending patterns through monthly and category-based summaries.

### 2.2 Key Features

The application offers the following core functionalities:

**Expense Management:**
- Create new expenses with date, amount, description, and category
- Edit existing expense records
- Delete expenses with confirmation dialogs
- View all expenses in a sortable table format
- Filter expenses by month and year combinations
- Real-time updates to the expense table

**Category Management:**
- Create custom expense categories
- Edit category names
- Delete categories with cascade deletion of associated expenses
- View all categories in a dedicated management dialog
- Category selection dropdown in the main window

**Reporting & Analytics:**
- Monthly expense summary (total spending for selected month/year)
- Category-based expense totals
- Filter expenses by month/year combinations
- "All months" option for viewing complete expense history

### 2.3 User Interface

The application features a clean and intuitive Swing-based graphical interface. The main window displays an expense table with all expense records, filtering controls for month and year selection, and action buttons for managing expenses. The interface follows standard desktop application patterns for ease of use.

![Main Application Window](images/main-window.png)
*Figure 1: Main Application Window - Displaying expense records with filtering and management options*

![Expense Dialog](images/expense-dialog.png)
*Figure 2: Expense Entry Dialog - For adding and editing expense records*

![Category Management Dialog](images/category-dialog.png)
*Figure 3: Category Management Dialog - For managing expense categories*

### 2.4 Data Persistence

The application uses **MongoDB** (version 4.11) as the persistent storage solution. All expense and category data is stored in a MongoDB database named `expense_tracker`, ensuring data persistence across application sessions. The database connection is configured through dependency injection, allowing for easy testing and configuration management.

---

## 3. Architecture & Design

### 3.1 Architectural Pattern

The application follows a **layered architecture** pattern with clear separation of concerns, promoting maintainability, testability, and scalability. The architecture consists of four main layers:

```
┌─────────────────────────────────────┐
│         UI Layer (Swing)            │
│  MainWindow, ExpenseDialog, etc.   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Controller Layer               │
│  ExpenseController, CategoryController│
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Service Layer                  │
│  ExpenseService, CategoryService     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        DAO Layer                     │
│  ExpenseDAO, CategoryDAO            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Database Layer                  │
│  MongoDB (via MongoClient)           │
└─────────────────────────────────────┘
```

**UI Layer:** Handles all user interface components and user interactions. The UI layer is responsible for displaying data and capturing user input but delegates business logic to controllers.

**Controller Layer:** Mediates between the UI and service layers, handling asynchronous operations and coordinating between UI events and business logic execution.

**Service Layer:** Contains the business logic for expense and category management, including validation, calculations, and business rules enforcement.

**DAO Layer:** Provides data access abstraction, handling all database operations including CRUD (Create, Read, Update, Delete) operations.

**Database Layer:** MongoDB database storing all application data.

### 3.2 Dependency Injection

The application uses **Google Guice 7.0** for dependency injection, following the dependency injection pattern to achieve loose coupling between components. All dependencies are configured in the `ExpenseTrackerModule` class, which defines the bindings and provides instances of services, DAOs, and database connections.

Benefits of using dependency injection:
- **Loose Coupling:** Components depend on abstractions rather than concrete implementations
- **Testability:** Dependencies can be easily mocked for unit testing
- **Centralized Configuration:** All dependencies are configured in one place
- **Singleton Management:** Database connections and services are managed as singletons

### 3.3 Design Patterns

Several design patterns are employed throughout the application:

- **Dependency Injection Pattern:** Used throughout the application via Google Guice
- **DAO Pattern:** Data Access Objects abstract database operations
- **MVC Pattern:** Model-View-Controller separation in the UI and controller layers
- **Singleton Pattern:** Database connections and services are managed as singletons

### 3.4 Package Structure

The application is organized into well-defined packages following Java package naming conventions:

- `com.mycompany.pet.model` - Domain models (Expense, Category)
- `com.mycompany.pet.dao` - Data Access Objects
- `com.mycompany.pet.service` - Business logic services
- `com.mycompany.pet.controller` - Controllers (mediates UI and services)
- `com.mycompany.pet.ui` - Swing UI components
- `com.mycompany.pet.database` - Database connection management
- `com.mycompany.pet.di` - Dependency injection configuration (Guice)
- `com.mycompany.pet.util` - Utility classes
- `com.mycompany.pet.annotation` - Custom annotations

This structure promotes code organization, maintainability, and follows the Single Responsibility Principle.

---

## 4. Technologies & Tools

### 4.1 Core Technologies

**Java 17:** The application is developed using Java 17, leveraging modern Java features including lambda expressions, streams, and improved type inference.

**MongoDB 4.11:** A NoSQL document database used for persistent storage. MongoDB's flexible document model allows for easy schema evolution and efficient data storage.

**Swing:** Java's standard GUI toolkit for creating the desktop application interface. Swing provides a mature and cross-platform solution for desktop applications.

**Google Guice 7.0:** Dependency injection framework that simplifies dependency management and promotes loose coupling between components.

**Maven 3.6+:** Build automation and dependency management tool. Maven provides standardized project structure, dependency management, and plugin ecosystem.

### 4.2 Testing Frameworks

**JUnit 4.13.2 & JUnit 5.10.0:** Comprehensive testing framework supporting both unit and integration tests. JUnit 4 is used for most unit tests, while JUnit 5 is used for modern test features and integration tests.

**Mockito 4.11.0:** Mocking framework for creating mock objects in unit tests, enabling isolated testing of components.

**Testcontainers 1.19.3:** Integration testing framework that provides lightweight, throwaway instances of MongoDB for integration tests using Docker.

**AssertJ Swing:** UI testing framework for testing Swing applications, providing fluent assertions and robot-based UI interaction.

**Cucumber 5.5.0:** Behavior-Driven Development (BDD) framework for writing acceptance tests in natural language.

**Awaitility 4.2.0:** Utility library for testing asynchronous code, providing convenient methods for waiting for conditions.

### 4.3 Code Quality Tools

**JaCoCo 0.8.11:** Java Code Coverage tool that measures code coverage and generates detailed coverage reports. The project targets 100% coverage for UI and utility packages.

**PIT 1.15.0:** Mutation testing framework that verifies test quality by introducing mutations (bugs) into the code and checking if tests detect them.

**SonarCloud:** Cloud-based code quality analysis platform that performs static code analysis, identifies code smells, security vulnerabilities, and maintains quality gates.

**Coveralls:** Code coverage tracking and reporting service that tracks coverage trends over time.

### 4.4 Build & CI/CD

**Maven:** Build automation tool handling compilation, testing, packaging, and deployment.

**GitHub Actions:** Continuous Integration platform providing automated build, test, and deployment pipelines.

**Docker:** Containerization platform used by Testcontainers for running integration tests.

### 4.5 Logging

**Apache Log4j 2.23.1:** Modern logging framework providing high performance, flexible configuration, and multiple output destinations (console, file, rolling file appenders).

**SLF4J 1.7.36:** Simple Logging Facade for Java, providing a logging abstraction layer.

**Logback 1.2.12:** Logging implementation used by some dependencies.

---

## 5. Testing Strategy

### 5.1 Testing Philosophy

The project follows **Test-Driven Development (TDD)** methodology, where tests are written before implementation code. This approach ensures that:

- Requirements are clearly understood before implementation
- Code is written only to satisfy test requirements
- High test coverage is achieved naturally
- Refactoring is safe due to comprehensive test suite

### 5.2 Test Pyramid

The testing strategy follows the **Test Pyramid** approach:

```
        /\
       /  \      E2E Tests (Few)
      /____\
     /      \    Integration Tests (Some)
    /________\
   /          \  Unit Tests (Many)
  /____________\
```

**Unit Tests (Many):** The majority of tests are unit tests that test individual components in isolation with mocked dependencies. These tests are fast, reliable, and provide rapid feedback.

**Integration Tests (Some):** Integration tests verify that components work together correctly, often using real database instances via Testcontainers.

**End-to-End Tests (Few):** E2E tests verify complete user workflows through the UI, ensuring the entire application works correctly from the user's perspective.

### 5.3 Test Types

**Unit Tests:**
- Service layer tests with mocked DAOs
- DAO tests with in-memory MongoDB (Testcontainers)
- Controller tests with mocked services
- UI component tests with AssertJ Swing
- Model tests for domain object behavior

**Integration Tests:**
- Database integration tests using Testcontainers
- Service integration tests with real DAOs
- End-to-end tests through the UI

**BDD Tests:**
- Cucumber feature files describing application behavior
- Step definitions implementing BDD scenarios
- Acceptance criteria written in natural language

### 5.4 Test Execution

Tests are executed using Maven's Surefire plugin (for unit tests) and Failsafe plugin (for integration tests). The project includes multiple build profiles for different testing scenarios:

- **Default Profile:** Runs all unit tests
- **UI Tests Profile (`-Pui-tests`):** Runs unit tests including UI tests, typically used with `xvfb-run` for headless GUI testing
- **Integration Test Profile:** Runs integration tests via Failsafe plugin
- **Mutation Testing Profile:** Runs unit tests with PIT mutation testing

**Example Test Execution Commands:**

```bash
# Run all unit tests
mvn clean test

# Run UI tests with xvfb (for headless GUI testing on Linux)
xvfb-run mvn clean test -Pui-tests jacoco:report

# Run integration tests
mvn clean verify -Pintegration-test-profile

# Run mutation testing
mvn clean verify -Pmutation-testing-with-coverage
```

### 5.5 Test Coverage

The project maintains high test coverage standards:
- **Target:** 100% coverage for `com.mycompany.pet.ui` and `com.mycompany.pet.util` packages
- **Overall Coverage:** High coverage across all packages
- **Coverage Reports:** Generated using JaCoCo and available in HTML, XML, and CSV formats

Coverage reports are generated using the command:
```bash
mvn clean test jacoco:report
```

Reports can be viewed by opening `target/site/jacoco/index.html` in a web browser.

---

## 6. Build Automation & CI/CD

### 6.1 Build Automation with Maven

Maven provides comprehensive build automation for the project:

**Build Lifecycle:**
- **compile:** Compiles source code
- **test:** Runs unit tests
- **package:** Creates JAR file
- **verify:** Runs integration tests
- **install:** Installs artifact to local repository
- **deploy:** Deploys artifact to remote repository

**Build Profiles:**
The project includes several Maven profiles for different build scenarios:

- **jacoco:** Generates JaCoCo coverage reports for SonarCloud
- **ui-tests:** Profile for running UI tests, typically used with xvfb-run
- **mutation-testing-with-coverage:** Runs PIT mutation testing with coverage
- **integration-test-profile:** Runs integration tests

### 6.2 Continuous Integration

The project uses **GitHub Actions** for continuous integration, with multiple workflows automating different aspects of the development process:

**Unit Tests Workflow (Linux):**
- Runs on multiple Java versions (8, 11, 17)
- Executes all unit tests
- Generates coverage reports
- Performs mutation testing on Java 8
- Uploads coverage to Coveralls
- Runs on every push and pull request

**Cross-Platform Unit Tests (Windows & macOS):**
- Ensures cross-platform compatibility
- Runs unit tests on Windows and macOS
- Validates that the application works across different operating systems

**Integration Tests Workflow (Linux):**
- Runs integration and end-to-end tests
- Uses Docker/Testcontainers for MongoDB
- Validates database interactions and complete workflows

**SonarCloud Workflow (Linux):**
- Performs static code analysis
- Generates code coverage reports
- Uploads results to SonarCloud
- Enforces quality gates
- Identifies code smells and security vulnerabilities

### 6.3 Code Quality Gates

SonarCloud enforces quality gates that must pass before code can be merged:

- **Code Coverage:** Minimum coverage thresholds
- **Code Duplication:** Limits on duplicated code
- **Code Smells:** Detection and reporting of code smells
- **Security Vulnerabilities:** Identification of security issues
- **Maintainability Rating:** Overall code maintainability score

### 6.4 Deployment

While the project focuses on development and testing practices, the build process creates a deployable JAR file. The application can be run using:

```bash
mvn exec:java -Dexec.mainClass="com.mycompany.pet.ui.ExpenseTrackerApp"
```

Or by running the JAR file directly if packaged appropriately.

---

## 7. Code Quality & Coverage

### 7.1 Code Coverage

The project maintains high code coverage standards using JaCoCo:

**Coverage Targets:**
- **UI Package (`com.mycompany.pet.ui`):** Target 100% coverage
- **Utility Package (`com.mycompany.pet.util`):** Target 100% coverage
- **Other Packages:** High coverage (typically above 95%)

**Coverage Metrics:**
- **Line Coverage:** Percentage of lines executed during tests
- **Branch Coverage:** Percentage of branches (if/else, switch) executed
- **Method Coverage:** Percentage of methods executed
- **Instruction Coverage:** Bytecode instruction-level coverage

**Coverage Reports:**
Coverage reports are generated in multiple formats:
- **HTML:** Human-readable reports with detailed line-by-line coverage
- **XML:** For integration with SonarCloud and other tools
- **CSV:** For data analysis and reporting

**Viewing Coverage Reports:**
```bash
# Generate coverage report
mvn clean test jacoco:report

# View HTML report
# Open: target/site/jacoco/index.html
```

![Code Coverage Report](images/coverage-report.png)
*Figure 4: JaCoCo Code Coverage Report - Showing comprehensive test coverage across all packages*

### 7.2 Mutation Testing

Mutation testing using PIT verifies test quality by:
- Introducing mutations (bugs) into the code
- Running the test suite
- Checking if tests detect the mutations
- Reporting mutation scores

A high mutation score indicates that tests are effective at detecting bugs.

### 7.3 Static Code Analysis

SonarCloud performs comprehensive static code analysis:

**Analysis Categories:**
- **Bugs:** Coding errors that may lead to failures
- **Vulnerabilities:** Security issues that could be exploited
- **Code Smells:** Maintainability issues that make code harder to understand
- **Security Hotspots:** Security-sensitive code that requires review

**Quality Metrics:**
- **Reliability Rating:** Based on bug density
- **Security Rating:** Based on vulnerability density
- **Maintainability Rating:** Based on code smell density
- **Coverage:** Code coverage percentage
- **Duplications:** Code duplication percentage

![SonarCloud Dashboard](images/sonarcloud-dashboard.png)
*Figure 5: SonarCloud Quality Dashboard - Displaying code quality metrics and analysis results*

### 7.4 Code Quality Practices

The project follows several code quality practices:

- **Exception-Based Exit Handling:** The application uses `ApplicationExitException` instead of `System.exit()` to allow 100% code coverage
- **Comprehensive Logging:** Structured logging using Log4j2 with appropriate log levels
- **Clean Code Principles:** Following SOLID principles, clean code practices, and design patterns
- **Code Documentation:** Javadoc comments for public APIs and important methods
- **Consistent Code Style:** Following Java coding conventions

### 7.5 Test Quality

Test quality is maintained through:

- **Test-Driven Development:** Tests written before implementation
- **Comprehensive Test Coverage:** High coverage across all packages
- **Mutation Testing:** Verification of test effectiveness
- **Multiple Test Types:** Unit, integration, and E2E tests
- **BDD Tests:** Acceptance criteria in natural language

---

## 8. Conclusion

The Personal Expense Tracker project successfully demonstrates modern software engineering practices including Test-Driven Development, dependency injection, comprehensive testing strategies, build automation, and continuous integration. The application provides a functional expense management system with a user-friendly interface while serving as an educational example of professional Java development.

### 8.1 Key Achievements

- **Comprehensive Testing:** High test coverage with unit, integration, and E2E tests
- **Code Quality:** Static code analysis and quality gates ensuring maintainable code
- **Build Automation:** Maven-based build system with multiple profiles
- **Continuous Integration:** GitHub Actions workflows automating testing and quality checks
- **Modern Architecture:** Layered architecture with dependency injection
- **Professional Practices:** Following TDD, clean code principles, and industry standards

### 8.2 Technical Highlights

- **100% Test Coverage Target:** For UI and utility packages
- **Exception-Based Error Handling:** Replacing `System.exit()` with exceptions for testability
- **Dependency Injection:** Using Google Guice for loose coupling
- **Multiple Testing Frameworks:** JUnit, Mockito, Testcontainers, AssertJ Swing, Cucumber
- **Code Quality Tools:** JaCoCo, PIT, SonarCloud, Coveralls
- **Cross-Platform Support:** Works on Windows, macOS, and Linux

### 8.3 Learning Outcomes

This project demonstrates mastery of:

1. **Test-Driven Development:** Writing tests before implementation
2. **Build Automation:** Maven build system and profiles
3. **Continuous Integration:** GitHub Actions workflows
4. **Code Quality:** Coverage analysis, mutation testing, static analysis
5. **Modern Java Development:** Java 17, dependency injection, layered architecture
6. **GUI Development:** Swing-based desktop application
7. **Database Integration:** MongoDB integration and testing
8. **Professional Practices:** Clean code, documentation, version control

### 8.4 Future Enhancements

Potential areas for future improvement:

- **Export Functionality:** Export expenses to CSV or PDF
- **Data Visualization:** Charts and graphs for expense analysis
- **Multi-Currency Support:** Support for different currencies
- **User Authentication:** Multiple user support with authentication
- **Cloud Deployment:** Deployment to cloud platforms
- **Mobile Application:** Mobile app version using JavaFX or other frameworks

### 8.5 Final Remarks

The Personal Expense Tracker project successfully combines practical application development with educational objectives, demonstrating professional software engineering practices. The project showcases the importance of testing, code quality, and build automation in modern software development, providing a solid foundation for professional Java development.

---

## Appendix

### A. Project Structure

```
personal-expense-tracker/
├── pom.xml                          # Maven Project Object Model
├── README.md                         # Project documentation
├── sonar-project.properties         # SonarCloud configuration
├── .github/
│   └── workflows/                    # GitHub Actions workflows
├── src/
│   ├── main/
│   │   ├── java/                    # Source code
│   │   └── resources/               # Configuration files
│   ├── test/
│   │   ├── java/                    # Unit tests
│   │   └── resources/               # Test resources
│   ├── it/
│   │   └── java/                    # Integration tests
│   ├── e2e/
│   │   └── java/                    # End-to-end tests
│   └── bdd/
│       ├── java/                    # BDD step definitions
│       └── resources/               # Feature files
└── target/                          # Build output
```

### B. Key Dependencies

- Java 17
- MongoDB 4.11
- Google Guice 7.0
- JUnit 4.13.2 & JUnit 5.10.0
- Mockito 4.11.0
- Testcontainers 1.19.3
- JaCoCo 0.8.11
- PIT 1.15.0
- Log4j 2.23.1

### C. Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn clean test

# Generate coverage report
mvn clean test jacoco:report

# Run UI tests with xvfb
xvfb-run mvn clean test -Pui-tests jacoco:report

# Run integration tests
mvn clean verify -Pintegration-test-profile

# Run mutation testing
mvn clean verify -Pmutation-testing-with-coverage

# Run application
mvn exec:java -Dexec.mainClass="com.mycompany.pet.ui.ExpenseTrackerApp"
```

---

**End of Report**
