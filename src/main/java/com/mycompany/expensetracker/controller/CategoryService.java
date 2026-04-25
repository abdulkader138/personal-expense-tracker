package com.mycompany.expensetracker.controller;

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.repository.CategoryRepository;

public class CategoryService {

	private final CategoryRepository repository;

	public CategoryService(CategoryRepository repository) {
		this.repository = repository;
	}

	public void addCategory(Category category) {
		if (category == null) {
			throw new IllegalArgumentException("Category cannot be null");
		}
		repository.save(category);
	}
}
