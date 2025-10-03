import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Panel for Inventory Manager to browse, search, and export stock reports
public class StockReports_IM extends JPanel {
    // Table displaying the list of stock reports
    private final JTable reportTable;
    // Table model for managing report data
    private final DefaultTableModel tableModel;
    // Search field for filtering reports
    private final JTextField searchField;
    // Sorter for table rows (enables sorting and filtering)
    private final TableRowSorter<DefaultTableModel> sorter;
    // Label for displaying summary statistics
    private final JLabel summaryLabel;

    // Constructor: sets up the UI and event handlers
    public StockReports_IM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Export + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel titleExportPanel = new JPanel(new BorderLayout());
        titleExportPanel.setOpaque(false);
        JLabel title = new JLabel("Stock Reports Browser", SwingConstants.LEFT);
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        titleExportPanel.add(title, BorderLayout.WEST);
        JButton exportBtn = new JButton("\uD83D\uDCC4 Export Report");
        exportBtn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        exportBtn.setBackground(new Color(255, 245, 220));
        exportBtn.setFocusPainted(false);
        exportBtn.setPreferredSize(new Dimension(160, 35));
        titleExportPanel.add(exportBtn, BorderLayout.EAST);
        topPanel.add(titleExportPanel);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel(" Search: ");
        searchLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchField = new JTextField();
        searchField.setToolTipText("Search by file, user, or date...");
        searchField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows report files ---
        String[] columnNames = {"File Name", "Created By", "Date/Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        reportTable.setRowHeight(24);
        reportTable.setGridColor(new Color(255, 235, 180));
        reportTable.setShowGrid(true);
        reportTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 14));
        reportTable.getTableHeader().setBackground(new Color(255, 245, 200));
        reportTable.getTableHeader().setForeground(new Color(120, 100, 30));
        reportTable.setFillsViewportHeight(true);
        reportTable.setSelectionBackground(new Color(255, 235, 180));
        reportTable.setSelectionForeground(Color.BLACK);
        reportTable.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(tableModel);
        reportTable.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // --- Load Reports into the table ---
        loadReports();

        // --- Search Listener: filters table as user types ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // --- Double-click to view report content ---
        reportTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && reportTable.getSelectedRow() != -1) {
                    int row = reportTable.convertRowIndexToModel(reportTable.getSelectedRow());
                    String fileName = (String) tableModel.getValueAt(row, 0);
                    showReportContent(fileName);
                }
            }
        });

        // --- SUMMARY PANEL: Shows total items and stock ---
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBackground(new Color(255, 245, 180));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(12, 20, 12, 20),
            BorderFactory.createLineBorder(new Color(220, 180, 80), 2, true)
        ));
        summaryLabel = new JLabel();
        summaryLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        summaryLabel.setForeground(new Color(120, 100, 30));
        summaryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        summaryPanel.add(summaryLabel);
        add(summaryPanel, BorderLayout.SOUTH);
        updateSummary();

        // --- Export Button Action: triggers report export ---
        exportBtn.addActionListener(_ -> {
            exportReport();
            loadReports();
            updateSummary();
        });
    }

    // Loads report files from the stock_reports directory and populates the table
    private void loadReports() {
        tableModel.setRowCount(0);
        File reportsDir = new File("stock_reports");
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
        File file = new File("stock_reports", fileName);
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

    // Updates the summary label with total items and stock
    private void updateSummary() {
        java.util.List<String> lines = File_Utils.readLines("data/items_data.txt");
        int totalItems = 0;
        int totalStock = 0;
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 9) {
                totalItems++;
                try {
                    int stock = Integer.parseInt(parts[3]);
                    totalStock += stock;
                } catch (Exception ignored) {}
            }
        }
        String summary = "<html><div style='text-align:center;'>"
            + "<span style='font-size:18px;font-weight:bold;'>Total Items: " + totalItems + "</span>"
            + " &nbsp; &nbsp; "
            + "<span style='font-size:18px;font-weight:bold;'>Total Stock: " + totalStock + "</span>"
            + "</div></html>";
        summaryLabel.setText(summary);
    }

    // --- Export Report Logic ---
    private void exportReport() {
        try {
            // Create reports folder if it doesn't exist
            String folderName = "stock_reports";
            File reportsDir = new File(folderName);
            if (!reportsDir.exists()) reportsDir.mkdir();

            // File name
            String fileName = "StockReport_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
            File reportFile = new File(reportsDir, fileName);

            // Get user ID and name
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

            // Prepare data for ranges
            java.util.List<String[]> range0_9 = new java.util.ArrayList<>();
            java.util.List<String[]> range10_19 = new java.util.ArrayList<>();
            java.util.List<String[]> range20_49 = new java.util.ArrayList<>();
            java.util.List<String[]> range50_99 = new java.util.ArrayList<>();
            java.util.List<String[]> range100plus = new java.util.ArrayList<>();
            int totalItems = 0, totalStock = 0;
            java.util.List<String> lines = File_Utils.readLines("data/items_data.txt");
            for (String line : lines) {
                String[] item = line.split(",", -1);
                if (item.length >= 9) {
                    int stock = 0;
                    try { stock = Integer.parseInt(item[3]); } catch (Exception ignored) {}
                    totalItems++;
                    totalStock += stock;
                    if (stock < 10) range0_9.add(item);
                    else if (stock < 20) range10_19.add(item);
                    else if (stock < 50) range20_49.add(item);
                    else if (stock < 100) range50_99.add(item);
                    else range100plus.add(item);
                }
            }

            // Write report
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
                writer.write("==============================\n");
                writer.write("   Omega Wholesale Sdn Bhd (OWSB)\n");
                writer.write("==============================\n");
                writer.write("         STOCK REPORT\n");
                writer.write("==============================\n");
                writer.write("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                writer.write("Generated by: " + (userId != null ? userId : "") + (userName.isEmpty() ? "" : (" " + userName)) + "\n\n");
                writer.write(String.format("Total Items: %d\nTotal Stock: %d\n", totalItems, totalStock));
                writer.write("\n");
                // Helper for section
                java.util.function.BiConsumer<String, java.util.List<String[]>> section = (title, list) -> {
                    try {
                        writer.write("------------------------------\n");
                        writer.write(title + "\n");
                        writer.write("------------------------------\n");
                        writer.write(String.format("%-12s %-18s %-12s %-8s %-12s\n", "ItemCode", "ItemName", "SupplierID", "Stock", "ExpiryDate"));
                        for (String[] item : list) {
                            writer.write(String.format("%-12s %-18s %-12s %-8s %-12s\n",
                                item[0], item[1], item[2], item[3], item[7]));
                        }
                        if (list.isEmpty()) writer.write("(None)\n");
                        writer.write("\n");
                    } catch (IOException _) { }
                };
                section.accept("Low Stock (0-9)", range0_9);
                section.accept("Stock 10-19", range10_19);
                section.accept("Stock 20-49", range20_49);
                section.accept("Stock 50-99", range50_99);
                section.accept("Stock 100+", range100plus);
                writer.write("==============================\n");
                writer.write("End of Report\n");
            }
            JOptionPane.showMessageDialog(this, "Report exported to: " + reportFile.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
