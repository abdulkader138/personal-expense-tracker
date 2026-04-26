package com.mycompany.expensetracker.repository;

import java.util.List;

import com.mycompany.expensetracker.model.Category;

public interface CategoryRepository {

	void save(Category category);

	List<Category> findAll();

	Category findById(String id);

	void delete(String id);
}
