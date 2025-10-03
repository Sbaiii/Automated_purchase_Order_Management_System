import javax.swing.*;
import java.awt.*;
import java.awt.LinearGradientPaint;

public class InventoryManagerDashboard {

    static class GradientSidebar extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            float[] fractions = {0f, 0.5f, 1f};
            Color[] colors = {new Color(0xff7300), new Color(0xffa751), new Color(0xffe259)};
            LinearGradientPaint paint = new LinearGradientPaint(0, 0, 0, h, fractions, colors);
            g2d.setPaint(paint);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }

    public void createUI(String username) {
        JFrame frame = new JFrame("📦 OWSB - Inventory Manager");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Sidebar (left)
        GradientSidebar sidebar = new GradientSidebar();
        sidebar.setOpaque(true);
        sidebar.setPreferredSize(new Dimension(200, 600));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        String displayName = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        JLabel profile = new JLabel("\uD83D\uDC64 " + displayName, SwingConstants.CENTER);
        profile.setFont(new Font("Times New Roman", Font.BOLD, 18));
        profile.setForeground(Color.WHITE);
        profile.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(profile);
        sidebar.add(Box.createVerticalStrut(20));

        String[] menuItems = {
                "View Items",
                "Update Stock",
                "Manage Stock",
                "Generate stock Report",
                "View PO",
                "Logout"
        };

        JPanel contentPanel = new JPanel(new BorderLayout());
        JLabel contentLabel = new JLabel("Welcome back, " + displayName + "!", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        contentPanel.add(contentLabel, BorderLayout.CENTER);

        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(180, 40));
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Times New Roman", Font.PLAIN, 14));

            button.addActionListener(_ -> {
                switch (item) {
                    case "Logout" -> {
                        frame.dispose(); // close dashboard
                        Main.createLoginUI(); // return to login
                    }
                    case "View Items" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewItemsPanel_IM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Update Stock" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new UpdateStock_IM(), BorderLayout.CENTER);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Manage Stock" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ManageStock_IM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "Generate stock Report" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new StockReports_IM());
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    }
                    case "View PO" -> {
                        contentPanel.removeAll();
                        contentPanel.add(new ViewPurchaseOrderPanel_IM());
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

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
