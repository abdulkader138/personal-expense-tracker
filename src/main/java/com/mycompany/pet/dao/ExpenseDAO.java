package com.mycompany.pet.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.model.Expense;

/**
 * Data Access Object for Expense entities.
 */
public class ExpenseDAO {
    private final MongoCollection<Document> collection;

    public ExpenseDAO(DatabaseConnection dbConnection) {
        MongoDatabase database = dbConnection.getDatabase();
        this.collection = database.getCollection("expenses");
    }

    public Expense create(Expense expense) throws SQLException {
        try {
            // Generate a simple incremental ID
            Document last = collection.find()
                    .sort(Sorts.descending("expenseId"))
                    .first();
            int nextId = 1;
            if (last != null && last.get("expenseId") != null) {
                nextId = last.getInteger("expenseId") + 1;
            }
            expense.setExpenseId(nextId);

            Document doc = new Document("_id", nextId)
                    .append("expenseId", nextId)
                    .append("date", expense.getDate().toString()) // store as ISO date string
                    .append("amount", expense.getAmount().toString()) // store as string
                    .append("description", expense.getDescription())
                    .append("categoryId", expense.getCategoryId());

            collection.insertOne(doc);
            return expense;
        } catch (Exception e) {
            throw new SQLException("Error creating expense in MongoDB", e);
        }
    }

    public Expense findById(Integer expenseId) throws SQLException {
        try {
            Document doc = collection.find(Filters.eq("expenseId", expenseId)).first();
            return doc != null ? mapDocumentToExpense(doc) : null;
        } catch (Exception e) {
            throw new SQLException("Error finding expense in MongoDB", e);
        }
    }

    public List<Expense> findAll() throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        try {
            for (Document doc : collection.find().sort(Sorts.descending("date"))) {
                expenses.add(mapDocumentToExpense(doc));
            }
            return expenses;
        } catch (Exception e) {
            throw new SQLException("Error finding all expenses in MongoDB", e);
        }
    }

    public List<Expense> findByMonth(int year, int month) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                Expense expense = mapDocumentToExpense(doc);
                if (expense.getDate().getYear() == year && expense.getDate().getMonthValue() == month) {
                    expenses.add(expense);
                }
            }
            expenses.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate()));
            return expenses;
        } catch (Exception e) {
            throw new SQLException("Error finding expenses by month in MongoDB", e);
        }
    }

    public List<Expense> findByCategory(Integer categoryId) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.eq("categoryId", categoryId)).sort(Sorts.descending("date"))) {
                expenses.add(mapDocumentToExpense(doc));
            }
            return expenses;
        } catch (Exception e) {
            throw new SQLException("Error finding expenses by category in MongoDB", e);
        }
    }

    public Expense update(Expense expense) throws SQLException {
        try {
            Document update = new Document("$set",
                    new Document("date", expense.getDate().toString())
                            .append("amount", expense.getAmount().toString())
                            .append("description", expense.getDescription())
                            .append("categoryId", expense.getCategoryId()));
            collection.updateOne(Filters.eq("expenseId", expense.getExpenseId()), update);
            return expense;
        } catch (Exception e) {
            throw new SQLException("Error updating expense in MongoDB", e);
        }
    }

    public boolean delete(Integer expenseId) throws SQLException {
        try {
            long deleted = collection.deleteOne(Filters.eq("expenseId", expenseId)).getDeletedCount();
            return deleted > 0;
        } catch (Exception e) {
            throw new SQLException("Error deleting expense in MongoDB", e);
        }
    }

    public BigDecimal getTotalByCategory(Integer categoryId) throws SQLException {
        try {
            BigDecimal total = BigDecimal.ZERO;
            for (Document doc : collection.find(Filters.eq("categoryId", categoryId))) {
                Expense expense = mapDocumentToExpense(doc);
                total = total.add(expense.getAmount());
            }
            return total;
        } catch (Exception e) {
            throw new SQLException("Error getting total by category in MongoDB", e);
        }
    }

    public BigDecimal getMonthlyTotal(int year, int month) throws SQLException {
        try {
            BigDecimal total = BigDecimal.ZERO;
            for (Document doc : collection.find()) {
                Expense expense = mapDocumentToExpense(doc);
                if (expense.getDate().getYear() == year && expense.getDate().getMonthValue() == month) {
                    total = total.add(expense.getAmount());
                }
            }
            return total;
        } catch (Exception e) {
            throw new SQLException("Error getting monthly total in MongoDB", e);
        }
    }

    private Expense mapDocumentToExpense(Document doc) {
        Expense expense = new Expense();
        expense.setExpenseId(doc.getInteger("expenseId"));
        String dateStr = doc.getString("date");
        expense.setDate(LocalDate.parse(dateStr));
        String amountStr = doc.getString("amount");
        expense.setAmount(new BigDecimal(amountStr));
        expense.setDescription(doc.getString("description"));
        expense.setCategoryId(doc.getInteger("categoryId"));
        return expense;
    }
}

