import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel for Finance Manager to view and search purchase requisitions, and see requisition details.
 * Provides a table with filtering and status options, and a detailed view dialog.
 * Uses a green color theme for the UI elements.
 */
public class ViewPurchaseRequisitionPanel_FM extends JPanel {
    /** Table displaying purchase requisition records */
    private final JTable requisitionTable;
    /** Table model for managing requisition data */
    private final DefaultTableModel tableModel;
    /** Search field for filtering requisitions */
    private final JTextField searchField;

    /**
     * Constructor: sets up the UI and event handlers for viewing purchase requisitions.
     * Initializes the panel with a search interface, status filter, and a table to display requisitions.
     */
    public ViewPurchaseRequisitionPanel_FM() {
        // Set the main layout for the panel
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Vertical stacking
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // Padding

        // Title label for the panel
        JLabel title = new JLabel("View Purchase Requisitions", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        topPanel.add(title);

        // Search panel containing search field and status filter
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
        // Status filter dropdown for filtering requisitions by their status
        String[] statusOptions = {"All", "Pending", "Approved", "Rejected", "Cancelled"};
        JComboBox<String> statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        statusFilter.setMaximumSize(new Dimension(140, 32));
        searchPanel.add(statusFilter, BorderLayout.EAST);
        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows purchase requisition records ---
        String[] columnNames = {"RequisitionID", "ItemCode", "ItemName", "Quantity", "RequiredBy", "SupplierID", "SalesManagerID", "Priority", "Remarks", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make all cells non-editable for read-only view
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        /**
         * Custom renderer for status coloring and striped rows (green theme).
         * Provides visual distinction between different requisition statuses and alternating row colors.
         */
        class StatusColorRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                // Get status value for coloring
                if (table.getColumnCount() > 9 && table.getValueAt(row, 9) != null) {
                    status = table.getValueAt(row, 9).toString().trim().toLowerCase();
                }
                // Set text color for status column based on requisition status
                if (column == 9) {
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
                // Striped rows with green theme
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(232, 245, 233) : Color.WHITE);
                } else {
                    c.setBackground(new Color(200, 230, 201));
                }
                return c;
            }
        }

        // Create and configure the table with custom renderer
        requisitionTable = new JTable(tableModel);
        StatusColorRenderer statusRenderer = new StatusColorRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            requisitionTable.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
        }
        // Set table appearance properties
        requisitionTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        requisitionTable.setRowHeight(22);
        requisitionTable.setGridColor(new Color(200, 230, 201));
        requisitionTable.setShowGrid(true);
        requisitionTable.setSelectionBackground(new Color(200, 230, 201));
        requisitionTable.setSelectionForeground(Color.BLACK);
        requisitionTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        requisitionTable.getTableHeader().setBackground(new Color(165, 214, 167));
        requisitionTable.getTableHeader().setForeground(new Color(27, 94, 32));
        requisitionTable.setFillsViewportHeight(true);
        // Set preferred column widths for better appearance
        int[] colWidths = {80, 80, 120, 60, 100, 90, 90, 70, 120, 70};
        for (int i = 0; i < colWidths.length && i < requisitionTable.getColumnCount(); i++) {
            requisitionTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(requisitionTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: View Selected Requisition ---
        JButton viewBtn = new JButton("\uD83D\uDCCA View Selected Requisition");
        viewBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        viewBtn.setBackground(new Color(220, 245, 220));
        viewBtn.setFocusPainted(false);
        viewBtn.setPreferredSize(new Dimension(220, 35));
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.setBackground(new Color(240, 255, 240));
        btnPanel.add(viewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load initial requisitions into the table (no filter)
        loadRequisitions("", null);

        // --- Search and status filter listeners ---
        // Update table as user types in the search field
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadRequisitions(searchField.getText().trim().toLowerCase(), (String) statusFilter.getSelectedItem()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadRequisitions(searchField.getText().trim().toLowerCase(), (String) statusFilter.getSelectedItem()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        // Update table when status filter changes
        statusFilter.addActionListener(_ -> loadRequisitions(searchField.getText().trim().toLowerCase(), (String) statusFilter.getSelectedItem()));

        // --- View Selected Requisition button handler ---
        viewBtn.addActionListener(_ -> {
            int row = requisitionTable.getSelectedRow();
            if (row == -1) {
                // No row selected
                JOptionPane.showMessageDialog(null, "Please select a requisition first.");
                return;
            }

            // Labels for the requisition details dialog
            String[] fieldLabels = {"Requisition ID", "Item Code", "Item Name", "Quantity", "Required By", "Supplier ID", "Sales Manager ID", "Priority", "Remarks", "Status"};
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBackground(new Color(232, 245, 233));
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(120, 180, 120), 2, true),
                    BorderFactory.createEmptyBorder(18, 28, 18, 28)
            ));

            // Title for the details dialog
            JLabel cardTitle = new JLabel("\uD83D\uDCCB  Purchase Requisition Details", SwingConstants.CENTER);
            cardTitle.setFont(new Font("Times New Roman", Font.BOLD, 20));
            cardTitle.setForeground(new Color(27, 94, 32));
            cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
            cardPanel.add(cardTitle, BorderLayout.NORTH);

            // Grid for displaying requisition details
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
                nameLabel.setForeground(new Color(27, 94, 32));
                nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                gbc.gridx = 0;
                gbc.weightx = 0.3;
                detailsGrid.add(nameLabel, gbc);

                JLabel valueLabel = new JLabel(tableModel.getValueAt(row, i).toString());
                valueLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
                valueLabel.setForeground(Color.DARK_GRAY);
                valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                gbc.gridx = 1;
                gbc.weightx = 0.7;
                // Color the status value
                if (i == 9) {
                    String status = tableModel.getValueAt(row, 9).toString().trim().toLowerCase();
                    switch (status) {
                        case "pending" -> valueLabel.setForeground(new Color(180, 140, 0));
                        case "approved" -> valueLabel.setForeground(new Color(0, 140, 0));
                        case "rejected" -> valueLabel.setForeground(new Color(180, 0, 0));
                        case "cancelled" -> valueLabel.setForeground(new Color(120, 120, 120));
                    }
                    valueLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
                }
                detailsGrid.add(valueLabel, gbc);
                gbc.gridy++;
            }

            cardPanel.add(detailsGrid, BorderLayout.CENTER);

            // Show the details dialog
            JOptionPane.showMessageDialog(null, cardPanel, "\uD83D\uDCCB  Purchase Requisition Details", JOptionPane.PLAIN_MESSAGE);
        });
    }

    /**
     * Loads purchase requisitions from file, applies search and status filters, and populates the table.
     * @param filter Text filter for searching requisitions (case-insensitive, matches any field)
     * @param statusFilter Status filter ("All" or specific status)
     */
    private void loadRequisitions(String filter, String statusFilter) {
        // Clear the table
        tableModel.setRowCount(0);
        // Read all lines from the data file
        List<String> lines = File_Utils.readLines("data/purchase_requisitions_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 10) {
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
                boolean statusMatch = statusFilter == null || statusFilter.equals("All") || parts[9].equalsIgnoreCase(statusFilter);
                if (match && statusMatch) tableModel.addRow(parts);
            }
        }
    }
}
