package com.mycompany.expensetracker.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.repository.CategoryRepository;

public class CategoryServiceTest {

	private CategoryRepository repository;
	private CategoryService service;

	@Before
	public void setUp() {
		repository = mock(CategoryRepository.class);
		service = new CategoryService(repository);
	}

	@Test
	public void testAddCategoryDelegatesToRepository() {
		Category category = new Category("1", "Food");
		service.addCategory(category);
		verify(repository).save(category);
	}
}
