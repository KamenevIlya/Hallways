package hallways.rmi.hallways.scoreboard;
import hallways.rmi.hallways.common.rabbitmqprovider.RabbitmqProvider;
import hallways.rmi.hallways.scoreboard.db.DbProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Controller {
    private static boolean isFinal = false;

    public Controller() {
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        var port = 7777;
        var directory = "F:\\учебка\\хуйня_илясика\\Hallways\\Hallways\\src\\hallways\\rmi\\hallways\\scoreboard\\resources";
        WebServer server = new WebServer(port, directory);

        System.out.println("Scoreboard started");
        RabbitmqProvider rabbitmqProvider = new RabbitmqProvider();

        while (!isFinal){
            TimeUnit.SECONDS.sleep(1);
            rabbitmqProvider.ReceiveMessage("score", (consumerTag, message) -> {
                if (message != null){
                    String newMessage = new String(message.getBody(), StandardCharsets.UTF_8);
                    System.out.println("RabbitMQ. Received '" + newMessage + "'");
                    if (newMessage.startsWith("final")) {
                        int winner = Integer.parseInt(String.valueOf(newMessage.toCharArray()[6]));
                        int score1 = 0;
                        int score2 = 0;
                        for (char c : newMessage.substring("final-p-".length()).toCharArray()) {
                            if (c == '-') {
                                score1 = score2;
                                score2 = 0;
                                continue;
                            }
                            score2 = score2 * 10 + Integer.parseInt(String.valueOf(c));
                        }
                        DbProvider dbProvider = new DbProvider();
                        dbProvider.InsertScore(winner, score1, score2);
                        System.out.printf("Final score: %d-%d. The winner is player #%d", score1, score2, winner);
                        isFinal = true;
                        server.start();
                    }
                }
            });
        }
    }
}


