import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

// Panel for Sales Manager to view low stock items and manage purchase requisitions
public class PurchaseRequisitionPanel_SM extends JPanel {
    // Table model for displaying low stock items and PRs
    private final DefaultTableModel tableModel;

    // Constructor: sets up the UI and event handlers
    public PurchaseRequisitionPanel_SM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title and Status Filter ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Low Stock Items – Create Purchase Requisitions", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        topPanel.add(title, BorderLayout.WEST);
        String[] statusOptions = {"All", "Pending", "Approved", "Rejected", "Cancelled", "Create PR"};
        JComboBox<String> statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        statusFilter.setMaximumSize(new Dimension(160, 32));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(statusFilter);
        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows low stock items and PR status ---
        String[] columnNames = {"Code", "Name", "Supplier ID", "Qty", "Price", "Category", "Date", "Required By Date", "Desired Qty", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Custom renderer for status coloring and row backgrounds
        class StatusColorRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                if (table.getColumnCount() > 9 && table.getValueAt(row, 9) != null) {
                    status = table.getValueAt(row, 9).toString().trim().toLowerCase();
                }
                // Set text color based on PR status
                switch (status) {
                    case "pending" -> c.setForeground(new Color(180, 140, 0));
                    case "approved" -> c.setForeground(new Color(0, 140, 0));
                    case "rejected" -> c.setForeground(new Color(180, 0, 0));
                    case "cancelled" -> c.setForeground(new Color(120, 120, 120));
                    default -> c.setForeground(Color.BLACK);
                }
                // Set background color for alternating rows
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 235, 235) : Color.WHITE);
                } else {
                    c.setBackground(new Color(255, 200, 200));
                }
                return c;
            }
        }

        JTable itemTable = new JTable(tableModel);
        StatusColorRenderer statusRenderer = new StatusColorRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
        }
        itemTable.setFont(new Font("Times New Roman", Font.PLAIN, 10));
        itemTable.setRowHeight(28);
        itemTable.setGridColor(new Color(255, 210, 210));
        itemTable.setShowGrid(true);
        itemTable.setSelectionBackground(new Color(255, 200, 200));
        itemTable.setSelectionForeground(Color.BLACK);
        itemTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 15));
        itemTable.getTableHeader().setBackground(new Color(255, 210, 210));
        itemTable.getTableHeader().setForeground(new Color(120, 30, 30));
        itemTable.setFillsViewportHeight(true);
        int[] colWidths = {90, 140, 100, 60, 80, 100, 100, 120};
        for (int i = 0; i < colWidths.length && i < itemTable.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        itemTable.setRowSelectionAllowed(true);
        itemTable.setColumnSelectionAllowed(false);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(itemTable);
        add(tableScroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: Create, Edit, Delete PR ---
        JPanel newButtonPanel = new JPanel();
        JButton createPRBtn = new JButton("Create PR");
        JButton editPRBtn = new JButton("Edit");
        JButton deletePRBtn = new JButton("Delete PR");
        newButtonPanel.add(createPRBtn);
        newButtonPanel.add(editPRBtn);
        newButtonPanel.add(deletePRBtn);
        add(newButtonPanel, BorderLayout.SOUTH);

        // --- Create PR button handler ---
        createPRBtn.addActionListener(_ -> {
            int selectedRow = itemTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select an item from the list.");
                return;
            }
            // Check if status is pending (should be column 9)
            String status = "";
            if (tableModel.getColumnCount() > 9 && tableModel.getValueAt(selectedRow, 9) != null) {
                status = tableModel.getValueAt(selectedRow, 9).toString().trim().toLowerCase();
            }
            if (status.equals("pending")) {
                JOptionPane.showMessageDialog(null, "A purchase requisition is already pending for this item.");
                return;
            }
            // Get item details from table
            String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
            String itemName = (String) tableModel.getValueAt(selectedRow, 1);
            String currentStock = (String) tableModel.getValueAt(selectedRow, 3);
            String salesManagerId = Session.getLoggedInUserId();

            // --- NEW: Load supplier IDs from file ---
            java.util.List<String> supplierLines = File_Utils.readLines("data/suppliers_data.txt");
            java.util.List<String> supplierIds = new java.util.ArrayList<>();
            for (String line : supplierLines) {
                String[] parts = line.split(",");
                if (parts.length > 0) supplierIds.add(parts[0]);
            }
            JComboBox<String> supplierIdBox = new JComboBox<>(supplierIds.toArray(new String[0]));

            JTextField requiredQtyField = new JTextField("50");
            JSpinner requiredBySpinner = new JSpinner(new javax.swing.SpinnerDateModel());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DATE, 3);
            requiredBySpinner.setValue(cal.getTime());
            requiredBySpinner.setEditor(new JSpinner.DateEditor(requiredBySpinner, "yyyy-MM-dd"));
            JComboBox<String> priorityBox = new JComboBox<>(new String[]{"High", "Medium", "Low"});
            JTextArea remarksArea = new JTextArea(3, 18);
            remarksArea.setLineWrap(true);
            remarksArea.setWrapStyleWord(true);
            JScrollPane remarksScroll = new JScrollPane(remarksArea);

            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 4));
            panel.setPreferredSize(new Dimension(350, 300));
            panel.add(new JLabel("Item Code:"));
            panel.add(new JLabel(itemCode));
            panel.add(new JLabel("Item Name:"));
            panel.add(new JLabel(itemName));
            panel.add(new JLabel("Supplier ID:"));
            panel.add(supplierIdBox);
            panel.add(new JLabel("Current Stock:"));
            panel.add(new JLabel(currentStock));
            panel.add(new JLabel("Required Qty:"));
            panel.add(requiredQtyField);
            panel.add(new JLabel("Required By:"));
            panel.add(requiredBySpinner);
            panel.add(new JLabel("Priority:"));
            panel.add(priorityBox);
            panel.add(new JLabel("Sales Manager ID:"));
            panel.add(new JLabel(salesManagerId != null ? salesManagerId : "Not logged in"));
            panel.add(new JLabel("Remarks:"));
            panel.add(remarksScroll);

            int result = JOptionPane.showConfirmDialog(null, panel, "Create Purchase Requisition", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String requiredQty = requiredQtyField.getText().trim();
                    int qty = Integer.parseInt(requiredQty);
                    if (qty <= 0) throw new NumberFormatException();
                    java.util.Date reqByDate = (java.util.Date) requiredBySpinner.getValue();
                    String reqByStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(reqByDate);
                    String priority = (String) priorityBox.getSelectedItem();
                    String remarks = remarksArea.getText().trim();
                    String supplierId = (String) supplierIdBox.getSelectedItem();
                    // Generate PR ID
                    String prId = generateNextRequisitionId();
                    // Compose line: PRID,ItemCode,ItemName,RequiredQty,RequiredBy,SupplierID,SalesManagerID,Priority,Remarks,Status
                    String line = String.join(",", prId, itemCode, itemName, requiredQty, reqByStr, supplierId, (salesManagerId != null ? salesManagerId : ""), priority, remarks, "Pending");
                    File_Utils.appendLine("data/purchase_requisitions_data.txt", line);
                    JOptionPane.showMessageDialog(null, "✅ Purchase Requisition created!");
                    // Update desired qty and status in the table
                    tableModel.setValueAt(requiredQty, selectedRow, 8);
                    tableModel.setValueAt("Pending", selectedRow, 9);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Invalid input: " + ex.getMessage());
                }
            }
        });

        // --- Edit PR button handler ---
        editPRBtn.addActionListener(_ -> {
            int selectedRow = itemTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a PR to edit.");
                return;
            }
            // Only allow editing if there is a PR (status not empty)
            String status = "";
            if (tableModel.getColumnCount() > 9 && tableModel.getValueAt(selectedRow, 9) != null) {
                status = tableModel.getValueAt(selectedRow, 9).toString().trim();
            }
            if (status.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No PR exists for this item to edit.");
                return;
            }
            if (!status.trim().equalsIgnoreCase("pending")) {
                JOptionPane.showMessageDialog(null, "You can only edit a PR with status 'Pending'.");
                return;
            }
            // Get current values
            String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
            String requiredByDate = (String) tableModel.getValueAt(selectedRow, 7);
            String desiredQty = (String) tableModel.getValueAt(selectedRow, 8);

            JTextField desiredQtyField = new JTextField(desiredQty.isEmpty() ? "50" : desiredQty);
            JSpinner requiredBySpinner = new JSpinner(new javax.swing.SpinnerDateModel());
            try {
                java.util.Date date = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(requiredByDate);
                requiredBySpinner.setValue(date);
            } catch (Exception e) {
                requiredBySpinner.setValue(new java.util.Date());
            }
            requiredBySpinner.setEditor(new JSpinner.DateEditor(requiredBySpinner, "yyyy-MM-dd"));

            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 4));
            panel.setPreferredSize(new Dimension(280, 120));
            panel.add(new JLabel("Required Qty:"));
            panel.add(desiredQtyField);
            panel.add(new JLabel("Required By:"));
            panel.add(requiredBySpinner);

            int result = JOptionPane.showConfirmDialog(null, panel, "Edit Purchase Requisition", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String newQty = desiredQtyField.getText().trim();
                    int qty = Integer.parseInt(newQty);
                    if (qty <= 0) throw new NumberFormatException();
                    java.util.Date reqByDate = (java.util.Date) requiredBySpinner.getValue();
                    String reqByStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(reqByDate);
                    // Update the file
                    List<String> prLines = File_Utils.readLines("data/purchase_requisitions_data.txt");
                    boolean updated = false;
                    for (int i = 0; i < prLines.size(); i++) {
                        String[] prParts = prLines.get(i).split(",");
                        if (prParts.length >= 10 && prParts[1].equals(itemCode)) {
                            prParts[3] = newQty; // Desired Qty
                            prParts[4] = reqByStr; // Required By
                            prLines.set(i, String.join(",", prParts));
                            updated = true;
                            break;
                        }
                    }
                    if (updated) {
                        File_Utils.writeLines("data/purchase_requisitions_data.txt", new java.util.ArrayList<>(prLines));
                        tableModel.setValueAt(newQty, selectedRow, 8);
                        tableModel.setValueAt(status, selectedRow, 9);
                        JOptionPane.showMessageDialog(null, "✅ PR updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(null, "❌ Could not find PR to update.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Invalid input: " + ex.getMessage());
                }
            }
        });

        // --- Delete PR button handler ---
        deletePRBtn.addActionListener(_ -> {
            int selectedRow = itemTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a PR to delete.");
                return;
            }
            String status = "";
            if (tableModel.getColumnCount() > 9 && tableModel.getValueAt(selectedRow, 9) != null) {
                status = tableModel.getValueAt(selectedRow, 9).toString().trim();
            }
            if (status.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No PR exists for this item to delete.");
                return;
            }
            String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
            // Remove from data/purchase_requisitions_data.txt
            List<String> prLines = File_Utils.readLines("data/purchase_requisitions_data.txt");
            boolean deleted = false;
            for (int i = 0; i < prLines.size(); i++) {
                String[] prParts = prLines.get(i).split(",");
                if (prParts.length >= 2 && prParts[1].equals(itemCode)) {
                    prLines.remove(i);
                    deleted = true;
                    break;
                }
            }
            if (deleted) {
                File_Utils.writeLines("data/purchase_requisitions_data.txt", new java.util.ArrayList<>(prLines));
                tableModel.setValueAt("", selectedRow, 8); // Clear desired qty
                tableModel.setValueAt("", selectedRow, 9); // Clear status
                JOptionPane.showMessageDialog(null, "✅ PR deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Could not find PR to delete.");
            }
        });

        loadLowStockItems(null);

        // Status filter listener
        statusFilter.addActionListener(_ -> {
            String selected = (String) statusFilter.getSelectedItem();
            if (selected != null && selected.equals("Create PR")) {
                loadLowStockItems(""); // Show only items with empty status
            } else if (selected != null && !selected.equals("All")) {
                loadLowStockItems(selected);
            } else {
                loadLowStockItems(null); // Show all
            }
        });
    }

    // Loads and displays low stock items, applying status filter if provided
    private void loadLowStockItems(String statusFilter) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/items_data.txt");
        java.time.LocalDate requiredBy = java.time.LocalDate.now().plusDays(3);
        // Read all PRs to find which items already have PRs and their status/desired qty
        java.util.Map<String, String[]> prItemStatusQty = new java.util.HashMap<>();
        List<String> prLines = File_Utils.readLines("data/purchase_requisitions_data.txt");
        for (String prLine : prLines) {
            String[] prParts = prLine.split(",");
            if (prParts.length >= 10) {
                prItemStatusQty.put(prParts[1], new String[]{prParts[3], prParts[9]}); // ItemCode: [DesiredQty, Status]
            }
        }
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 8) {
                try {
                    int qty = Integer.parseInt(parts[3].trim());
                    if (qty < 20) {
                        String[] statusQty = prItemStatusQty.getOrDefault(parts[0], new String[]{"", ""});
                        String itemStatus = statusQty[1] == null ? "" : statusQty[1];
                        boolean match = true;
                        if (statusFilter != null) {
                            if (statusFilter.isEmpty()) {
                                match = itemStatus.isEmpty();
                            } else {
                                match = itemStatus.equalsIgnoreCase(statusFilter);
                            }
                        }
                        if (match) {
                            Object[] row = new Object[] {
                                parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6],
                                requiredBy.toString(), statusQty[0], itemStatus
                            };
                            rows.add(row);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        // Sort rows by status: Pending first, then empty, then Approved, then others
        rows.sort((a, b) -> {
            int orderA = statusOrder((String)a[9]);
            int orderB = statusOrder((String)b[9]);
            return Integer.compare(orderA, orderB);
        });
        for (Object[] row : rows) tableModel.addRow(row);
    }

    // Helper for status order
    private int statusOrder(String status) {
        if (status == null || status.trim().isEmpty()) return 1; // empty status second
        status = status.trim().toLowerCase();
        if (status.equals("pending")) return 0; // pending first
        if (status.equals("approved")) return 2; // approved third
        return 3; // others after
    }

    // Generates the next PR ID in the format PR###
    private String generateNextRequisitionId() {
        List<String> lines = File_Utils.readLines("data/purchase_requisitions_data.txt");
        int max = 0;
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length > 0 && parts[0].startsWith("PR")) {
                try {
                    int num = Integer.parseInt(parts[0].substring(2)); // Extract the numeric part after "PR"
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PR%03d", max + 1);
    }
}
