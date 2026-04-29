package com.mycompany.expensetracker.model;

import java.util.Objects;

public class Expense {

	private String id;
	private String description;
	private double amount;
	private Category category;

	public Expense(String id, String description, double amount, Category category) {
		this.id = id;
		this.description = description;
		this.amount = amount;
		this.category = category;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Expense)) return false;
		Expense other = (Expense) o;
		return Double.compare(other.amount, amount) == 0
				&& Objects.equals(id, other.id)
				&& Objects.equals(description, other.description)
				&& Objects.equals(category, other.category);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, description, amount, category);
	}

	@Override
	public String toString() {
		return "Expense{id='" + id + "', description='" + description
				+ "', amount=" + amount + ", category=" + category + "}";
	}
}
