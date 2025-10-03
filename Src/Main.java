import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.swing.border.AbstractBorder;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


// Main entry point and UI logic for the OWSB system
public class Main {
    // Main method: launches the login UI on the Event Dispatch Thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createLoginUI);
    }

    // Custom border with smooth rounded corners for text fields
    static class SmoothRoundedBorder extends AbstractBorder {
        private final int radius;

        public SmoothRoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(0xCCCCCC));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 10, 6, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(6, 10, 6, 10);
            return insets;
        }
    }

    // Custom button with rounded corners and gradient/hover effects
    static class RoundedButton extends JButton {
        private static final int ARC_WIDTH = 15;
        private static final int ARC_HEIGHT = 15;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_WIDTH, ARC_HEIGHT);
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    // Custom dialog for styled pop-up messages (success, error, etc.)
    static class StyledDialog extends JDialog {
        private static final int ARC_WIDTH = 20;
        private static final int ARC_HEIGHT = 20;

        public StyledDialog(Frame parent, String title, String message, boolean isError) {
            super(parent, true);
            setUndecorated(true);
            setSize(350, 200);
            setLocationRelativeTo(parent);
            setBackground(new Color(0, 0, 0, 0));

            JPanel mainPanel = getJPanel();

            // Icon and title panel
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setOpaque(false);
            JLabel iconLabel = new JLabel(isError ? "❌" : "✅");
            iconLabel.setFont(new Font("Times New Roman", Font.PLAIN, 24));
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
            topPanel.add(iconLabel);
            topPanel.add(Box.createHorizontalStrut(10));
            topPanel.add(titleLabel);

            // Message panel
            JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            messagePanel.setOpaque(false);
            JLabel messageLabel = new JLabel("<html><body style='width: 250px'>" + message + "</body></html>");
            messageLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
            messagePanel.add(messageLabel);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);
            RoundedButton okButton = getRoundedButton();
            buttonPanel.add(okButton);

            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(messagePanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            setContentPane(mainPanel);
        }

        // Creates the custom OK button for the dialog
        private RoundedButton getRoundedButton() {
            RoundedButton okButton = new RoundedButton("OK") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    if (getModel().isPressed()) {
                        g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb).darker(), 0, h, new Color(0x2575fc).darker()));
                    } else if (getModel().isRollover()) {
                        g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb).brighter(), 0, h, new Color(0x2575fc).brighter()));
                    } else {
                        g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb), 0, h, new Color(0x2575fc)));
                    }
                    g2d.fillRoundRect(0, 0, w, h, 15, 15);
                    g2d.setColor(getForeground());
                    g2d.setFont(new Font("Times New Roman", Font.BOLD, 14));
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (w - fm.stringWidth(getText())) / 2;
                    int y = ((h - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(getText(), x, y);
                    g2d.dispose();
                }
            };
            okButton.setForeground(Color.WHITE);
            okButton.setPreferredSize(new Dimension(100, 35));
            okButton.addActionListener(_ -> dispose());
            return okButton;
        }

        // Creates the main panel for the dialog with rounded background
        private JPanel getJPanel() {
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(0xEEEEEE));
                    g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, ARC_WIDTH, ARC_HEIGHT);
                    g2d.setColor(Color.WHITE);
                    g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, ARC_WIDTH, ARC_HEIGHT);
                    g2d.dispose();
                }
            };
            mainPanel.setLayout(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            return mainPanel;
        }
    }

    // Creates and displays the login UI
    public static void createLoginUI() {
        JFrame frame = new JFrame("📦 OWSB Login");
        frame.setSize(650, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel container = new JPanel(new BorderLayout());
        // Left panel with animated background and logo
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                g2d.setPaint(new GradientPaint(0, 0, new Color(0x1a2a6c), 0, h, new Color(0xfdbb2d)));
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(200, 350));
        leftPanel.setLayout(new OverlayLayout(leftPanel));

        // Animated panel for the left panel only
        class AnimatedPanel extends JPanel {
            private final List<Point> positions = new ArrayList<>();
            private final List<Point> velocities = new ArrayList<>();
            private final int numCircles = 10;
            private final int radius = 18;
            private final Timer timer;

            public AnimatedPanel() {
                setOpaque(false);
                for (int i = 0; i < numCircles; i++) {
                    Random random = new Random();
                    positions.add(new Point(random.nextInt(200), random.nextInt(350)));
                    velocities.add(new Point(random.nextInt(3) + 1, random.nextInt(3) + 1));
                }
                timer = new Timer(30, _ -> {
                    for (int i = 0; i < numCircles; i++) {
                        Point pos = positions.get(i);
                        Point vel = velocities.get(i);
                        pos.x += vel.x;
                        pos.y += vel.y;
                        if (pos.x < 0 || pos.x > getWidth() - radius) vel.x = -vel.x;
                        if (pos.y < 0 || pos.y > getHeight() - radius) vel.y = -vel.y;
                    }
                    repaint();
                });
                timer.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < numCircles; i++) {
                    Point pos = positions.get(i);
                    g2d.setColor(new Color(255, 255, 255, 40 + (i * 15) % 100));
                    g2d.fillOval(pos.x, pos.y, radius, radius);
                }
                g2d.dispose();
            }
            @Override
            public void removeNotify() {
                super.removeNotify();
                timer.stop();
            }
        }

        AnimatedPanel animatedPanel = new AnimatedPanel();
        animatedPanel.setAlignmentX(0.5f);
        animatedPanel.setAlignmentY(0.5f);
        leftPanel.add(animatedPanel);

        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setAlignmentX(0.5f);
        logoPanel.setAlignmentY(0.5f);

        JLabel logo = new JLabel("📦", SwingConstants.CENTER);
        logo.setFont(new Font("Times New Roman", Font.PLAIN, 48));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("OWSB System", SwingConstants.CENTER);
        name.setFont(new Font("Times New Roman", Font.BOLD, 22));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(Box.createVerticalGlue());
        logoPanel.add(logo);
        logoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        logoPanel.add(name);
        logoPanel.add(Box.createVerticalGlue());

        leftPanel.add(logoPanel);

        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0xEEEEEE));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
                g2d.dispose();
            }
        };
        rightPanel.setOpaque(false);
        rightPanel.setLayout(null);

        JLabel chooseLabel = new JLabel("Choose Your Account Type:");
        chooseLabel.setBounds(40, 30, 300, 25);
        chooseLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));

        String[] roles = {"Sales Manager", "Purchase Manager", "Administrator", "Inventory Manager", "Finance Manager"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setBounds(40, 60, 240, 30);
        roleBox.setFont(new Font("Times New Roman", Font.BOLD, 14));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(40, 100, 100, 25);
        userLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        JTextField userField = new JTextField();
        userField.setBounds(40, 130, 240, 30);
        userField.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        userField.setBorder(new SmoothRoundedBorder(15));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(40, 170, 100, 25);
        passLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        JPasswordField passField = new JPasswordField();
        passField.setBounds(40, 200, 240, 30);
        passField.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        passField.setBorder(new SmoothRoundedBorder(15));

        JButton loginBtn = new RoundedButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                if (getModel().isPressed()) {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb).darker(), 0, h, new Color(0x2575fc).darker()));
                } else if (getModel().isRollover()) {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb).brighter(), 0, h, new Color(0x2575fc).brighter()));
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb), 0, h, new Color(0x2575fc)));
                }
                g2d.fillRoundRect(0, 0, w, h, 15, 15);
                g2d.setColor(getForeground());
                g2d.setFont(new Font("Times New Roman", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = ((h - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        loginBtn.setBounds(40, 250, 115, 35);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Times New Roman", Font.BOLD, 14));

        JButton signUpBtn = new RoundedButton("Sign Up") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                if (getModel().isPressed()) {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb).darker(), 0, h, new Color(0x2575fc).darker()));
                } else if (getModel().isRollover()) {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb).brighter(), 0, h, new Color(0x2575fc).brighter()));
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0x6a11cb), 0, h, new Color(0x2575fc)));
                }
                g2d.fillRoundRect(0, 0, w, h, 15, 15);
                g2d.setColor(getForeground());
                g2d.setFont(new Font("Times New Roman", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = ((h - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        signUpBtn.setBounds(165, 250, 115, 35);
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFont(new Font("Times New Roman", Font.BOLD, 14));

        userField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    passField.requestFocus();
                }
            }
        });

        passField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    loginBtn.doClick();
                }
            }
        });

        loginBtn.addActionListener(_ -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            String selectedRole = (String) roleBox.getSelectedItem();
            boolean userFound = false;

            try (BufferedReader reader = new BufferedReader(new FileReader("data/users_data.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 6 && parts[1].equals(username) && parts[2].equals(password)) {
                        userFound = true;
                        if (parts[3].equalsIgnoreCase(selectedRole)) {
                            Session.setLoggedInUserId(parts[0]);
                            frame.dispose();
                            switch (selectedRole.toLowerCase()) {
                                case "sales manager":
                                    new SalesManagerDashboard().createUI(username);
                                    break;
                                case "purchase manager":
                                    new PurchaseManagerDashboard().createUI(username);
                                    break;
                                case "administrator":
                                    new AdministratorDashboard().createUI(username);
                                    break;
                                case "inventory manager":
                                    new InventoryManagerDashboard().createUI(username);
                                    break;
                                case "finance manager":
                                    new FinanceManagerDashboard().createUI(username);
                                    break;
                                default:
                                    new StyledDialog(frame, "Success", "✅ Login successful, but no dashboard for role: " + selectedRole, false).setVisible(true);
                            }
                        } else {
                            new StyledDialog(frame, "Access Denied", "❌ You don't have permission to access this role.", true).setVisible(true);
                        }
                        return;
                    }
                }
            } catch (IOException ex) {
                new StyledDialog(frame, "File Error", "⚠️ Error reading data/data/users_data.txt", true).setVisible(true);
            }

            if (!userFound) {
                new StyledDialog(frame, "Login Failed", "❌ Incorrect username or password.", true).setVisible(true);
            }
        });

        signUpBtn.addActionListener(_ -> {
            JTextField adminUserField = new JTextField();
            JPasswordField adminPassField = new JPasswordField();
            JPanel adminLoginPanel = new JPanel(new GridLayout(0, 1));
            adminLoginPanel.add(new JLabel("Admin Username:"));
            adminLoginPanel.add(adminUserField);
            adminLoginPanel.add(new JLabel("Admin Password:"));
            adminLoginPanel.add(adminPassField);

            int adminCheck = JOptionPane.showConfirmDialog(frame, adminLoginPanel, "🔒 Admin Login Required", JOptionPane.OK_CANCEL_OPTION);
            if (adminCheck != JOptionPane.OK_OPTION) return;

            String adminUser = adminUserField.getText().trim();
            String adminPass = new String(adminPassField.getPassword()).trim();
            boolean isAdminValid = false;

            try (BufferedReader reader = new BufferedReader(new FileReader("data/users_data.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5 &&
                            parts[1].equals(adminUser) &&
                            parts[2].equals(adminPass) &&
                            parts[3].equalsIgnoreCase("Administrator") &&
                            parts[4].equalsIgnoreCase("Active")) {
                        isAdminValid = true;
                        break;
                    }
                }
            } catch (IOException ex) {
                new StyledDialog(frame, "Error", "⚠️ Could not read data/users_data.txt", true).setVisible(true);
                return;
            }

            if (!isAdminValid) {
                new StyledDialog(frame, "Access Denied", "❌ Invalid Admin credentials or account not active.", true).setVisible(true);
                return;
            }

            JTextField newUserField = new JTextField();
            JPasswordField newPassField = new JPasswordField();
            JComboBox<String> newRoleBox = new JComboBox<>(roles);

            JPanel signUpPanel = new JPanel(new GridLayout(0, 1));
            signUpPanel.add(new JLabel("New Username:"));
            signUpPanel.add(newUserField);
            signUpPanel.add(new JLabel("New Password:"));
            signUpPanel.add(newPassField);
            signUpPanel.add(new JLabel("Role:"));
            signUpPanel.add(newRoleBox);

            int result = JOptionPane.showConfirmDialog(frame, signUpPanel, "Sign Up", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newUser = newUserField.getText().trim();
                String newPass = new String(newPassField.getPassword()).trim();
                String newRole = (String) newRoleBox.getSelectedItem();
                String status = "Active";
                String registrationDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

                try {
                    List<String> lines = File_Utils.readLines("data/users_data.txt");
                    boolean exists = false;
                    int maxNum = 0;

                    for (String line : lines) {
                        String[] parts = line.split(",");
                        if (parts.length >= 2 && parts[1].equalsIgnoreCase(newUser)) {
                            exists = true;
                            break;
                        }
                        if (parts.length >= 1 && parts[0].startsWith("OW")) {
                            try {
                                int num = Integer.parseInt(parts[0].substring(2));
                                if (num > maxNum) maxNum = num;
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    if (exists) {
                        new StyledDialog(frame, "Error", "❌ Username already exists.", true).setVisible(true);
                        return;
                    }

                    String nextID = String.format("OW%03d", maxNum + 1);
                    String newLine = String.join(",", nextID, newUser, newPass, newRole, status, registrationDate);

                    File_Utils.appendLine("data/users_data.txt", newLine);
                    new StyledDialog(frame, "Success", "✅ User registered successfully!", false).setVisible(true);
                } catch (Exception ex) {
                    new StyledDialog(frame, "Error", "⚠️ Failed to save user: " + ex.getMessage(), true).setVisible(true);
                }
            }
        });

        rightPanel.add(chooseLabel);
        rightPanel.add(roleBox);
        rightPanel.add(userLabel);
        rightPanel.add(userField);
        rightPanel.add(passLabel);
        rightPanel.add(passField);
        rightPanel.add(loginBtn);
        rightPanel.add(signUpBtn);

        container.add(leftPanel, BorderLayout.WEST);
        container.add(rightPanel, BorderLayout.CENTER);
        frame.setContentPane(container);
        frame.setVisible(true);
    }
}