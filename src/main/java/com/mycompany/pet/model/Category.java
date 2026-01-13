package com.mycompany.pet.model;

import java.util.Objects;

/**
 * Represents an expense category 
 */
public class Category {
    /**
     * The unique identifier for this category.
     */
    private Integer categoryId;
    
    /**
     * The name of the category.
     */
    private String name;

    public Category() {
    }

    public Category(Integer categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryId, category.categoryId) &&
               Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, name);
    }

    @Override
    public String toString() {
        return "Category{" +
               "categoryId=" + categoryId +
               ", name='" + name + '\'' +
               '}';
    }
}



