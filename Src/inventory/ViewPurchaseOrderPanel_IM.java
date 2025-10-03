import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel for Inventory Manager to view and search purchase orders, and see order details.
 * Provides a table with filtering and status options, and a detailed view dialog.
 */
public class ViewPurchaseOrderPanel_IM extends JPanel {
    /** Table displaying purchase order records */
    private final JTable ordersTable;
    /** Table model for managing order data */
    private final DefaultTableModel tableModel;
    /** Search field for filtering orders */
    private final JTextField searchField;

    /**
     * Constructor: sets up the UI and event handlers for viewing purchase orders.
     */
    public ViewPurchaseOrderPanel_IM() {
        // Set the main layout for the panel
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Vertical stacking
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // Padding

        // Title label
        JLabel title = new JLabel("View Purchase Orders", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        topPanel.add(title);

        // Search panel (search field + status filter)
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
        // Status filter dropdown for filtering by order status
        String[] statusOptions = {"All", "Issued", "Approved", "Delivered", "Rejected", "Verified"};
        JComboBox<String> statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        statusFilter.setMaximumSize(new Dimension(140, 32));
        searchPanel.add(statusFilter, BorderLayout.EAST);
        topPanel.add(searchPanel);

        // Add the top panel to the north region
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows purchase order records ---
        String[] columnNames = {"POID", "RequisitionID", "ItemCode", "ItemName", "Quantity", "Purchase Price", "RequiredBy", "SupplierID", "ManagerID", "Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make all cells non-editable
            public boolean isCellEditable(int row, int column) { return false; }
        };

        /**
         * Custom renderer for striped rows (blue theme) and status coloring.
         */
        class BlueStripedRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                // Get status value for coloring
                if (table.getColumnCount() > 10 && table.getValueAt(row, 10) != null) {
                    status = table.getValueAt(row, 10).toString().trim().toLowerCase();
                }
                // Set text color for status column
                if (column == 10) {
                    switch (status) {
                        case "issued" -> c.setForeground(new Color(66, 133, 244)); // Blue
                        case "approved", "delivered" -> c.setForeground(new Color(0, 140, 0)); // Green
                        case "rejected" -> c.setForeground(new Color(180, 0, 0)); // Red
                        default -> c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
                // Striped rows (blue theme)
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 250, 230) : Color.WHITE);
                } else {
                    c.setBackground(new Color(255, 235, 180));
                }
                return c;
            }
        }

        // Create the table and apply the custom renderer
        ordersTable = new JTable(tableModel);
        BlueStripedRowRenderer blueRenderer = new BlueStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(blueRenderer);
        }
        ordersTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        ordersTable.setRowHeight(22);
        ordersTable.setGridColor(new Color(255, 235, 180));
        ordersTable.setShowGrid(true);
        ordersTable.setSelectionBackground(new Color(255, 235, 180));
        ordersTable.setSelectionForeground(Color.BLACK);
        ordersTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        ordersTable.getTableHeader().setBackground(new Color(255, 245, 200));
        ordersTable.getTableHeader().setForeground(new Color(120, 100, 30));
        ordersTable.setFillsViewportHeight(true);
        // Set preferred column widths for better appearance
        int[] colWidths = {80, 90, 90, 120, 70, 100, 110, 90, 90, 100, 80};
        for (int i = 0; i < colWidths.length && i < ordersTable.getColumnCount(); i++) {
            ordersTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(ordersTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: View Selected Order ---
        JButton viewBtn = new JButton("\uD83D\uDCE6 View Selected Order");
        viewBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        viewBtn.setBackground(new Color(255, 245, 220));
        viewBtn.setFocusPainted(false);
        viewBtn.setPreferredSize(new Dimension(220, 35));
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.setBackground(new Color(255, 250, 240));
        btnPanel.add(viewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load initial orders into the table (no filter)
        loadOrders("", null);

        // --- Search and status filter listeners ---
        // Update table as user types in the search field
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadOrders(searchField.getText().trim().toLowerCase(), (String) statusFilter.getSelectedItem()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadOrders(searchField.getText().trim().toLowerCase(), (String) statusFilter.getSelectedItem()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        // Update table when status filter changes
        statusFilter.addActionListener(_ -> loadOrders(searchField.getText().trim().toLowerCase(), (String) statusFilter.getSelectedItem()));

        // --- View Selected Order button handler ---
        viewBtn.addActionListener(_ -> {
            int row = ordersTable.getSelectedRow();
            if (row == -1) {
                // No row selected
                JOptionPane.showMessageDialog(null, "Please select an order first.");
                return;
            }

            // Labels for the order details
            String[] fieldLabels = {"PO ID", "Requisition ID", "Item Code", "Item Name", "Quantity", "Purchase Price", "Required By", "Supplier ID", "Manager ID", "Date", "Status"};
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBackground(new Color(255, 250, 230));
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 180, 120), 2, true),
                    BorderFactory.createEmptyBorder(18, 28, 18, 28)
            ));

            // Title for the details dialog
            JLabel cardTitle = new JLabel("\uD83D\uDCE6  Purchase Order Details", SwingConstants.CENTER);
            cardTitle.setFont(new Font("Times New Roman", Font.BOLD, 20));
            cardTitle.setForeground(new Color(120, 100, 30));
            cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
            cardPanel.add(cardTitle, BorderLayout.NORTH);

            // Grid for displaying order details
            JPanel detailsGrid = new JPanel(new GridBagLayout());
            detailsGrid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 10, 6, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridy = 0;

            // Add each field label and value to the grid
            for (int i = 0; i < fieldLabels.length; i++) {
                JLabel nameLabel = new JLabel(fieldLabels[i] + ":");
                nameLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
                nameLabel.setForeground(new Color(120, 100, 30));
                nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                gbc.gridx = 0;
                gbc.weightx = 0.3;
                detailsGrid.add(nameLabel, gbc);

                JLabel valueLabel = new JLabel(tableModel.getValueAt(row, i).toString());
                valueLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
                valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                if (i == 10) { // Status column
                    String status = valueLabel.getText().trim().toLowerCase();
                    switch (status) {
                        case "issued" -> valueLabel.setForeground(new Color(66, 133, 244)); // Blue
                        case "approved", "delivered" -> valueLabel.setForeground(new Color(0, 140, 0)); // Green
                        case "rejected" -> valueLabel.setForeground(new Color(180, 0, 0)); // Red
                        default -> valueLabel.setForeground(Color.BLACK);
                    }
                } else {
                    valueLabel.setForeground(Color.DARK_GRAY);
                }
                gbc.gridx = 1;
                gbc.weightx = 0.7;
                detailsGrid.add(valueLabel, gbc);
                gbc.gridy++;
            }

            cardPanel.add(detailsGrid, BorderLayout.CENTER);

            // Show the details dialog
            JOptionPane.showMessageDialog(null, cardPanel, "\uD83D\uDCE6  Purchase Order Details", JOptionPane.PLAIN_MESSAGE);
        });
    }

    /**
     * Loads purchase orders from file, applies search and status filters, and populates the table.
     * @param filter Text filter for searching orders (case-insensitive, matches any field)
     * @param statusFilter Status filter ("All" or specific status)
     */
    private void loadOrders(String filter, String statusFilter) {
        // Clear the table
        tableModel.setRowCount(0);
        // Read all lines from the data file
        List<String> lines = File_Utils.readLines("data/purchase_orders_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 11) {
                boolean match = filter.isEmpty();
                // Check if any field matches the filter
                if (!match) {
                    for (String part : parts) {
                        if (part.toLowerCase().contains(filter)) {
                            match = true;
                            break;
                        }
                    }
                }
                // Check if status matches the selected filter
                boolean statusMatch = statusFilter == null || statusFilter.equals("All") || parts[10].equalsIgnoreCase(statusFilter);
                if (match && statusMatch) tableModel.addRow(parts);
            }
        }
    }
}