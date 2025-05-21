import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginRegisterUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JButton loginBtn, registerBtn;

    public LoginRegisterUI() {
        setTitle("Online Exam System");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("Nimbus L&F not available.");
        }

        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Welcome to Exam Portal", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(44, 62, 80));
        mainPanel.add(title, BorderLayout.NORTH);

        // Center Panel with BoxLayout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(400, 100));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        JLabel roleLabel = new JLabel("Role:");

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        userLabel.setFont(labelFont);
        passLabel.setFont(labelFont);
        roleLabel.setFont(labelFont);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        roleCombo = new JComboBox<>(new String[]{"admin", "teacher", "student"});

        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);
        formPanel.add(roleLabel);
        formPanel.add(roleCombo);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn = new JButton("Login");
        registerBtn = new JButton("Register as Student");

        loginBtn.setBackground(new Color(52, 152, 219));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        buttonPanel.add(loginBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(registerBtn);

        // Add form and buttons to center
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(formPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(buttonPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());

        add(mainPanel);
        setVisible(true);
    }

    // Gradient background panel
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color color1 = new Color(230, 240, 255);
            Color color2 = new Color(200, 220, 250);
            int width = getWidth();
            int height = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                JOptionPane.showMessageDialog(this, "Login successful as " + role);
                dispose();

                switch (role) {
                    case "admin":
                        new AdminDashboard(userId);
                        break;
                    case "teacher":
                        new TeacherDashboard(userId);
                        break;
                    case "student":
                        new StudentDashboard(userId);
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'student')";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student registered. You can now log in.");

        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginRegisterUI::new);
    }
}
