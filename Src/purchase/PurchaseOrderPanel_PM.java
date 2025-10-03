import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseOrderPanel_PM extends JPanel {
    private final DefaultTableModel tableModel;
    private final JTextField searchField;

    public PurchaseOrderPanel_PM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("Create Purchase Order", SwingConstants.LEFT);
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
        searchField = new JTextField();
        searchField.setToolTipText("Search by any field...");
        searchField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        // Status filter dropdown
        String[] statusOptions = {"All", "Pending", "Approved", "Rejected", "Cancelled"};
        JComboBox<String> statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        statusFilter.setMaximumSize(new Dimension(140, 32));
        searchPanel.add(statusFilter, BorderLayout.EAST);
        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Show purchase requisition data ---
        String[] columnNames = {"RequisitionID", "ItemCode", "Quantity", "RequiredBy", "SupplierID", "Priority", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Custom renderer for status coloring and blue striped rows
        class StatusColorRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                if (table.getColumnCount() > 6 && table.getValueAt(row, 6) != null) {
                    status = table.getValueAt(row, 6).toString().trim().toLowerCase();
                }
                // Set text color for status column
                if (column == 6) {
                    switch (status) {
                        case "pending" -> c.setForeground(new Color(180, 140, 0));
                        case "approved" -> c.setForeground(new Color(0, 140, 0));
                        case "rejected" -> c.setForeground(new Color(180, 0, 0));
                        case "cancelled" -> c.setForeground(new Color(120, 120, 120));
                        default -> c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
                // Striped rows (blue theme)
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(230, 240, 255) : Color.WHITE);
                } else {
                    c.setBackground(new Color(180, 210, 255));
                }
                return c;
            }
        }

        JTable requisitionTable = new JTable(tableModel);
        StatusColorRenderer statusRenderer = new StatusColorRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            requisitionTable.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
        }
        requisitionTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        requisitionTable.setRowHeight(22);
        requisitionTable.setGridColor(new Color(180, 210, 255));
        requisitionTable.setShowGrid(true);
        requisitionTable.setSelectionBackground(new Color(180, 210, 255));
        requisitionTable.setSelectionForeground(Color.BLACK);
        requisitionTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        requisitionTable.getTableHeader().setBackground(new Color(200, 220, 250));
        requisitionTable.getTableHeader().setForeground(new Color(30, 60, 120));
        requisitionTable.setFillsViewportHeight(true);
        int[] colWidths = {90, 90, 70, 110, 90, 80, 80};
        for (int i = 0; i < colWidths.length && i < requisitionTable.getColumnCount(); i++) {
            requisitionTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(requisitionTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- BUTTON PANEL ---
        JButton addBtn = new JButton("Create");
        addBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        addBtn.setBackground(new Color(220, 235, 255));
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(180, 35));
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.setBackground(new Color(240, 245, 255));
        btnPanel.add(addBtn);

        // --- EDIT BUTTON ---
        JButton editBtn = new JButton("Edit");
        editBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        editBtn.setBackground(new Color(220, 235, 255));
        editBtn.setFocusPainted(false);
        editBtn.setPreferredSize(new Dimension(180, 35));
        btnPanel.add(editBtn);

        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(_ -> {
            int row = requisitionTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Please select a requisition first.");
                return;
            }
            String requisitionId = (String) tableModel.getValueAt(row, 0);
            String itemCode = (String) tableModel.getValueAt(row, 1);
            String quantity = (String) tableModel.getValueAt(row, 2);
            String requiredBy = (String) tableModel.getValueAt(row, 3);
            String supplierId = (String) tableModel.getValueAt(row, 4);
            String status = (String) tableModel.getValueAt(row, 6);

            if ("approved".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(null, "This requisition is already approved.", "Add Not Allowed", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Load supplier IDs from file
            List<String> supplierLines = File_Utils.readLines("data/suppliers_data.txt");
            List<String> supplierIds = new ArrayList<>();
            for (String line : supplierLines) {
                String[] parts = line.split(",");
                if (parts.length > 0) supplierIds.add(parts[0]);
            }
            JComboBox<String> supplierIdBox = new JComboBox<>(supplierIds.toArray(new String[0]));
            supplierIdBox.setSelectedItem(supplierId); // Pre-select the existing supplier ID

            JTextField quantityField = new JTextField(quantity);
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            try {
                ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setText(requiredBy);
            } catch (Exception ignored) {}
            JComboBox<String> statusBox = new JComboBox<>(new String[]{"Approved", "Rejected", "Cancelled"});
            statusBox.setSelectedItem("Approved");

            JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
            panel.add(new JLabel("Supplier ID:"));
            panel.add(supplierIdBox);
            panel.add(new JLabel("Quantity:"));
            panel.add(quantityField);
            panel.add(new JLabel("Required By (yyyy-MM-dd):"));
            panel.add(dateSpinner);
            panel.add(new JLabel("Status:"));
            panel.add(statusBox);

            int result = JOptionPane.showConfirmDialog(null, panel, "Update & Create PO", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newQuantity = quantityField.getText().trim();
                String newRequiredBy = new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue());
                String newStatus = ((String) Objects.requireNonNull(statusBox.getSelectedItem())).trim();
                String newSupplierId = (String) supplierIdBox.getSelectedItem();

                // 1. Update data/purchase_requisitions_data.txt
                List<String> lines = File_Utils.readLines("data/purchase_requisitions_data.txt");
                List<String> updated = new ArrayList<>();
                String[] updatedPR = null;
                for (String line : lines) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 10 && parts[0].equals(requisitionId)) {
                        parts[3] = newQuantity;
                        parts[4] = newRequiredBy;
                        parts[5] = newSupplierId; // Update supplier ID
                        parts[9] = newStatus;
                        updatedPR = parts;
                        updated.add(String.join(",", parts));
                    } else {
                        updated.add(line);
                    }
                }
                File_Utils.writeLines("data/purchase_requisitions_data.txt", new ArrayList<>(updated));

                // 2. If status is Approved, create PO in data/purchase_orders_data.txt with status 'Issued'
                if (newStatus.equalsIgnoreCase("Approved")) {
                    String poid = generateNextPOId();
                    String managerId = "PM001"; // TODO: Replace with actual session or selection
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    if (updatedPR != null) managerId = updatedPR[6];
                    String itemName = getItemName(itemCode);
                    // --- Fetch purchase price from data/items_data.txt and calculate total ---
                    double purchasePrice = getItemPurchasePrice(itemCode);
                    int qty = 1;
                    try { qty = Integer.parseInt(newQuantity); } catch (Exception ignored) {}
                    double totalPrice = purchasePrice * qty;
                    String totalPriceStr = String.format("%.2f", totalPrice);
                    // RequiredBy is newRequiredBy
                    String newLine = String.join(",",
                        poid, requisitionId, itemCode, itemName, newQuantity, totalPriceStr, newRequiredBy, newSupplierId, managerId, date, "Issued"
                    );
                    File_Utils.appendLine("data/purchase_orders_data.txt", newLine);
                }

                JOptionPane.showMessageDialog(null, "Status updated and PO created (if approved).", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseRequisitions("", null);
                filterTable((String) statusFilter.getSelectedItem());
            }
        });

        editBtn.addActionListener(_ -> {
            int row = requisitionTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Please select a requisition first.");
                return;
            }
            String requisitionId = (String) tableModel.getValueAt(row, 0);
            String itemCode = (String) tableModel.getValueAt(row, 1);
            String quantity = (String) tableModel.getValueAt(row, 2);
            String requiredBy = (String) tableModel.getValueAt(row, 3);
            String supplierId = (String) tableModel.getValueAt(row, 4);
            String status = (String) tableModel.getValueAt(row, 6);

            if (!"approved".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(null, "You can only edit requisitions with status 'Approved'.", "Edit Not Allowed", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JTextField quantityField = new JTextField(quantity);
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            try {
                ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setText(requiredBy);
            } catch (Exception ignored) {}
            JComboBox<String> statusBox = new JComboBox<>(new String[]{"Approved", "Rejected", "Cancelled"});
            statusBox.setSelectedItem(status);

            JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
            panel.add(new JLabel("Quantity:"));
            panel.add(quantityField);
            panel.add(new JLabel("Required By (yyyy-MM-dd):"));
            panel.add(dateSpinner);
            panel.add(new JLabel("Status:"));
            panel.add(statusBox);

            int result = JOptionPane.showConfirmDialog(null, panel, "Edit Purchase Order", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newQuantity = quantityField.getText().trim();
                String newRequiredBy = new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue());
                String newStatus = ((String) Objects.requireNonNull(statusBox.getSelectedItem())).trim();

                // 1. Update data/purchase_requisitions_data.txt
                List<String> lines = File_Utils.readLines("data/purchase_requisitions_data.txt");
                List<String> updated = new ArrayList<>();
                String[] updatedPR = null;
                for (String line : lines) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 10 && parts[0].equals(requisitionId)) {
                        parts[3] = newQuantity;
                        parts[4] = newRequiredBy;
                        parts[9] = newStatus;
                        updatedPR = parts;
                        updated.add(String.join(",", parts));
                    } else {
                        updated.add(line);
                    }
                }
                File_Utils.writeLines("data/purchase_requisitions_data.txt", new ArrayList<>(updated));

                // 2. PO logic
                boolean nowApproved = newStatus.equalsIgnoreCase("Approved");
                // Find PO for this requisition
                List<String> poLines = File_Utils.readLines("data/purchase_orders_data.txt");
                List<String> updatedPOs = new ArrayList<>();
                boolean poExists = false;
                for (String poLine : poLines) {
                    String[] poParts = poLine.split(",", -1);
                    if (poParts.length >= 2 && poParts[1].equals(requisitionId)) {
                        poExists = true;
                        // If status is not approved anymore, skip (delete) this PO
                        if (!nowApproved) continue;
                    }
                    updatedPOs.add(poLine);
                }
                // If status is now approved and PO does not exist, create it
                if (nowApproved && !poExists) {
                    String newPoid = generateNextPOId();
                    String managerId = "PM001";
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    if (updatedPR != null) managerId = updatedPR[6];
                    String itemName = getItemName(itemCode);
                    String newLine = String.join(",",
                        newPoid, requisitionId, itemCode, itemName, newQuantity, newRequiredBy, supplierId, managerId, date, "Issued"
                    );
                    updatedPOs.add(newLine);
                }
                File_Utils.writeLines("data/purchase_orders_data.txt", new ArrayList<>(updatedPOs));

                JOptionPane.showMessageDialog(null, "Requisition updated and PO created/deleted as needed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseRequisitions("", null);
                filterTable((String) statusFilter.getSelectedItem());
            }
        });

        loadPurchaseRequisitions("", null);

        // Search and status filter listeners
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable((String) statusFilter.getSelectedItem()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable((String) statusFilter.getSelectedItem()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        statusFilter.addActionListener(_ -> filterTable((String) statusFilter.getSelectedItem()));
    }

    private void loadPurchaseRequisitions(String filter, String statusFilter) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/purchase_requisitions_data.txt");
        List<String[]> rows = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 10) {
                String[] row = new String[] {
                    parts[0], parts[1], parts[3], parts[4], parts[5], parts[7], parts[9]
                };
                boolean match = filter == null || filter.isEmpty();
                if (!match) {
                    for (String part : row) {
                        if (part.toLowerCase().contains(filter)) {
                            match = true;
                            break;
                        }
                    }
                }
                boolean statusMatch = statusFilter == null || statusFilter.equals("All") || row[6].equalsIgnoreCase(statusFilter);
                if (match && statusMatch) rows.add(row);
            }
        }
        rows.sort((a, b) -> {
            int statusCmp = statusOrder(a[6]) - statusOrder(b[6]);
            if (statusCmp != 0) return statusCmp;
            return priorityOrder(a[5]) - priorityOrder(b[5]);
        });
        for (String[] row : rows) tableModel.addRow(row);
    }

    private int statusOrder(String status) {
        if (status == null) return 4;
        status = status.trim().toLowerCase();
        return switch (status) {
            case "pending" -> 0;
            case "approved" -> 1;
            case "rejected" -> 2;
            case "cancelled" -> 3;
            default -> 4;
        };
    }

    private String generateNextPOId() {
        List<String> lines = File_Utils.readLines("data/purchase_orders_data.txt");
        int max = 0;
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts[0].startsWith("PO")) {
                try {
                    int num = Integer.parseInt(parts[0].substring(2));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PO%03d", max + 1);
    }

    private void filterTable(String statusFilter) {
        String filter = searchField.getText().trim().toLowerCase();
        loadPurchaseRequisitions(filter, statusFilter);
    }

    private int priorityOrder(String priority) {
        if (priority == null) return 3;
        priority = priority.trim().toLowerCase();
        return switch (priority) {
            case "high" -> 0;
            case "medium" -> 1;
            case "low" -> 2;
            default -> 3;
        };
    }

    // Helper: get item name from data/items_data.txt
    private String getItemName(String itemCode) {
        List<String> itemLines = File_Utils.readLines("data/items_data.txt");
        for (String itemLine : itemLines) {
            String[] itemParts = itemLine.split(",", -1);
            if (itemParts.length >= 2 && itemParts[0].equals(itemCode)) {
                return itemParts[1];
            }
        }
        return "";
    }

    private double getItemPurchasePrice(String itemCode) {
        List<String> itemLines = File_Utils.readLines("data/items_data.txt");
        for (String itemLine : itemLines) {
            String[] itemParts = itemLine.split(",", -1);
            if (itemParts.length >= 6 && itemParts[0].equals(itemCode)) {
                try {
                    return Double.parseDouble(itemParts[5]); // Purchase Price is the 6th column (index 5)
                } catch (Exception ignored) {}
            }
        }
        return 0.0;
    }
}