import javax.swing.*;
import java.awt.*;
import java.awt.LinearGradientPaint;

// Dashboard window for the Sales Manager, providing navigation and access to sales-related features
public class SalesManagerDashboard {

    // Sidebar panel with a vertical red gradient background for navigation
    static class GradientSidebar extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            float[] fractions = {0f, 0.5f, 1f};
            Color[] colors = {new Color(0x7a0c0c), new Color(0xff1a1a), new Color(0x7a0c0c)};
            LinearGradientPaint paint = new LinearGradientPaint(0, 0, 0, h, fractions, colors);
            g2d.setPaint(paint);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }

    // Creates and displays the Sales Manager dashboard UI
    public void createUI(String username) {
        JFrame frame = new JFrame("\uD83D\uDCBC OWSB - Sales Manager");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // --- SIDEBAR: Navigation panel on the left ---
        GradientSidebar sidebar = new GradientSidebar();
        sidebar.setOpaque(true);
        sidebar.setPreferredSize(new Dimension(250, 600));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Display the logged-in user's name at the top of the sidebar
        String displayName = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        JLabel profile = new JLabel("\uD83D\uDC64 " + displayName, SwingConstants.CENTER);
        profile.setFont(new Font("Times New Roman", Font.BOLD, 18));
        profile.setForeground(Color.WHITE);
        profile.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(profile);
        sidebar.add(Box.createVerticalStrut(20));

        // Navigation menu items for the sidebar
        String[] menuItems = {
                "Manage Items",
                "Manage Suppliers",
                "Record Sales",
                "Create Requisition",
                "View Requisitions",
                "View Purchase Orders",
                "Logout"
        };

        // --- CONTENT PANEL: Main area to display selected feature ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        JLabel contentLabel = new JLabel("Welcome back, " + displayName + "!", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        contentPanel.add(contentLabel, BorderLayout.CENTER);

        // Add navigation buttons to the sidebar and set up their event handlers
        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(180, 40));
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Times New Roman", Font.PLAIN, 14));

            // Button click event: switch content panel based on menu selection
            button.addActionListener(_ -> {
                switch (item) {
                    case "Logout" -> {
                        frame.dispose(); // close dashboard
                        Main.createLoginUI(); // return to login
                    }
                    case "Manage Items" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ItemEntryPanel_SM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Manage Suppliers" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new SupplierEntryPanel_SM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Record Sales" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new SalesEntryPanel_SM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Create Requisition" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new PurchaseRequisitionPanel_SM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View Requisitions" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewPurchaseRequisitionPanel_SM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View Purchase Orders" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewPurchaseOrderPanel_SM());
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

            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(button);
        }

        // Add sidebar and content panel to the main frame
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
