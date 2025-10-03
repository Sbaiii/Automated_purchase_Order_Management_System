import javax.swing.*;  // Imports Swing components for GUI elements like JFrame, JPanel, JButton
import java.awt.*;     // Imports AWT classes for graphics, fonts, layouts, and colors

/* 
 * AdministratorDashboard - Main class for the administrator interface
 * This class creates a modern, responsive dashboard with a sidebar navigation
 * system and multiple management sections for different aspects of the business.
 */
public class AdministratorDashboard {

    /* 
     * SidebarSection - Custom component for creating expandable/collapsible menu sections
     * Each section has a header button and a submenu panel containing action buttons.
     * Implements an accordion-style menu where only one section can be expanded at a time.
     */
    static class SidebarSection extends JPanel {
        private final JButton headerBtn;         // Main button that acts as the section header
        private final JPanel submenuPanel;       // Container for submenu buttons
        private boolean expanded = false;        // Tracks the expanded/collapsed state
        private final String chevronRight = "▶"; // Icon for collapsed state
        private final String originalTitle;      // Section title text

        /* 
         * Constructor for creating a new sidebar section with custom styling and behavior
         * Creates a header button with gradient and submenu buttons with actions
         */
        public SidebarSection(String title, Color headerColor, Color submenuColor, String[] subLabels, Runnable[] subActions) {
            this.originalTitle = title;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  // Vertical layout for header + submenu
            setOpaque(false);  // Makes background transparent so gradient paint can show through

            // Custom painting for header button with gradient and centered text
            headerBtn = new JButton(chevronRight + "  " + originalTitle) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    g2.setPaint(new GradientPaint(0, 0, headerColor.brighter(), 0, h, headerColor.darker()));
                    g2.fillRoundRect(0, 0, w, h-1, 18, 18);  // Rounded background
                    g2.setFont(new Font("Times New Roman", Font.BOLD, 15));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (w - fm.stringWidth(getText())) / 2;  // Centering text horizontally
                    int y = ((h - fm.getHeight()) / 2) + fm.getAscent(); // Vertically centering text
                    g2.setColor(Color.WHITE);
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            // Configure header button appearance and behavior
            headerBtn.setFocusPainted(false);
            headerBtn.setFont(new Font("Times New Roman", Font.BOLD, 15));
            headerBtn.setForeground(Color.WHITE);
            headerBtn.setMaximumSize(new Dimension(220, 40));
            headerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            headerBtn.addActionListener(_ -> toggle()); // Toggle submenu on click
            add(headerBtn);

            // Create and configure submenu panel that will contain action buttons
            submenuPanel = new JPanel();
            submenuPanel.setLayout(new BoxLayout(submenuPanel, BoxLayout.Y_AXIS));
            submenuPanel.setOpaque(false);
            submenuPanel.setVisible(false);
            submenuPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            submenuPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // Create submenu buttons with custom styling and bind their actions
            for (int i = 0; i < subLabels.length; i++) {
                final int idx = i;  // Needed for lambda scope
                JButton btn = new JButton(subLabels[i]) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int w = getWidth(), h = getHeight();
                        g2.setPaint(new GradientPaint(0, 0, submenuColor.brighter(), 0, h, submenuColor.darker()));
                        g2.fillRoundRect(0, 0, w, h-1, 14, 14);  // Rounded background
                        g2.setFont(new Font("Times New Roman", Font.BOLD, 12));
                        FontMetrics fm = g2.getFontMetrics();
                        int x = (w - fm.stringWidth(getText())) / 2;
                        int y = ((h - fm.getHeight()) / 2) + fm.getAscent();
                        g2.setColor(Color.WHITE);
                        g2.drawString(getText(), x, y);
                        g2.dispose();
                    }
                };
                // Submenu button appearance
                btn.setFocusPainted(false);
                btn.setFont(new Font("Times New Roman", Font.BOLD, 12));
                btn.setForeground(Color.WHITE);
                btn.setMaximumSize(new Dimension(180, 26));
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                if (subActions[idx] != null) {
                    btn.addActionListener(_ -> subActions[idx].run());  // Bind action
                }
                submenuPanel.add(Box.createVerticalStrut(6));  // Spacer
                submenuPanel.add(btn);
            }
            add(submenuPanel);  // Add submenu to the section
        }

        /* 
         * Toggles the section's expanded/collapsed state
         * Implements accordion behavior by collapsing other sections
         */
        public void toggle() {
            expanded = !expanded;
            submenuPanel.setVisible(expanded);
            String chevronDown = "▼";
            headerBtn.setText((expanded ? chevronDown : chevronRight) + "  " + originalTitle);
            // Accordion behavior: close other sections
            Container parent = getParent();
            if (parent != null) {
                for (Component c : parent.getComponents()) {
                    if (c instanceof SidebarSection && c != this) {
                        ((SidebarSection) c).collapse();
                    }
                }
            }
        }

        /* 
         * Forces the section to collapse
         * Used by other sections when implementing accordion behavior
         */
        public void collapse() {
            expanded = false;
            submenuPanel.setVisible(false);
            headerBtn.setText(chevronRight + "  " + originalTitle);
        }
    }

    /* 
     * Creates and displays the main administrator dashboard interface
     * Sets up the main window, sidebar, and content area with all management sections
     */
    public void createUI(String username) {
        JFrame frame = new JFrame("📦 OWSB - Administrator");
        frame.setSize(900, 600);  // Set window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);  // Center on screen
        frame.setLayout(new BorderLayout());

        /* 
         * SidebarPanel - Custom panel with gradient background
         * Creates a smooth purple gradient for the sidebar background
         */
        class SidebarPanel extends JPanel {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight();
                g2d.setPaint(new GradientPaint(0, 0, new Color(60, 30, 120), 0, h, new Color(180, 140, 255)));
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        }

        // Initialize and configure sidebar panel with gradient background
        SidebarPanel sidebar = new SidebarPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));
        sidebar.setPreferredSize(new Dimension(270, 700));

        // Create and style username display at the top of sidebar
        String displayName = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        JLabel userLabel = new JLabel("\uD83D\uDC64  " + displayName, SwingConstants.CENTER);
        userLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userLabel);
        sidebar.add(Box.createVerticalStrut(18));  // Spacer

        // Initialize main content panel that will display different management interfaces
        JPanel contentPanel = new JPanel(new BorderLayout());
        JLabel contentLabel = new JLabel("Welcome back, " + displayName + "!", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        contentPanel.add(contentLabel, BorderLayout.CENTER);

        // Create User Management button with custom gradient styling
        // This is a static button (not part of an expandable section)
        JButton userMgmtBtn = new JButton("User Management") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, new Color(120, 60, 220), 0, h, new Color(180, 140, 255)));
                g2.fillRoundRect(0, 0, w, h-1, 18, 18);
                g2.setFont(new Font("Times New Roman", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = ((h - fm.getHeight()) / 2) + fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        // Configure User Management button appearance and behavior
        userMgmtBtn.setFont(new Font("Times New Roman", Font.BOLD, 15));
        userMgmtBtn.setForeground(Color.WHITE);
        userMgmtBtn.setMaximumSize(new Dimension(220, 40));
        userMgmtBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        userMgmtBtn.setFocusPainted(false);
        userMgmtBtn.setBackground(new Color(120, 60, 220));
        userMgmtBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userMgmtBtn.addActionListener(_ -> {
            contentPanel.removeAll();
            contentPanel.add(new UserManagementPanel_A(), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });
        sidebar.add(userMgmtBtn);
        sidebar.add(Box.createVerticalStrut(18));

        // Sales Management Section
        // Define submenu items and their corresponding actions
        // Uses red color theme for visual distinction
        String[] smLabels = {
                "Item Entry",
                "Supplier Entry",
                "Sales Entry",
                "Purchase Requisition",
                "View Purchase Requisition",
                "View Purchase Order"
        };
        Runnable[] smActions = new Runnable[] {
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ItemEntryPanel_SM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new SupplierEntryPanel_SM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new SalesEntryPanel_SM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new PurchaseRequisitionPanel_SM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewPurchaseRequisitionPanel_SM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewPurchaseOrderPanel_SM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
        };
        // Create Sales Management section with red color theme
        SidebarSection salesSection = new SidebarSection(
                "Sales Management",
                new Color(0xffb3b3),    // Light red for header
                new Color(0xff5252),    // Darker red for submenu
                smLabels,
                smActions
        );
        sidebar.add(salesSection);
        sidebar.add(Box.createVerticalStrut(12)); // Spacer

        // Purchasing Management Section
        // Define submenu items and their corresponding actions
        // Uses blue color theme for visual distinction
        String[] pmLabels = {
                "View Items",
                "View Suppliers",
                "View Purchase Requisition",
                "Purchase Order",
                "View Purchase Order"
        };
        Runnable[] pmActions = new Runnable[] {
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewItemsPanel_PM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewSuppliersPanel_PM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewPurchaseRequisitionPanel_PM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new PurchaseOrderPanel_PM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewPurchaseOrderPanel_PM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
        };
        // Create Purchasing Management section with blue color theme
        SidebarSection purchasingSection = new SidebarSection(
                "Purchasing Management",
                new Color(0x8ec5fc),    // Light blue
                new Color(0x2193b0),    // Deep blue
                pmLabels,
                pmActions
        );
        sidebar.add(purchasingSection);
        sidebar.add(Box.createVerticalStrut(12)); // Spacer

        // Inventory Management Section
        // Define submenu items and their corresponding actions
        // Uses yellow/orange color theme for visual distinction
        String[] imLabels = {
                "View Items",
                "Update Stock",
                "Manage Stock",
                "Stock Reports",
                "View Purchase Order"
        };
        Runnable[] imActions = new Runnable[] {
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewItemsPanel_IM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new UpdateStock_IM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ManageStock_IM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new StockReports_IM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewPurchaseOrderPanel_IM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
        };
        // Create Inventory Management section with yellow/orange color theme
        SidebarSection inventorySection = new SidebarSection(
                "Inventory Management",
                new Color(0xffe259),  // Bright yellow-orange
                new Color(0xffa751),  // Orange
                imLabels,
                imActions
        );
        sidebar.add(inventorySection);
        sidebar.add(Box.createVerticalStrut(12)); // Spacer

        // Financial Management Section
        // Define submenu items and their corresponding actions
        // Uses green color theme for visual distinction
        String[] fmLabels = {
                "Purchase Orders",
                "Verify Inventory",
                "Process Payments",
                "Financial Reports",
                "View Purchase Order",
                "View Purchase Requisition"
        };
        Runnable[] fmActions = new Runnable[] {
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new PurchaseOrders_FM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new VerifyInventory_FM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ProccessPayments_FM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new FinancialReports_FM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewPurchaseOrderPanel_FM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                },
                () -> {
                    contentPanel.removeAll();
                    contentPanel.add(new ViewPurchaseRequisitionPanel_FM(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
        };
        // Create Financial Management section with green color theme
        SidebarSection financialSection = new SidebarSection(
                "Financial Management",
                new Color(0x43e97b),    // Light green
                new Color(0x14532d),    // Deep forest green
                fmLabels,
                fmActions
        );
        sidebar.add(financialSection);

        // Create Logout button with custom gradient styling
        // Positioned at the bottom of the sidebar
        JButton logoutBtn = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, new Color(140, 80, 240), 0, h, new Color(200, 170, 255)));
                g2.fillRoundRect(0, 0, w, h-1, 18, 18);
                g2.setFont(new Font("Times New Roman", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = ((h - fm.getHeight()) / 2) + fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        logoutBtn.setFont(new Font("Times New Roman", Font.BOLD, 15));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setMaximumSize(new Dimension(210, 34));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(new Color(140, 80, 240));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        logoutBtn.addActionListener(_ -> {
            SwingUtilities.getWindowAncestor(sidebar).dispose();  // Close current window
            Main.createLoginUI();  // Return to login screen
        });
        sidebar.add(Box.createVerticalStrut(10));  // Spacer
        sidebar.add(logoutBtn);

        // Create scrollable sidebar for smaller screens
        // Hides scrollbars when not needed and maintains transparent background
        JScrollPane sidebarScrollPane = new JScrollPane(
                sidebar,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        sidebarScrollPane.setBorder(null);  // No border
        sidebarScrollPane.setOpaque(false);  // Transparent background
        sidebarScrollPane.getViewport().setOpaque(false);

        // Add all components to the main window
        // Sidebar on the left, content area on the right
        frame.add(sidebarScrollPane, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);  // Show the frame
    }
}