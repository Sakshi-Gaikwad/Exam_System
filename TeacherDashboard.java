import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TeacherDashboard extends JFrame {
    private JComboBox<String> subjectCombo;
    private JTextField newSubjectField;
    private JButton addSubjectBtn, addQuestionBtn, viewScoresBtn, backButton;
    private int teacherId;

    public TeacherDashboard(int teacherId) {
        this.teacherId = teacherId;

        setTitle("Teacher Dashboard");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        subjectCombo = new JComboBox<>();
        newSubjectField = new JTextField();
        addSubjectBtn = new JButton("Add Subject");
        addQuestionBtn = new JButton("Add Question");
        viewScoresBtn = new JButton("View Scores");
        backButton = new JButton("Back");

        loadSubjects();

        JPanel centerPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.add(new JLabel("Select Subject:"));
        centerPanel.add(subjectCombo);
        centerPanel.add(new JLabel("Or Add New Subject:"));
        centerPanel.add(newSubjectField);
        centerPanel.add(addSubjectBtn);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        bottomPanel.add(addQuestionBtn);
        bottomPanel.add(viewScoresBtn);
        bottomPanel.add(backButton);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addSubjectBtn.addActionListener(e -> addSubject());
        addQuestionBtn.addActionListener(e -> openAddQuestionWindow());
        viewScoresBtn.addActionListener(e -> viewStudentScores());
        backButton.addActionListener(e -> {
            dispose();
            new LoginRegisterUI();
        });

        setVisible(true);
    }

    private void loadSubjects() {
        subjectCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM subjects");
            while (rs.next()) {
                subjectCombo.addItem(rs.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addSubject() {
        String subject = newSubjectField.getText().trim();
        if (subject.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter subject name.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO subjects (name) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, subject);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Subject added.");
            newSubjectField.setText("");
            loadSubjects();
        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Subject already exists.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openAddQuestionWindow() {
        String subject = (String) subjectCombo.getSelectedItem();
        if (subject == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject.");
            return;
        }
        new AddQuestionWindow(subject, teacherId);
    }

    private void viewStudentScores() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT u.username, s.name, r.score FROM results r " +
                           "JOIN users u ON u.id = r.student_id " +
                           "JOIN subjects s ON s.id = r.subject_id";
            ResultSet rs = conn.createStatement().executeQuery(query);

            StringBuilder result = new StringBuilder("Student Scores:\n\n");
            while (rs.next()) {
                result.append("Student: ").append(rs.getString(1))
                      .append(" | Subject: ").append(rs.getString(2))
                      .append(" | Score: ").append(rs.getInt(3)).append("\n");
            }

            if (result.toString().equals("Student Scores:\n\n")) {
                JOptionPane.showMessageDialog(this, "No scores available.");
            } else {
                JOptionPane.showMessageDialog(this, result.toString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
