import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

// Panel for Administrator to manage user accounts (register, edit, deactivate, search)
public class UserManagementPanel_A extends JPanel {
    // Table model for displaying user data
    private final DefaultTableModel tableModel;

    // Constructor: sets up the UI and event handlers
    public UserManagementPanel_A() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Title + Search + Role Filter ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel title = new JLabel("User Management", SwingConstants.LEFT);
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
        // Add role filter dropdown
        String[] roles = {"All", "Administrator", "Sales Manager", "Purchase Manager", "Inventory Manager", "Finance Manager"};
        JComboBox<String> roleFilter = new JComboBox<>(roles);
        roleFilter.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        searchPanel.add(roleFilter, BorderLayout.EAST);
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE: Shows user records ---
        String[] columnNames = {"User ID", "Username", "Password", "Role", "Status", "Registered Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable itemTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                // Row background
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(235, 230, 255) : Color.WHITE);
                } else {
                    c.setBackground(new Color(200, 180, 255));
                }
                // Role text color
                int roleCol = 3;
                if (column == roleCol) {
                    String role = getValueAt(row, roleCol).toString();
                    switch (role) {
                        case "Sales Manager":
                            c.setForeground(new Color(200, 0, 0)); // red
                            break;
                        case "Purchase Manager":
                            c.setForeground(new Color(0, 70, 200)); // blue
                            break;
                        case "Inventory Manager":
                            c.setForeground(new Color(200, 170, 0)); // yellow
                            break;
                        case "Finance Manager":
                            c.setForeground(new Color(0, 150, 60)); // green
                            break;
                        case "Administrator":
                            c.setForeground(new Color(120, 30, 180)); // purple
                            break;
                        default:
                            c.setForeground(Color.BLACK);
                    }
                } else if (column == 4) { // Status column
                    String status = getValueAt(row, 4).toString();
                    if (status.equalsIgnoreCase("Active")) {
                        c.setBackground(new Color(46, 204, 113)); // green
                        c.setForeground(Color.WHITE);
                    } else if (status.equalsIgnoreCase("Inactive")) {
                        c.setBackground(new Color(231, 76, 60)); // red
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        itemTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        itemTable.setRowHeight(28);
        itemTable.setGridColor(new Color(210, 200, 255));
        itemTable.setShowGrid(true);
        itemTable.setSelectionBackground(new Color(200, 180, 255));
        itemTable.setSelectionForeground(Color.BLACK);
        itemTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 15));
        itemTable.getTableHeader().setBackground(new Color(180, 140, 255));
        itemTable.getTableHeader().setForeground(new Color(60, 30, 120));
        itemTable.setFillsViewportHeight(true);
        // Adjust column widths
        int[] colWidths = {90, 120, 120, 140, 100, 120};
        for (int i = 0; i < colWidths.length && i < itemTable.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        JScrollPane tableScroll = new JScrollPane(itemTable);
        add(tableScroll, BorderLayout.CENTER);

        // --- BUTTON PANEL: Register, Edit, Deactivate ---
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Register");
        JButton editBtn = new JButton("Edit");
        JButton deactivateBtn = new JButton("Deactivate");
        JButton deleteBtn = new JButton("Delete");
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(deactivateBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial user records
        loadUsers();

        // --- SEARCH FILTER LISTENER ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(searchField.getText().trim().toLowerCase(), (String)roleFilter.getSelectedItem()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(searchField.getText().trim().toLowerCase(), (String)roleFilter.getSelectedItem()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        roleFilter.addActionListener(_ -> filterTable(searchField.getText().trim().toLowerCase(), (String)roleFilter.getSelectedItem()));

        // --- Register User button handler ---
        addBtn.addActionListener(_ -> {
            JTextField usernameField = new JTextField();
            JTextField passwordField = new JTextField();
            String[] registerRoles = {"Administrator", "Sales Manager", "Purchase Manager", "Inventory Manager", "Finance Manager"};
            JComboBox<String> roleCombo = new JComboBox<>(registerRoles);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(new JLabel("Role:"));
            panel.add(roleCombo);

            int result = JOptionPane.showConfirmDialog(null, panel, "Register New User", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    if (usernameExists(usernameField.getText().trim())) {
                        JOptionPane.showMessageDialog(null, "❌ Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String nextCode = generateNextUserCode();
                    String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    String newLine = String.join(",",
                            nextCode,
                            usernameField.getText().trim(),
                            passwordField.getText().trim(),
                            Objects.requireNonNull(roleCombo.getSelectedItem()).toString(),
                            "Active",
                            today
                    );

                    File_Utils.appendLine("data/data/users_data.txt", newLine);
                    tableModel.addRow(newLine.split(","));
                    JOptionPane.showMessageDialog(null, "✅ User registered successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Failed to register user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Edit User button handler ---
        editBtn.addActionListener(_ -> {
            int selectedRow = tableModel.getRowCount() > 0 ? ((JTable) ((JScrollPane) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER)).getViewport().getView()).getSelectedRow() : -1;
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a user to edit.");
                return;
            }
            String[] selected = new String[6];
            for (int i = 0; i < 6; i++) selected[i] = (String) tableModel.getValueAt(selectedRow, i);

            JTextField usernameField = new JTextField(selected[1]);
            JTextField passwordField = new JTextField(selected[2]);
            String[] editRoles = {"Administrator", "Sales Manager", "Purchase Manager", "Inventory Manager", "Finance Manager"};
            JComboBox<String> roleCombo = new JComboBox<>(editRoles);
            roleCombo.setSelectedItem(selected[3]);
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
            statusCombo.setSelectedItem(selected[4]);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(new JLabel("Role:"));
            panel.add(roleCombo);
            panel.add(new JLabel("Status:"));
            panel.add(statusCombo);

            int update = JOptionPane.showConfirmDialog(null, panel, "Edit User", JOptionPane.OK_CANCEL_OPTION);
            if (update == JOptionPane.OK_OPTION) {
                try {
                    String newLine = String.join(",",
                            selected[0], // User ID (unchanged)
                            usernameField.getText().trim(),
                            passwordField.getText().trim(),
                            Objects.requireNonNull(roleCombo.getSelectedItem()).toString(),
                            Objects.requireNonNull(statusCombo.getSelectedItem()).toString(),
                            selected[5] // Registered Date (unchanged)
                    );

                    List<String> lines = File_Utils.readLines("data/users_data.txt");
                    List<String> updated = new ArrayList<>();
                    for (String line : lines) {
                        if (line.startsWith(selected[0] + ",")) {
                            updated.add(newLine);
                        } else {
                            updated.add(line);
                        }
                    }
                    File_Utils.writeLines("data/users_data.txt", new ArrayList<>(updated));
                    loadUsers();
                    JOptionPane.showMessageDialog(null, "✅ User updated successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Update failed: " + ex.getMessage());
                }
            }
        });

        // --- Delete User button handler ---
        deleteBtn.addActionListener(_ -> {
            int selectedRow = tableModel.getRowCount() > 0 ? ((JTable) ((JScrollPane) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER)).getViewport().getView()).getSelectedRow() : -1;
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a user to delete.");
                return;
            }

            String userId = (String) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this user?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            List<String> lines = File_Utils.readLines("data/users_data.txt");
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[0].equals(userId)) {
                    found = true; // Skip this line to delete the user
                } else {
                    updated.add(line);
                }
            }

            if (found) {
                File_Utils.writeLines("data/users_data.txt", new ArrayList<>(updated));
                loadUsers();
                JOptionPane.showMessageDialog(null, "🗑️ User deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "User not found.");
            }
        });

        // --- Deactivate User button handler ---
        deactivateBtn.addActionListener(_ -> {
            int selectedRow = tableModel.getRowCount() > 0 ? ((JTable) ((JScrollPane) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER)).getViewport().getView()).getSelectedRow() : -1;
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a user to deactivate.");
                return;
            }
            String userId = (String) tableModel.getValueAt(selectedRow, 0);
            List<String> lines = File_Utils.readLines("data/users_data.txt");
            List<String> updated = new ArrayList<>();
            boolean found = false;
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[0].equals(userId)) {
                    parts[4] = "Inactive";
                    updated.add(String.join(",", parts));
                    found = true;
                } else {
                    updated.add(line);
                }
            }
            if (found) {
                File_Utils.writeLines("data/users_data.txt", new ArrayList<>(updated));
                loadUsers();
                JOptionPane.showMessageDialog(null, "✅ User deactivated successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "User not found.");
            }



        });
    }

    // Loads and displays user records from data/users_data.txt
    private void loadUsers() {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/users_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                tableModel.addRow(parts);
            }
        }
    }

    // Generates the next User ID in the format OW###
    private String generateNextUserCode() {
        List<String> lines = File_Utils.readLines("data/users_data.txt");
        int maxNum = 0;
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length > 0 && parts[0].startsWith("OW")) {
                try {
                    int num = Integer.parseInt(parts[0].substring(2));
                    maxNum = Math.max(maxNum, num);
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("OW%03d", maxNum + 1);
    }

    // Checks if a username already exists in data/users_data.txt
    private boolean usernameExists(String username) {
        List<String> lines = File_Utils.readLines("data/users_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 2 && parts[1].equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    // Filters the table based on the search field and role filter
    private void filterTable(String filter, String role) {
        tableModel.setRowCount(0);
        List<String> lines = File_Utils.readLines("data/users_data.txt");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                boolean match = filter.isEmpty();
                if (!match) {
                    for (String part : parts) {
                        if (part.toLowerCase().contains(filter)) {
                            match = true;
                            break;
                        }
                    }
                }
                boolean roleMatch = role.equals("All") || (parts[3].equalsIgnoreCase(role));
                if (match && roleMatch) tableModel.addRow(parts);
            }
        }
    }

}