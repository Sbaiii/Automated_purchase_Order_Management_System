import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.util.List;

// Panel for Finance Manager to process and mark payments as paid
public class ProccessPayments_FM extends JPanel {
    // Table for displaying payment records
    private final JTable paymentTable;
    // Table model for managing payment data
    private final DefaultTableModel tableModel;
    // Search field for filtering payments
    private final JTextField searchField;
    // Dropdown for filtering by payment status
    private final JComboBox<String> statusFilter;
    // Column names for the payment table
    private static final String[] COLUMN_NAMES = {
        "PaymentID", "PO_Number", "ItemCode", "SupplierID", "TotalPrice", "Date", "VerifiedBy", "SupplierName", "SupplierPhone", "SupplierEmail", "SupplierBank", "Status"
    };
    // Index of the status column
    private static final int STATUS_COL = 11;

    // Constructor: sets up the UI and event handlers
    public ProccessPayments_FM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search + Status Filter ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("Process Payments", SwingConstants.LEFT);
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
        String[] statusOptions = {"All", "pending", "paid"};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        statusFilter.setSelectedIndex(0);
        searchPanel.add(statusFilter, BorderLayout.EAST);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE ---
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        paymentTable = new JTable(tableModel);
        // Select whole row, not just cell
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentTable.setRowSelectionAllowed(true);
        paymentTable.setColumnSelectionAllowed(false);
        // Custom renderer for striped rows (green theme, with status coloring for Status column)
        class GreenStripedRowRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                if (table.getColumnCount() > STATUS_COL && table.getValueAt(row, STATUS_COL) != null) {
                    status = table.getValueAt(row, STATUS_COL).toString().trim().toLowerCase();
                }
                Color bg = isSelected
                        ? new Color(200, 230, 201)
                        : (row % 2 == 0 ? new Color(232, 245, 233) : Color.WHITE);
                if (column == STATUS_COL) {
                    c.setHorizontalAlignment(SwingConstants.LEFT);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    switch (status) {
                        case "pending": c.setForeground(new Color(255, 140, 0)); break;
                        case "paid": c.setForeground(new Color(0, 140, 0)); break;
                        default: c.setForeground(Color.BLACK); break;
                    }
                    c.setBackground(bg);
                } else {
                    c.setForeground(Color.BLACK);
                    c.setBackground(bg);
                }
                return c;
            }
        }
        GreenStripedRowRenderer greenRenderer = new GreenStripedRowRenderer();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            paymentTable.getColumnModel().getColumn(i).setCellRenderer(greenRenderer);
        }
        paymentTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        paymentTable.setRowHeight(22);
        paymentTable.setGridColor(new Color(200, 230, 201));
        paymentTable.setShowGrid(true);
        paymentTable.setSelectionBackground(new Color(200, 230, 201));
        paymentTable.setSelectionForeground(Color.BLACK);
        paymentTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        paymentTable.getTableHeader().setBackground(new Color(165, 214, 167));
        paymentTable.getTableHeader().setForeground(new Color(27, 94, 32));
        paymentTable.setFillsViewportHeight(true);
        int[] colWidths = {90, 90, 90, 90, 90, 140, 90, 120, 120, 160, 120, 90};
        for (int i = 0; i < colWidths.length && i < paymentTable.getColumnCount(); i++) {
            paymentTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scroll = new JScrollPane(paymentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- Listeners ---
        Runnable updateTable = this::filterPayments;
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTable.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTable.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        statusFilter.addActionListener(_ -> updateTable.run());

        // --- BUTTONS PANEL ---
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.setBackground(new Color(240, 255, 240));
        JButton viewBtn = new JButton("View Payment");
        viewBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        viewBtn.setBackground(new Color(220, 235, 255));
        viewBtn.setFocusPainted(false);
        viewBtn.setPreferredSize(new Dimension(160, 35));
        btnPanel.add(viewBtn);
        JButton payBtn = new JButton("Mark as Paid");
        payBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        payBtn.setBackground(new Color(200, 230, 255));
        payBtn.setFocusPainted(false);
        payBtn.setPreferredSize(new Dimension(160, 35));
        btnPanel.add(payBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // --- VIEW PAYMENT LOGIC ---
        viewBtn.addActionListener(_ -> {
            int row = paymentTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a payment first.");
                return;
            }
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(new Color(232, 245, 233));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 180, 120), 2, true),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)
            ));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridy = 0;

            JLabel icon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
            icon.setPreferredSize(new Dimension(48, 48));
            gbc.gridx = 0; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(icon, gbc);
            gbc.gridy++;
            JLabel titleLabel = new JLabel("\uD83D\uDCB3  Payment Details");
            titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 22));
            titleLabel.setForeground(new Color(27, 94, 32));
            gbc.gridx = 0; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(titleLabel, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;

            String[] labels = {"Payment ID:", "PO Number:", "Item Code:", "Supplier ID:", "Total Price:", "Date:", "Verified By:", "Supplier Name:", "Supplier Phone:", "Supplier Email:", "Supplier Bank:", "Status:"};
            for (int i = 0; i < labels.length; i++) {
                gbc.gridx = 0;
                JLabel l = new JLabel(labels[i]);
                l.setFont(new Font("Times New Roman", Font.BOLD, 16));
                panel.add(l, gbc);
                gbc.gridx = 1;
                JLabel v = new JLabel(tableModel.getValueAt(row, i).toString());
                v.setFont(new Font("Times New Roman", Font.PLAIN, 16));
                if (labels[i].equals("Status:")) {
                    String status = v.getText().toLowerCase();
                    switch (status) {
                        case "pending": v.setForeground(new Color(255, 140, 0)); break;
                        case "paid": v.setForeground(new Color(0, 140, 0)); break;
                        default: v.setForeground(Color.BLACK); break;
                    }
                    v.setFont(v.getFont().deriveFont(Font.BOLD));
                }
                panel.add(v, gbc);
                gbc.gridy++;
            }
            JOptionPane.showMessageDialog(this, panel, "Payment Details", JOptionPane.PLAIN_MESSAGE);
        });

        // --- MARK AS PAID LOGIC ---
        payBtn.addActionListener(_ -> {
            int row = paymentTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a payment to mark as paid.");
                return;
            }
            String paymentId = tableModel.getValueAt(row, 0).toString();
            List<String> lines = File_Utils.readLines("data/payments_data.txt");
            boolean updated = false;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/payments_data.txt"))) {
                for (String line : lines) {
                    String[] parts = line.split(",", -1);
                    if (parts.length > STATUS_COL && parts[0].equals(paymentId)) {
                        parts[STATUS_COL] = "paid";
                        writer.write(String.join(",", parts));
                        updated = true;
                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (updated) {
                JOptionPane.showMessageDialog(this, "Payment marked as paid.");
                loadPayments();
            } else {
                JOptionPane.showMessageDialog(this, "Payment not found or already paid.");
            }
        });

        // --- Initial load ---
        loadPayments();
    }

    // Loads all payments from data/payments_data.txt into the table
    private void loadPayments() {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/payments_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            Object[] row = new Object[COLUMN_NAMES.length];
            for (int i = 0; i < COLUMN_NAMES.length; i++) {
                row[i] = parts.length > i ? parts[i] : "";
            }
            tableModel.addRow(row);
        }
    }

    // Filters the payments table based on search and status filter
    private void filterPayments() {
        String filter = searchField.getText().trim().toLowerCase();
        String statusFilterText = (String) statusFilter.getSelectedItem();
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/payments_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            Object[] row = new Object[COLUMN_NAMES.length];
            for (int i = 0; i < COLUMN_NAMES.length; i++) {
                row[i] = parts.length > i ? parts[i] : "";
            }
            boolean match = filter.isEmpty();
            if (!match) {
                for (Object part : row) {
                    if (part != null && part.toString().toLowerCase().contains(filter)) {
                        match = true;
                        break;
                    }
                }
            }
            boolean statusMatch = statusFilterText == null || statusFilterText.equals("All") || row[STATUS_COL].toString().equalsIgnoreCase(statusFilterText);
            if (match && statusMatch) tableModel.addRow(row);
        }
    }
} 