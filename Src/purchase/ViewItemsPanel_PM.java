import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

// Panel for Purchase Manager to view and search items, and see item details
public class ViewItemsPanel_PM extends JPanel {
    // Table displaying item records
    private final JTable itemTable;
    // Table model for managing item data
    private final DefaultTableModel tableModel;
    // Search field for filtering items
    private final JTextField searchField;

    // Constructor: sets up the UI and event handlers
    public ViewItemsPanel_PM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("View Items", SwingConstants.LEFT);
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
        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows item records ---
        String[] columnNames = {"ItemCode", "ItemName", "SupplierID", "Stock", "UnitPrice", "PurchasePrice", "Category", "ExpiryDate", "Remarks"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Custom renderer for striped rows (blue theme)
        class BlueStripedRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(230, 240, 255) : Color.WHITE);
                } else {
                    c.setBackground(new Color(180, 210, 255));
                }
                return c;
            }
        }

        itemTable = new JTable(tableModel);
        BlueStripedRowRenderer blueRenderer = new BlueStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setCellRenderer(blueRenderer);
        }
        itemTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        itemTable.setRowHeight(22);
        itemTable.setGridColor(new Color(180, 210, 255));
        itemTable.setShowGrid(true);
        itemTable.setSelectionBackground(new Color(180, 210, 255));
        itemTable.setSelectionForeground(Color.BLACK);
        itemTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        itemTable.getTableHeader().setBackground(new Color(200, 220, 250));
        itemTable.getTableHeader().setForeground(new Color(30, 60, 120));
        itemTable.setFillsViewportHeight(true);
        int[] colWidths = {80, 120, 90, 60, 80, 80, 100, 100, 160};
        for (int i = 0; i < colWidths.length && i < itemTable.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(itemTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: View Selected Item ---
        JButton viewBtn = new JButton("\uD83D\uDCE6 View Selected Item");
        viewBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        viewBtn.setBackground(new Color(220, 235, 255));
        viewBtn.setFocusPainted(false);
        viewBtn.setPreferredSize(new Dimension(220, 35));
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.setBackground(new Color(240, 245, 255));
        btnPanel.add(viewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load initial items into the table
        loadItems("");

        // Search listener to filter items as user types
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loadItems(searchField.getText().trim().toLowerCase());
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loadItems(searchField.getText().trim().toLowerCase());
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // --- View Selected Item button handler ---
        viewBtn.addActionListener(_ -> {
            int row = itemTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Please select an item first.");
                return;
            }

            String[] fieldLabels = {"Item Code", "Item Name", "Supplier ID", "Stock", "Unit Price", "Purchase Price", "Category", "Expiry Date", "Remarks"};
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBackground(new Color(230, 240, 255));
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 160, 220), 2, true),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)
            ));

            JLabel cardTitle = new JLabel("\uD83D\uDCE6  Item Details", SwingConstants.CENTER);
            cardTitle.setFont(new Font("Times New Roman", Font.BOLD, 20));
            cardTitle.setForeground(new Color(30, 60, 120));
            cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
            cardPanel.add(cardTitle, BorderLayout.NORTH);

            JPanel detailsGrid = new JPanel(new GridBagLayout());
            detailsGrid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 10, 6, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridy = 0;

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

            JOptionPane.showMessageDialog(null, cardPanel, "\uD83D\uDCE6  Item Details", JOptionPane.PLAIN_MESSAGE);
        });
    }

    private void loadItems(String filter) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/items_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
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