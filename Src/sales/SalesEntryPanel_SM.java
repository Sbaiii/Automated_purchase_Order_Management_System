import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

// Panel for Sales Manager to record, edit, and delete sales entries
public class SalesEntryPanel_SM extends JPanel {
    // Table model for displaying sales records
    private final DefaultTableModel tableModel;

    // Constructor: sets up the UI and event handlers
    public SalesEntryPanel_SM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title ---
        JLabel title = new JLabel("Record Sales", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // --- TABLE: Shows sales records ---
        String[] columnNames = {"SaleID", "ItemCode", "ItemName", "Quantity", "Date", "SalesManagerID", "Remarks"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable salesTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 235, 235) : Color.WHITE); // light red
                } else {
                    c.setBackground(new Color(255, 200, 200)); // selected row light red
                }
                return c;
            }
        };
        salesTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        salesTable.setRowHeight(28);
        salesTable.setGridColor(new Color(255, 210, 210)); // light red grid
        salesTable.setShowGrid(true);
        salesTable.setSelectionBackground(new Color(255, 200, 200)); // match selected row (light red)
        salesTable.setSelectionForeground(Color.BLACK);
        salesTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 15));
        salesTable.getTableHeader().setBackground(new Color(255, 210, 210)); // header light red
        salesTable.getTableHeader().setForeground(new Color(120, 30, 30));
        salesTable.setFillsViewportHeight(true);
        // Adjust column widths
        int[] colWidths = {80, 90, 120, 70, 100, 110, 160};
        for (int i = 0; i < colWidths.length && i < salesTable.getColumnCount(); i++) {
            salesTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane tableScroll = new JScrollPane(salesTable);
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

        // Load initial sales records
        loadsales();

        // --- Add Sale button handler ---
        addBtn.addActionListener(_ -> {
            JComboBox<String> itemComboBox = createItemComboBox();
            JTextField qtyField = new JTextField();
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            JTextField remarksField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new JLabel("Select Item:"));
            panel.add(itemComboBox);
            panel.add(new JLabel("Quantity:"));
            panel.add(qtyField);
            panel.add(new JLabel("Date (yyyy-MM-dd):"));
            panel.add(dateSpinner);
            panel.add(new JLabel("Remarks:"));
            panel.add(remarksField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Add New Sale", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String[] itemInfo = parseItemSelection((String)itemComboBox.getSelectedItem());
                    if (itemInfo == null) {
                        JOptionPane.showMessageDialog(null, "❌ Please select an item", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    String inputItemCode = itemInfo[0];
                    String itemName = itemInfo[1];

                    // Look up current quantity from data/items_data.txt
                    List<String> itemLines = File_Utils.readLines("data/data/items_data.txt");
                    int itemIndex = -1;
                    int currentQty = -1;
                    for (int i = 0; i < itemLines.size(); i++) {
                        String[] parts = itemLines.get(i).split(",");
                        if (parts.length >= 4 && parts[0].equalsIgnoreCase(inputItemCode)) {
                            try {
                                currentQty = Integer.parseInt(parts[3].trim());
                            } catch (Exception _) {
                            }
                            itemIndex = i;
                            break;
                        }
                    }

                    String quantityStr = qtyField.getText().trim();
                    int saleQty;
                    try {
                        saleQty = Integer.parseInt(quantityStr);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "❌ Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (saleQty > currentQty) {
                        JOptionPane.showMessageDialog(null, "❌ Not enough stock. Available: " + currentQty, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Deduct quantity and update data/items_data.txt
                    int newQty = currentQty - saleQty;
                    String[] itemParts = itemLines.get(itemIndex).split(",");
                    itemParts[3] = String.valueOf(newQty);
                    itemLines.set(itemIndex, String.join(",", itemParts));
                    File_Utils.writeLines("data/items_data.txt", new ArrayList<>(itemLines));

                    String date = new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue());
                    String remarks = remarksField.getText().trim();
                    String saleId = generateNextSaleId();
                    String managerId = Session.getLoggedInUserId();

                    String newLine = String.join(",", saleId, inputItemCode, itemName, String.valueOf(saleQty), date, managerId, remarks);

                    File_Utils.appendLine("data/sales_data.txt", newLine);
                    tableModel.addRow(newLine.split(","));
                    JOptionPane.showMessageDialog(null, "✅ Sale recorded successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Failed to add sale: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Edit Sale button handler ---
        editBtn.addActionListener(_ -> {
            String keyword = JOptionPane.showInputDialog("Enter Sale ID, Item Code, Item Name, or Sales Manager ID:");
            if (keyword == null || keyword.isBlank()) return;
            keyword = keyword.trim().toLowerCase();

            List<String> lines = File_Utils.readLines("data/sales_data.txt");
            DefaultTableModel searchModel = new DefaultTableModel(
                    new String[]{"SaleID", "ItemCode", "ItemName", "Quantity", "Date", "SalesManagerID", "Remarks"}, 0);
            JTable resultTable = new JTable(searchModel);

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 7 &&
                        (parts[0].toLowerCase().contains(keyword) ||
                                parts[1].toLowerCase().contains(keyword) ||
                                parts[2].toLowerCase().contains(keyword) ||
                                parts[5].toLowerCase().contains(keyword))) {
                    searchModel.addRow(parts);
                }
            }

            if (searchModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No matching sales record found.");
                return;
            }

            JScrollPane resultScroll = new JScrollPane(resultTable);
            resultScroll.setPreferredSize(new Dimension(700, 150));
            int pick = JOptionPane.showConfirmDialog(null, resultScroll, "Select Sale to Edit", JOptionPane.OK_CANCEL_OPTION);
            if (pick != JOptionPane.OK_OPTION || resultTable.getSelectedRow() == -1) return;

            int rowIndex = resultTable.getSelectedRow();
            String[] selected = new String[7];
            for (int i = 0; i < 7; i++) selected[i] = (String) searchModel.getValueAt(rowIndex, i);

            JTextField qtyField = new JTextField(selected[3]);
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            JTextField remarksField = new JTextField(selected[6]);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Quantity:"));
            panel.add(qtyField);
            panel.add(new JLabel("Date:"));
            panel.add(dateSpinner);
            panel.add(new JLabel("Remarks:"));
            panel.add(remarksField);

            int update = JOptionPane.showConfirmDialog(null, panel, "Edit Sale", JOptionPane.OK_CANCEL_OPTION);
            if (update == JOptionPane.OK_OPTION) {
                try {
                    String managerId = Session.getLoggedInUserId();
                    String newLine = String.join(",",
                            selected[0], selected[1], selected[2],
                            qtyField.getText().trim(),
                            new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue()),
                            managerId,
                            remarksField.getText().trim());

                    List<String> updated = new ArrayList<>();
                    for (String line : lines) {
                        if (line.startsWith(selected[0] + ",")) {
                            updated.add(newLine);
                        } else {
                            updated.add(line);
                        }
                    }

                    File_Utils.writeLines("data/sales_data.txt", new ArrayList<>(updated));
                    loadsales();
                    JOptionPane.showMessageDialog(null, "✅ Sale updated.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Update failed: " + ex.getMessage());
                }
            }
        });

        // --- Delete Sale button handler ---
        deleteBtn.addActionListener(_ -> {
            String keyword = JOptionPane.showInputDialog("Enter Sale ID, Item Code, Item Name, or Sales Manager ID to delete:");
            if (keyword == null || keyword.isBlank()) return;
            keyword = keyword.trim().toLowerCase();

            List<String> lines = File_Utils.readLines("data/sales_data.txt");
            DefaultTableModel searchModel = new DefaultTableModel(
                    new String[]{"SaleID", "ItemCode", "ItemName", "Quantity", "Date", "SalesManagerID", "Remarks"}, 0);
            JTable resultTable = new JTable(searchModel);

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 7 &&
                        (parts[0].toLowerCase().contains(keyword) ||
                                parts[1].toLowerCase().contains(keyword) ||
                                parts[2].toLowerCase().contains(keyword) ||
                                parts[5].toLowerCase().contains(keyword))) {
                    searchModel.addRow(parts);
                }
            }

            if (searchModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No matching sales record found.");
                return;
            }

            JScrollPane resultScroll = new JScrollPane(resultTable);
            resultScroll.setPreferredSize(new Dimension(700, 150));
            int pick = JOptionPane.showConfirmDialog(null, resultScroll, "Select Sale to Delete", JOptionPane.OK_CANCEL_OPTION);
            if (pick != JOptionPane.OK_OPTION || resultTable.getSelectedRow() == -1) return;

            String saleIdToDelete = (String) searchModel.getValueAt(resultTable.getSelectedRow(), 0);

            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                if (!line.startsWith(saleIdToDelete + ",")) {
                    updatedLines.add(line);
                }
            }

            File_Utils.writeLines("data/sales_data.txt", new ArrayList<>(updatedLines));
            loadsales();
            JOptionPane.showMessageDialog(null, "✅ Sale record deleted successfully.");
        });
    }

    // Loads and displays sales records from data/sales_data.txt
    private void loadsales() {
        tableModel.setRowCount(0); // Clear existing rows
        List<String> lines = File_Utils.readLines("data/sales_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 7) {
                tableModel.addRow(parts);
            }
        }
    }

    // Generates the next Sale ID in the format SD###
    private String generateNextSaleId() {
        List<String> lines = File_Utils.readLines("data/sales_data.txt");
        int max = 0;
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length > 0 && parts[0].startsWith("SD")) {
                try {
                    int num = Integer.parseInt(parts[0].substring(2)); // Extract the numeric part after "SD"
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("SD%03d", max + 1);
    }

    // Creates a combo box (drop-down) for selecting an item (from data/items_data.txt)
    private JComboBox<String> createItemComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        
        // Add a default empty option
        comboBox.addItem("Select an item...");
        
        // Load items from data/items_data.txt
        List<String> itemLines = File_Utils.readLines("data/items_data.txt");
        for (String line : itemLines) {
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                String itemCode = parts[0].trim();
                String itemName = parts[1].trim();
                comboBox.addItem(itemCode + " - " + itemName);
            }
        }
        return comboBox;
    }

    // Parses the selected item string (format: "ITMxxx - Item Name") into an array of {itemCode, itemName}.
    private String[] parseItemSelection(String selectedItem) {
        if (selectedItem == null || selectedItem.equals("Select an item...")) {
            return null;
        }
        // Format is "ITMxxx - Item Name"
        String[] parts = selectedItem.split(" - ", 2);
        if (parts.length == 2) {
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        return null;
    }
}