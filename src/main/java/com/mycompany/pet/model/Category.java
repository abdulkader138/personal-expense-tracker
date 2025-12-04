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

    /**
     * Default constructor.
     */
    public Category() {
    }

    /**
     * * Parameterised constructor.
     * 
     * @param categoryId the unique identifier for this category
     * @param name the name of the category
     */
    public Category(Integer categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    /**
     * Parameterised constructor.
     * 
     * @param name the name of the category
     */
    public Category(String name) {
        this.name = name;
    }

    /**
     * Returns the id of this category.
     * 
     * @return the category identifier, or null if not yet persisted
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the unique id of category.
     * 
     * @param categoryId the category identifier to set
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Returns the name of this category.
     * 
     * @return the category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this category.
     * 
     * @param name the category name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Two categories are considered equal if they have the same categoryId and name.
     * 
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryId, category.categoryId) &&
               Objects.equals(name, category.name);
    }

    /**
     * Returns a hash code value for this category.
     * 
     * @return a hash code value for this category
     */
    @Override
    public int hashCode() {
        return Objects.hash(categoryId, name);
    }

    /**
     * Returns a string representation of this category.
     * The string includes the categoryId and name.
     * 
     * @return a string representation of this category
     */
    @Override
    public String toString() {
        return "Category{" +
               "categoryId=" + categoryId +
               ", name='" + name + '\'' +
               '}';
    }
}



