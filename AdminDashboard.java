import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton addTeacherBtn;

    public AdminDashboard(int adminId) {
        setTitle("Admin Dashboard - Add Teacher");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel userLabel = new JLabel("Teacher Username:");
        JLabel passLabel = new JLabel("Password:");

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        addTeacherBtn = new JButton("Add Teacher");

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(addTeacherBtn);

        add(panel);

        addTeacherBtn.addActionListener(e -> addTeacher());

        setVisible(true);

        JButton backButton = new JButton("Back");
panel.add(backButton);

backButton.addActionListener(e -> {
    dispose(); // close AdminDashboard
    new LoginRegisterUI(); // open Login screen
});
    }

    private void addTeacher() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'teacher')";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Teacher added successfully.");

        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Username already exists.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
