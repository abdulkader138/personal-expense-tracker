# Personal Expense Tracker

A Java Swing desktop application to manage expense categories and expenses,
backed by MongoDB. Built following TDD with JUnit, Mockito, and AssertJ.

[![CI](https://github.com/abdulkader138/personal-expense-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/abdulkader138/personal-expense-tracker/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=abdulkader138_personal-expense-tracker&metric=coverage)](https://sonarcloud.io/dashboard?id=abdulkader138_personal-expense-tracker)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=abdulkader138_personal-expense-tracker&metric=alert_status)](https://sonarcloud.io/dashboard?id=abdulkader138_personal-expense-tracker)

## Build

```
./mvnw verify
```

## Run with coverage report

```
./mvnw verify -Pjacoco
```

## Run mutation tests

```
./mvnw verify -Ppit
```
