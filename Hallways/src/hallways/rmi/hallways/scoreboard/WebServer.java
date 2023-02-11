package hallways.rmi.hallways.scoreboard;

import java.io.IOException;
import java.net.ServerSocket;

public class WebServer {
    private final int port;
    private final String directory;

    public WebServer(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    public void start() {
        try (var server = new ServerSocket(this.port)) {
            while(true) {
                var socket = server.accept();
                var thread = new Handler(socket, this.directory);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
