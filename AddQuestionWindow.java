import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddQuestionWindow extends JFrame {
    private JTextArea questionArea;
    private JTextField optAField, optBField, optCField, optDField;
    private JComboBox<String> correctOptionCombo;
    private JButton submitBtn, backButton;
    private String subject;
    private int teacherId;

    public AddQuestionWindow(String subject, int teacherId) {
        this.subject = subject;
        this.teacherId = teacherId;

        setTitle("Add Question - " + subject);
        setSize(500, 450);
        setLocationRelativeTo(null);

        questionArea = new JTextArea(3, 40);
        optAField = new JTextField();
        optBField = new JTextField();
        optCField = new JTextField();
        optDField = new JTextField();
        correctOptionCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        submitBtn = new JButton("Submit");
        backButton = new JButton("Back");

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Question:"));
        panel.add(new JScrollPane(questionArea));
        panel.add(new JLabel("Option A:"));
        panel.add(optAField);
        panel.add(new JLabel("Option B:"));
        panel.add(optBField);
        panel.add(new JLabel("Option C:"));
        panel.add(optCField);
        panel.add(new JLabel("Option D:"));
        panel.add(optDField);
        panel.add(new JLabel("Correct Option:"));
        panel.add(correctOptionCombo);
        panel.add(submitBtn);
        panel.add(backButton);

        add(panel);

        submitBtn.addActionListener(e -> submitQuestion());
        backButton.addActionListener(e -> {
            dispose();
            new TeacherDashboard(teacherId);
        });

        setVisible(true);
    }

    private void submitQuestion() {
        String question = questionArea.getText().trim();
        String optA = optAField.getText().trim();
        String optB = optBField.getText().trim();
        String optC = optCField.getText().trim();
        String optD = optDField.getText().trim();
        String correct = (String) correctOptionCombo.getSelectedItem();

        if (question.isEmpty() || optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String getSubjectIdQuery = "SELECT id FROM subjects WHERE name = ?";
            PreparedStatement subStmt = conn.prepareStatement(getSubjectIdQuery);
            subStmt.setString(1, subject);
            ResultSet rs = subStmt.executeQuery();

            if (rs.next()) {
                int subjectId = rs.getInt("id");

                String insertQuery = "INSERT INTO questions (subject_id, question, option_a, option_b, option_c, option_d, correct_option, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setInt(1, subjectId);
                stmt.setString(2, question);
                stmt.setString(3, optA);
                stmt.setString(4, optB);
                stmt.setString(5, optC);
                stmt.setString(6, optD);
                stmt.setString(7, correct);
                stmt.setInt(8, teacherId);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Question added successfully.");
                dispose();
                new TeacherDashboard(teacherId);
            } else {
                JOptionPane.showMessageDialog(this, "Subject not found.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
