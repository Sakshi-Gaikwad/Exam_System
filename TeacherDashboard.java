import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.Vector;

public class TeacherDashboard extends JFrame {
    private int teacherId;

    // Manage Questions
    private JComboBox<String> subjectComboManage;
    private JButton addQuestionBtn, editQuestionBtn, deleteQuestionBtn, bulkUploadBtn;
    private JTable questionsTable;
    private DefaultTableModel questionsTableModel;
    private JTextField searchQuestionField;
    private JButton searchQuestionBtn;

    // View Scores
    private JTable scoresTable;
    private DefaultTableModel scoresTableModel;
    private JComboBox<String> subjectFilterCombo;
    private JTextField studentFilterField;
    private JButton filterScoresBtn, exportScoresBtn;

    // Exam Settings
    private JComboBox<String> subjectComboSettings;
    private JTextField numQuestionsField, durationField;
    private JButton saveSettingsBtn;

    // Reports
    private JTextArea reportsArea;
    private JButton refreshReportsBtn;

    public TeacherDashboard(int teacherId) {
        this.teacherId = teacherId;

        setTitle("Teacher Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Manage Questions Tab
        JPanel manageQuestionsPanel = new JPanel(new BorderLayout(10, 10));
        manageQuestionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topManagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subjectComboManage = new JComboBox<>();
        loadSubjects(subjectComboManage);
        topManagePanel.add(new JLabel("Select Subject:"));
        topManagePanel.add(subjectComboManage);

        searchQuestionField = new JTextField(20);
        searchQuestionBtn = new JButton("Search");
        topManagePanel.add(new JLabel("Search:"));
        topManagePanel.add(searchQuestionField);
        topManagePanel.add(searchQuestionBtn);

        manageQuestionsPanel.add(topManagePanel, BorderLayout.NORTH);

        questionsTableModel = new DefaultTableModel(new String[]{"ID", "Question", "Option A", "Option B", "Option C", "Option D", "Correct"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionsTable = new JTable(questionsTableModel);
        JScrollPane scrollQuestions = new JScrollPane(questionsTable);
        manageQuestionsPanel.add(scrollQuestions, BorderLayout.CENTER);

        JPanel bottomManagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addQuestionBtn = new JButton("Add Question");
        editQuestionBtn = new JButton("Edit Question");
        deleteQuestionBtn = new JButton("Delete Question");
        bulkUploadBtn = new JButton("Bulk Upload CSV");
        bottomManagePanel.add(bulkUploadBtn);
        bottomManagePanel.add(addQuestionBtn);
        bottomManagePanel.add(editQuestionBtn);
        bottomManagePanel.add(deleteQuestionBtn);

        manageQuestionsPanel.add(bottomManagePanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Manage Questions", manageQuestionsPanel);

        // 2. View Scores Tab
        JPanel viewScoresPanel = new JPanel(new BorderLayout(10, 10));
        viewScoresPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topScoresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subjectFilterCombo = new JComboBox<>();
        loadSubjects(subjectFilterCombo);
        subjectFilterCombo.insertItemAt("All Subjects", 0);
        subjectFilterCombo.setSelectedIndex(0);

        studentFilterField = new JTextField(15);
        filterScoresBtn = new JButton("Filter");
        exportScoresBtn = new JButton("Export CSV");

        topScoresPanel.add(new JLabel("Subject:"));
        topScoresPanel.add(subjectFilterCombo);
        topScoresPanel.add(new JLabel("Student:"));
        topScoresPanel.add(studentFilterField);
        topScoresPanel.add(filterScoresBtn);
        topScoresPanel.add(exportScoresBtn);

        viewScoresPanel.add(topScoresPanel, BorderLayout.NORTH);

        scoresTableModel = new DefaultTableModel(new String[]{"Student Name", "Subject", "Score", "Date"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scoresTable = new JTable(scoresTableModel);
        JScrollPane scrollScores = new JScrollPane(scoresTable);
        viewScoresPanel.add(scrollScores, BorderLayout.CENTER);

        tabbedPane.addTab("View Scores", viewScoresPanel);

        // 3. Exam Settings Tab
        JPanel examSettingsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        examSettingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        subjectComboSettings = new JComboBox<>();
        loadSubjects(subjectComboSettings);

        numQuestionsField = new JTextField();
        durationField = new JTextField();
        saveSettingsBtn = new JButton("Save Settings");

        examSettingsPanel.add(new JLabel("Select Subject:"));
        examSettingsPanel.add(subjectComboSettings);
        examSettingsPanel.add(new JLabel("Number of Questions:"));
        examSettingsPanel.add(numQuestionsField);
        examSettingsPanel.add(new JLabel("Exam Duration (minutes):"));
        examSettingsPanel.add(durationField);
        examSettingsPanel.add(new JLabel(""));
        examSettingsPanel.add(saveSettingsBtn);

        tabbedPane.addTab("Exam Settings", examSettingsPanel);

        // 4. Reports & Analytics Tab
        JPanel reportsPanel = new JPanel(new BorderLayout(10, 10));
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        reportsArea = new JTextArea();
        reportsArea.setEditable(false);
        JScrollPane reportsScroll = new JScrollPane(reportsArea);
        refreshReportsBtn = new JButton("Refresh Reports");

        reportsPanel.add(reportsScroll, BorderLayout.CENTER);
        reportsPanel.add(refreshReportsBtn, BorderLayout.SOUTH);

        tabbedPane.addTab("Reports & Analytics", reportsPanel);

        add(tabbedPane);

        // Listeners and initial data loading
        subjectComboManage.addActionListener(e -> loadQuestionsForSelectedSubject());
        searchQuestionBtn.addActionListener(e -> searchQuestions());
        addQuestionBtn.addActionListener(e -> openAddQuestionWindow());
        editQuestionBtn.addActionListener(e -> editSelectedQuestion());
        deleteQuestionBtn.addActionListener(e -> deleteSelectedQuestion());
        bulkUploadBtn.addActionListener(e -> bulkUploadQuestions());
        filterScoresBtn.addActionListener(e -> loadScoresWithFilters());
        exportScoresBtn.addActionListener(e -> exportScoresToCSV());
        saveSettingsBtn.addActionListener(e -> saveExamSettings());
        refreshReportsBtn.addActionListener(e -> refreshReports());

        loadQuestionsForSelectedSubject();
        loadScoresWithFilters();
        refreshReports();

        setVisible(true);
    }

    private void loadSubjects(JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM subjects ORDER BY name");
            while (rs.next()) {
                combo.addItem(rs.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Manage Questions functions

    private void loadQuestionsForSelectedSubject() {
        String subject = (String) subjectComboManage.getSelectedItem();
        if (subject == null) return;

        questionsTableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT q.id, q.question, q.option_a, q.option_b, q.option_c, q.option_d, q.correct_option " +
                    "FROM questions q JOIN subjects s ON q.subject_id = s.id WHERE s.name = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("question"));
                row.add(rs.getString("option_a"));
                row.add(rs.getString("option_b"));
                row.add(rs.getString("option_c"));
                row.add(rs.getString("option_d"));
                row.add(rs.getString("correct_option"));
                questionsTableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void searchQuestions() {
        String subject = (String) subjectComboManage.getSelectedItem();
        if (subject == null) return;

        String keyword = searchQuestionField.getText().trim();
        questionsTableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT q.id, q.question, q.option_a, q.option_b, q.option_c, q.option_d, q.correct_option " +
                    "FROM questions q JOIN subjects s ON q.subject_id = s.id WHERE s.name = ? AND q.question LIKE ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, subject);
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("question"));
                row.add(rs.getString("option_a"));
                row.add(rs.getString("option_b"));
                row.add(rs.getString("option_c"));
                row.add(rs.getString("option_d"));
                row.add(rs.getString("correct_option"));
                questionsTableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openAddQuestionWindow() {
        String subject = (String) subjectComboManage.getSelectedItem();
        if (subject == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject.");
            return;
        }
        AddQuestionWindow addWindow = new AddQuestionWindow(subject, teacherId);
        addWindow.setVisible(true);
        addWindow.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                loadQuestionsForSelectedSubject();
            }
        });
    }

    private void editSelectedQuestion() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a question to edit.");
            return;
        }
        int questionId = (int) questionsTableModel.getValueAt(selectedRow, 0);
        EditQuestionWindow editWindow = new EditQuestionWindow(questionId);
        editWindow.setVisible(true);
        editWindow.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                loadQuestionsForSelectedSubject();
            }
        });
    }

    private void deleteSelectedQuestion() {
        int selectedRow = questionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a question to delete.");
            return;
        }
        int questionId = (int) questionsTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this question?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String deleteQuery = "DELETE FROM questions WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(deleteQuery);
                ps.setInt(1, questionId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Question deleted.");
                    loadQuestionsForSelectedSubject();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete question.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void bulkUploadQuestions() {
        String subject = (String) subjectComboManage.getSelectedItem();
        if (subject == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject before bulk uploading.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File csvFile = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line;
                int count = 0;
                Connection conn = DBConnection.getConnection();
                conn.setAutoCommit(false);

                // Get subject id
                PreparedStatement psSubj = conn.prepareStatement("SELECT id FROM subjects WHERE name = ?");
                psSubj.setString(1, subject);
                ResultSet rsSubj = psSubj.executeQuery();
                if (!rsSubj.next()) {
                    JOptionPane.showMessageDialog(this, "Selected subject not found.");
                    conn.close();
                    return;
                }
                int subjectId = rsSubj.getInt("id");

                String insertQuery = "INSERT INTO questions (subject_id, question, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement psInsert = conn.prepareStatement(insertQuery);

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    // Expected CSV columns: question, option_a, option_b, option_c, option_d, correct_option
                    String[] parts = line.split(",");
                    if (parts.length != 6) {
                        JOptionPane.showMessageDialog(this, "Invalid CSV format. Each line must have 6 columns.");
                        conn.rollback();
                        conn.close();
                        return;
                    }
                    psInsert.setInt(1, subjectId);
                    psInsert.setString(2, parts[0].trim());
                    psInsert.setString(3, parts[1].trim());
                    psInsert.setString(4, parts[2].trim());
                    psInsert.setString(5, parts[3].trim());
                    psInsert.setString(6, parts[4].trim());
                    psInsert.setString(7, parts[5].trim());
                    psInsert.addBatch();
                    count++;
                }
                psInsert.executeBatch();
                conn.commit();
                conn.close();
                JOptionPane.showMessageDialog(this, count + " questions uploaded successfully.");
                loadQuestionsForSelectedSubject();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error uploading questions: " + ex.getMessage());
            }
        }
    }

    // View Scores functions

    private void loadScoresWithFilters() {
        String subject = (String) subjectFilterCombo.getSelectedItem();
        String studentName = studentFilterField.getText().trim();

        scoresTableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder query = new StringBuilder("SELECT st.name AS student_name, su.name AS subject_name, sc.score, sc.date FROM scores sc JOIN students st ON sc.student_id = st.id JOIN subjects su ON sc.subject_id = su.id WHERE 1=1");
            if (subject != null && !subject.equals("All Subjects")) {
                query.append(" AND su.name = ?");
            }
            if (!studentName.isEmpty()) {
                query.append(" AND st.name LIKE ?");
            }
            query.append(" ORDER BY sc.date DESC");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            int paramIndex = 1;
            if (subject != null && !subject.equals("All Subjects")) {
                ps.setString(paramIndex++, subject);
            }
            if (!studentName.isEmpty()) {
                ps.setString(paramIndex, "%" + studentName + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("student_name"));
                row.add(rs.getString("subject_name"));
                row.add(rs.getInt("score"));
                row.add(rs.getDate("date"));
                scoresTableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void exportScoresToCSV() {
        if (scoresTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No scores to export.");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Scores as CSV");
        fileChooser.setSelectedFile(new File("scores_export.csv"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(fileToSave)) {
                // Write header
                for (int i = 0; i < scoresTableModel.getColumnCount(); i++) {
                    pw.print(scoresTableModel.getColumnName(i));
                    if (i < scoresTableModel.getColumnCount() - 1) pw.print(",");
                }
                pw.println();

                // Write data rows
                for (int r = 0; r < scoresTableModel.getRowCount(); r++) {
                    for (int c = 0; c < scoresTableModel.getColumnCount(); c++) {
                        pw.print(scoresTableModel.getValueAt(r, c));
                        if (c < scoresTableModel.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Scores exported to: " + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export scores.");
            }
        }
    }

    // Exam Settings functions

    private void saveExamSettings() {
        String subject = (String) subjectComboSettings.getSelectedItem();
        if (subject == null) {
            JOptionPane.showMessageDialog(this, "Select a subject.");
            return;
        }
        String numQuestionsStr = numQuestionsField.getText().trim();
        String durationStr = durationField.getText().trim();

        if (numQuestionsStr.isEmpty() || durationStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter number of questions and exam duration.");
            return;
        }

        int numQuestions, duration;
        try {
            numQuestions = Integer.parseInt(numQuestionsStr);
            duration = Integer.parseInt(durationStr);
            if (numQuestions <= 0 || duration <= 0) {
                JOptionPane.showMessageDialog(this, "Numbers must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid numbers.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Get subject id
            PreparedStatement psSubj = conn.prepareStatement("SELECT id FROM subjects WHERE name = ?");
            psSubj.setString(1, subject);
            ResultSet rsSubj = psSubj.executeQuery();
            if (!rsSubj.next()) {
                JOptionPane.showMessageDialog(this, "Subject not found.");
                return;
            }
            int subjectId = rsSubj.getInt("id");

            // Check if exists
            PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM exam_settings WHERE subject_id = ?");
            psCheck.setInt(1, subjectId);
            ResultSet rsCheck = psCheck.executeQuery();
            boolean exists = false;
            if (rsCheck.next()) {
                exists = rsCheck.getInt(1) > 0;
            }

            if (exists) {
                PreparedStatement psUpdate = conn.prepareStatement("UPDATE exam_settings SET num_questions = ?, duration = ? WHERE subject_id = ?");
                psUpdate.setInt(1, numQuestions);
                psUpdate.setInt(2, duration);
                psUpdate.setInt(3, subjectId);
                psUpdate.executeUpdate();
            } else {
                PreparedStatement psInsert = conn.prepareStatement("INSERT INTO exam_settings(subject_id, num_questions, duration) VALUES (?, ?, ?)");
                psInsert.setInt(1, subjectId);
                psInsert.setInt(2, numQuestions);
                psInsert.setInt(3, duration);
                psInsert.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Exam settings saved.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save exam settings.");
        }
    }

    // Reports functions

    private void refreshReports() {
        reportsArea.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            // Average scores per subject
            String avgQuery = "SELECT su.name, AVG(sc.score) AS avg_score FROM scores sc JOIN subjects su ON sc.subject_id = su.id GROUP BY su.name";
            PreparedStatement psAvg = conn.prepareStatement(avgQuery);
            ResultSet rsAvg = psAvg.executeQuery();

            reportsArea.append("Average Scores per Subject:\n");
            while (rsAvg.next()) {
                reportsArea.append(String.format("%s: %.2f\n", rsAvg.getString("name"), rsAvg.getDouble("avg_score")));
            }
            reportsArea.append("\n");

            // Number of attempts per subject
            String attemptsQuery = "SELECT su.name, COUNT(*) AS attempts FROM scores sc JOIN subjects su ON sc.subject_id = su.id GROUP BY su.name";
            PreparedStatement psAtt = conn.prepareStatement(attemptsQuery);
            ResultSet rsAtt = psAtt.executeQuery();

            reportsArea.append("Total Attempts per Subject:\n");
            while (rsAtt.next()) {
                reportsArea.append(String.format("%s: %d\n", rsAtt.getString("name"), rsAtt.getInt("attempts")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            reportsArea.append("Failed to load reports.");
        }
    }

    // Inner class for AddQuestionWindow
    private class AddQuestionWindow extends JFrame {
        private JTextField questionField, optionAField, optionBField, optionCField, optionDField;
        private JComboBox<String> correctOptionCombo;
        private JButton saveBtn;
        private String subject;
        private int teacherId;

        public AddQuestionWindow(String subject, int teacherId) {
            this.subject = subject;
            this.teacherId = teacherId;
            setTitle("Add Question - " + subject);
            setSize(400, 350);
            setLocationRelativeTo(TeacherDashboard.this);
            setLayout(new GridLayout(7, 2, 5, 5));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            add(new JLabel("Question:"));
            questionField = new JTextField();
            add(questionField);

            add(new JLabel("Option A:"));
            optionAField = new JTextField();
            add(optionAField);

            add(new JLabel("Option B:"));
            optionBField = new JTextField();
            add(optionBField);

            add(new JLabel("Option C:"));
            optionCField = new JTextField();
            add(optionCField);

            add(new JLabel("Option D:"));
            optionDField = new JTextField();
            add(optionDField);

            add(new JLabel("Correct Option:"));
            correctOptionCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
            add(correctOptionCombo);

            saveBtn = new JButton("Save");
            add(new JLabel());
            add(saveBtn);

            saveBtn.addActionListener(e -> saveQuestion());
        }

        private void saveQuestion() {
            String qText = questionField.getText().trim();
            String a = optionAField.getText().trim();
            String b = optionBField.getText().trim();
            String c = optionCField.getText().trim();
            String d = optionDField.getText().trim();
            String correct = (String) correctOptionCombo.getSelectedItem();

            if (qText.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement psSubj = conn.prepareStatement("SELECT id FROM subjects WHERE name = ?");
                psSubj.setString(1, subject);
                ResultSet rsSubj = psSubj.executeQuery();
                if (!rsSubj.next()) {
                    JOptionPane.showMessageDialog(this, "Subject not found.");
                    return;
                }
                int subjectId = rsSubj.getInt("id");

                String insertQuery = "INSERT INTO questions (subject_id, question, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement psInsert = conn.prepareStatement(insertQuery);
                psInsert.setInt(1, subjectId);
                psInsert.setString(2, qText);
                psInsert.setString(3, a);
                psInsert.setString(4, b);
                psInsert.setString(5, c);
                psInsert.setString(6, d);
                psInsert.setString(7, correct);
                psInsert.executeUpdate();

                JOptionPane.showMessageDialog(this, "Question added.");
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to add question.");
            }
        }
    }

    // Inner class for EditQuestionWindow
    private class EditQuestionWindow extends JFrame {
        private int questionId;
        private JTextField questionField, optionAField, optionBField, optionCField, optionDField;
        private JComboBox<String> correctOptionCombo;
        private JButton saveBtn;

        public EditQuestionWindow(int questionId) {
            this.questionId = questionId;
            setTitle("Edit Question");
            setSize(400, 350);
            setLocationRelativeTo(TeacherDashboard.this);
            setLayout(new GridLayout(7, 2, 5, 5));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            add(new JLabel("Question:"));
            questionField = new JTextField();
            add(questionField);

            add(new JLabel("Option A:"));
            optionAField = new JTextField();
            add(optionAField);

            add(new JLabel("Option B:"));
            optionBField = new JTextField();
            add(optionBField);

            add(new JLabel("Option C:"));
            optionCField = new JTextField();
            add(optionCField);

            add(new JLabel("Option D:"));
            optionDField = new JTextField();
            add(optionDField);

            add(new JLabel("Correct Option:"));
            correctOptionCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
            add(correctOptionCombo);

            saveBtn = new JButton("Save");
            add(new JLabel());
            add(saveBtn);

            loadQuestionDetails();

            saveBtn.addActionListener(e -> saveEditedQuestion());
        }

        private void loadQuestionDetails() {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT question, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE id = ?");
                ps.setInt(1, questionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    questionField.setText(rs.getString("question"));
                    optionAField.setText(rs.getString("option_a"));
                    optionBField.setText(rs.getString("option_b"));
                    optionCField.setText(rs.getString("option_c"));
                    optionDField.setText(rs.getString("option_d"));
                    correctOptionCombo.setSelectedItem(rs.getString("correct_option"));
                } else {
                    JOptionPane.showMessageDialog(this, "Question not found.");
                    dispose();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void saveEditedQuestion() {
            String qText = questionField.getText().trim();
            String a = optionAField.getText().trim();
            String b = optionBField.getText().trim();
            String c = optionCField.getText().trim();
            String d = optionDField.getText().trim();
            String correct = (String) correctOptionCombo.getSelectedItem();

            if (qText.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String updateQuery = "UPDATE questions SET question = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_option = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(updateQuery);
                ps.setString(1, qText);
                ps.setString(2, a);
                ps.setString(3, b);
                ps.setString(4, c);
                ps.setString(5, d);
                ps.setString(6, correct);
                ps.setInt(7, questionId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Question updated.");
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to update question.");
            }
        }
    }
}
