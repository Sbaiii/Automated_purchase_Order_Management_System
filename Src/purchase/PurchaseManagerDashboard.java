import javax.swing.*;
import java.awt.*;
import java.awt.LinearGradientPaint;

// Dashboard class for Purchase Manager role
public class PurchaseManagerDashboard {

    // Sidebar panel with a vertical blue/green gradient background
    static class GradientSidebar extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            // Define gradient stops and colors (blue/green shades)
            float[] fractions = {0f, 0.5f, 1f};
            Color[] colors = {new Color(0x162447), new Color(0x1fa2ff), new Color(0xa6ffcb)};
            LinearGradientPaint paint = new LinearGradientPaint(0, 0, 0, h, fractions, colors);
            g2d.setPaint(paint);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }

    // Main method to construct and display the Purchase Manager dashboard UI
    public void createUI(String username) {
        // Create and configure the main dashboard window
        JFrame frame = new JFrame("📦 OWSB - Purchase Manager");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Sidebar (left) with gradient background
        GradientSidebar sidebar = new GradientSidebar();
        sidebar.setOpaque(true);
        sidebar.setPreferredSize(new Dimension(250, 600));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Format and display the username at the top of the sidebar
        String displayName = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        JLabel profile = new JLabel("\uD83D\uDC64 " + displayName, SwingConstants.CENTER);
        profile.setFont(new Font("Times New Roman", Font.BOLD, 18));
        profile.setForeground(Color.WHITE);
        profile.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(30)); // Top spacer
        sidebar.add(profile);                    // Add user profile label
        sidebar.add(Box.createVerticalStrut(20)); // Spacer below profile

        // Sidebar menu options for navigation
        String[] menuItems = {
                "View Items",
                "View Suppliers",
                "View Purchase Requisition",
                "Create Purchase Orders",
                "View Purchase Orders",
                "Logout"
        };

        // Main content panel that updates based on menu selection
        JPanel contentPanel = new JPanel(new BorderLayout());
        JLabel contentLabel = new JLabel("Welcome back, " + displayName + "!", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        contentPanel.add(contentLabel, BorderLayout.CENTER);

        // Create each menu button and assign actions
        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(240, 40));
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Times New Roman", Font.PLAIN, 14));

            // Define button behavior for each menu item
            button.addActionListener(_ -> {
                switch (item) {
                    case "Logout" -> {
                        frame.dispose(); // close dashboard
                        Main.createLoginUI(); // return to login
                    }
                    case "View Items" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewItemsPanel_PM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View Suppliers" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewSuppliersPanel_PM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View Purchase Requisition" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewPurchaseRequisitionPanel_PM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Create Purchase Orders" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new PurchaseOrderPanel_PM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View Purchase Orders" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewPurchaseOrderPanel_PM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    default -> {
                        contentLabel.setText(item + " selected. (Functionality to be implemented)");
                        contentPanel.removeAll();
                        contentPanel.add(contentLabel, BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                }
            });

            sidebar.add(Box.createVerticalStrut(10)); // Spacer between buttons
            sidebar.add(button);                      // Add button to sidebar
        }

        // Add sidebar and content panel to the frame
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);

        frame.setVisible(true); // Show the window
    }
}
