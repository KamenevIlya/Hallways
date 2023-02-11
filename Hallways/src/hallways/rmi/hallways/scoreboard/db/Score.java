package hallways.rmi.hallways.scoreboard.db;

import java.sql.Timestamp;

public record Score(int id, Timestamp dateTime, int winner, int score1, int score2) {
}
