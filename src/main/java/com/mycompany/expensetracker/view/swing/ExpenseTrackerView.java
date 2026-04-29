package com.mycompany.expensetracker.view.swing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;

public class ExpenseTrackerView extends JFrame {

	JTextField txtDescription;
	JTextField txtAmount;
	JButton btnAddExpense;
	JButton btnUpdateExpense;
	JButton btnDeleteExpense;
	JList<Expense> listExpenses;
	JTextField txtCategoryName;
	JButton btnAddCategory;
	JButton btnUpdateCategory;
	JButton btnDeleteCategory;
	JList<Category> listCategories;

	private JLabel lblError;
	private JComboBox<Category> comboCategory;
	private DefaultListModel<Expense> expenseListModel;
	private DefaultComboBoxModel<Category> categoryComboModel;
	private DefaultListModel<Category> categoryListModel;

	public ExpenseTrackerView() {
		setTitle("Expense Tracker");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		txtDescription = new JTextField();
		txtDescription.setName("txtDescription");

		txtAmount = new JTextField();
		txtAmount.setName("txtAmount");

		btnAddExpense = new JButton("Add Expense");
		btnAddExpense.setName("btnAddExpense");
		btnAddExpense.setEnabled(false);

		btnUpdateExpense = new JButton("Update Expense");
		btnUpdateExpense.setName("btnUpdateExpense");

		btnDeleteExpense = new JButton("Delete Expense");
		btnDeleteExpense.setName("btnDeleteExpense");

		expenseListModel = new DefaultListModel<>();
		listExpenses = new JList<>(expenseListModel);
		listExpenses.setName("listExpenses");

		lblError = new JLabel(" ");
		lblError.setName("lblError");

		categoryComboModel = new DefaultComboBoxModel<>();
		comboCategory = new JComboBox<>(categoryComboModel);
		comboCategory.setName("comboCategory");

		txtCategoryName = new JTextField();
		txtCategoryName.setName("txtCategoryName");

		btnAddCategory = new JButton("Add Category");
		btnAddCategory.setName("btnAddCategory");

		btnUpdateCategory = new JButton("Update Category");
		btnUpdateCategory.setName("btnUpdateCategory");

		btnDeleteCategory = new JButton("Delete Category");
		btnDeleteCategory.setName("btnDeleteCategory");

		categoryListModel = new DefaultListModel<>();
		listCategories = new JList<>(categoryListModel);
		listCategories.setName("listCategories");
		listCategories.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

		DocumentListener enabler = new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { updateAddButton(); }
			@Override public void removeUpdate(DocumentEvent e) { updateAddButton(); }
			@Override public void changedUpdate(DocumentEvent e) { updateAddButton(); }
		};
		txtDescription.getDocument().addDocumentListener(enabler);
		txtAmount.getDocument().addDocumentListener(enabler);

		JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
		inputPanel.add(new JLabel("Description:"));
		inputPanel.add(txtDescription);
		inputPanel.add(new JLabel("Amount:"));
		inputPanel.add(txtAmount);
		inputPanel.add(new JLabel("Category:"));
		inputPanel.add(comboCategory);

		JPanel categoryForm = new JPanel(new GridLayout(2, 2, 5, 5));
		categoryForm.add(new JLabel("Category name:"));
		categoryForm.add(txtCategoryName);
		categoryForm.add(btnAddCategory);
		categoryForm.add(btnUpdateCategory);

		JPanel categoryButtonPanel = new JPanel();
		categoryButtonPanel.add(btnDeleteCategory);

		JPanel categoryTopPanel = new JPanel(new BorderLayout(5, 5));
		categoryTopPanel.add(categoryForm, BorderLayout.NORTH);
		categoryTopPanel.add(categoryButtonPanel, BorderLayout.SOUTH);

		JPanel categoryPanel = new JPanel(new BorderLayout(5, 5));
		categoryPanel.add(categoryTopPanel, BorderLayout.NORTH);
		categoryPanel.add(new JScrollPane(listCategories), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btnAddExpense);
		buttonPanel.add(btnUpdateExpense);
		buttonPanel.add(btnDeleteExpense);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(buttonPanel, BorderLayout.CENTER);
		bottomPanel.add(lblError, BorderLayout.SOUTH);

		add(inputPanel, BorderLayout.NORTH);
		add(new JScrollPane(listExpenses), BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		add(categoryPanel, BorderLayout.EAST);

		pack();
	}

	private void updateAddButton() {
		btnAddExpense.setEnabled(
			!txtDescription.getText().isEmpty() && !txtAmount.getText().isEmpty()
		);
	}

	public void showExpenses(List<Expense> expenses) {
		expenseListModel.clear();
		expenses.forEach(expenseListModel::addElement);
	}

	public void showError(String message) {
		lblError.setText(message);
	}

	public void showCategories(List<Category> categories) {
		categoryComboModel.removeAllElements();
		categoryListModel.clear();
		categories.forEach(categoryComboModel::addElement);
		categories.forEach(categoryListModel::addElement);
	}

	public String getDescriptionText() {
		return txtDescription.getText();
	}

	public String getAmountText() {
		return txtAmount.getText();
	}

	public void setDescriptionText(String text) {
		txtDescription.setText(text);
	}

	public void setAmountText(String text) {
		txtAmount.setText(text);
	}

	public Category getSelectedCategory() {
		return (Category) comboCategory.getSelectedItem();
	}

	public void setSelectedCategory(Category category) {
		comboCategory.setSelectedItem(category);
	}

	public Expense getSelectedExpense() {
		return listExpenses.getSelectedValue();
	}

	public Category getSelectedCategoryInList() {
		return listCategories.getSelectedValue();
	}

	public String getCategoryNameText() {
		return txtCategoryName.getText();
	}

	public void setCategoryNameText(String text) {
		txtCategoryName.setText(text);
	}

	public void addAddExpenseListener(ActionListener listener) {
		btnAddExpense.addActionListener(listener);
	}

	public void addDeleteExpenseListener(ActionListener listener) {
		btnDeleteExpense.addActionListener(listener);
	}

	public void addUpdateExpenseListener(ActionListener listener) {
		btnUpdateExpense.addActionListener(listener);
	}

	public void addExpenseSelectionListener(javax.swing.event.ListSelectionListener listener) {
		listExpenses.addListSelectionListener(listener);
	}

	public void addAddCategoryListener(ActionListener listener) {
		btnAddCategory.addActionListener(listener);
	}

	public void addUpdateCategoryListener(ActionListener listener) {
		btnUpdateCategory.addActionListener(listener);
	}

	public void addDeleteCategoryListener(ActionListener listener) {
		btnDeleteCategory.addActionListener(listener);
	}

	public void addCategorySelectionListener(ListSelectionListener listener) {
		listCategories.addListSelectionListener(listener);
	}
}
