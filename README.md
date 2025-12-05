# Personal Expense Tracker

[![JAVA CI Unit tests, Mutation tests and Coveralls with Maven in Linux](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/maven.yaml/badge.svg?branch=main)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/maven.yaml)  [![JAVA CI Unit tests with Maven in Windows & MacOS](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/mac_and_windows_workflow_for_unit_tests.yaml/badge.svg)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/mac_and_windows_workflow_for_unit_tests.yaml)  [![JAVA IT, E2E tests with Maven in Linux](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/ubuntu_workflow_flow_for_integration_and_end2end_tests.yaml/badge.svg)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/ubuntu_workflow_flow_for_integration_and_end2end_tests.yaml)  [![Coverage Status](https://coveralls.io/repos/github/YOUR_USERNAME/personal-expense-tracker/badge.svg?branch=main)](https://coveralls.io/github/YOUR_USERNAME/personal-expense-tracker?branch=main) [![Java CI with Maven, Docker and SonarCloud in Linux](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/java_ci_maven_docker_sonarcloud.yaml/badge.svg)](https://github.com/YOUR_USERNAME/personal-expense-tracker/actions/workflows/java_ci_maven_docker_sonarcloud.yaml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=personal-expense-tracker&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=personal-expense-tracker)

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

## Requirements

- Java 8 or higher (Java 17 recommended)
- Maven 3.6 or higher
- MongoDB 4.11 (or Docker for Testcontainers)
- Eclipse IDE (recommended)

## Note

- `integration-test-profile`: When activated, this profile skips unit tests executed by the maven-surefire-plugin and runs integration tests via the maven-failsafe-plugin. To activate this profile, use the following command: 

    `mvn verify -Pintegration-test-profile`

- `mutation-testing-with-coverage`: This profile runs unit tests with JaCoCo coverage and PIT mutation testing, and uploads coverage to Coveralls. To activate this profile, use:

    `mvn verify -Pmutation-testing-with-coverage -DrepoToken=$COVERALLS_REPO_TOKEN coveralls:report`

- `jacoco`: This profile runs unit tests with JaCoCo coverage for SonarCloud analysis. To activate this profile, use:

    `mvn verify -Pjacoco`


## Project Structure

### Directory Structure

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
│   │   ├── java/                     # Main source code
│   │   │   └── com/
│   │   │       └── mycompany/
│   │   │           └── pet/
│   │   │               ├── model/    # Domain models
│   │   │               ├── dao/      # Data Access Objects
│   │   │               ├── service/  # Business logic services
│   │   │               ├── ui/      # Swing UI components
│   │   │               └── database/ # Database connection management
│   │   └── resources/                # Main resources (config files, etc.)
│   └── test/
│       ├── java/                     # Test source code
│       │   └── com/
│       │       └── mycompany/
│       │           └── pet/
│       │               ├── dao/      # DAO unit tests
│       │               ├── service/  # Service unit tests
│       │               └── database/ # Integration tests
│       └── resources/                # Test resources
├── src/it/java/                      # Integration tests (optional)
├── src/e2e/java/                      # End-to-end tests (optional)
└── target/                           # Build output (ignored by git)
```

## Testing Strategy

This project follows **Test-Driven Development (TDD)** methodology and adheres to the **Test Pyramid**. It includes:

### Unit Tests
- **Service Tests**: Test business logic with mocked dependencies
- **DAO Tests**: Test data access layer with in-memory MongoDB (Testcontainers)
- **Controller/View Tests**: Test UI components with AssertJ Swing

### Integration Tests
- **Database Integration Tests**: Test real database interactions using Testcontainers
- **Service Integration Tests**: Test service layer with real DAOs

### End-to-End Tests (Optional)
- **E2E Tests**: Test complete user workflows through the UI

## Continuous Integration

The project includes **GitHub Actions workflows** that:

1. **Unit Tests Workflow** (Linux):
   - Runs on Java 8, 11, and 17
   - Performs mutation testing on Java 8
   - Uploads coverage to Coveralls
   - Generates test reports

2. **Cross-Platform Unit Tests** (Windows & macOS):
   - Runs unit tests on multiple operating systems
   - Ensures compatibility across platforms

3. **Integration Tests Workflow** (Linux):
   - Runs integration and end-to-end tests
   - Uses Docker/Testcontainers for MongoDB

4. **SonarCloud Workflow** (Linux):
   - Performs code quality analysis
   - Generates coverage reports
   - Uploads results to SonarCloud

## Code Quality

- **JaCoCo**: Code coverage analysis with configurable thresholds
- **PIT**: Mutation testing to verify test quality
- **SonarCloud**: Static code analysis for code quality and security
- **Coveralls**: Coverage tracking and reporting

## Configuration

### SonarCloud

To enable SonarCloud analysis:

1. Create a project on [SonarCloud](https://sonarcloud.io)
2. Get your organization key and project key
3. Update `sonar-project.properties` with your keys
4. Add `SONAR_TOKEN` to GitHub Secrets

### Coveralls

To enable Coveralls reporting:

1. Connect your repository to [Coveralls](https://coveralls.io)
2. Add `COVERALLS_REPO_TOKEN` to GitHub Secrets

**Note**: Replace `YOUR_USERNAME` in the badge URLs in the README with your actual GitHub username.

## Overview

This project demonstrates the creation of a software application utilizing **Test-Driven Development (TDD)**, **Build Automation**, and **Continuous Integration**. The project uses a range of technologies like **Eclipse, Maven, Docker, GitHub Actions**, and **SonarCloud**. It emphasizes thorough testing through **white-box (unit and integration tests)** and **black-box (end-to-end tests)** testing methodologies.

The **"Personal Expense Tracker"** facilitates the management of personal expenses and categories, providing features for tracking spending, managing categories, and generating reports.

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd personal-expense-tracker
```

### 2. Build and Run Tests

```bash
# Run unit tests
mvn clean test

# Run all tests including integration tests
mvn clean verify

# Run with coverage
mvn clean test jacoco:report

# Run mutation testing
mvn clean verify -Pmutation-testing-with-coverage
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
