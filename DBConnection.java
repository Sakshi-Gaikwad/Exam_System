import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/exam_system";
    private static final String USER = "root"; // XAMPP default user
    private static final String PASS = ""; // Leave empty unless you've set a password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
