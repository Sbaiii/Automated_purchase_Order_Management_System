import javax.swing.*;                              // Swing components for GUI
import javax.swing.table.DefaultTableModel;       // Table model for handling table data
import javax.swing.table.TableRowSorter;          // To enable sorting/filtering in table
import java.awt.*;                                 // For layouts, dimensions, colors, etc.
import java.awt.event.MouseAdapter;               // To detect mouse interactions
import java.awt.event.MouseEvent;                 // Mouse event handler
import java.io.*;                                  // File input/output
import java.nio.file.Files;                        // File reading utility for Paths
import java.nio.file.Path;
import java.text.ParseException;                   // Date parsing exceptions
import java.text.SimpleDateFormat;                 // Used to parse and format date/time
import java.util.Date;                             // Date object for timestamp handling

// Panel class responsible for displaying and interacting with financial reports
public class FinancialReports_FM extends JPanel {
    // Table to list all financial report files
    private final JTable reportTable;
    // Table model to manage table data
    private final DefaultTableModel tableModel;
    // Search box for filtering reports
    private final JTextField searchField;
    // Sorter and filter logic for the table
    private final TableRowSorter<DefaultTableModel> sorter;
    // Label to show overall financial summary
    private final JLabel summaryLabel;

    // Constructor: builds the entire financial reports panel
    public FinancialReports_FM() {
        setLayout(new BorderLayout());                           // Use border layout for main panel

        // --- TOP PANEL: Contains the title, export button, and search bar ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));     // Vertical layout
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // Padding around top

        // Panel that contains the title on the left and export button on the right
        JPanel titleExportPanel = new JPanel(new BorderLayout());
        titleExportPanel.setOpaque(false); // Transparent background

        // Title label setup
        JLabel title = new JLabel("Financial Reports Browser", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0)); // Bottom margin

        // Export report button
        JButton exportBtn = new JButton("\uD83D\uDCC4 Export Report");
        exportBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        exportBtn.setBackground(new Color(200, 230, 201)); // Light green
        exportBtn.setFocusPainted(false);
        exportBtn.setPreferredSize(new Dimension(160, 35));

        // Add title and button to the titleExportPanel
        titleExportPanel.add(title, BorderLayout.WEST);
        titleExportPanel.add(exportBtn, BorderLayout.EAST);
        topPanel.add(titleExportPanel); // Add to the topPanel

        // Search panel setup
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align to the left
        searchPanel.setOpaque(false);

        // Search label
        JLabel searchLabel = new JLabel(" Search: ");
        searchLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));

        // Text field to enter search queries
        searchField = new JTextField();
        searchField.setToolTipText("Search by file, user, or date...");
        searchField.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        // Add search label and field to search panel
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32)); // Restrict height

        topPanel.add(searchPanel); // Add search panel to the top panel
        add(topPanel, BorderLayout.NORTH); // Top panel goes to the NORTH of main layout

        // --- TABLE SETUP: Shows list of financial report files with basic info ---
        String[] columnNames = {"File Name", "Created By", "Date/Time"}; // Table headers

        // Table model defines columns and prevents cell editing
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // All cells are non-editable
            }
        };

        // Table configuration
        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        reportTable.setRowHeight(24);
        reportTable.setGridColor(new Color(200, 230, 201)); // Light green lines
        reportTable.setShowGrid(true); // Enable grid lines
        reportTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 14));
        reportTable.getTableHeader().setBackground(new Color(165, 214, 167)); // Header background
        reportTable.getTableHeader().setForeground(new Color(27, 94, 32));    // Header text
        reportTable.setFillsViewportHeight(true);
        reportTable.setSelectionBackground(new Color(200, 230, 201)); // Selected row background
        reportTable.setSelectionForeground(Color.BLACK);              // Selected text color

        reportTable.setAutoCreateRowSorter(true); // Enable default sorting
        sorter = new TableRowSorter<>(tableModel); // Assign custom sorter
        reportTable.setRowSorter(sorter);          // Apply to table

        // Wrap table in a scroll pane
        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        add(scroll, BorderLayout.CENTER); // Place table in center of the layout

        // --- Load reports from disk into the table ---
        loadReports();

        // --- Setup search functionality ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {} // Not needed for plain text
        });

        // --- Setup double-click action on a table row to open a report ---
        reportTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && reportTable.getSelectedRow() != -1) {
                    int row = reportTable.convertRowIndexToModel(reportTable.getSelectedRow()); // Get model index
                    String fileName = (String) tableModel.getValueAt(row, 0); // Get file name from row
                    showReportContent(fileName); // Open the file content
                }
            }
        });

        // --- SUMMARY PANEL ---
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBackground(new Color(232, 245, 233));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(12, 20, 12, 20),
            BorderFactory.createLineBorder(new Color(120, 180, 120), 2, true)
        ));
        summaryLabel = new JLabel();
        summaryLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        summaryLabel.setForeground(new Color(27, 94, 32));
        summaryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        summaryPanel.add(summaryLabel);
        add(summaryPanel, BorderLayout.SOUTH);
        updateSummary();

        // --- Export Button Action ---
        exportBtn.addActionListener(_ -> {
            // Show date range selection dialog
            String[] options = {"Today", "This Week", "This Month", "Custom Range"};
            JComboBox<String> rangeBox = new JComboBox<>(options);
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Select Range:"), gbc);
            gbc.gridx = 1; panel.add(rangeBox, gbc);
            gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("From:"), gbc);
            gbc.gridx = 1;
            JSpinner fromSpinner = new JSpinner(new SpinnerDateModel());
            fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
            panel.add(fromSpinner, gbc);
            gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("To:"), gbc);
            gbc.gridx = 1;
            JSpinner toSpinner = new JSpinner(new SpinnerDateModel());
            toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd"));
            panel.add(toSpinner, gbc);
            fromSpinner.setEnabled(false);
            toSpinner.setEnabled(false);
            rangeBox.addActionListener(_ -> {
                boolean custom = rangeBox.getSelectedIndex() == 3;
                fromSpinner.setEnabled(custom);
                toSpinner.setEnabled(custom);
            });
            int result = JOptionPane.showConfirmDialog(this, panel, "Export Report Range", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;
            // Determine date range
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.getTime();
            Date fromDate, toDate;
            switch (rangeBox.getSelectedIndex()) {
                case 0: // Today
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    fromDate = cal.getTime();
                    cal.add(java.util.Calendar.DATE, 1);
                    toDate = cal.getTime();
                    break;
                case 1: // This Week
                    cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    fromDate = cal.getTime();
                    cal.add(java.util.Calendar.DATE, 7);
                    toDate = cal.getTime();
                    break;
                case 2: // This Month
                    cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    fromDate = cal.getTime();
                    cal.add(java.util.Calendar.MONTH, 1);
                    toDate = cal.getTime();
                    break;
                case 3: // Custom
                default:
                    fromDate = (Date) fromSpinner.getValue();
                    toDate = (Date) toSpinner.getValue();
                    // Ensure toDate is after fromDate
                    if (toDate.before(fromDate)) {
                        JOptionPane.showMessageDialog(this, "End date must be after start date.", "Invalid Range", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // Add one day to include the end date
                    java.util.Calendar cal2 = java.util.Calendar.getInstance();
                    cal2.setTime(toDate);
                    cal2.add(java.util.Calendar.DATE, 1);
                    toDate = cal2.getTime();
                    break;
            }
            exportReport(fromDate, toDate);
            loadReports();
            updateSummary();
        });
    }

    // Loads all report files from the financial_reports directory and populates the table
    private void loadReports() {
        tableModel.setRowCount(0);
        File reportsDir = new File("financial_reports");
        if (!reportsDir.exists() || !reportsDir.isDirectory()) return;
        File[] files = reportsDir.listFiles((_, name) -> name.endsWith(".txt"));
        if (files == null) return;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        for (File file : files) {
            String fileName = file.getName();
            String createdBy = "";
            String dateStr = "";
            String dateTimeDisplay = "";
            // Try to parse metadata from file
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Generated by:")) {
                        createdBy = line.replace("Generated by:", "").trim();
                    }
                    if (line.startsWith("Generated:")) {
                        dateStr = line.replace("Generated:", "").trim();
                    }
                    if (!createdBy.isEmpty() && !dateStr.isEmpty()) break;
                }
            } catch (Exception ignored) {}
            // Fallback: parse date from filename
            if (dateStr.isEmpty()) {
                try {
                    String[] parts = fileName.split("_");
                    if (parts.length >= 3) {
                        String datePart = parts[1] + "_" + parts[2].replace(".txt", "");
                        Date d = df.parse(datePart);
                        dateTimeDisplay = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
                    }
                } catch (ParseException ignored) {}
            } else {
                dateTimeDisplay = dateStr;
            }
            tableModel.addRow(new Object[]{fileName, createdBy, dateTimeDisplay});
        }
    }

    // Filters the table based on the search field input
    private void filterTable() {
        String text = searchField.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    // Shows the content of a selected report file in a dialog
    private void showReportContent(String fileName) {
        File file = new File("financial_reports", fileName);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File not found: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception ex) {
            content.append("Error reading file: ").append(ex.getMessage());
        }
        JTextArea textArea = new JTextArea(content.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        JOptionPane.showMessageDialog(this, scrollPane, fileName, JOptionPane.PLAIN_MESSAGE);
    }

    // Updates the summary label at the bottom with total payments and sales
    private void updateSummary() {
        java.util.List<String> payments = File_Utils.readLines("data/payments_data.txt");
        int totalPayments = 0;
        double totalPaid = 0.0;
        for (String line : payments) {
            String[] parts = line.split(",", -1);
            if (parts.length > 11) {
                totalPayments++;
                if (parts[11].equalsIgnoreCase("paid")) {
                    try { totalPaid += Double.parseDouble(parts[4]); } catch (Exception ignored) {}
                }
            }
        }
        java.util.List<String> sales = File_Utils.readLines("data/sales_data.txt");
        java.util.List<String> items = File_Utils.readLines("data/items_data.txt");
        int totalSales = 0;
        double totalSalesAmount = 0.0;
        for (String line : sales) {
            String[] s = line.split(",", -1);
            if (s.length >= 4) {
                totalSales++;
                // Find price from data/items_data.txt
                double price = 0.0;
                for (String itemLine : items) {
                    String[] item = itemLine.split(",", -1);
                    if (item.length > 4 && item[0].equals(s[1])) {
                        try { price = Double.parseDouble(item[4]); } catch (Exception ignored) {}
                        break;
                    }
                }
                try { totalSalesAmount += price * Integer.parseInt(s[3]); } catch (Exception ignored) {}
            }
        }
        String summary = "<html><div style='text-align:center;'>"
            + "<span style='font-size:18px;font-weight:bold;'>Total Payments: " + totalPayments + "</span>"
            + " &nbsp; &nbsp; "
            + "<span style='font-size:18px;font-weight:bold;'>Total Paid: RM " + String.format("%.2f", totalPaid) + "</span>"
            + "<br>"
            + "<span style='font-size:18px;font-weight:bold;'>Total Sales: " + totalSales + "</span>"
            + " &nbsp; &nbsp; "
            + "<span style='font-size:18px;font-weight:bold;'>Total Sales Amount: RM " + String.format("%.2f", totalSalesAmount) + "</span>"
            + "</div></html>";
        summaryLabel.setText(summary);
    }

    // --- Export Report Logic ---
    // Exports a financial report for the selected date range to a text file
    private void exportReport(Date fromDate, Date toDate) {
        try {
            String folderName = "financial_reports";
            File reportsDir = new File(folderName);
            if (!reportsDir.exists()) reportsDir.mkdir();
            String fileName = "FinancialReport_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
            File reportFile = new File(reportsDir, fileName);
            String userId = Session.getLoggedInUserId();
            String userName = "";
            try {
                java.util.List<String> userLines = Files.readAllLines(Path.of("data/users_data.txt"));
                for (String line : userLines) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 2 && parts[0].equals(userId)) {
                        userName = parts[1];
                        break;
                    }
                }
            } catch (Exception ex) { userName = ""; }
            // Prepare payment data
            java.util.List<String[]> paid = new java.util.ArrayList<>();
            java.util.List<String[]> pending = new java.util.ArrayList<>();
            int totalPayments = 0;
            double totalPaid = 0.0;
            java.util.List<String> payments = File_Utils.readLines("data/payments_data.txt");
            for (String line : payments) {
                String[] p = line.split(",", -1);
                if (p.length > 11) {
                    // p[5] is date (yyyy-MM-dd HH:mm:ss)
                    Date d = null;
                    try { d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(p[5]); } catch (Exception ignored) {}
                    if (d == null || d.before(fromDate) || d.after(toDate)) continue;
                    totalPayments++;
                    if (p[11].equalsIgnoreCase("paid")) {
                        paid.add(p);
                        try { totalPaid += Double.parseDouble(p[4]); } catch (Exception ignored) {}
                    } else {
                        pending.add(p);
                    }
                }
            }
            // Prepare sales data
            java.util.List<String> sales = File_Utils.readLines("data/sales_data.txt");
            java.util.List<String> items = File_Utils.readLines("data/items_data.txt");
            java.util.List<String[]> salesList = new java.util.ArrayList<>();
            double totalSalesAmount = 0.0;
            for (String line : sales) {
                String[] s = line.split(",", -1);
                if (s.length >= 5) {
                    // s[4] is date (yyyy-MM-dd)
                    Date d = null;
                    try { d = new SimpleDateFormat("yyyy-MM-dd").parse(s[4]); } catch (Exception ignored) {}
                    if (d == null || d.before(fromDate) || d.after(toDate)) continue;
                    double price = 0.0;
                    for (String itemLine : items) {
                        String[] item = itemLine.split(",", -1);
                        if (item.length > 4 && item[0].equals(s[1])) {
                            try { price = Double.parseDouble(item[4]); } catch (Exception ignored) {}
                            break;
                        }
                    }
                    double amount = 0.0;
                    try { amount = price * Integer.parseInt(s[3]); } catch (Exception ignored) {}
                    totalSalesAmount += amount;
                    salesList.add(new String[]{s[0], s[1], s[2], s[3], s[4], s[5], String.format("%.2f", amount)});
                }
            }
            // Write report
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
                writer.write("==============================\n");
                writer.write("   Omega Wholesale Sdn Bhd (OWSB)\n");
                writer.write("==============================\n");
                writer.write("      FINANCIAL REPORT\n");
                writer.write("==============================\n");
                writer.write("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                writer.write("Generated by: " + (userId != null ? userId : "") + (userName.isEmpty() ? "" : (" " + userName)) + "\n\n");
                writer.write(String.format("Total Payments: %d\nTotal Paid: RM %.2f\n", totalPayments, totalPaid));
                writer.write(String.format("Total Sales: %d\nTotal Sales Amount: RM %.2f\n", salesList.size(), totalSalesAmount));
                writer.write("\n");
                // Payments section
                java.util.function.BiConsumer<String, java.util.List<String[]>> section = (title, list) -> {
                    try {
                        writer.write("------------------------------\n");
                        writer.write(title + "\n");
                        writer.write("------------------------------\n");
                        writer.write(String.format("%-10s %-10s %-10s %-10s %-10s %-20s %-8s\n", "PayID", "POID", "ItemCode", "SuppID", "Amount", "Date", "Status"));
                        for (String[] p : list) {
                            writer.write(String.format("%-10s %-10s %-10s %-10s %-10s %-20s %-8s\n",
                                p[0], p[1], p[2], p[3], p[4], p[5], p[11]));
                        }
                        if (list.isEmpty()) writer.write("(None)\n");
                        writer.write("\n");
                    } catch (IOException _) { }
                };
                section.accept("Paid Payments", paid);
                section.accept("Pending Payments", pending);
                // Sales section
                writer.write("------------------------------\n");
                writer.write("Sales\n");
                writer.write("------------------------------\n");
                writer.write(String.format("%-10s %-10s %-18s %-8s %-12s %-10s %-10s\n", "SaleID", "ItemCode", "ItemName", "Qty", "Date", "ManagerID", "Amount"));
                for (String[] s : salesList) {
                    writer.write(String.format("%-10s %-10s %-18s %-8s %-12s %-10s %-10s\n",
                        s[0], s[1], s[2], s[3], s[4], s[5], s[6]));
                }
                if (salesList.isEmpty()) writer.write("(None)\n");
                writer.write("\n");
                writer.write("==============================\n");
                writer.write(String.format("Grand Total (Paid + Sales): RM %.2f\n", (totalPaid + totalSalesAmount)));
                writer.write("End of Report\n");
            }
            JOptionPane.showMessageDialog(this, "Report exported to: " + reportFile.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} 