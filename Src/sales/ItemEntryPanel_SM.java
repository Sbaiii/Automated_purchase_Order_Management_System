import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

// Panel for Sales Manager to view, add, edit, and delete items
public class ItemEntryPanel_SM extends JPanel {
    // Table model for managing item data
    private final DefaultTableModel tableModel;

    // Constructor: sets up the UI and event handlers
    public ItemEntryPanel_SM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title and search bar ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel title = new JLabel("Items Entry", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        topPanel.add(title);
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel(" Search: ");
        searchLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        JTextField searchField = new JTextField();
        searchField.setToolTipText("Search by any field...");
        searchField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE SETUP: Shows all items ---
        String[] columnNames = {"Code", "Name", "Supplier ID", "Qty", "Unit Price", "Purchase Price", "Category", "Date", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable itemTable = new JTable(tableModel) {
            // Custom row coloring for better readability
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 235, 235) : Color.WHITE);
                } else {
                    c.setBackground(new Color(255, 200, 200));
                }
                return c;
            }
        };
        itemTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        itemTable.setRowHeight(28);
        itemTable.setGridColor(new Color(255, 210, 210));
        itemTable.setShowGrid(true);
        itemTable.setSelectionBackground(new Color(255, 200, 200));
        itemTable.setSelectionForeground(Color.BLACK);
        itemTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 15));
        itemTable.getTableHeader().setBackground(new Color(255, 210, 210));
        itemTable.getTableHeader().setForeground(new Color(120, 30, 30));
        itemTable.setFillsViewportHeight(true);
        // Adjust column widths for better display
        int[] colWidths = {90, 140, 100, 60, 80, 100, 100, 100, 160};
        for (int i = 0; i < colWidths.length && i < itemTable.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane tableScroll = new JScrollPane(itemTable);
        add(tableScroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: Add, Edit, Delete ---
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load items from file into the table
        loadItems();

        // --- SEARCH FILTER LISTENER ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(searchField.getText().trim().toLowerCase()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(searchField.getText().trim().toLowerCase()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // --- ADD BUTTON HANDLER ---
        addBtn.addActionListener(_ -> {
            // Create input fields for new item
            JTextField nameField = new JTextField();
            JTextField supplierField = new JTextField();
            JTextField qtyField = new JTextField();
            JTextField priceField = new JTextField();
            JTextField purchasePriceField = new JTextField();
            JTextField categoryField = new JTextField();
            JTextField noteField = new JTextField();
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Item Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Supplier ID (SUPxxx):"));
            panel.add(supplierField);
            panel.add(new JLabel("Quantity:"));
            panel.add(qtyField);
            panel.add(new JLabel("Price (Selling):"));
            panel.add(priceField);
            panel.add(new JLabel("Purchase Price:"));
            panel.add(purchasePriceField);
            panel.add(new JLabel("Category:"));
            panel.add(categoryField);
            panel.add(new JLabel("Date:"));
            panel.add(dateSpinner);
            panel.add(new JLabel("Notes:"));
            panel.add(noteField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Add New Item", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String supplierId = supplierField.getText().trim();
                    if (!supplierExists(supplierId)) {
                        JOptionPane.showMessageDialog(null, "❌ Supplier ID not found in data/suppliers_data.txt", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String nextCode = generateNextItemCode();
                    String newLine = String.join(",",
                            nextCode,
                            nameField.getText().trim(),
                            supplierId,
                            qtyField.getText().trim(),
                            priceField.getText().trim(),
                            purchasePriceField.getText().trim(),
                            categoryField.getText().trim(),
                            new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue()),
                            noteField.getText().trim()
                    );

                    File_Utils.appendLine("data/items_data.txt", newLine);
                    tableModel.addRow(newLine.split(","));
                    JOptionPane.showMessageDialog(null, "✅ Item added successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Failed to add item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- EDIT BUTTON HANDLER ---
        editBtn.addActionListener(_ -> {
            // Prompt for a keyword to search for the item to edit
            String keyword = JOptionPane.showInputDialog("Enter any part of Item Code, Name, Supplier ID, Qty, Unit Price, Purchase Price, Category, Date, or Notes to edit:");
            if (keyword == null || keyword.isBlank()) return;
            String searchKey = keyword.trim().toLowerCase();

            List<String> lines = File_Utils.readLines("data/items_data.txt");
            DefaultTableModel searchModel = new DefaultTableModel(new String[]{"Code", "Name", "Supplier ID", "Qty", "Unit Price", "Purchase Price", "Category", "Date", "Notes"}, 0);
            JTable resultTable = new JTable(searchModel);

            // Search for matching items
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 9) {
                    boolean match = false;
                    for (int i = 0; i < parts.length; i++) {
                        String field = parts[i].toLowerCase();
                        if (field.contains(searchKey)) {
                            match = true;
                            break;
                        }
                        if (i == 0) { // Code field
                            if (field.startsWith("itm") && searchKey.matches("\\d+")) {
                                String codeNum = parts[0].substring(3);
                                if (codeNum.equals(searchKey)) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (match) searchModel.addRow(parts);
                }
            }

            if (searchModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No matching items found.");
                return;
            }

            JScrollPane resultScroll = new JScrollPane(resultTable);
            resultScroll.setPreferredSize(new Dimension(700, 150));
            int pick = JOptionPane.showConfirmDialog(null, resultScroll, "Select Item to Edit", JOptionPane.OK_CANCEL_OPTION);
            if (pick != JOptionPane.OK_OPTION || resultTable.getSelectedRow() == -1) return;

            int rowIndex = resultTable.getSelectedRow();
            String[] selected = new String[9];
            for (int i = 0; i < 9; i++) selected[i] = (String) searchModel.getValueAt(rowIndex, i);

            // Create input fields pre-filled with selected item data
            JTextField qtyField = new JTextField(selected[3]);
            JTextField priceField = new JTextField(selected[4]);
            JTextField purchasePriceField = new JTextField(selected[5]);
            JTextField categoryField = new JTextField(selected[6]);
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            JTextField noteField = new JTextField(selected[8]);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Quantity:"));
            panel.add(qtyField);
            panel.add(new JLabel("Price (Selling):"));
            panel.add(priceField);
            panel.add(new JLabel("Purchase Price:"));
            panel.add(purchasePriceField);
            panel.add(new JLabel("Category:"));
            panel.add(categoryField);
            panel.add(new JLabel("Date:"));
            panel.add(dateSpinner);
            panel.add(new JLabel("Notes:"));
            panel.add(noteField);

            int update = JOptionPane.showConfirmDialog(null, panel, "Edit Item", JOptionPane.OK_CANCEL_OPTION);
            if (update == JOptionPane.OK_OPTION) {
                try {
                    String newLine = String.join(",",
                            selected[0], selected[1], selected[2],
                            qtyField.getText().trim(),
                            priceField.getText().trim(),
                            purchasePriceField.getText().trim(),
                            categoryField.getText().trim(),
                            new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue()),
                            noteField.getText().trim()
                    );

                    List<String> updated = new ArrayList<>();
                    for (String line : lines) {
                        if (line.startsWith(selected[0] + ",")) {
                            updated.add(newLine);
                        } else {
                            updated.add(line);
                        }
                    }
                    File_Utils.writeLines("data/items_data.txt", new ArrayList<>(updated));
                    loadItems();
                    JOptionPane.showMessageDialog(null, "✅ Item updated.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Update failed: " + ex.getMessage());
                }
            }
        });

        // --- DELETE BUTTON HANDLER ---
        deleteBtn.addActionListener(_ -> {
            // Prompt for a keyword to search for the item to delete
            String keyword = JOptionPane.showInputDialog("Enter any part of Item Code, Name, Supplier ID, Qty, Price, Purchase Price, Category, Date, or Notes to Delete:");
            if (keyword == null || keyword.isBlank()) return;
            String searchKey = keyword.trim().toLowerCase();

            List<String> lines = File_Utils.readLines("data/items_data.txt");
            DefaultTableModel searchModel = new DefaultTableModel(new String[]{"Code", "Name", "Supplier ID", "Qty", "Price", "Purchase Price", "Category", "Date", "Notes"}, 0);
            JTable resultTable = new JTable(searchModel);

            // Search for matching items
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 9) {
                    boolean match = false;
                    for (int i = 0; i < parts.length; i++) {
                        String field = parts[i].toLowerCase();
                        if (field.contains(searchKey)) {
                            match = true;
                            break;
                        }
                        if (i == 0) { // Code field
                            if (field.startsWith("itm") && searchKey.matches("\\d+")) {
                                String codeNum = parts[0].substring(3);
                                if (codeNum.equals(searchKey)) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (match) searchModel.addRow(parts);
                }
            }

            if (searchModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No matching items found.");
                return;
            }

            JScrollPane resultScroll = new JScrollPane(resultTable);
            resultScroll.setPreferredSize(new Dimension(700, 150));
            int pick = JOptionPane.showConfirmDialog(null, resultScroll, "Select Item to Delete", JOptionPane.OK_CANCEL_OPTION);
            if (pick != JOptionPane.OK_OPTION || resultTable.getSelectedRow() == -1) return;

            String itemCodeToDelete = (String) searchModel.getValueAt(resultTable.getSelectedRow(), 0);
            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                if (!line.startsWith(itemCodeToDelete + ",")) {
                    updatedLines.add(line);
                }
            }
            File_Utils.writeLines("data/items_data.txt", new ArrayList<>(updatedLines));
            loadItems();
            JOptionPane.showMessageDialog(null, "✅ Item deleted successfully.");
        });
    }

    // Loads all items from data/items_data.txt into the table
    private void loadItems() {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/items_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 9) {
                tableModel.addRow(parts);
            }
        }
    }

    // Generates the next available item code in the format ITM###
    private String generateNextItemCode() {
        List<String> lines = File_Utils.readLines("data/items_data.txt");
        int max = 0;
        for (String line : lines) {
            if (line.startsWith("ITM")) {
                try {
                    int num = Integer.parseInt(line.substring(3, 6));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return String.format("ITM%03d", max + 1);
    }

    // Checks if a supplier ID exists in data/suppliers_data.txt
    private boolean supplierExists(String supplierId) {
        List<String> lines = File_Utils.readLines("data/suppliers_data.txt");
        for (String line : lines) {
            if (line.startsWith(supplierId + ",")) {
                return true;
            }
        }
        return false;
    }

    // Filters the table based on the search filter string
    private void filterTable(String filter) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/items_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 9) {
                boolean match = filter.isEmpty();
                if (!match) {
                    for (String part : parts) {
                        if (part.toLowerCase().contains(filter)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (match) tableModel.addRow(parts);
            }
        }
    }
}