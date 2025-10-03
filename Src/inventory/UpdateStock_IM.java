import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Objects;

// Panel for Inventory Manager to update stock based on delivered purchase orders
public class UpdateStock_IM extends JPanel {
    // Table displaying purchase orders
    private final JTable poTable;
    // Table model for managing purchase order data
    private final DefaultTableModel tableModel;
    // Search field for filtering purchase orders
    private final JTextField searchField;
    // Combo box for filtering by status (Approved/Delivered)
    private final JComboBox<String> statusFilter;

    // Constructor: sets up the UI and event handlers
    public UpdateStock_IM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search + Filter ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("Update Stock from Purchase Orders", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        topPanel.add(title);

        // Search and Filter Panel
        JPanel searchFilterPanel = new JPanel(new BorderLayout(10, 0));
        searchFilterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchFilterPanel.setOpaque(false);

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel(" Search: ");
        searchLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchField = new JTextField();
        searchField.setToolTipText("Search by any field...");
        searchField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Status Filter Panel
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel(" Status: ");
        filterLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        filterPanel.add(filterLabel, BorderLayout.WEST);
        String[] statuses = {"All", "Approved", "Delivered"};
        statusFilter = new JComboBox<>(statuses);
        statusFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        filterPanel.add(statusFilter, BorderLayout.CENTER);

        searchFilterPanel.add(searchPanel, BorderLayout.CENTER);
        searchFilterPanel.add(filterPanel, BorderLayout.EAST);
        searchFilterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        topPanel.add(searchFilterPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows purchase order data ---
        String[] columnNames = {"PONumber", "PRNumber", "ItemCode", "ItemName", "Quantity", "TotalPrice", "OrderDate1", "SupplierID", "OWNUM", "OrderDate2", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Custom renderer for striped rows (yellow theme)
        class YellowStripedRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK);
                
                // Color the status column
                if (column == 10 && value != null) {
                    String status = value.toString().toLowerCase();
                    if (status.equals("approved")) {
                        c.setForeground(new Color(0, 128, 0)); // Dark green
                    } else if (status.equals("delivered")) {
                        c.setForeground(new Color(0, 102, 204)); // Vivid blue
                    }
                }

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 250, 230) : Color.WHITE);
                } else {
                    c.setBackground(new Color(255, 235, 180));
                }
                return c;
            }
        }

        poTable = new JTable(tableModel);
        YellowStripedRowRenderer yellowRenderer = new YellowStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            poTable.getColumnModel().getColumn(i).setCellRenderer(yellowRenderer);
        }
        poTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        poTable.setRowHeight(22);
        poTable.setGridColor(new Color(255, 235, 180));
        poTable.setShowGrid(true);
        poTable.setSelectionBackground(new Color(255, 235, 180));
        poTable.setSelectionForeground(Color.BLACK);
        poTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        poTable.getTableHeader().setBackground(new Color(255, 245, 200));
        poTable.getTableHeader().setForeground(new Color(120, 100, 30));
        poTable.setFillsViewportHeight(true);

        // Set column widths for better readability
        int[] colWidths = {80, 80, 80, 120, 60, 80, 100, 90, 80, 100, 80};
        for (int i = 0; i < colWidths.length && i < poTable.getColumnCount(); i++) {
            poTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scroll = new JScrollPane(poTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: Update Stock ---
        JButton updateBtn = new JButton("\uD83D\uDCE6 Update Stock");
        updateBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        updateBtn.setBackground(new Color(255, 245, 220));
        updateBtn.setFocusPainted(false);
        updateBtn.setPreferredSize(new Dimension(220, 35));
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.setBackground(new Color(255, 250, 240));
        btnPanel.add(updateBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // --- Search and status filter listeners ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        statusFilter.addActionListener(_ -> filterTable());

        // --- Update Stock button handler ---
        updateBtn.addActionListener(_ -> {
            int row = poTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Please select a purchase order first.");
                return;
            }

            String status = poTable.getValueAt(row, 10).toString().toLowerCase();
            if (!status.equals("approved")) {
                JOptionPane.showMessageDialog(null, "Only approved purchase orders can be updated to delivered status.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to mark this purchase order as delivered and update the stock?",
                    "Confirm Update",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Update the status in the file
                String poNumber = poTable.getValueAt(row, 0).toString();
                String itemCode = poTable.getValueAt(row, 2).toString();
                String quantity = poTable.getValueAt(row, 4).toString();
                
                // Update purchase order status
                File_Utils.updateLine("data/purchase_orders_data.txt", poNumber, "delivered");
                
                // Update item stock
                File_Utils.updateItemStock(itemCode, Integer.parseInt(quantity));
                
                // Refresh the table
                filterTable();
                
                JOptionPane.showMessageDialog(null, "Stock updated successfully!");
            }
        });

        // Initial load of the table
        filterTable();
    }

    // Filters and displays purchase orders based on search and status filters
    private void filterTable() {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedStatus = Objects.requireNonNull(statusFilter.getSelectedItem()).toString();
        
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/purchase_orders_data.txt");
        
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 11) {
                String status = parts[10].toLowerCase();
                
                // Only show approved and delivered POs
                if (!status.equals("approved") && !status.equals("delivered")) {
                    continue;
                }
                
                // Apply status filter
                if (selectedStatus.equals("Approved") && !status.equals("approved")) {
                    continue;
                }
                if (selectedStatus.equals("Delivered") && !status.equals("delivered")) {
                    continue;
                }
                
                // Apply search filter
                boolean match = searchText.isEmpty();
                if (!match) {
                    for (String part : parts) {
                        if (part.toLowerCase().contains(searchText)) {
                            match = true;
                            break;
                        }
                    }
                }
                
                if (match) {
                    tableModel.addRow(parts);
                }
            }
        }
    }
} 