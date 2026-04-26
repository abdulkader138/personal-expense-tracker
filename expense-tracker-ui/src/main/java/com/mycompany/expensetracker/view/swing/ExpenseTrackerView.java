package com.mycompany.expensetracker.view.swing;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.event.ActionListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;

public class ExpenseTrackerView extends JFrame {

	private static final long serialVersionUID = 1L;

	JTextField txtDescription;
	JTextField txtAmount;
	JComboBox<Category> comboCategory;
	JButton btnAddExpense;
	DefaultListModel<Expense> listExpensesModel;
	JList<Expense> listExpenses;
	JButton btnDeleteExpense;
	JLabel lblError;

	public ExpenseTrackerView() {
		setTitle("Expense Tracker");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(600, 400);

		txtDescription = new JTextField(20);
		txtDescription.setName("txtDescription");

		txtAmount = new JTextField(10);
		txtAmount.setName("txtAmount");

		comboCategory = new JComboBox<>();
		comboCategory.setName("comboCategory");

		btnAddExpense = new JButton("Add Expense");
		btnAddExpense.setName("btnAddExpense");
		btnAddExpense.setEnabled(false);

		listExpensesModel = new DefaultListModel<>();
		listExpenses = new JList<>(listExpensesModel);
		listExpenses.setName("listExpenses");

		btnDeleteExpense = new JButton("Delete Expense");
		btnDeleteExpense.setName("btnDeleteExpense");

		lblError = new JLabel(" ");
		lblError.setName("lblError");

		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateAddButton();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateAddButton();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateAddButton();
			}

			private void updateAddButton() {
				btnAddExpense.setEnabled(
					!txtDescription.getText().trim().isEmpty() &&
					!txtAmount.getText().trim().isEmpty()
				);
			}
		};

		txtDescription.getDocument().addDocumentListener(documentListener);
		txtAmount.getDocument().addDocumentListener(documentListener);

		getContentPane().setLayout(new java.awt.FlowLayout());
		getContentPane().add(new JLabel("Description:"));
		getContentPane().add(txtDescription);
		getContentPane().add(new JLabel("Amount:"));
		getContentPane().add(txtAmount);
		getContentPane().add(new JLabel("Category:"));
		getContentPane().add(comboCategory);
		getContentPane().add(btnAddExpense);
		getContentPane().add(new JScrollPane(listExpenses));
		getContentPane().add(btnDeleteExpense);
		getContentPane().add(lblError);
	}

	public void showExpenses(List<Expense> expenses) {
		listExpensesModel.clear();
		expenses.forEach(listExpensesModel::addElement);
	}

	public void showError(String message) {
		lblError.setText(message);
	}

	public void showCategories(List<Category> categories) {
		DefaultComboBoxModel<Category> model = new DefaultComboBoxModel<>();
		categories.forEach(model::addElement);
		comboCategory.setModel(model);
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

	public void addAddExpenseListener(ActionListener al) {
		btnAddExpense.addActionListener(al);
	}

	public void addDeleteExpenseListener(ActionListener al) {
		btnDeleteExpense.addActionListener(al);
	}
}
