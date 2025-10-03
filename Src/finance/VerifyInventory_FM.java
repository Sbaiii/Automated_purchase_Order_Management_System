import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// Panel for Finance Manager to verify delivered purchase orders and record payments
public class VerifyInventory_FM extends JPanel {
    // Table model for displaying purchase orders
    private final DefaultTableModel tableModel;
    // Search field for filtering purchase orders
    private final JTextField searchField;

    // Constructor: sets up the UI and event handlers
    public VerifyInventory_FM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("Verify Delivered Purchase Orders", SwingConstants.LEFT);
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

        // --- TABLE: Shows delivered purchase orders ---
        String[] columnNames = {"PONumber", "PRNumber", "ItemCode", "ItemName", "Quantity", "TotalPrice", "OrderDate1", "SupplierID", "OWNUM", "OrderDate2", "Status", "Verify"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 11; // Only the Verify button is editable
            }
        };

        // Custom renderer for striped rows (green theme)
        class GreenStripedRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK);
                if (column == 10 && value != null && value.toString().equalsIgnoreCase("delivered")) {
                    c.setForeground(new Color(0, 102, 204)); // Vivid blue for delivered
                }
                Color bg = isSelected
                        ? new Color(200, 230, 201)
                        : (row % 2 == 0 ? new Color(232, 245, 233) : Color.WHITE);
                c.setBackground(bg);
                return c;
            }
        }

        JTable poTable = new JTable(tableModel);
        GreenStripedRowRenderer greenRenderer = new GreenStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount() - 1; i++) {
            poTable.getColumnModel().getColumn(i).setCellRenderer(greenRenderer);
        }
        poTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        poTable.setRowHeight(22);
        poTable.setGridColor(new Color(200, 230, 201));
        poTable.setShowGrid(true);
        poTable.setSelectionBackground(new Color(200, 230, 201));
        poTable.setSelectionForeground(Color.BLACK);
        poTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        poTable.getTableHeader().setBackground(new Color(165, 214, 167));
        poTable.getTableHeader().setForeground(new Color(27, 94, 32));
        poTable.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(poTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // Add Verify button as a cell editor and renderer
        poTable.getColumnModel().getColumn(11).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JButton button = new JButton("Verify");
                button.setBackground(new Color(255, 245, 220));
                button.setFont(new Font("Times New Roman", Font.BOLD, 12));
                button.addActionListener(_ -> verifyPO(row));
                return button;
            }
        });
        poTable.getColumnModel().getColumn(11).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JButton button = new JButton("Verify");
                button.setBackground(new Color(255, 245, 220));
                button.setFont(new Font("Times New Roman", Font.BOLD, 12));
                return button;
            }
        });

        // Search listener to filter purchase orders as user types
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadPOs(searchField.getText().trim().toLowerCase()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadPOs(searchField.getText().trim().toLowerCase()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // Initial load of delivered purchase orders
        loadPOs("");
    }

    // Loads and displays delivered purchase orders, applying search filter if provided
    private void loadPOs(String filter) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/purchase_orders_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 11) {
                String status = parts[10].toLowerCase();
                if (!status.equals("delivered")) continue;
                boolean match = filter.isEmpty();
                if (!match) {
                    for (String part : parts) {
                        if (part.toLowerCase().contains(filter)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (match) {
                    Object[] row = new Object[12];
                    System.arraycopy(parts, 0, row, 0, 11);
                    row[11] = "Verify";
                    tableModel.addRow(row);
                }
            }
        }
    }

    // Generates the next Payment ID in the format PAY###
    private String generateNextPaymentId() {
        List<String> lines = File_Utils.readLines("data/payments_data.txt");
        int max = 0;
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length > 0 && parts[0].startsWith("PAY")) {
                try {
                    int num = Integer.parseInt(parts[0].substring(3));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PAY%03d", max + 1);
    }

    // Handles verification of a delivered purchase order and records payment
    private void verifyPO(int row) {
        String poNumber = (String) tableModel.getValueAt(row, 0);
        String totalPrice = (String) tableModel.getValueAt(row, 5);
        String supplierId = (String) tableModel.getValueAt(row, 7);
        String itemCode = (String) tableModel.getValueAt(row, 2);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String verifiedBy = Session.getLoggedInUserId();
        String paymentId = generateNextPaymentId();

        // Fetch supplier infos (supplier name, phone, email, bank account) from data/suppliers_data.txt
        String supplierName = "", supplierPhone = "", supplierEmail = "", supplierBank = "";
        List<String> supplierLines = File_Utils.readLines("data/suppliers_data.txt");
        for (String line : supplierLines) {
            String[] parts = line.split(",", -1);
            if (parts.length > 0 && parts[0].equals(supplierId)) {
                if (parts.length > 1) supplierName = parts[1];
                if (parts.length > 2) supplierPhone = parts[2];
                if (parts.length > 7) supplierEmail = parts[7];
                if (parts.length > 8) supplierBank = parts[8];
                break;
            }
        }

        // Update data/purchase_orders_data.txt to mark as verified
        List<String> lines = File_Utils.readLines("data/purchase_orders_data.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/purchase_orders_data.txt"));
            for (String line : lines) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 11 && parts[0].equals(poNumber)) {
                    parts[10] = "verified";
                    writer.write(String.join(",", parts));
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
            writer.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update PO: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add payment record (now including supplier infos)
        try (BufferedWriter payWriter = new BufferedWriter(new FileWriter("data/payments_data.txt", true))) {
            String paymentLine = String.join(",",
                paymentId, poNumber, itemCode, supplierId, totalPrice, date, verifiedBy,
                supplierName, supplierPhone, supplierEmail, supplierBank
            ) + ",pending";
            payWriter.write(paymentLine);
            payWriter.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to record payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "PO verified and payment recorded.");
        loadPOs(searchField.getText().trim().toLowerCase());
    }
} 