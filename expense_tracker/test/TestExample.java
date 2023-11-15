// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
// import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Date;
import java.util.List;

import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import view.ExpenseTrackerView;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;


public class TestExample {
  
    private ExpenseTrackerModel model;
    private ExpenseTrackerView view;
    private ExpenseTrackerController controller;

    @Before
    public void setup() {
        model = new ExpenseTrackerModel();
        view = new ExpenseTrackerView();
        controller = new ExpenseTrackerController(model, view);
    }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }


    public void checkTransaction(double amount, String category, Transaction transaction) {
	    assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }

    public void checkHighlightedTable(boolean[] expectedRows){
        for (int row = 0; row < view.getTableModel().getRowCount(); row++) {
            boolean shouldBeHighlighted = expectedRows[row];
            for (int column = 0; column < view.getTableModel().getColumnCount(); column++) {
                TableCellRenderer renderer = view.getTransactionsTable().getCellRenderer(row, column);
                Component component = view.getTransactionsTable().prepareRenderer(renderer, row, column);
                Color backgroundColor = component.getBackground();
                // Check the color
                if (shouldBeHighlighted) {
                    assertEquals(new Color(173, 255, 168), backgroundColor); // Light green
                } else {
                    assertEquals(Color.WHITE, backgroundColor);
                }
            }
        }
    }

    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
	    //                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
	
	    // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }


    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
	    //                the added transaction
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        assertEquals(amount, getTotalCost(), 0.01);
	
	    // Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);
    
        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());
    
        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }

    @Test
    public void testAddTransactionView() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, view.getTableModel().getRowCount());
    
        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only the added
	    //                 transaction and the total row	
        assertEquals(2, view.getTableModel().getRowCount());
    
        // Check the contents of the list
        // Transaction firstTransaction = model.getTransactions().get(0);
        double firstAmount = (double) view.getTableModel().getValueAt(0, 1);
        String firstCategory = (String) view.getTableModel().getValueAt(0, 2);
        // checkTransaction(amount, category, firstTransaction);
	
	    // Check the amount and category of the first row
        assertEquals(amount, firstAmount, 0.01);
        assertEquals(category, firstCategory);

        // Check the total amount
        double totalCost = (double) view.getTableModel().getValueAt(1, 3);
        assertEquals(amount, totalCost, 0.01);
    }

    @Test
    public void testAddInvalidTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add an invalid transaction
        double amount = -50.0;
        String category = "food";
        assertFalse(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Add a valid transaction
        double amount2 = 20.0;
        assertTrue(controller.addTransaction(amount2, category));

        // Try to add an invalid transaction again
        assertFalse(controller.addTransaction(amount, category));

        // Post-condition: List of transactions contains only the valid
        //                 transaction and the total row
        assertEquals(2, view.getTableModel().getRowCount());

        // Check the first row
        double firstAmount = (double) view.getTableModel().getValueAt(0, 1);
        String firstCategory = (String) view.getTableModel().getValueAt(0, 2);
        assertEquals(amount2, firstAmount, 0.01);
        assertEquals(category, firstCategory);

        // Check the total amount
        double totalCost = (double) view.getTableModel().getValueAt(1, 3);
        assertEquals(amount2, totalCost, 0.01);
    }

    @Test
    public void testAmountFilter() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add multiple transactions
        assertTrue(controller.addTransaction(50, "food"));
        assertTrue(controller.addTransaction(20, "bills"));
        assertTrue(controller.addTransaction(50, "bills"));
        assertTrue(controller.addTransaction(30, "food"));
        assertTrue(controller.addTransaction(50, "food"));

        // Expected filtered rows
        boolean[] expectedRows = {true, false, true, false, true, false};
        
    
        // Post-condition: List of transactions contains only the added
        //                 transaction and the total row	
        assertEquals(6, view.getTableModel().getRowCount());
    
        // Perform the action: Apply an amount filter
        double amountFilter = 50.0;
        controller.setFilter(new AmountFilter(amountFilter));
        controller.applyFilter();

        // Post-condition: Only the transactions with the specified amount
        //                 are highlighted
        checkHighlightedTable(expectedRows);

        // Filter for a different amount
        amountFilter = 30.0;
        expectedRows = new boolean[]{false, false, false, true, false, false};

        // Perform the action: Apply an amount filter
        controller.setFilter(new AmountFilter(amountFilter));
        controller.applyFilter();

        // Post-condition: Only the transactions with the specified amount
        //                 are highlighted
        checkHighlightedTable(expectedRows);
    }

    @Test
    public void testCategoryFilter() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add multiple transactions
        assertTrue(controller.addTransaction(50, "food"));
        assertTrue(controller.addTransaction(20, "bills"));
        assertTrue(controller.addTransaction(50, "bills"));
        assertTrue(controller.addTransaction(30, "food"));
        assertTrue(controller.addTransaction(50, "food"));

        // Expected filtered rows
        boolean[] expectedRows = {true, false, false, true, true, false};
        
    
        // Post-condition: List of transactions contains only the added
        //                 transaction and the total row	
        assertEquals(6, view.getTableModel().getRowCount());
    
        // Perform the action: Apply a category filter
        String categoryFilter = "food";
        controller.setFilter(new CategoryFilter(categoryFilter));
        controller.applyFilter();

        // Post-condition: Only the transactions with the specified category
        //                 are highlighted
        checkHighlightedTable(expectedRows);

        // Filter for a different category
        categoryFilter = "bills";
        expectedRows = new boolean[]{false, true, true, false, false, false};

        // Perform the action: Apply a category filter
        controller.setFilter(new CategoryFilter(categoryFilter));
        controller.applyFilter();

        // Post-condition: Only the transactions with the specified category
        //                 are highlighted
        checkHighlightedTable(expectedRows);
    }

    @Test(expected = RuntimeException.class)
    public void testUndoDisallowed() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Try to perform the action: Remove a transaction (show give runtime error)
            controller.undo(1);
    }

    @Test
    public void testUndoAllowed() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add multiple transactions
        assertTrue(controller.addTransaction(50, "food"));
        assertTrue(controller.addTransaction(20, "bills"));
        assertTrue(controller.addTransaction(50, "bills"));
        assertTrue(controller.addTransaction(30, "food"));
        assertTrue(controller.addTransaction(50, "food"));

        // Check the total amount
        double totalCost = (double) view.getTableModel().getValueAt(5, 3);
        assertEquals(200, totalCost, 0.01);

        // Perform the action: Remove a transaction
        assertTrue(controller.undo(1));

        // Check the number of rows in the model
        assertEquals(4, model.getTransactions().size());

        // Check the number of rows in the view
        assertEquals(5, view.getTableModel().getRowCount());

        // Check the total amount
        assertEquals(180, getTotalCost(), 0.01);
    }
}
