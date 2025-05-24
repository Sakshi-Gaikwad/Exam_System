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
        setSize(800, 450);  // Increased width to accommodate left panel
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        questionLabel = new JLabel();
        timerLabel = new JLabel("Time left: " + formatTime(timeRemaining));
        optionA = new JRadioButton();
        optionB = new JRadioButton();
        optionC = new JRadioButton();
        optionD = new JRadioButton();

        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> checkAndLoadNext());

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
        optionsPanel.add(optionA);
        optionsPanel.add(optionB);
        optionsPanel.add(optionC);
        optionsPanel.add(optionD);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rightPanel.add(questionLabel, BorderLayout.NORTH);
        rightPanel.add(optionsPanel, BorderLayout.CENTER);
        rightPanel.add(nextButton, BorderLayout.SOUTH);
        rightPanel.add(timerLabel, BorderLayout.PAGE_START);

        // Left panel for progress info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        leftPanel.setPreferredSize(new Dimension(220, 0));  // fix width

        totalQuestionsLabel = new JLabel("Total Questions: 0");
        answeredLabel = new JLabel("Answered: 0");
        remainingLabel = new JLabel("Remaining: 0");

        leftPanel.add(totalQuestionsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(answeredLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(remainingLabel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(230);
        splitPane.setEnabled(false); // Prevent user from changing divider

        add(splitPane);

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
        questionLabel.setText("Q" + (currentQuestionIndex + 1) + ": " + q.text);
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
        } else {
            // User didn't select any option, treat as unanswered - no score change
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
        try (Connection conn = DBConnection.getConnection()) {
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
