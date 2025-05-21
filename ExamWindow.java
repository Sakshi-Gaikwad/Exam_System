import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ExamWindow extends JFrame {
    private JPanel questionPanel;
    private JButton submitBtn, backButton;
    private int studentId;
    private String subject;
    private ArrayList<Question> questions;
    private Map<Integer, String> answers = new HashMap<>();

    public ExamWindow(int studentId, String subject) {
        this.studentId = studentId;
        this.subject = subject;

        setTitle("Exam - " + subject);
        setSize(700, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header with title and back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Subject: " + subject, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        backButton = new JButton("< Back");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setBackground(new Color(231, 76, 60));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        headerPanel.add(backButton, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Question panel
        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(questionPanel);
        scrollPane.setPreferredSize(new Dimension(680, 600));
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Submit button
        submitBtn = new JButton("Submit Exam");
        submitBtn.setBackground(new Color(52, 152, 219));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitBtn.setFocusPainted(false);
        mainPanel.add(submitBtn, BorderLayout.SOUTH);

        add(mainPanel);

        loadQuestions();
        displayQuestions();

        submitBtn.addActionListener(e -> submitExam());
        backButton.addActionListener(e -> {
            dispose();
            new StudentDashboard(studentId);
        });

        setVisible(true);
    }

    private void loadQuestions() {
        questions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String getSubjectId = "SELECT id FROM subjects WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(getSubjectId);
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int subjectId = rs.getInt("id");

                String query = "SELECT * FROM questions WHERE subject_id = ? ORDER BY RAND() LIMIT 10";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, subjectId);
                ResultSet qrs = stmt.executeQuery();

                while (qrs.next()) {
                    questions.add(new Question(
                            qrs.getInt("id"),
                            qrs.getString("question"),
                            qrs.getString("option_a"),
                            qrs.getString("option_b"),
                            qrs.getString("option_c"),
                            qrs.getString("option_d"),
                            qrs.getString("correct_option")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayQuestions() {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);

            JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
            panel.setBorder(BorderFactory.createTitledBorder("Q" + (i + 1) + ": " + q.question));
            panel.setBackground(new Color(255, 255, 255, 180));
            panel.setOpaque(true);
            panel.setPreferredSize(new Dimension(650, 120));

            JRadioButton optA = new JRadioButton("A. " + q.optionA);
            JRadioButton optB = new JRadioButton("B. " + q.optionB);
            JRadioButton optC = new JRadioButton("C. " + q.optionC);
            JRadioButton optD = new JRadioButton("D. " + q.optionD);

            Font optionFont = new Font("Segoe UI", Font.PLAIN, 14);
            optA.setFont(optionFont);
            optB.setFont(optionFont);
            optC.setFont(optionFont);
            optD.setFont(optionFont);

            int questionId = q.id;

            optA.addActionListener(e -> answers.put(questionId, "A"));
            optB.addActionListener(e -> answers.put(questionId, "B"));
            optC.addActionListener(e -> answers.put(questionId, "C"));
            optD.addActionListener(e -> answers.put(questionId, "D"));

            ButtonGroup group = new ButtonGroup();
            group.add(optA);
            group.add(optB);
            group.add(optC);
            group.add(optD);

            panel.add(optA);
            panel.add(optB);
            panel.add(optC);
            panel.add(optD);

            questionPanel.add(panel);
            questionPanel.add(Box.createVerticalStrut(10));
        }
        revalidate();
        repaint();
    }

    private void submitExam() {
        int score = 0;
        for (Question q : questions) {
            String selected = answers.getOrDefault(q.id, "");
            if (selected.equalsIgnoreCase(q.correctOption)) {
                score++;
            }
        }

        // Save score to database
        try (Connection conn = DBConnection.getConnection()) {
            String getSubjectId = "SELECT id FROM subjects WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(getSubjectId);
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int subjectId = rs.getInt("id");

                String insertScore = "INSERT INTO results (student_id, subject_id, score) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insertScore);
                stmt.setInt(1, studentId);
                stmt.setInt(2, subjectId);
                stmt.setInt(3, score);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, "Exam submitted!\nScore: " + score + " out of " + questions.size());
        dispose();
    }

    class Question {
        int id;
        String question, optionA, optionB, optionC, optionD, correctOption;

        public Question(int id, String question, String a, String b, String c, String d, String correctOption) {
            this.id = id;
            this.question = question;
            this.optionA = a;
            this.optionB = b;
            this.optionC = c;
            this.optionD = d;
            this.correctOption = correctOption;
        }
    }

    // Gradient background panel
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color c1 = new Color(230, 240, 255);
            Color c2 = new Color(210, 225, 250);
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
