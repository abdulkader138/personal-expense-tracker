package com.mycompany.pet.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit tests for Category model class.
 */
public class CategoryTest {
    
    private static final Integer CATEGORY_ID = 1;
    private static final String CATEGORY_NAME = "Food";
    
    @Test
    public void testDefaultConstructor() {
        Category category = new Category();
        assertThat(category.getCategoryId()).isNull();
        assertThat(category.getName()).isNull();
    }
    
    @Test
    public void testConstructorWithIdAndName() {
        Category category = new Category(CATEGORY_ID, CATEGORY_NAME);
        assertThat(category.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(category.getName()).isEqualTo(CATEGORY_NAME);
    }
    
    @Test
    public void testConstructorWithNameOnly() {
        Category category = new Category(CATEGORY_NAME);
        assertThat(category.getCategoryId()).isNull();
        assertThat(category.getName()).isEqualTo(CATEGORY_NAME);
    }
    
    @Test
    public void testSetCategoryId() {
        Category category = new Category();
        category.setCategoryId(CATEGORY_ID);
        assertThat(category.getCategoryId()).isEqualTo(CATEGORY_ID);
    }
    
    @Test
    public void testSetName() {
        Category category = new Category();
        category.setName(CATEGORY_NAME);
        assertThat(category.getName()).isEqualTo(CATEGORY_NAME);
    }
    
    @Test
    public void testEquals_SameInstance() {
        Category category = new Category(CATEGORY_ID, CATEGORY_NAME);
        assertThat(category.equals(category)).isTrue();
    }
    
    @Test
    public void testEquals_Null() {
        Category category = new Category(CATEGORY_ID, CATEGORY_NAME);
        assertThat(category.equals(null)).isFalse();
    }
    
    @Test
    public void testEquals_DifferentClass() {
        Category category = new Category(CATEGORY_ID, CATEGORY_NAME);
        assertThat(category.equals("not a category")).isFalse();
    }
    
    @Test
    public void testEquals_SameIdAndName() {
        Category category1 = new Category(CATEGORY_ID, CATEGORY_NAME);
        Category category2 = new Category(CATEGORY_ID, CATEGORY_NAME);
        assertThat(category1.equals(category2)).isTrue();
    }
    
    @Test
    public void testEquals_DifferentId() {
        Category category1 = new Category(CATEGORY_ID, CATEGORY_NAME);
        Category category2 = new Category(2, CATEGORY_NAME);
        assertThat(category1.equals(category2)).isFalse();
    }
    
    @Test
    public void testEquals_DifferentName() {
        Category category1 = new Category(CATEGORY_ID, CATEGORY_NAME);
        Category category2 = new Category(CATEGORY_ID, "Travel");
        assertThat(category1.equals(category2)).isFalse();
    }
    
    @Test
    public void testEquals_NullId() {
        Category category1 = new Category(null, CATEGORY_NAME);
        Category category2 = new Category(null, CATEGORY_NAME);
        assertThat(category1.equals(category2)).isTrue();
    }
    
    @Test
    public void testEquals_NullName() {
        Category category1 = new Category(CATEGORY_ID, null);
        Category category2 = new Category(CATEGORY_ID, null);
        assertThat(category1.equals(category2)).isTrue();
    }
    
    @Test
    public void testEquals_OneNullId() {
        Category category1 = new Category(CATEGORY_ID, CATEGORY_NAME);
        Category category2 = new Category(null, CATEGORY_NAME);
        assertThat(category1.equals(category2)).isFalse();
    }
    
    @Test
    public void testEquals_OneNullName() {
        Category category1 = new Category(CATEGORY_ID, CATEGORY_NAME);
        Category category2 = new Category(CATEGORY_ID, null);
        assertThat(category1.equals(category2)).isFalse();
    }
    
    @Test
    public void testHashCode_SameValues() {
        Category category1 = new Category(CATEGORY_ID, CATEGORY_NAME);
        Category category2 = new Category(CATEGORY_ID, CATEGORY_NAME);
        assertThat(category1).hasSameHashCodeAs(category2);
    }
    
    @Test
    public void testHashCode_NullValues() {
        Category category1 = new Category(null, null);
        Category category2 = new Category(null, null);
        assertThat(category1).hasSameHashCodeAs(category2);
    }
    
    @Test
    public void testToString() {
        Category category = new Category(CATEGORY_ID, CATEGORY_NAME);
        String toString = category.toString();
        assertThat(toString).contains("Category{");
        assertThat(toString).contains("categoryId=" + CATEGORY_ID);
        assertThat(toString).contains("name='" + CATEGORY_NAME + "'");
    }
    
    @Test
    public void testToString_WithNullValues() {
        Category category = new Category(null, null);
        String toString = category.toString();
        assertThat(toString).contains("Category{");
        assertThat(toString).contains("categoryId=null");
        assertThat(toString).contains("name='null'");
    }
}

