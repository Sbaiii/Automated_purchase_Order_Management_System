import javax.swing.*;               // For creating GUI components like JFrame, JPanel, JButton
import java.awt.*;                 // For layouts, fonts, colors, gradients, etc.
import java.awt.LinearGradientPaint; // For advanced gradient painting

// Dashboard GUI class for Finance Manager
public class FinanceManagerDashboard {

    // Inner class to paint the sidebar with a smooth vertical gradient
    static class GradientSidebar extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Preserve default paint behavior
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight(); // Width and height of the panel

            // Define color stops for the vertical gradient
            float[] fractions = {0f, 0.5f, 1f}; // Gradient positions (top, middle, bottom)
            Color[] colors = {
                    new Color(0x14532d), // Dark green
                    new Color(0x43e97b), // Soft green
                    new Color(0x38f9d7)  // Aqua blue
            };

            // Create the vertical gradient paint
            LinearGradientPaint paint = new LinearGradientPaint(0, 0, 0, h, fractions, colors);
            g2d.setPaint(paint);
            g2d.fillRect(0, 0, w, h); // Fill the entire panel
            g2d.dispose();
        }
    }

    // Method to construct and display the Finance Manager dashboard UI
    public void createUI(String username) {
        // Create and configure the main dashboard window
        JFrame frame = new JFrame("📦 OWSB - Finance Manager"); // Window title
        frame.setSize(900, 600);                                // Set window dimensions
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // Close app on exit
        frame.setLocationRelativeTo(null);                      // Center the window
        frame.setLayout(new BorderLayout());                    // Use BorderLayout

        // Sidebar with custom gradient background
        GradientSidebar sidebar = new GradientSidebar();
        sidebar.setOpaque(true);                                // Required for custom painting
        sidebar.setPreferredSize(new Dimension(250, 600));      // Fixed width
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); // Vertical alignment

        // Format the username display for the sidebar profile
        String displayName = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        JLabel profile = new JLabel("\uD83D\uDC64 " + displayName, SwingConstants.CENTER); // User icon + name
        profile.setFont(new Font("Times New Roman", Font.BOLD, 18));
        profile.setForeground(Color.WHITE);
        profile.setAlignmentX(Component.CENTER_ALIGNMENT); // Centered horizontally

        // Add some spacing and then the user profile
        sidebar.add(Box.createVerticalStrut(30)); // Top spacer
        sidebar.add(profile);                     // Add user profile label
        sidebar.add(Box.createVerticalStrut(20)); // Spacer below profile

        // Sidebar menu options for navigation
        String[] menuItems = {
                "Approve POs",
                "Verify Inv Updates",
                "Process Payments",
                "Generate Reports",
                "View Purchase Requisitions",
                "View Purchase Orders",
                "Logout"
        };

        // Content panel that updates based on menu selection
        JPanel contentPanel = new JPanel(new BorderLayout());
        JLabel contentLabel = new JLabel("Welcome back, " + displayName + "!", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        contentPanel.add(contentLabel, BorderLayout.CENTER); // Default greeting

        // Create each menu button and assign actions
        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);      // Align center
            button.setMaximumSize(new Dimension(180, 40));         // Fixed size
            button.setBackground(Color.WHITE);                     // Flat style
            button.setFocusPainted(false);
            button.setFont(new Font("Times New Roman", Font.PLAIN, 14));

            // Define button behavior for each menu item
            button.addActionListener(_ -> {
                switch (item) {
                    case "Logout" -> {
                        frame.dispose();               // Close the dashboard window
                        Main.createLoginUI();          // Open login screen
                    }
                    case "Approve POs" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new PurchaseOrders_FM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Verify Inv Updates" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new VerifyInventory_FM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Process Payments" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ProccessPayments_FM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Generate Reports" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new FinancialReports_FM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View Purchase Requisitions" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewPurchaseRequisitionPanel_FM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View Purchase Orders" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewPurchaseOrderPanel_FM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    default -> {
                        // Fallback for unimplemented actions
                        contentLabel.setText(item + " selected. (Functionality to be implemented)");
                        contentPanel.removeAll();
                        contentPanel.add(contentLabel, BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                }
            });

            sidebar.add(Box.createVerticalStrut(10));  // Spacer between buttons
            sidebar.add(button);                       // Add button to sidebar
        }

        // Add sidebar and content panel to the frame
        frame.add(sidebar, BorderLayout.WEST);         // Sidebar on the left
        frame.add(contentPanel, BorderLayout.CENTER);  // Dynamic content on the right

        frame.setVisible(true);                        // Show the window
    }
}