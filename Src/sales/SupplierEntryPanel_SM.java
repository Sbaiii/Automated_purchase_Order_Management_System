import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Panel for Sales Manager to manage supplier records (add, edit, delete, search)
public class SupplierEntryPanel_SM extends JPanel {
    // Table model for displaying supplier data
    private final DefaultTableModel tableModel;
    // Formatter for date fields
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Constructor: sets up the UI and event handlers
    public SupplierEntryPanel_SM() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel title = new JLabel("Supplier Entry", SwingConstants.LEFT);
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
        JTextField searchField = new JTextField();
        searchField.setToolTipText("Search by any field...");
        searchField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows supplier records ---
        String[] columnNames = {
            "ID", "Name", "Phone", "Region", "Rating", "Specialty 1", "Specialty 2",
            "Email", "Bank Info", "Lead Time", "Last Supplied", "Active", "Max Capacity", "Notes"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable supplierTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 235, 235) : Color.WHITE); // light red
                } else {
                    c.setBackground(new Color(255, 200, 200)); // selected row light red
                }
                return c;
            }
        };
        supplierTable.setFont(new Font("Times New Roman", Font.PLAIN, 10));
        supplierTable.setRowHeight(18);
        supplierTable.setGridColor(new Color(255, 210, 210)); // light red grid
        supplierTable.setShowGrid(true);
        supplierTable.setSelectionBackground(new Color(255, 200, 200)); // match selected row (light red)
        supplierTable.setSelectionForeground(Color.BLACK);
        supplierTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        supplierTable.getTableHeader().setBackground(new Color(255, 210, 210)); // header light red
        supplierTable.getTableHeader().setForeground(new Color(120, 30, 30));
        supplierTable.setFillsViewportHeight(true);
        // Adjust column widths (even smaller)
        int[] colWidths = {50, 80, 70, 55, 50, 70, 70, 90, 90, 55, 70, 45, 60, 100};
        for (int i = 0; i < colWidths.length && i < supplierTable.getColumnCount(); i++) {
            supplierTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // --- BUTTON PANEL: Add, Edit, Delete ---
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial supplier records
        loadSuppliers();

        // --- SEARCH FILTER LISTENER ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(searchField.getText().trim().toLowerCase()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(searchField.getText().trim().toLowerCase()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // --- Add Supplier button handler ---
        addBtn.addActionListener(_ -> {
            JTextField nameField = new JTextField();
            JTextField contactField = new JTextField();
            JTextField regionField = new JTextField();
            JTextField ratingField = new JTextField();
            JTextField specialty1Field = new JTextField();
            JTextField specialty2Field = new JTextField();
            JTextField emailField = new JTextField();
            JTextField bankInfoField = new JTextField();
            JSpinner leadTimeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
            JSpinner lastSuppliedSpinner = new JSpinner(new SpinnerDateModel());
            lastSuppliedSpinner.setEditor(new JSpinner.DateEditor(lastSuppliedSpinner, "yyyy-MM-dd"));
            JCheckBox activeCheckBox = new JCheckBox("Active", true);
            JTextField maxCapacityField = new JTextField();
            JTextArea notesArea = new JTextArea(3, 20);

            JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            formPanel.add(new JLabel("Supplier Name:"));
            formPanel.add(nameField);
            formPanel.add(new JLabel("Phone Number:"));
            formPanel.add(contactField);
            formPanel.add(new JLabel("Region:"));
            formPanel.add(regionField);
            formPanel.add(new JLabel("Rating (★ to ★★★★★):"));
            formPanel.add(ratingField);
            formPanel.add(new JLabel("Primary Specialty:"));
            formPanel.add(specialty1Field);
            formPanel.add(new JLabel("Secondary Specialty:"));
            formPanel.add(specialty2Field);
            formPanel.add(new JLabel("Email:"));
            formPanel.add(emailField);
            formPanel.add(new JLabel("Bank Info:"));
            formPanel.add(bankInfoField);
            formPanel.add(new JLabel("Supply Lead Time (days):"));
            formPanel.add(leadTimeSpinner);
            formPanel.add(new JLabel("Last Supplied Date (YYYY-MM-DD):"));
            formPanel.add(lastSuppliedSpinner);
            formPanel.add(activeCheckBox);
            formPanel.add(new JLabel("Max Capacity:"));
            formPanel.add(maxCapacityField);
            formPanel.add(new JLabel("Notes:"));
            formPanel.add(new JScrollPane(notesArea));

            JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.add(formPanel, BorderLayout.CENTER);
            dialogPanel.setPreferredSize(new Dimension(500, 600));

            int result = JOptionPane.showConfirmDialog(null, dialogPanel, "Add New Supplier", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String newId = generateNextSupplierId();
                    String lastSupplied = new java.text.SimpleDateFormat("yyyy-MM-dd").format(lastSuppliedSpinner.getValue());
                    int leadTimeNum = (int) leadTimeSpinner.getValue();
                    String leadTime = leadTimeNum + "days";
                    String maxCapacity = validatePositiveInt(maxCapacityField.getText().trim(), "Max Capacity");

                    String newLine = String.join(",",
                        quoteIfNeeded(newId),
                        quoteIfNeeded(nameField.getText().trim()),
                        quoteIfNeeded(contactField.getText().trim()),
                        quoteIfNeeded(regionField.getText().trim()),
                        quoteIfNeeded(convertStars(ratingField.getText().trim())),
                        quoteIfNeeded(specialty1Field.getText().trim()),
                        quoteIfNeeded(specialty2Field.getText().trim()),
                        quoteIfNeeded(emailField.getText().trim()),
                        quoteIfNeeded(bankInfoField.getText().trim()),
                        quoteIfNeeded(leadTime),
                        quoteIfNeeded(lastSupplied),
                        quoteIfNeeded(String.valueOf(activeCheckBox.isSelected())),
                        quoteIfNeeded(maxCapacity),
                        quoteIfNeeded(notesArea.getText().trim())
                    );

                    File_Utils.appendLine("data/suppliers_data.txt", newLine);
                    tableModel.addRow(parseCSVLine(newLine));
                    JOptionPane.showMessageDialog(null, "✅ Supplier added successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Failed to add supplier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Edit Supplier button handler ---
        editBtn.addActionListener(_ -> {
            String keyword = JOptionPane.showInputDialog("Enter Supplier ID, Name or Region to Edit:");
            if (keyword == null || keyword.isBlank()) return;
            String searchKey = keyword.trim().toLowerCase();

            List<String> lines = File_Utils.readLines("data/suppliers_data.txt");
            DefaultTableModel searchModel = new DefaultTableModel(columnNames, 0);
            JTable resultTable = new JTable(searchModel);

            for (String line : lines) {
                String[] parts = parseCSVLine(line);
                if (parts.length >= 14 &&
                    (parts[0].toLowerCase().contains(searchKey) ||
                     parts[1].toLowerCase().contains(searchKey) ||
                     parts[3].toLowerCase().contains(searchKey))) {
                    searchModel.addRow(parts);
                }
            }

            if (searchModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No matching suppliers found.");
                return;
            }

            JScrollPane resultScroll = new JScrollPane(resultTable);
            resultScroll.setPreferredSize(new Dimension(1100, 150));
            int pick = JOptionPane.showConfirmDialog(null, resultScroll, "Select Supplier to Edit", JOptionPane.OK_CANCEL_OPTION);
            if (pick != JOptionPane.OK_OPTION || resultTable.getSelectedRow() == -1) return;

            int rowIndex = resultTable.getSelectedRow();
            String[] selected = new String[14];
            for (int i = 0; i < 14; i++) selected[i] = (String) searchModel.getValueAt(rowIndex, i);

            JTextField contactField = new JTextField(selected[2]);
            JTextField regionField = new JTextField(selected[3]);
            JTextField ratingField = new JTextField(selected[4]);
            JTextField specialty1Field = new JTextField(selected[5]);
            JTextField specialty2Field = new JTextField(selected[6]);
            JTextField emailField = new JTextField(selected[7]);
            JTextField bankInfoField = new JTextField(selected[8]);
            JTextField leadTimeField = new JTextField(selected[9]);
            JTextField lastSuppliedField = new JTextField(selected[10]);
            JCheckBox activeCheckBox = new JCheckBox("Active", Boolean.parseBoolean(selected[11]));
            JTextField maxCapacityField = new JTextField(selected[12]);
            JTextArea notesArea = new JTextArea(selected[13].replace(";", ","), 3, 20);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Phone Number:"));
            panel.add(contactField);
            panel.add(new JLabel("Region:"));
            panel.add(regionField);
            panel.add(new JLabel("Rating (★ to ★★★★★):"));
            panel.add(ratingField);
            panel.add(new JLabel("Primary Specialty:"));
            panel.add(specialty1Field);
            panel.add(new JLabel("Secondary Specialty:"));
            panel.add(specialty2Field);
            panel.add(new JLabel("Email:"));
            panel.add(emailField);
            panel.add(new JLabel("Bank Info:"));
            panel.add(bankInfoField);
            panel.add(new JLabel("Supply Lead Time (days):"));
            panel.add(leadTimeField);
            panel.add(new JLabel("Last Supplied Date (YYYY-MM-DD):"));
            panel.add(lastSuppliedField);
            panel.add(activeCheckBox);
            panel.add(new JLabel("Max Capacity:"));
            panel.add(maxCapacityField);
            panel.add(new JLabel("Notes:"));
            panel.add(new JScrollPane(notesArea));

            // Wrap the panel in a scroll pane for better UX
            JScrollPane editScrollPane = new JScrollPane(panel);
            editScrollPane.setPreferredSize(new Dimension(500, 500));

            int update = JOptionPane.showConfirmDialog(null, editScrollPane, "Edit Supplier", JOptionPane.OK_CANCEL_OPTION);
            if (update == JOptionPane.OK_OPTION) {
                try {
                    String lastSupplied = validateAndFormatDate(lastSuppliedField.getText().trim());
                    String leadTime = validatePositiveInt(leadTimeField.getText().trim(), "Lead Time");
                    String maxCapacity = validatePositiveInt(maxCapacityField.getText().trim(), "Max Capacity");

                    String newLine = String.join(",",
                        quoteIfNeeded(selected[0]),
                        quoteIfNeeded(selected[1]),
                        quoteIfNeeded(contactField.getText().trim()),
                        quoteIfNeeded(regionField.getText().trim()),
                        quoteIfNeeded(convertStars(ratingField.getText().trim())),
                        quoteIfNeeded(specialty1Field.getText().trim()),
                        quoteIfNeeded(specialty2Field.getText().trim()),
                        quoteIfNeeded(emailField.getText().trim()),
                        quoteIfNeeded(bankInfoField.getText().trim()),
                        quoteIfNeeded(leadTime),
                        quoteIfNeeded(lastSupplied),
                        quoteIfNeeded(String.valueOf(activeCheckBox.isSelected())),
                        quoteIfNeeded(maxCapacity),
                        quoteIfNeeded(notesArea.getText().trim())
                    );

                    List<String> updated = new ArrayList<>();
                    for (String line : lines) {
                        String[] parts = parseCSVLine(line);
                        if (parts.length > 0 && parts[0].equals(selected[0])) {
                            updated.add(newLine);
                        } else {
                            updated.add(line);
                        }
                    }
                    File_Utils.writeLines("data/suppliers_data.txt", new ArrayList<>(updated));
                    loadSuppliers();
                    JOptionPane.showMessageDialog(null, "✅ Supplier updated successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Update failed: " + ex.getMessage());
                }
            }
        });

        // --- Delete Supplier button handler ---
        deleteBtn.addActionListener(_ -> {
            String keyword = JOptionPane.showInputDialog("Enter Supplier ID, Name, or Region to Delete:");
            if (keyword == null || keyword.isBlank()) return;
            String searchKey = keyword.trim().toLowerCase();

            List<String> lines = File_Utils.readLines("data/suppliers_data.txt");
            DefaultTableModel searchModel = new DefaultTableModel(columnNames, 0);
            JTable resultTable = new JTable(searchModel);

            for (String line : lines) {
                String[] parts = parseCSVLine(line);
                if (parts.length >= 14 &&
                    (parts[0].toLowerCase().contains(searchKey) ||
                     parts[1].toLowerCase().contains(searchKey) ||
                     parts[3].toLowerCase().contains(searchKey))) {
                    searchModel.addRow(parts);
                }
            }

            if (searchModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No matching suppliers found.");
                return;
            }

            JScrollPane resultScroll = new JScrollPane(resultTable);
            resultScroll.setPreferredSize(new Dimension(1100, 150));
            int pick = JOptionPane.showConfirmDialog(null, resultScroll, "Select Supplier to Delete", JOptionPane.OK_CANCEL_OPTION);
            if (pick != JOptionPane.OK_OPTION || resultTable.getSelectedRow() == -1) return;

            String supplierIdToDelete = (String) searchModel.getValueAt(resultTable.getSelectedRow(), 0);
            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                String[] parts = parseCSVLine(line);
                if (parts.length > 0 && !parts[0].equals(supplierIdToDelete)) {
                    updatedLines.add(line);
                }
            }
            File_Utils.writeLines("data/suppliers_data.txt", new ArrayList<>(updatedLines));
            loadSuppliers();
            JOptionPane.showMessageDialog(null, "✅ Supplier deleted successfully.");
        });
    }

    // Loads and displays supplier records from data/suppliers_data.txt
    private void loadSuppliers() {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/suppliers_data.txt");
        for (String line : lines) {
            String[] parts = parseCSVLine(line);
            if (parts.length >= 14) {
                tableModel.addRow(parts);
            }
        }
    }

    // Generates the next Supplier ID in the format SUP###
    private String generateNextSupplierId() {
        List<String> lines = File_Utils.readLines("data/suppliers_data.txt");
        int max = 0;
        for (String line : lines) {
            if (line.startsWith("SUP")) {
                try {
                    int num = Integer.parseInt(line.substring(3, 6));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return String.format("SUP%03d", max + 1);
    }

    // Converts a numeric rating to star symbols (★)
    private String convertStars(String input) {
        try {
            int stars = Integer.parseInt(input);
            return "★".repeat(Math.max(1, Math.min(stars, 5)));
        } catch (Exception e) {
            return "★★★";
        }
    }

    // Validates and formats a date string (YYYY-MM-DD)
    private String validateAndFormatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD");
        }
    }

    // Validates that a string is a positive integer
    private String validatePositiveInt(String value, String fieldName) {
        try {
            int v = Integer.parseInt(value);
            if (v < 0) throw new IllegalArgumentException(fieldName + " cannot be negative");
            return String.valueOf(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ". Please enter a valid number");
        }
    }

    // Utility to parse a CSV line with quoted fields
    private static String[] parseCSVLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    // Utility to quote a field if it contains a comma or quote
    private static String quoteIfNeeded(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return '"' + field.replace("\"", "\"\"") + '"';
        }
        return field;
    }

    // Filters the table based on the search field input
    private void filterTable(String filter) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/suppliers_data.txt");
        for (String line : lines) {
            String[] parts = parseCSVLine(line);
            if (parts.length >= 14) {
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