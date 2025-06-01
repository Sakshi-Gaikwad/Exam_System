import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentDashboard extends JFrame {
    private JComboBox<String> subjectCombo;
    private JButton startExamBtn, backButton;
    private int studentId;

    public StudentDashboard(int studentId) {
        this.studentId = studentId;

        setTitle("Student Dashboard");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("Nimbus Look & Feel not available.");
        }

        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Welcome Student", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(44, 62, 80));
        mainPanel.add(title, BorderLayout.NORTH);

        // Center Panel with BoxLayout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(400, 60));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subjectLabel = new JLabel("Select Subject:");
        subjectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        subjectCombo = new JComboBox<>();
        loadSubjects();

        formPanel.add(subjectLabel);
        formPanel.add(subjectCombo);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        startExamBtn = new JButton("Start Exam");
        startExamBtn.setBackground(new Color(52, 152, 219));
        startExamBtn.setForeground(Color.WHITE);
        startExamBtn.setFocusPainted(false);
        startExamBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        backButton = new JButton("Back");
        backButton.setBackground(new Color(231, 76, 60));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        buttonPanel.add(startExamBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(backButton);

        // Add form and buttons to center
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(formPanel);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(buttonPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Action Listeners
        startExamBtn.addActionListener(e -> startExam());
        backButton.addActionListener(e -> {
            dispose();
            new LoginRegisterUI();
        });

        setVisible(true);
    }

    private void loadSubjects() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM subjects");
            while (rs.next()) {
                subjectCombo.addItem(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startExam() {
        String subject = (String) subjectCombo.getSelectedItem();
        if (subject == null || subject.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a subject.");
            return;
        }

        // Check if student already attempted this subject
        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM results r JOIN subjects s ON r.subject_id = s.id WHERE r.student_id = ? AND s.name = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, studentId);
                ps.setString(2, subject);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "You have already attempted this subject. Only one attempt is allowed.");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking previous attempts.");
            return;
        }

        int duration = 10; // default fallback

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT exam_duration FROM subjects WHERE name = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, subject);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        duration = rs.getInt("exam_duration");  // get the duration set by teacher
                        if(duration <= 0) {
                            duration = 10; // fallback if value is invalid
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving exam duration, defaulting to 10 minutes.");
        }

        // Now start exam with the fetched duration
        new ExamWindow(studentId, subject);
        dispose();
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
}