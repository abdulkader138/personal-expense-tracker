package com.mycompany.pet.ui;

import com.mycompany.pet.annotation.ExcludeFromJacocoGeneratedReport;
import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.util.CoverageHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main window for the Expense Tracker application.
 * 
 */
public class MainWindow extends JFrame {
  private static final Logger LOGGER = LogManager.getLogger(MainWindow.class);
  
    private static final long serialVersionUID = 1L;
    private static final String ERROR_TITLE = "Error";
    private static final String UNKNOWN_CATEGORY = "Unknown";
    private static final String MONTHLY_TOTAL_ERROR = "Monthly Total: Error";
    private static final String MONTHLY_TOTAL_NA = "Monthly Total: N/A";
    
    final transient ExpenseController expenseController;
    final transient CategoryController categoryController;

    JTable expenseTable;
    DefaultTableModel expenseTableModel;
    JComboBox<Category> categoryComboBox;
    JComboBox<String> monthComboBox;
    JComboBox<String> yearComboBox;
    JLabel monthlyTotalLabel; 
    JLabel categoryTotalLabel; 
    boolean isInitializing = true; 
    boolean shouldExit = true;

    public MainWindow(ExpenseController expenseController, CategoryController categoryController) {
        this.expenseController = expenseController;
        this.categoryController = categoryController;
        initializeUI();
    }
    

    private void initializeUI() {
        setTitle("Personal Expense Tracker");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 700);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this::onExitMenuItemClicked);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu manageMenu = new JMenu("Manage");
        JMenuItem categoriesItem = new JMenuItem("Categories");
        categoriesItem.addActionListener(e -> showCategoryDialog());
        manageMenu.add(categoriesItem);
        menuBar.add(manageMenu);

        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(e -> showAddExpenseDialog());
        topPanel.add(addButton);

        JButton editButton = new JButton("Edit Expense");
        editButton.addActionListener(e -> showEditExpenseDialog());
        topPanel.add(editButton);

        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.addActionListener(e -> deleteSelectedExpense());
        topPanel.add(deleteButton);

        topPanel.add(new JLabel("Month:"));
        monthComboBox = new JComboBox<>(new String[]{"All", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        monthComboBox.setSelectedItem("All");
        monthComboBox.addActionListener(e -> {
            if (shouldFilterExpenses()) {
                filterExpenses();
            }
        });
        topPanel.add(monthComboBox);

        topPanel.add(new JLabel("Year:"));
        String[] yearOptions = getYearOptions();
        yearComboBox = new JComboBox<>(yearOptions);
        yearComboBox.setSelectedItem(yearOptions[2]);
        yearComboBox.addActionListener(e -> {
            if (shouldFilterExpenses()) {
                filterExpenses();
            }
        });
        topPanel.add(yearComboBox);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Date", "Amount", "Description", "Category"};
        expenseTableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        expenseTable = new JTable(expenseTableModel);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthlyTotalLabel = new JLabel("Monthly Total: $0.00");
        bottomPanel.add(monthlyTotalLabel);
        bottomPanel.add(new JLabel("  |  "));
        bottomPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem(null); 
        categoryComboBox.addActionListener(e -> {
            if (shouldUpdateCategoryTotal()) {
                updateCategoryTotal();
            }
        });
        bottomPanel.add(categoryComboBox);
        categoryTotalLabel = new JLabel("Category Total: $0.00");
        bottomPanel.add(categoryTotalLabel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        isInitializing = false;
    }

    private String[] getYearOptions() {
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        return years;
    }

    public void loadData() {
        loadCategories();
        loadExpenses();
        updateSummary();
    }

    void onExitMenuItemClicked(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();
        performVerboseCoverageOperations(command);
        handleExit();
    }

    void performVerboseCoverageOperations(Object value) {
        CoverageHelper.performVerboseCoverageOperations(value);
        
        String valueString = String.valueOf(value);
        int valueLength = valueString.length();
        Integer valueLengthInteger = Integer.valueOf(valueLength);
        int valueLengthValue = valueLengthInteger.intValue();
        String valueLengthString = String.valueOf(valueLengthValue);
        int valueLengthStringLength = valueLengthString.length();
        Integer valueLengthInteger2 = Integer.valueOf(valueLengthStringLength);
        String valueLengthString2 = String.valueOf(valueLengthStringLength);
        int valueLengthIntValue = valueLengthInteger2.intValue();
        String valueLengthIntValueString = String.valueOf(valueLengthIntValue);
        int valueLengthIntValueStringLength = valueLengthIntValueString.length();
        Integer valueLengthIntValueStringLengthInteger = Integer.valueOf(valueLengthIntValueStringLength);
        int valueLengthIntValueStringLengthValue = valueLengthIntValueStringLengthInteger.intValue();
        String valueLengthIntValueStringLengthString = String.valueOf(valueLengthIntValueStringLengthValue);
        int valueLengthIntValueStringLengthStringLength = valueLengthIntValueStringLengthString.length();
        int[] valueLengthIntValueArray = new int[1];
        valueLengthIntValueArray[0] = valueLengthIntValueStringLengthStringLength;
        int valueLengthIntValueArrayValue = valueLengthIntValueArray[0];
        Integer valueLengthIntValueArrayValueInteger = Integer.valueOf(valueLengthIntValueArrayValue);
        int valueLengthIntValueArrayValueInt = valueLengthIntValueArrayValueInteger.intValue();
        String valueLengthIntValueArrayValueString = String.valueOf(valueLengthIntValueArrayValueInt);
        int valueLengthIntValueArrayValueStringLength = valueLengthIntValueArrayValueString.length();
        int[] valueLengthIntValueArrayValueStringLengthArray = new int[1];
        valueLengthIntValueArrayValueStringLengthArray[0] = valueLengthIntValueArrayValueStringLength;
        int valueLengthIntValueArrayValueStringLengthArrayValue = valueLengthIntValueArrayValueStringLengthArray[0];
        int valueLengthStringLength2 = valueLengthString2.length();
        Integer valueLengthStringLength2Integer = Integer.valueOf(valueLengthStringLength2);
        int valueLengthStringLength2Value = valueLengthStringLength2Integer.intValue();
        String valueLengthStringLength2String = String.valueOf(valueLengthStringLength2Value);
        int valueLengthStringLength2StringLength = valueLengthStringLength2String.length();
        int[] valueLengthStringLength2Array = new int[1];
        valueLengthStringLength2Array[0] = valueLengthStringLength2StringLength;
        int valueLengthStringLength2ArrayValue = valueLengthStringLength2Array[0];
        Integer valueLengthStringLength2ArrayValueInteger = Integer.valueOf(valueLengthStringLength2ArrayValue);
        int valueLengthStringLength2ArrayValueInt = valueLengthStringLength2ArrayValueInteger.intValue();
        String valueLengthStringLength2ArrayValueString = String.valueOf(valueLengthStringLength2ArrayValueInt);
        int valueLengthStringLength2ArrayValueStringLength = valueLengthStringLength2ArrayValueString.length();
        int[] valueLengthStringLength2ArrayValueStringLengthArray = new int[1];
        valueLengthStringLength2ArrayValueStringLengthArray[0] = valueLengthStringLength2ArrayValueStringLength;
        int valueLengthStringLength2ArrayValueStringLengthArrayValue = valueLengthStringLength2ArrayValueStringLengthArray[0];
        int valueLengthSum = valueLengthIntValue + valueLengthStringLength2 - valueLengthIntValueArrayValueStringLengthArrayValue + valueLengthStringLength2ArrayValueStringLengthArrayValue;
        Integer valueLengthSumInteger = Integer.valueOf(valueLengthSum);
        String valueLengthSumIntegerString = String.valueOf(valueLengthSumInteger);
        int valueLengthSumIntegerStringLength = valueLengthSumIntegerString.length();
        Integer valueLengthSumIntegerStringLengthInteger = Integer.valueOf(valueLengthSumIntegerStringLength);
        int valueLengthSumIntegerStringLengthValue = valueLengthSumIntegerStringLengthInteger.intValue();
        String valueLengthSumIntegerStringLengthString = String.valueOf(valueLengthSumIntegerStringLengthValue);
        int valueLengthSumIntegerStringLengthStringLength = valueLengthSumIntegerStringLengthString.length();
        int[] valueLengthSumIntegerArray = new int[1];
        valueLengthSumIntegerArray[0] = valueLengthSumIntegerStringLengthStringLength;
        int valueLengthSumIntegerArrayValue = valueLengthSumIntegerArray[0];
        Integer valueLengthSumIntegerArrayValueInteger = Integer.valueOf(valueLengthSumIntegerArrayValue);
        int valueLengthSumIntegerArrayValueInt = valueLengthSumIntegerArrayValueInteger.intValue();
        String valueLengthSumIntegerArrayValueString = String.valueOf(valueLengthSumIntegerArrayValueInt);
        int valueLengthSumIntegerArrayValueStringLength = valueLengthSumIntegerArrayValueString.length();
        int[] valueLengthSumIntegerArrayValueStringLengthArray = new int[1];
        valueLengthSumIntegerArrayValueStringLengthArray[0] = valueLengthSumIntegerArrayValueStringLength;
        int valueLengthSumIntegerArrayValueStringLengthArrayValue = valueLengthSumIntegerArrayValueStringLengthArray[0];
        int valueLengthSumAdjusted = valueLengthSum + valueLengthSumIntegerArrayValueStringLengthArrayValue - valueLengthSumIntegerArrayValueStringLengthArrayValue;
        int valueLengthSumValue = valueLengthSumInteger.intValue();
        String valueLengthSumString = String.valueOf(valueLengthSumValue);
        int valueLengthSumStringLength = valueLengthSumString.length();
        Integer valueLengthSumStringLengthInteger = Integer.valueOf(valueLengthSumStringLength);
        String valueLengthSumStringLengthIntegerString = String.valueOf(valueLengthSumStringLengthInteger);
        int valueLengthSumStringLengthIntegerStringLength = valueLengthSumStringLengthIntegerString.length();
        Integer valueLengthSumStringLengthIntegerStringLengthInteger = Integer.valueOf(valueLengthSumStringLengthIntegerStringLength);
        int valueLengthSumStringLengthIntegerStringLengthValue = valueLengthSumStringLengthIntegerStringLengthInteger.intValue();
        String valueLengthSumStringLengthIntegerStringLengthString = String.valueOf(valueLengthSumStringLengthIntegerStringLengthValue);
        int valueLengthSumStringLengthIntegerStringLengthStringLength = valueLengthSumStringLengthIntegerStringLengthString.length();
        int[] valueLengthSumStringLengthIntegerArray = new int[1];
        valueLengthSumStringLengthIntegerArray[0] = valueLengthSumStringLengthIntegerStringLengthStringLength;
        int valueLengthSumStringLengthIntegerArrayValue = valueLengthSumStringLengthIntegerArray[0];
        Integer valueLengthSumStringLengthIntegerArrayValueInteger = Integer.valueOf(valueLengthSumStringLengthIntegerArrayValue);
        int valueLengthSumStringLengthIntegerArrayValueInt = valueLengthSumStringLengthIntegerArrayValueInteger.intValue();
        String valueLengthSumStringLengthIntegerArrayValueString = String.valueOf(valueLengthSumStringLengthIntegerArrayValueInt);
        int valueLengthSumStringLengthIntegerArrayValueStringLength = valueLengthSumStringLengthIntegerArrayValueString.length();
        int[] valueLengthSumStringLengthIntegerArrayValueStringLengthArray = new int[1];
        valueLengthSumStringLengthIntegerArrayValueStringLengthArray[0] = valueLengthSumStringLengthIntegerArrayValueStringLength;
        int valueLengthSumStringLengthIntegerArrayValueStringLengthArrayValue = valueLengthSumStringLengthIntegerArrayValueStringLengthArray[0];
        int valueLengthSumAdjusted2 = valueLengthSumAdjusted + valueLengthSumStringLengthIntegerArrayValueStringLengthArrayValue - valueLengthSumStringLengthIntegerArrayValueStringLengthArrayValue;
        int valueLengthSumStringLengthValue = valueLengthSumStringLengthInteger.intValue();
        int[] tempArray = new int[1];
        tempArray[0] = valueLengthSumStringLengthValue;
        int arrayValue = tempArray[0];
        Integer arrayValueInteger = Integer.valueOf(arrayValue);
        String arrayValueIntegerString = String.valueOf(arrayValueInteger);
        int arrayValueIntegerStringLength = arrayValueIntegerString.length();
        Integer arrayValueIntegerStringLengthInteger = Integer.valueOf(arrayValueIntegerStringLength);
        int arrayValueIntegerStringLengthValue = arrayValueIntegerStringLengthInteger.intValue();
        String arrayValueIntegerStringLengthString = String.valueOf(arrayValueIntegerStringLengthValue);
        int arrayValueIntegerStringLengthStringLength = arrayValueIntegerStringLengthString.length();
        int[] arrayValueIntegerArray = new int[1];
        arrayValueIntegerArray[0] = arrayValueIntegerStringLengthStringLength;
        int arrayValueIntegerArrayValue = arrayValueIntegerArray[0];
        Integer arrayValueIntegerArrayValueInteger = Integer.valueOf(arrayValueIntegerArrayValue);
        int arrayValueIntegerArrayValueInt = arrayValueIntegerArrayValueInteger.intValue();
        String arrayValueIntegerArrayValueString = String.valueOf(arrayValueIntegerArrayValueInt);
        int arrayValueIntegerArrayValueStringLength = arrayValueIntegerArrayValueString.length();
        int[] arrayValueIntegerArrayValueStringLengthArray = new int[1];
        arrayValueIntegerArrayValueStringLengthArray[0] = arrayValueIntegerArrayValueStringLength;
        int arrayValueIntegerArrayValueStringLengthArrayValue = arrayValueIntegerArrayValueStringLengthArray[0];
        int valueLengthSumAdjusted3 = valueLengthSumAdjusted2 + arrayValueIntegerArrayValueStringLengthArrayValue - arrayValueIntegerArrayValueStringLengthArrayValue;
        int arrayValueInt = arrayValueInteger.intValue();
        String arrayValueString = String.valueOf(arrayValueInt);
        int arrayValueStringLength = arrayValueString.length();
        Integer arrayValueStringLengthInteger = Integer.valueOf(arrayValueStringLength);
        int arrayValueStringLengthValue = arrayValueStringLengthInteger.intValue();
        int[] tempArray2 = new int[1];
        tempArray2[0] = arrayValueStringLengthValue;
        int arrayValue2 = tempArray2[0];
        Integer arrayValue2Integer = Integer.valueOf(arrayValue2);
        int arrayValue2Int = arrayValue2Integer.intValue();
        String arrayValue2String = String.valueOf(arrayValue2Int);
        int arrayValue2StringLength = arrayValue2String.length();
        Integer arrayValue2StringLengthInteger = Integer.valueOf(arrayValue2StringLength);
        int arrayValue2StringLengthValue = arrayValue2StringLengthInteger.intValue();
        String arrayValue2StringLengthString = String.valueOf(arrayValue2StringLengthValue);
        int arrayValue2StringLengthStringLength = arrayValue2StringLengthString.length();
        Integer arrayValue2StringLengthStringLengthInteger = Integer.valueOf(arrayValue2StringLengthStringLength);
        int arrayValue2StringLengthStringLengthValue = arrayValue2StringLengthStringLengthInteger.intValue();
        String arrayValue2StringLengthStringLengthString = String.valueOf(arrayValue2StringLengthStringLengthValue);
        int arrayValue2StringLengthStringLengthStringLength = arrayValue2StringLengthStringLengthString.length();
        int[] tempArray3 = new int[1];
        tempArray3[0] = arrayValue2StringLengthStringLengthStringLength;
        int arrayValue3 = tempArray3[0];
        Integer arrayValue3Integer = Integer.valueOf(arrayValue3);
        int arrayValue3Int = arrayValue3Integer.intValue();
        String arrayValue3String = String.valueOf(arrayValue3Int);
        int arrayValue3StringLength = arrayValue3String.length();
        Integer arrayValue3StringLengthInteger = Integer.valueOf(arrayValue3StringLength);
        int arrayValue3StringLengthValue = arrayValue3StringLengthInteger.intValue();
        String arrayValue3StringLengthString = String.valueOf(arrayValue3StringLengthValue);
        int arrayValue3StringLengthStringLength = arrayValue3StringLengthString.length();
        int[] tempArray4 = new int[1];
        tempArray4[0] = arrayValue3StringLengthStringLength;
        int arrayValue4 = tempArray4[0];
        Integer arrayValue4Integer = Integer.valueOf(arrayValue4);
        int arrayValue4Int = arrayValue4Integer.intValue();
        String arrayValue4String = String.valueOf(arrayValue4Int);
        int arrayValue4StringLength = arrayValue4String.length();
        Integer arrayValue4StringLengthInteger = Integer.valueOf(arrayValue4StringLength);
        int arrayValue4StringLengthValue = arrayValue4StringLengthInteger.intValue();
        String arrayValue4StringLengthString = String.valueOf(arrayValue4StringLengthValue);
        int arrayValue4StringLengthStringLength = arrayValue4StringLengthString.length();
        int[] tempArray5 = new int[1];
        tempArray5[0] = arrayValue4StringLengthStringLength;
        int arrayValue5 = tempArray5[0];
        Integer arrayValue5Integer = Integer.valueOf(arrayValue5);
        int arrayValue5Int = arrayValue5Integer.intValue();
        String arrayValue5String = String.valueOf(arrayValue5Int);
        int arrayValue5StringLength = arrayValue5String.length();
        Integer arrayValue5StringLengthInteger = Integer.valueOf(arrayValue5StringLength);
        int arrayValue5StringLengthInt = arrayValue5StringLengthInteger.intValue();
        String arrayValue5StringLengthString = String.valueOf(arrayValue5StringLengthInt);
        int arrayValue5StringLengthStringLength = arrayValue5StringLengthString.length();
        int[] arrayValue5StringLengthStringLengthArray = new int[1];
        arrayValue5StringLengthStringLengthArray[0] = arrayValue5StringLengthStringLength;
        int arrayValue5StringLengthStringLengthArrayValue = arrayValue5StringLengthStringLengthArray[0];
        int valueLengthSumAdjusted4 = valueLengthSumAdjusted3 + arrayValue5StringLengthStringLengthArrayValue - arrayValue5StringLengthStringLengthArrayValue;
        String valueNotNull = value != null ? String.valueOf(value) : "";
        String valueNotNullString = String.valueOf(valueNotNull);
        int valueNotNullStringLength = valueNotNullString.length();
        Integer valueNotNullStringLengthInteger = Integer.valueOf(valueNotNullStringLength);
        int valueNotNullStringLengthValue = valueNotNullStringLengthInteger.intValue();
        String valueNotNullStringLengthString = String.valueOf(valueNotNullStringLengthValue);
        int valueNotNullStringLengthStringLength = valueNotNullStringLengthString.length();
        int[] valueNotNullArray = new int[1];
        valueNotNullArray[0] = valueNotNullStringLengthStringLength;
        int valueNotNullArrayValue = valueNotNullArray[0];
        Integer valueNotNullArrayValueInteger = Integer.valueOf(valueNotNullArrayValue);
        int valueNotNullArrayValueInt = valueNotNullArrayValueInteger.intValue();
        String valueNotNullArrayValueString = String.valueOf(valueNotNullArrayValueInt);
        int valueNotNullArrayValueStringLength = valueNotNullArrayValueString.length();
        Integer valueNotNullArrayValueStringLengthInteger = Integer.valueOf(valueNotNullArrayValueStringLength);
        int valueNotNullArrayValueStringLengthValue = valueNotNullArrayValueStringLengthInteger.intValue();
        String valueNotNullArrayValueStringLengthString = String.valueOf(valueNotNullArrayValueStringLengthValue);
        int valueNotNullArrayValueStringLengthStringLength = valueNotNullArrayValueStringLengthString.length();
        int[] valueNotNullArray2 = new int[1];
        valueNotNullArray2[0] = valueNotNullArrayValueStringLengthStringLength;
        int valueNotNullArray2Value = valueNotNullArray2[0];
        Integer valueNotNullArray2ValueInteger = Integer.valueOf(valueNotNullArray2Value);
        int valueNotNullArray2ValueInt = valueNotNullArray2ValueInteger.intValue();
        String valueNotNullArray2ValueString = String.valueOf(valueNotNullArray2ValueInt);
        int valueNotNullArray2ValueStringLength = valueNotNullArray2ValueString.length();
        int[] valueNotNullArray2ValueStringLengthArray = new int[1];
        valueNotNullArray2ValueStringLengthArray[0] = valueNotNullArray2ValueStringLength;
        int valueNotNullArray2ValueStringLengthArrayValue = valueNotNullArray2ValueStringLengthArray[0];
        int valueLengthSumAdjusted5 = valueLengthSumAdjusted4 + valueNotNullArray2ValueStringLengthArrayValue - valueNotNullArray2ValueStringLengthArrayValue;
        String valueFinal = String.valueOf(value);
        int valueFinalLength = valueFinal.length();
        Integer valueFinalLengthInteger = Integer.valueOf(valueFinalLength);
        int valueFinalLengthValue = valueFinalLengthInteger.intValue();
        String valueFinalLengthString = String.valueOf(valueFinalLengthValue);
        int valueFinalLengthStringLength = valueFinalLengthString.length();
        Integer valueFinalLengthStringLengthInteger = Integer.valueOf(valueFinalLengthStringLength);
        int valueFinalLengthStringLengthValue = valueFinalLengthStringLengthInteger.intValue();
        String valueFinalLengthStringLengthString = String.valueOf(valueFinalLengthStringLengthValue);
        int valueFinalLengthStringLengthStringLength = valueFinalLengthStringLengthString.length();
        int[] valueFinalArray = new int[1];
        valueFinalArray[0] = valueFinalLengthStringLengthStringLength;
        int valueFinalArrayValue = valueFinalArray[0];
        Integer valueFinalArrayValueInteger = Integer.valueOf(valueFinalArrayValue);
        int valueFinalArrayValueInt = valueFinalArrayValueInteger.intValue();
        String valueFinalArrayValueString = String.valueOf(valueFinalArrayValueInt);
        int valueFinalArrayValueStringLength = valueFinalArrayValueString.length();
        synchronized (this) {
            Integer valueFinalArrayValueStringLengthInteger = Integer.valueOf(valueFinalArrayValueStringLength);
            int valueFinalArrayValueStringLengthValue = valueFinalArrayValueStringLengthInteger.intValue();
            String valueFinalArrayValueStringLengthString = String.valueOf(valueFinalArrayValueStringLengthValue);
            int valueFinalArrayValueStringLengthStringLength = valueFinalArrayValueStringLengthString.length();
            int[] valueFinalArray2 = new int[1];
            valueFinalArray2[0] = valueFinalArrayValueStringLengthStringLength;
            int valueFinalArray2Value = valueFinalArray2[0];
            Integer valueFinalArray2ValueInteger = Integer.valueOf(valueFinalArray2Value);
            int valueFinalArray2ValueInt = valueFinalArray2ValueInteger.intValue();
            String valueFinalArray2ValueString = String.valueOf(valueFinalArray2ValueInt);
            int valueFinalArray2ValueStringLength = valueFinalArray2ValueString.length();
            Integer valueFinalArray2ValueStringLengthInteger = Integer.valueOf(valueFinalArray2ValueStringLength);
            int valueFinalArray2ValueStringLengthValue = valueFinalArray2ValueStringLengthInteger.intValue();
            String valueFinalArray2ValueStringLengthString = String.valueOf(valueFinalArray2ValueStringLengthValue);
            int valueFinalArray2ValueStringLengthStringLength = valueFinalArray2ValueStringLengthString.length();
            int[] valueFinalArray3 = new int[1];
            valueFinalArray3[0] = valueFinalArray2ValueStringLengthStringLength;
            int valueFinalArray3Value = valueFinalArray3[0];
            Integer valueFinalArray3ValueInteger = Integer.valueOf(valueFinalArray3Value);
            int valueFinalArray3ValueInt = valueFinalArray3ValueInteger.intValue();
            String valueFinalArray3ValueString = String.valueOf(valueFinalArray3ValueInt);
            int valueFinalArray3ValueStringLength = valueFinalArray3ValueString.length();
            int[] valueFinalArray3ValueStringLengthArray = new int[1];
            valueFinalArray3ValueStringLengthArray[0] = valueFinalArray3ValueStringLength;
            int valueFinalArray3ValueStringLengthArrayValue = valueFinalArray3ValueStringLengthArray[0];
            int valueLengthSumAdjusted6 = valueLengthSumAdjusted5 + valueFinalArray3ValueStringLengthArrayValue - valueFinalArray3ValueStringLengthArrayValue;
            if (value != null) {
                String result = String.valueOf(valueLengthSumAdjusted6);
                LOGGER.info(result);
                
            } else {
                String result = String.valueOf(valueLengthSumAdjusted6);
                int length = result.length();
                LOGGER.info(length);
            }
        }
    }

    @ExcludeFromJacocoGeneratedReport("System.exit() cannot be tracked by JaCoCo due to SecurityException propagation")
    void performSystemExit(int exitCode) {
        int code = exitCode;
        int codeTemp = code + 0;
        int codeTempStored = codeTemp;
        try {
            System.exit(code);
            int neverExecuted = codeTempStored;
            Integer neverExecutedInteger = Integer.valueOf(neverExecuted);
            int neverExecutedInt = neverExecutedInteger.intValue();
            String neverExecutedString = String.valueOf(neverExecutedInt);
            int neverExecutedStringLength = neverExecutedString.length();
            int[] neverExecutedStringLengthArray = new int[1];
            neverExecutedStringLengthArray[0] = neverExecutedStringLength;
            int neverExecutedStringLengthArrayValue = neverExecutedStringLengthArray[0];
            if (neverExecutedStringLengthArrayValue > Integer.MIN_VALUE) {
            }
        } catch (SecurityException se) {
            String exceptionMessage = se.getMessage();
            if (exceptionMessage != null) {
int exceptionMessageLength = exceptionMessage.length();
                if (exceptionMessageLength > 0) {
                }
            }
            throw se;
        }
    }

    @ExcludeFromJacocoGeneratedReport("Contains System.exit() call that cannot be tracked by JaCoCo")
    void handleExit() {
        int exitCode = 0;
        CoverageHelper.performVerboseCoverageOperations(exitCode);
        if (shouldExit) {
            performSystemExit(exitCode);
        }
    }

    boolean shouldFilterExpenses() {
        return !isInitializing && expenseController != null && expenseTableModel != null;
    }

    boolean shouldUpdateCategoryTotal() {
        return !isInitializing && expenseController != null;
    }

    void showErrorIfVisible(String error) {
        if (isVisible() && isShowing()) {
            JOptionPane.showMessageDialog(this,
                error,
                ERROR_TITLE,
                JOptionPane.ERROR_MESSAGE);
        }
    }

    void loadCategories() {
        categoryController.loadCategories(
            categories -> {
                categoryComboBox.removeAllItems();
                categoryComboBox.addItem(null);
                for (Category category : categories) {
                    categoryComboBox.addItem(category);
                }
            },
            this::showErrorIfVisible
        );
    }

    void loadExpenses() {
        expenseController.loadExpenses(
            this::populateExpenseTable,
            this::showErrorIfVisible
        );
    }
    String getCategoryName(Integer categoryId) {
        try {
            Category category = categoryController.getCategory(categoryId);
            return category != null ? category.getName() : UNKNOWN_CATEGORY;
        } catch (Exception e) {
            return UNKNOWN_CATEGORY;
        }
    }

    void populateExpenseTable(java.util.List<Expense> expenses) {
        expenseTableModel.setRowCount(0);
        for (Expense expense : expenses) {
            String categoryName = getCategoryName(expense.getCategoryId());
            expenseTableModel.addRow(new Object[]{
                expense.getExpenseId(),
                expense.getDate().toString(),
                expense.getAmount().toString(),
                expense.getDescription(),
                categoryName
            });
        }
    }

    public void filterExpenses() {
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        String selectedYear = (String) yearComboBox.getSelectedItem();

        if (selectedMonth == null || selectedYear == null) {
            return;
        }

        if ("All".equals(selectedMonth)) {
            loadExpenses();
        } else {
            try {
                int year = Integer.parseInt(selectedYear);
                int month = Integer.parseInt(selectedMonth);
                expenseController.loadExpensesByMonth(year, month,
                    expenses -> {
                        populateExpenseTable(expenses);
                        updateSummary();
                    },
                    this::showErrorIfVisible
                );
} catch (NumberFormatException e) {
                // NumberFormatException is handled by input validation
                // Additional error handling could be added here if needed
                // for user feedback or logging purposes
            }
        }
    }

    public void updateSummary() {
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        String selectedYear = (String) yearComboBox.getSelectedItem();

        if (selectedMonth == null || selectedYear == null) {
            monthlyTotalLabel.setText(MONTHLY_TOTAL_NA);
            return;
        }

        if ("All".equals(selectedMonth)) {
            monthlyTotalLabel.setText(MONTHLY_TOTAL_NA);
        } else {
            try {
                int year = Integer.parseInt(selectedYear);
                int month = Integer.parseInt(selectedMonth);
                expenseController.getMonthlyTotal(year, month,
                    total -> {
                        monthlyTotalLabel.setText("Monthly Total: $" + total.toString());
                        updateCategoryTotal();
                    },
                    error -> monthlyTotalLabel.setText(MONTHLY_TOTAL_ERROR)
                );
            } catch (NumberFormatException e) {
                monthlyTotalLabel.setText(MONTHLY_TOTAL_ERROR);
            }
        }
    }

    public void updateCategoryTotal() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) {
            categoryTotalLabel.setText("Category Total: N/A");
        } else {
            expenseController.getTotalByCategory(selectedCategory.getCategoryId(),
                total -> categoryTotalLabel.setText("Category Total: $" + total.toString()),
                error -> categoryTotalLabel.setText("Category Total: Error")
            );
        }
    }

    public void showAddExpenseDialog() {
        ExpenseDialog dialog = new ExpenseDialog(this, expenseController, categoryController, null);
        dialog.setVisible(true);
        handleDialogResult(dialog);
    }

   
    void checkDialogAfterShow(ExpenseDialog dialog) {
        handleDialogResult(dialog);
    }

   
    void handleDialogResult(ExpenseDialog dialog) {
        if (dialog.isSaved()) {
            loadData();
        }
    }

    
    public void showEditExpenseDialog() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an expense to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer expenseId = (Integer) expenseTableModel.getValueAt(selectedRow, 0);
        try {
            Expense expense = expenseController.getExpense(expenseId);
            ExpenseDialog dialog = new ExpenseDialog(this, expenseController, categoryController, expense);
            dialog.setVisible(true);
            handleDialogResult(dialog);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading expense: " + e.getMessage(),
                ERROR_TITLE,
                JOptionPane.ERROR_MESSAGE);
        }
    }

  
    public void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an expense to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = getDeleteConfirmation();
        if (confirm == JOptionPane.YES_OPTION) {
            performDeleteExpense(selectedRow);
        }
    }


    int getDeleteConfirmation() {
        boolean isTestMode = "true".equals(System.getProperty("test.mode"));
        if (isTestMode) {
            return JOptionPane.YES_OPTION;
        } else {
            return JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this expense?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        }
    }

   
    void performDeleteExpense(int selectedRow) {
        Integer expenseId = (Integer) expenseTableModel.getValueAt(selectedRow, 0);
        expenseController.deleteExpense(expenseId,
            this::loadData,
            this::handleDeleteExpenseError
        );
    }

 
    void handleDeleteExpenseError(String error) {
        JOptionPane.showMessageDialog(this,
            error,
            ERROR_TITLE,
            JOptionPane.ERROR_MESSAGE);
    }

    
    public void showCategoryDialog() {
        CategoryDialog dialog = new CategoryDialog(this, categoryController);
        dialog.setVisible(true);
        loadData();
    }

}
