import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddQuestionWindow extends JFrame {
    private JTextArea questionArea;
    private JTextField optAField, optBField, optCField, optDField;
    private JComboBox<String> correctOptionCombo;
    private JButton submitBtn, backButton;
    private String subject;
    private int teacherId;
    private int numQuestions;
    private int durationMinutes;
    private int questionsAdded = 0;
    private int subjectId;

    public AddQuestionWindow(String subject, int teacherId, int numQuestions, int durationMinutes) {
        this.subject = subject;
        this.teacherId = teacherId;
        this.numQuestions = numQuestions;
        this.durationMinutes = durationMinutes;

        setTitle("Add Question - " + subject + " (" + (questionsAdded + 1) + "/" + numQuestions + ")");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Fetch subjectId once here
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM subjects WHERE name = ?")) {
            ps.setString(1, subject);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    subjectId = rs.getInt("id");
                } else {
                    JOptionPane.showMessageDialog(this, "Subject not found in DB.");
                    dispose();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage());
            dispose();
            return;
        }

        questionArea = new JTextArea(4, 40);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);

        optAField = new JTextField();
        optBField = new JTextField();
        optCField = new JTextField();
        optDField = new JTextField();

        correctOptionCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        submitBtn = new JButton("Submit");
        backButton = new JButton("Back");

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Question:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JScrollPane scrollPane = new JScrollPane(questionArea);
        scrollPane.setPreferredSize(new Dimension(350, 80));
        panel.add(scrollPane, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Option A:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(optAField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Option B:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(optBField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Option C:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(optCField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Option D:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(optDField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Correct Option:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(correctOptionCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 6; panel.add(submitBtn, gbc);
        gbc.gridx = 1; gbc.gridy = 6; panel.add(backButton, gbc);

        add(panel);

        submitBtn.addActionListener(e -> submitQuestion());
        backButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to go back? Unsaved questions will be lost.",
                "Confirm Back", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new TeacherDashboard(teacherId);
            }
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
            String insertQuery = "INSERT INTO questions (subject_id, question, option_a, option_b, option_c, option_d, correct_option, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, subjectId);
                stmt.setString(2, question);
                stmt.setString(3, optA);
                stmt.setString(4, optB);
                stmt.setString(5, optC);
                stmt.setString(6, optD);
                stmt.setString(7, correct);
                stmt.setInt(8, teacherId);

                stmt.executeUpdate();
            }

            questionsAdded++;

            if (questionsAdded >= numQuestions) {
                JOptionPane.showMessageDialog(this, "All " + numQuestions + " questions added.");
                dispose();
                new TeacherDashboard(teacherId);
            } else {
                JOptionPane.showMessageDialog(this, "Question added (" + questionsAdded + "/" + numQuestions + ").");

                // Clear fields for next question
                questionArea.setText("");
                optAField.setText("");
                optBField.setText("");
                optCField.setText("");
                optDField.setText("");
                correctOptionCombo.setSelectedIndex(0);

                setTitle("Add Question - " + subject + " (" + (questionsAdded + 1) + "/" + numQuestions + ")");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding question: " + ex.getMessage());
        }
    }
}
