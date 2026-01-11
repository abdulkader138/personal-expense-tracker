# Personal Expense Tracker

<!-- [![JAVA CI Unit tests, Mutation tests and Coveralls with Maven in Linux](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/maven.yaml/badge.svg?branch=main)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/maven.yaml)  [![JAVA CI Unit tests with Maven in Windows & MacOS](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/mac_and_windows_workflow_for_unit_tests.yaml/badge.svg)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/mac_and_windows_workflow_for_unit_tests.yaml)  [![JAVA IT, E2E tests with Maven in Linux](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/ubuntu_workflow_flow_for_integration_and_end2end_tests.yaml/badge.svg)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/ubuntu_workflow_flow_for_integration_and_end2end_tests.yaml)  [![Coverage Status](https://coveralls.io/repos/github/YOUR_USERNAME/personal-expense-tracker/badge.svg?branch=main)](https://coveralls.io/github/YOUR_USERNAME/personal-expense-tracker?branch=main) [![Java CI with Maven, Docker and SonarCloud in Linux](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/java_ci_maven_docker_sonarcloud.yaml/badge.svg)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/java_ci_maven_docker_sonarcloud.yaml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=personal-expense-tracker&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=personal-expense-tracker) -->

A comprehensive Personal Expense Tracker application developed following **Test-Driven Development (TDD)** principles, with full build automation, continuous integration, and high code coverage standards.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technologies & Tools](#technologies--tools)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Setup Instructions](#setup-instructions)
- [Project Structure](#project-structure)
- [Testing Strategy](#testing-strategy)
- [Code Quality & Coverage](#code-quality--coverage)
- [Build Profiles](#build-profiles)
- [Continuous Integration](#continuous-integration)
- [Configuration](#configuration)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Author](#author)

## 🎯 Project Overview

This application provides a complete solution for managing personal expenses with a clean, intuitive Swing-based graphical user interface. The project demonstrates best practices in software development including:

- **Test-Driven Development (TDD)**
- **Dependency Injection** using Google Guice
- **Build Automation** with Maven
- **Continuous Integration** with GitHub Actions
- **Code Quality** monitoring with SonarCloud
- **High Code Coverage** (targeting 100% for UI and utility packages)

### Key Features

- ✅ **Expense Management**: Add, edit, and delete expenses
- ✅ **Category Management**: Create and manage expense categories
- ✅ **Monthly Summary**: View expenses filtered by month and year
- ✅ **Category Totals**: Track total spending per category
- ✅ **Persistent Storage**: MongoDB database for data persistence
- ✅ **Modern UI**: Clean Swing-based graphical interface

## 🚀 Features

### Expense Management

- Create new expenses with date, amount, description, and category
- Edit existing expenses
- Delete expenses with confirmation dialog
- View all expenses in a sortable table
- Filter expenses by month and year
- Real-time expense table updates

### Category Management

- Create new expense categories
- Edit category names
- Delete categories (with cascade delete of associated expenses)
- View all categories in a dedicated dialog
- Category selection dropdown in main window

### Reporting & Analytics

- Monthly expense summary (total for selected month/year)
- Category-based expense totals
- Filter expenses by month/year combination
- "All months" option for viewing complete expense history

## 🛠 Technologies & Tools

### Core Technologies

- **Java 17** - Programming language
- **MongoDB 4.11** - NoSQL database
- **Maven 3.6+** - Build automation and dependency management
- **Swing** - Graphical user interface framework
- **Google Guice 7.0** - Dependency injection framework

### Testing Frameworks

- **JUnit 4.13.2** - Unit testing framework
- **JUnit 5.10.0** - Modern testing framework (for integration tests)
- **Mockito 4.11.0** - Mocking framework
- **AssertJ Swing** - UI testing framework
- **Testcontainers 1.19.3** - Integration testing with Docker
- **Cucumber 5.5.0** - BDD testing framework
- **Awaitility 4.2.0** - Async testing utilities

### Code Quality Tools

- **JaCoCo 0.8.11** - Code coverage analysis
- **PIT 1.15.0** - Mutation testing
- **SonarCloud** - Static code analysis and quality gates
- **Coveralls** - Coverage tracking and reporting

### Build & CI/CD

- **Maven** - Build automation
- **GitHub Actions** - Continuous integration
- **Docker** - Containerization for integration tests

### Logging

- **Apache Log4j 2.23.1** - Logging framework
- **SLF4J 1.7.36** - Logging facade (for dependencies)
- **Logback 1.2.12** - Logging implementation (for dependencies)

## 🏗 Architecture

The application follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────┐
│         UI Layer (Swing)            │
│  MainWindow, ExpenseDialog, etc.   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Controller Layer                │
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
│  ExpenseDAO, CategoryDAO             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Database Layer                  │
│  MongoDB (via MongoClient)           │
└─────────────────────────────────────┘
```

### Dependency Injection

The application uses **Google Guice** for dependency injection, configured in `ExpenseTrackerModule`. This ensures:

- Loose coupling between components
- Easy testing with mock dependencies
- Centralized configuration
- Singleton management for database connections

### Package Structure

- **`com.mycompany.pet.model`** - Domain models (Expense, Category)
- **`com.mycompany.pet.dao`** - Data Access Objects
- **`com.mycompany.pet.service`** - Business logic services
- **`com.mycompany.pet.controller`** - Controllers (mediates UI and services)
- **`com.mycompany.pet.ui`** - Swing UI components
- **`com.mycompany.pet.database`** - Database connection management
- **`com.mycompany.pet.di`** - Dependency injection configuration
- **`com.mycompany.pet.util`** - Utility classes
- **`com.mycompany.pet.annotation`** - Custom annotations

## 📦 Requirements

### Minimum Requirements

- **Java**: Java 17 or higher
- **Maven**: 3.6 or higher
- **MongoDB**: 4.11 or higher (or Docker for Testcontainers)
- **Operating System**: Windows, macOS, or Linux

### Recommended

- **IDE**: Eclipse IDE or IntelliJ IDEA
- **Docker**: For running integration tests with Testcontainers
- **Git**: For version control

### Optional

- **X Server** (for WSL users): VcXsrv, Xming, or similar for GUI display

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd personal-expense-tracker
```

### 2. Configure MongoDB

#### Option A: Local MongoDB Installation

1. Install MongoDB 4.11 or higher
2. Start MongoDB service:

   ```bash
   # Linux/macOS
   sudo systemctl start mongod

   # Windows
   net start MongoDB
   ```

#### Option B: Docker (Recommended for Testing)

```bash
docker run -d -p 27017:27017 --name mongodb mongo:4.11
```

### 3. Build the Project

```bash
# Clean and compile
mvn clean compile

# Run unit tests
mvn clean test

# Generate coverage report
mvn clean test jacoco:report

# View coverage report
# Open: target/site/jacoco/index.html
```

### 4. Run the Application

```bash
# From command line
mvn exec:java -Dexec.mainClass="com.mycompany.pet.ui.ExpenseTrackerApp"

# Or from IDE
# Run: com.mycompany.pet.ui.ExpenseTrackerApp.main()
```

### 5. WSL Users (GUI Display)

If running on WSL without a display:

```bash
# Install X server on Windows (VcXsrv, Xming, etc.)
# Then set display:
export DISPLAY=:0.0

# Or use Eclipse IDE which handles display automatically
```

## 📁 Project Structure

```
personal-expense-tracker/
├── pom.xml                          # Maven Project Object Model
├── README.md                         # Project documentation
├── sonar-project.properties         # SonarCloud configuration
├── .github/
│   └── workflows/                    # GitHub Actions workflows
│       ├── maven.yaml                # Unit tests, mutation tests, coverage
│       ├── mac_and_windows_workflow_for_unit_tests.yaml
│       ├── ubuntu_workflow_flow_for_integration_and_end2end_tests.yaml
│       └── java_ci_maven_docker_sonarcloud.yaml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mycompany/pet/
│   │   │       ├── model/           # Domain models (Expense, Category)
│   │   │       ├── dao/            # Data Access Objects
│   │   │       ├── service/        # Business logic services
│   │   │       ├── controller/     # Controllers (UI-Service mediation)
│   │   │       ├── ui/             # Swing UI components
│   │   │       ├── database/       # Database connection management
│   │   │       ├── di/             # Dependency injection (Guice)
│   │   │       ├── util/           # Utility classes
│   │   │       └── annotation/     # Custom annotations
│   │   └── resources/              # Main resources (config files, etc.)
│   ├── test/
│   │   ├── java/                    # Unit tests
│   │   │   └── com/mycompany/pet/
│   │   │       ├── dao/            # DAO unit tests
│   │   │       ├── service/        # Service unit tests
│   │   │       ├── controller/     # Controller unit tests
│   │   │       ├── ui/             # UI unit tests
│   │   │       └── database/       # Database unit tests
│   │   └── resources/              # Test resources
│   ├── it/
│   │   └── java/                   # Integration tests
│   ├── e2e/
│   │   └── java/                   # End-to-end tests
│   └── bdd/
│       ├── java/                   # BDD step definitions
│       └── resources/              # Feature files
└── target/                         # Build output (ignored by git)
    ├── classes/                    # Compiled classes
    ├── test-classes/               # Compiled test classes
    ├── site/jacoco/                # Coverage reports
    └── surefire-reports/           # Test reports
```

## 🧪 Testing Strategy

This project follows **Test-Driven Development (TDD)** methodology and adheres to the **Test Pyramid**:

```
        /\
       /  \      E2E Tests (Few)
      /____\
     /      \    Integration Tests (Some)
    /________\
   /          \  Unit Tests (Many)
  /____________\
```

### Unit Tests

- **Service Tests**: Test business logic with mocked dependencies
- **DAO Tests**: Test data access layer with in-memory MongoDB (Testcontainers)
- **Controller Tests**: Test controller logic with mocked services
- **UI Tests**: Test UI components with AssertJ Swing
- **Model Tests**: Test domain model behavior

### Integration Tests

- **Database Integration Tests**: Test real database interactions using Testcontainers
- **Service Integration Tests**: Test service layer with real DAOs
- **End-to-End Tests**: Test complete user workflows through the UI

### BDD Tests

- **Cucumber Feature Files**: Behavior-driven development tests
- **Step Definitions**: Implementation of BDD scenarios

### Test Execution

```bash
# Run only unit tests
mvn clean test

# Run integration tests
mvn clean verify -Pintegration-test-profile

# Run all tests with coverage
mvn clean test jacoco:report

# Run mutation testing
mvn clean verify -Pmutation-testing-with-coverage
```

## 📊 Code Quality & Coverage

### Code Coverage

The project maintains high code coverage standards:

- **Target**: 100% coverage for `com.mycompany.pet.ui` and `com.mycompany.pet.util` packages
- **Current Status**:
  - UI Package: ~98% coverage (targeting 100%)
  - Util Package: ~87% coverage (targeting 100%)
  - Other packages: 100% coverage

### Coverage Reports

```bash
# Generate coverage report
mvn clean test jacoco:report

# View HTML report
# Open: target/site/jacoco/index.html

# View CSV report
# Open: target/site/jacoco/jacoco.csv

# View XML report (for SonarCloud)
# Open: target/site/jacoco/jacoco.xml
```

### Code Quality Tools

- **JaCoCo**: Code coverage analysis with configurable thresholds
- **PIT**: Mutation testing to verify test quality
- **SonarCloud**: Static code analysis for code quality, security, and maintainability
- **Coveralls**: Coverage tracking and reporting

### Quality Metrics

- **Code Smells**: Monitored via SonarCloud
- **Security Vulnerabilities**: Scanned via SonarCloud
- **Test Coverage**: Tracked via JaCoCo and Coveralls
- **Mutation Score**: Verified via PIT

## 🔧 Build Profiles

### Default Profile

Runs unit tests with standard configuration:

```bash
mvn clean test
```

### Integration Test Profile

Skips unit tests and runs integration tests via Failsafe plugin:

```bash
mvn verify -Pintegration-test-profile
```

### Mutation Testing with Coverage

Runs unit tests with JaCoCo coverage and PIT mutation testing, uploads coverage to Coveralls:

```bash
mvn verify -Pmutation-testing-with-coverage -DrepoToken=$COVERALLS_REPO_TOKEN coveralls:report
```

### JaCoCo Profile

Runs unit tests with JaCoCo coverage for SonarCloud analysis:

```bash
mvn verify -Pjacoco
```

## 🔄 Continuous Integration

The project includes **GitHub Actions workflows** that automate:

### 1. Unit Tests Workflow (Linux)

- Runs on Java 8, 11, and 17
- Performs mutation testing on Java 8
- Uploads coverage to Coveralls
- Generates test reports

### 2. Cross-Platform Unit Tests (Windows & macOS)

- Runs unit tests on multiple operating systems
- Ensures compatibility across platforms
- Validates cross-platform behavior

### 3. Integration Tests Workflow (Linux)

- Runs integration and end-to-end tests
- Uses Docker/Testcontainers for MongoDB
- Validates database interactions

### 4. SonarCloud Workflow (Linux)

- Performs code quality analysis
- Generates coverage reports
- Uploads results to SonarCloud
- Enforces quality gates

## ⚙️ Configuration

### SonarCloud

To enable SonarCloud analysis:

1. Create a project on [SonarCloud](https://sonarcloud.io)
2. Get your organization key and project key
3. Update `sonar-project.properties` with your keys:
   ```properties
   sonar.projectKey=your_project_key
   sonar.organization=your_organization
   ```
4. Add `SONAR_TOKEN` to GitHub Secrets

### Coveralls

To enable Coveralls reporting:

1. Connect your repository to [Coveralls](https://coveralls.io)
2. Add `COVERALLS_REPO_TOKEN` to GitHub Secrets
3. Update badge URLs in README with your username

### MongoDB Configuration

Default MongoDB connection settings:

- **Host**: `localhost`
- **Port**: `27017`
- **Database**: `expense_tracker`

These can be configured in `ExpenseTrackerModule.java`:

```java
ExpenseTrackerModule module = new ExpenseTrackerModule()
    .mongoHost("localhost")
    .mongoPort(27017)
    .databaseName("expense_tracker");
```

## 💻 Usage

### Running the Application

1. **Start MongoDB** (if not using Docker):

   ```bash
   # Linux/macOS
   sudo systemctl start mongod

   # Windows
   net start MongoDB
   ```

2. **Run the application**:

   ```bash
   mvn exec:java -Dexec.mainClass="com.mycompany.pet.ui.ExpenseTrackerApp"
   ```

3. **Or from IDE**: Run `ExpenseTrackerApp.main()`

### Using the Application

1. **Add Categories**: Click "Manage Categories" → "Add" → Enter category name → "Save"
2. **Add Expenses**: Click "Add Expense" → Fill in details → "Save"
3. **Edit Expenses**: Select expense in table → Click "Edit Expense" → Modify → "Save"
4. **Delete Expenses**: Select expense → Click "Delete Expense" → Confirm
5. **Filter Expenses**: Select month and year from dropdowns
6. **View Totals**: Monthly total and category total are displayed automatically

## 🤝 Contributing

This is an educational project demonstrating TDD, build automation, and CI/CD practices. Contributions and suggestions are welcome!

### Contribution Guidelines

1. Follow TDD principles (write tests first)
2. Maintain code coverage above 95%
3. Follow existing code style and conventions
4. Update tests for new features
5. Ensure all tests pass before submitting

## 📄 License

This project is developed for educational purposes as part of a course on **Test-Driven Development, Build Automation, and Continuous Integration with Java**.

## 👤 Author

**Abdul Kader**

---

## 📝 Notes

- **Code Coverage**: The project targets 100% coverage for UI and utility packages. Some methods containing `System.exit()` are excluded from coverage as they cannot be properly tracked by JaCoCo.

- **GUI Testing**: UI tests use AssertJ Swing and may require a display. For headless environments, tests are configured to handle this gracefully.

- **Dependency Injection**: The application uses Google Guice for dependency injection, following patterns from "Test-Driven Development, Build Automation, Continuous Integration" book.

- **Badge URLs**: Replace `YOUR_USERNAME` in badge URLs at the top of this README with your actual GitHub username to enable CI/CD badges.

---

**Last Updated**: 2024
