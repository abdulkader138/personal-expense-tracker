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

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;

public class ExpenseTrackerView extends JFrame {

	JTextField txtDescription;
	JTextField txtAmount;
	JButton btnAddExpense;
	JButton btnDeleteExpense;
	JList<Expense> listExpenses;

	private JLabel lblError;
	private JComboBox<Category> comboCategory;
	private DefaultListModel<Expense> expenseListModel;
	private DefaultComboBoxModel<Category> categoryComboModel;

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

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btnAddExpense);
		buttonPanel.add(btnDeleteExpense);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(buttonPanel, BorderLayout.CENTER);
		bottomPanel.add(lblError, BorderLayout.SOUTH);

		add(inputPanel, BorderLayout.NORTH);
		add(new JScrollPane(listExpenses), BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

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
		categories.forEach(categoryComboModel::addElement);
	}

	public String getDescriptionText() {
		return txtDescription.getText();
	}

	public String getAmountText() {
		return txtAmount.getText();
	}

	public Category getSelectedCategory() {
		return (Category) comboCategory.getSelectedItem();
	}

	public Expense getSelectedExpense() {
		return listExpenses.getSelectedValue();
	}

	public void addAddExpenseListener(ActionListener listener) {
		btnAddExpense.addActionListener(listener);
	}

	public void addDeleteExpenseListener(ActionListener listener) {
		btnDeleteExpense.addActionListener(listener);
	}
}
