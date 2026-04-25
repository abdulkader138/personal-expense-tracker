package com.mycompany.expensetracker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

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

	@Test
	public void testAddCategoryWithNullThrowsException() {
		assertThatThrownBy(() -> service.addCategory(null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testGetAllCategoriesReturnsFromRepository() {
		List<Category> categories = Arrays.asList(new Category("1", "Food"));
		when(repository.findAll()).thenReturn(categories);
		assertThat(service.getAllCategories()).isEqualTo(categories);
	}
}
