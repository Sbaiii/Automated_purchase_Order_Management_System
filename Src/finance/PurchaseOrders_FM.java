import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

// Panel for Finance Manager to view, approve, and reject purchase orders
public class PurchaseOrders_FM extends JPanel {
    // Table displaying purchase orders
    private final JTable ordersTable;
    // Table model for managing order data
    private final DefaultTableModel tableModel;
    // Search field for filtering purchase orders
    private final JTextField searchField;

    // Constructor: sets up the UI and event handlers
    public PurchaseOrders_FM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("Approve Purchase Orders", SwingConstants.LEFT);
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
        // Status filter combo box for filtering by order status
        String[] statusOptions = {"All", "Issued", "Approved", "Rejected", "Delivered"};
        JComboBox<String> statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        statusFilter.setSelectedIndex(0);
        searchPanel.add(statusFilter, BorderLayout.EAST);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows purchase order data ---
        String[] columnNames = {"POID", "RequisitionID", "ItemCode", "ItemName", "Quantity", "Purchase Price", "RequiredBy", "SupplierID", "ManagerID", "Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Custom renderer for striped rows (green theme, with pill status coloring)
        class GreenStripedRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                if (table.getColumnCount() > 10 && table.getValueAt(row, 10) != null) {
                    status = table.getValueAt(row, 10).toString().trim().toLowerCase();
                }
                // Only color the text for the status column: issued (blue), approved/delivered (green), rejected (red)
                Color bg = isSelected
                        ? new Color(200, 230, 201)
                        : (row % 2 == 0 ? new Color(232, 245, 233) : Color.WHITE);
                if (column == 10) {
                    c.setHorizontalAlignment(SwingConstants.LEFT);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    switch (status) {
                        case "issued" -> c.setForeground(new Color(66, 133, 244));
                        case "approved", "delivered" -> c.setForeground(new Color(0, 140, 0));
                        case "rejected" -> c.setForeground(new Color(180, 0, 0));
                        default -> c.setForeground(Color.BLACK);
                    }
                    c.setBackground(bg);
                } else {
                    c.setForeground(Color.BLACK);
                    c.setBackground(bg);
                }
                return c;
            }
        }

        ordersTable = new JTable(tableModel);
        GreenStripedRowRenderer greenRenderer = new GreenStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(greenRenderer);
        }
        ordersTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        ordersTable.setRowHeight(22);
        ordersTable.setGridColor(new Color(200, 230, 201));
        ordersTable.setShowGrid(true);
        ordersTable.setSelectionBackground(new Color(200, 230, 201));
        ordersTable.setSelectionForeground(Color.BLACK);
        ordersTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        ordersTable.getTableHeader().setBackground(new Color(165, 214, 167));
        ordersTable.getTableHeader().setForeground(new Color(27, 94, 32));
        ordersTable.setFillsViewportHeight(true);
        int[] colWidths = {80, 90, 90, 120, 70, 100, 110, 90, 90, 100, 80};
        for (int i = 0; i < colWidths.length && i < ordersTable.getColumnCount(); i++) {
            ordersTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(ordersTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: Approve/Reject ---
        JButton approveBtn = new JButton("✅ Approve/Reject PO");
        approveBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        approveBtn.setBackground(new Color(220, 245, 220));
        approveBtn.setFocusPainted(false);
        approveBtn.setPreferredSize(new Dimension(220, 35));
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.setBackground(new Color(240, 255, 240));
        btnPanel.add(approveBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load initial orders into the table
        loadOrders("", "All");

        // --- Search and status filter listeners ---
        Runnable updateTable = () -> loadOrders(
            searchField.getText().trim().toLowerCase(),
            Objects.requireNonNull(statusFilter.getSelectedItem()).toString()
        );
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTable.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTable.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        statusFilter.addActionListener(_ -> updateTable.run());

        // --- Approve/Reject button handler ---
        approveBtn.addActionListener(_ -> {
            int row = ordersTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Please select a purchase order first.");
                return;
            }
            // Gather original order data
            String[] original = new String[11];
            for (int i = 0; i < 11; i++) original[i] = (String) tableModel.getValueAt(row, i);

            // Load supplier IDs from file
            List<String> supplierLines = File_Utils.readLines("data/suppliers_data.txt");
            List<String> supplierIds = new ArrayList<>();
            for (String line : supplierLines) {
                String[] parts = line.split(",");
                if (parts.length > 0) supplierIds.add(parts[0]);
            }
            JComboBox<String> supplierIdBox = new JComboBox<>(supplierIds.toArray(new String[0]));
            supplierIdBox.setSelectedItem(original[7]); // Pre-select the existing supplier ID

            JTextField quantityField = new JTextField(original[4]);
            JComboBox<String> statusBox = new JComboBox<>(new String[]{"Approved", "Rejected"});
            statusBox.setSelectedItem(capitalize(original[10]));

            JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
            panel.add(new JLabel("Supplier ID:"));
            panel.add(supplierIdBox);
            panel.add(new JLabel("Quantity:"));
            panel.add(quantityField);
            panel.add(new JLabel("Status:"));
            panel.add(statusBox);

            int confirm = JOptionPane.showConfirmDialog(null, panel, "Approve/Reject Purchase Order", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                String newQuantity = quantityField.getText().trim();
                String newStatus = ((String) Objects.requireNonNull(statusBox.getSelectedItem())).trim();
                String newSupplierId = (String) supplierIdBox.getSelectedItem();

                // Update data/purchase_orders_data.txt
                List<String> lines = File_Utils.readLines("data/purchase_orders_data.txt");
                List<String> updated = new ArrayList<>();
                for (String line : lines) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 11 && parts[0].equals(original[0])) {
                        parts[4] = newQuantity;
                        parts[7] = newSupplierId; // Update supplier ID
                        parts[10] = newStatus.toLowerCase();
                        updated.add(String.join(",", parts));
                    } else {
                        updated.add(line);
                    }
                }
                File_Utils.writeLines("data/purchase_orders_data.txt", new ArrayList<>(updated));
                JOptionPane.showMessageDialog(null, "Purchase order updated successfully!");
                loadOrders(searchField.getText().trim().toLowerCase(), (String) statusFilter.getSelectedItem());
            }
        });
    }

    // Helper to capitalize status for display
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // Loads and displays purchase orders from file, applying search and status filters
    private void loadOrders(String filter, String statusFilter) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/purchase_orders_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 11) {
                boolean match = filter.isEmpty();
                if (!match) {
                    for (String part : parts) {
                        if (part.toLowerCase().contains(filter)) {
                            match = true;
                            break;
                        }
                    }
                }
                boolean statusMatch = statusFilter == null || statusFilter.equals("All") || parts[10].equalsIgnoreCase(statusFilter);
                if (match && statusMatch) tableModel.addRow(parts);
            }
        }
    }
} 