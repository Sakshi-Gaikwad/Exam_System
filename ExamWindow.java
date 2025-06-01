import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.Timer;

public class ExamWindow extends JFrame {
    private int studentId;
    private int subjectId;
    private java.util.List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private ButtonGroup optionsGroup;
    private JRadioButton optionA, optionB, optionC, optionD;
    private JLabel questionLabel, timerLabel;

    // New labels for progress
    private JLabel totalQuestionsLabel, answeredLabel, remainingLabel;

    private Timer timer;
    private int timeRemaining;

    // Track which questions answered
    private boolean[] answeredQuestions;

    public ExamWindow(int studentId, String subjectName) {
        this.studentId = studentId;

        // Get subject ID from database
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM subjects WHERE name = ?");
            ps.setString(1, subjectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                subjectId = rs.getInt("id");
            } else {
                JOptionPane.showMessageDialog(this, "Subject not found.");
                dispose();
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // Fetch teacher-defined duration
        int durationMinutes = getTeacherSetDuration(subjectId);
        if (durationMinutes <= 0) {
            JOptionPane.showMessageDialog(this, "Exam duration not set by teacher for this subject.");
            dispose();
            return;
        }

        timeRemaining = durationMinutes * 60; // convert minutes to seconds

        setTitle("Exam Window - " + subjectName);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main panel with gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());

        // Timer label at top
        timerLabel = new JLabel("Time left: " + formatTime(timeRemaining), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setForeground(new Color(44, 62, 80));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));
        mainPanel.add(timerLabel, BorderLayout.NORTH);

        // Left panel for progress info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 10));
        leftPanel.setPreferredSize(new Dimension(220, 0));

        totalQuestionsLabel = new JLabel("Total Questions: 0");
        answeredLabel = new JLabel("Answered: 0");
        remainingLabel = new JLabel("Remaining: 0");

        Font progressFont = new Font("Segoe UI", Font.BOLD, 15);
        totalQuestionsLabel.setFont(progressFont);
        answeredLabel.setFont(progressFont);
        remainingLabel.setFont(progressFont);

        leftPanel.add(totalQuestionsLabel);
        leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(answeredLabel);
        leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(remainingLabel);

        // Right panel for question and options
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        questionLabel.setForeground(new Color(44, 62, 80));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPanel.add(questionLabel);
        rightPanel.add(Box.createVerticalStrut(25));

        optionA = new JRadioButton();
        optionB = new JRadioButton();
        optionC = new JRadioButton();
        optionD = new JRadioButton();

        Font optionFont = new Font("Segoe UI", Font.PLAIN, 15);
        optionA.setFont(optionFont);
        optionB.setFont(optionFont);
        optionC.setFont(optionFont);
        optionD.setFont(optionFont);

        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);
        optionsPanel.add(optionA);
        optionsPanel.add(Box.createVerticalStrut(10));
        optionsPanel.add(optionB);
        optionsPanel.add(Box.createVerticalStrut(10));
        optionsPanel.add(optionC);
        optionsPanel.add(Box.createVerticalStrut(10));
        optionsPanel.add(optionD);

        rightPanel.add(optionsPanel);
        rightPanel.add(Box.createVerticalStrut(30));

        JButton nextButton = new JButton("Next");
        nextButton.setBackground(new Color(52, 152, 219));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(e -> checkAndLoadNext());

        rightPanel.add(nextButton);

        // Split pane for left and right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(230);
        splitPane.setEnabled(false); // Prevent user from changing divider

        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);

        loadQuestions();
        if (questions.size() == 0) {
            JOptionPane.showMessageDialog(this, "No questions available for this subject.");
            dispose();
            return;
        }

        answeredQuestions = new boolean[questions.size()]; // initialize tracking

        updateProgressLabels();
        displayQuestion();

        timer = new Timer(1000, e -> updateTimer());
        timer.start();

        setVisible(true);
    }

    private void updateTimer() {
        timeRemaining--;
        timerLabel.setText("Time left: " + formatTime(timeRemaining));
        if (timeRemaining <= 0) {
            timer.stop();
            submitExam();
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void loadQuestions() {
        questions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM questions WHERE subject_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"),
                        rs.getString("question"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        rs.getString("correct_option").charAt(0)
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayQuestion() {
        Question q = questions.get(currentQuestionIndex);
        questionLabel.setText("<html>Q" + (currentQuestionIndex + 1) + ": " + q.text + "</html>");
        optionA.setText("A. " + q.optionA);
        optionB.setText("B. " + q.optionB);
        optionC.setText("C. " + q.optionC);
        optionD.setText("D. " + q.optionD);
        optionsGroup.clearSelection();
    }

    private void checkAndLoadNext() {
        Question q = questions.get(currentQuestionIndex);
        char selected = ' ';
        if (optionA.isSelected()) selected = 'A';
        else if (optionB.isSelected()) selected = 'B';
        else if (optionC.isSelected()) selected = 'C';
        else if (optionD.isSelected()) selected = 'D';

        // Only count question as answered if user selected an option
        if (selected != ' ') {
            if (!answeredQuestions[currentQuestionIndex]) {
                answeredQuestions[currentQuestionIndex] = true;
                if (selected == q.correctOption) {
                    score++;
                }
                updateProgressLabels();
            }
        }

        currentQuestionIndex++;
        if (currentQuestionIndex >= questions.size()) {
            timer.stop();
            submitExam();
        } else {
            displayQuestion();
        }
    }

    private void updateProgressLabels() {
        int answeredCount = 0;
        for (boolean answered : answeredQuestions) {
            if (answered) answeredCount++;
        }
        int remaining = questions.size() - answeredCount;

        totalQuestionsLabel.setText("Total Questions: " + questions.size());
        answeredLabel.setText("Answered: " + answeredCount);
        remainingLabel.setText("Remaining: " + remaining);
    }

    private void submitExam() {
        // Prevent duplicate result insert (should not happen, but extra safety)
        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM results WHERE student_id = ? AND subject_id = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, studentId);
                checkPs.setInt(2, subjectId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "You have already submitted this exam.");
                        dispose();
                        return;
                    }
                }
            }

            String query = "INSERT INTO results (student_id, subject_id, score) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, score);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, "Exam finished. Your score: " + score + "/" + questions.size());
        dispose();
    }

    private int getTeacherSetDuration(int subjectId) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT duration_minutes FROM exam_settings WHERE subject_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("duration_minutes");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
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

    class Question {
        int id;
        String text;
        String optionA, optionB, optionC, optionD;
        char correctOption;

        public Question(int id, String text, String a, String b, String c, String d, char correct) {
            this.id = id;
            this.text = text;
            this.optionA = a;
            this.optionB = b;
            this.optionC = c;
            this.optionD = d;
            this.correctOption = correct;
        }
    }
}