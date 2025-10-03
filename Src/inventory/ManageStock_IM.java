import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Objects;

// Panel for Inventory Manager to view and filter stock items
public class ManageStock_IM extends JPanel {
    // Table model for managing stock data
    private final DefaultTableModel tableModel;
    // Search field for filtering items
    private final JTextField searchField;
    // Dropdown for stock level filtering
    private final JComboBox<String> stockFilter;

    // Constructor: sets up the UI and event handlers
    public ManageStock_IM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search + Filter ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("Manage Stock", SwingConstants.LEFT);
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

        // Stock Filter Panel
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel(" Stock: ");
        filterLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        filterPanel.add(filterLabel, BorderLayout.WEST);
        String[] stockOptions = {
            "All", "Lower than 10", "Lower than 20", "Lower than 30", "Lower than 40",
            "Lower than 50", "Lower than 60", "Lower than 70", "Lower than 80", "Lower than 90", "Lower than 100"
        };
        stockFilter = new JComboBox<>(stockOptions);
        stockFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        filterPanel.add(stockFilter, BorderLayout.CENTER);

        searchFilterPanel.add(searchPanel, BorderLayout.CENTER);
        searchFilterPanel.add(filterPanel, BorderLayout.EAST);
        searchFilterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        topPanel.add(searchFilterPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE ---
        String[] columnNames = {"ItemCode", "ItemName", "SupplierID", "Stock", "ExpiryDate"};
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
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 250, 230) : Color.WHITE);
                } else {
                    c.setBackground(new Color(255, 235, 180));
                }
                return c;
            }
        }

        JTable itemTable = new JTable(tableModel);
        YellowStripedRowRenderer yellowRenderer = new YellowStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setCellRenderer(yellowRenderer);
        }
        itemTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        itemTable.setRowHeight(22);
        itemTable.setGridColor(new Color(255, 235, 180));
        itemTable.setShowGrid(true);
        itemTable.setSelectionBackground(new Color(255, 235, 180));
        itemTable.setSelectionForeground(Color.BLACK);
        itemTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        itemTable.getTableHeader().setBackground(new Color(255, 245, 200));
        itemTable.getTableHeader().setForeground(new Color(120, 100, 30));
        itemTable.setFillsViewportHeight(true);
        int[] colWidths = {80, 120, 90, 60, 100};
        for (int i = 0; i < colWidths.length && i < itemTable.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(itemTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // Listeners for search and stock filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        stockFilter.addActionListener(_ -> filterTable());

        // Initial load of table data
        filterTable();
    }

    // Filters and loads items from data/items_data.txt based on search and stock filter
    private void filterTable() {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedStock = Objects.requireNonNull(stockFilter.getSelectedItem()).toString();
        int stockThreshold = -1;
        if (!selectedStock.equals("All")) {
            stockThreshold = Integer.parseInt(selectedStock.replaceAll("[^0-9]", ""));
        }

        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/items_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 8) {
                String itemCode = parts[0];
                String itemName = parts[1];
                String supplierId = parts[2];
                String stockStr = parts[3];
                String expiryDate = parts[7];
                int stock = 0;
                try { stock = Integer.parseInt(stockStr); } catch (Exception ignored) {}

                // Stock filter
                if (stockThreshold != -1 && stock >= stockThreshold) {
                    continue;
                }

                // Search filter
                boolean match = searchText.isEmpty();
                if (!match) {
                    if (itemCode.toLowerCase().contains(searchText) ||
                        itemName.toLowerCase().contains(searchText) ||
                        supplierId.toLowerCase().contains(searchText) ||
                        stockStr.toLowerCase().contains(searchText) ||
                        expiryDate.toLowerCase().contains(searchText)) {
                        match = true;
                    }
                }
                if (match) {
                    tableModel.addRow(new Object[]{itemCode, itemName, supplierId, stockStr, expiryDate});
                }
            }
        }
    }
} 