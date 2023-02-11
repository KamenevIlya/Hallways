package hallways.rmi.hallways.scoreboard.db;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;

public class DbProvider {
    private final String _url = "jdbc:postgresql://127.0.0.1:5432/postgres";

    public DbProvider() {
    }

    private static final String INSERT_USERS_SQL = "INSERT INTO hallways_app.score" +
            "  (game_date, winner, score1, score2) VALUES " +
            " (?, ?, ?, ?);";

    public ArrayList<Score> getAllScores() {
        try {
            Class.forName("org.postgresql.Driver");
            Properties authorization = new Properties();
            authorization.put("user", "postgres");
            authorization.put("password", "postgres");
            Connection connection = DriverManager.getConnection(_url, authorization);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM hallways_app.score");
            ResultSet rs = preparedStatement.executeQuery();

            ArrayList<Score> data = new ArrayList<Score>();
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp dateTime = rs.getTimestamp("game_date");
                int winner = rs.getInt("winner");
                int score1 = rs.getInt("score1");
                int score2 = rs.getInt("score2");
                var record = new Score(id, dateTime, winner, score1, score2);
                data.add(record);
            }
            return data;
        } catch (SQLException throwables) {
            for (Throwable e: throwables) {
                if (e instanceof SQLException) {
                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                    System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                    System.err.println("Message: " + e.getMessage());
                    Throwable t = throwables.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
            return new ArrayList<>();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void InsertScore(int winner, int score1, int score2) {

        try {
            Class.forName("org.postgresql.Driver");
            Properties authorization = new Properties();
            authorization.put("user", "postgres");
            authorization.put("password", "postgres");
            Connection connection = DriverManager.getConnection(_url, authorization);

            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL);
            preparedStatement.setTimestamp(1, java.sql.Timestamp.from(Instant.now()));
            preparedStatement.setInt(2, winner);
            preparedStatement.setInt(3, score1);
            preparedStatement.setInt(4, score2);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            for (Throwable e: throwables) {
                if (e instanceof SQLException) {
                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                    System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                    System.err.println("Message: " + e.getMessage());
                    Throwable t = throwables.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
