package hallways.rmi.hallways.scoreboard;

import hallways.rmi.hallways.scoreboard.db.DbProvider;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Handler extends Thread {
    private static final Map<String, String> CONTENT_TYPES = new HashMap<>() {
        {
            put("jpg", "image/jpeg");
            put("html", "text/html");
            put("json", "application/json");
            put("txt", "text/plain");
            put("", "text/plain");
        }
    };

    private static final String NOT_FOUND_MESSAGE = "NOT FOUND";

    private final Socket socket;
    private final String directory;

    public Handler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }

    @Override
    public void run() {
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            var url = this.getRequestUrl(input);
            if (url.equals("/")) {
                url = "/records.txt";
            }
            var filePath = Path.of(this.directory + url);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                DbProvider dbProvider = new DbProvider();
                var scores = dbProvider.getAllScores();
                try (FileWriter writer = new FileWriter("Hallways\\src\\hallways\\rmi\\hallways\\scoreboard\\resources\\records.txt", false))
                {
                    for (var score : scores) {
                        String record = String.format("%s - Player %d won with score %d:%d\n", score.dateTime(), score.winner(), score.score1(), score.score2());
                        writer.write(record);
                    }
                    writer.flush();
                } catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
                var extension = getFileExtension(filePath);
                var type = CONTENT_TYPES.get(extension);
                var fileBytes = Files.readAllBytes(filePath);
                this.sendHeader(output, 200, "OK", type, fileBytes.length);
                output.write(fileBytes);
            } else {
                var type = CONTENT_TYPES.get("text");
                this.sendHeader(output, 404, "Not Found", type, NOT_FOUND_MESSAGE.length());
                output.write(NOT_FOUND_MESSAGE.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRequestUrl(InputStream inputStream) {
        var reader = new Scanner(inputStream).useDelimiter("\r\n");
        var line = reader.next();
        return line.split(" ")[1];
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long length) {
        var ps = new PrintStream(output);
        ps.printf("HTTP/1.1%s %s%n", statusCode, statusText);
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", length);
    }

    private String getFileExtension(Path path) {
        var name = path.getFileName().toString();
        var extensionStart = name.lastIndexOf(".");
        return extensionStart == -1 ? "" : name.substring(extensionStart + 1);
    }
}
