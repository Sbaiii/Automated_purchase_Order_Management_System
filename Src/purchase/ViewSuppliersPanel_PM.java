import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel for Purchase Manager to view and search suppliers, and see supplier details.
 * Provides a table with search functionality and a detailed view dialog.
 * Uses a blue color theme for the UI elements.
 */
public class ViewSuppliersPanel_PM extends JPanel {
    /** Table displaying supplier records */
    private final JTable supplierTable;
    /** Table model for managing supplier data */
    private final DefaultTableModel tableModel;
    /** Search field for filtering suppliers */
    private final JTextField searchField;

    /**
     * Constructor: sets up the UI and event handlers for viewing suppliers.
     * Initializes the panel with a search interface and a table to display suppliers.
     */
    public ViewSuppliersPanel_PM() {
        // Set the main layout for the panel
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Vertical stacking
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // Padding

        // Title label for the panel
        JLabel title = new JLabel("View Suppliers", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        topPanel.add(title);

        // Search panel containing search field
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
        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows supplier records ---
        String[] columnNames = {
            "SupplierID", "Supplier Name", "Phone Number", "Address", "Rating", "Specialty 1", "Specialty 2",
            "Email", "Bank Account", "Lead Time", "Contract Expiry", "Active", "Total Transactions", "Remarks"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make all cells non-editable for read-only view
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        /**
         * Custom renderer for striped rows (blue theme).
         * Provides visual distinction between alternating supplier rows.
         */
        class BlueStripedRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK);
                // Striped rows with blue theme
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(230, 240, 255) : Color.WHITE);
                } else {
                    c.setBackground(new Color(180, 210, 255));
                }
                return c;
            }
        }

        // Create and configure the table with custom renderer
        supplierTable = new JTable(tableModel);
        BlueStripedRowRenderer blueRenderer = new BlueStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            supplierTable.getColumnModel().getColumn(i).setCellRenderer(blueRenderer);
        }
        // Set table appearance properties
        supplierTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        supplierTable.setRowHeight(22);
        supplierTable.setGridColor(new Color(180, 210, 255));
        supplierTable.setShowGrid(true);
        supplierTable.setSelectionBackground(new Color(180, 210, 255));
        supplierTable.setSelectionForeground(Color.BLACK);
        supplierTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        supplierTable.getTableHeader().setBackground(new Color(200, 220, 250));
        supplierTable.getTableHeader().setForeground(new Color(30, 60, 120));
        supplierTable.setFillsViewportHeight(true);
        // Set preferred column widths for better appearance
        int[] colWidths = {80, 120, 110, 120, 60, 90, 90, 140, 120, 100, 100, 60, 80, 160};
        for (int i = 0; i < colWidths.length && i < supplierTable.getColumnCount(); i++) {
            supplierTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(supplierTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: View Selected Supplier ---
        JButton viewBtn = new JButton("View Selected Supplier");
        viewBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        viewBtn.setBackground(new Color(225, 225, 255));
        viewBtn.setFocusPainted(false);
        viewBtn.setPreferredSize(new Dimension(200, 35));
        JPanel btnPanel = new JPanel();
        btnPanel.add(viewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load initial suppliers into the table (no filter)
        loadSuppliers("");

        // --- Search listener ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loadSuppliers(searchField.getText().trim().toLowerCase());
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loadSuppliers(searchField.getText().trim().toLowerCase());
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // --- View supplier details button handler ---
        viewBtn.addActionListener(_ -> {
            int row = supplierTable.getSelectedRow();
            if (row == -1) {
                // No row selected
                JOptionPane.showMessageDialog(null, "Please select a supplier first.");
                return;
            }

            // Labels for the supplier details dialog
            String[] fieldLabels = {
                "Supplier ID", "Supplier Name", "Phone Number", "Address", "Rating", "Specialty 1", "Specialty 2",
                "Email", "Bank Account", "Payment Terms", "Contract Expiry", "Active", "Total Transactions", "Remarks"
            };
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBackground(new Color(230, 240, 255));
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 160, 220), 2, true),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)
            ));

            // Title for the details dialog
            JLabel cardTitle = new JLabel("🏷️  Supplier Details", SwingConstants.CENTER);
            cardTitle.setFont(new Font("Times New Roman", Font.BOLD, 20));
            cardTitle.setForeground(new Color(30, 60, 120));
            cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
            cardPanel.add(cardTitle, BorderLayout.NORTH);

            // Grid for displaying supplier details
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
                nameLabel.setForeground(new Color(30, 60, 120));
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
                detailsGrid.add(valueLabel, gbc);
                gbc.gridy++;
            }

            cardPanel.add(detailsGrid, BorderLayout.CENTER);

            // Show the details dialog
            JOptionPane.showMessageDialog(null, cardPanel, "🏷️  Supplier Details", JOptionPane.PLAIN_MESSAGE);
        });
    }

    /**
     * Loads suppliers from file, applies search filter, and populates the table.
     * @param filter Text filter for searching suppliers (case-insensitive, matches any field)
     */
    private void loadSuppliers(String filter) {
        // Clear the table
        tableModel.setRowCount(0);
        // Read all lines from the data file
        List<String> lines = File_Utils.readLines("data/suppliers_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 14) {
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
                if (match) tableModel.addRow(parts);
            }
        }
    }
}