package hallways.rmi.hallways.server;

import hallways.rmi.hallways.common.PlayerItem;
import hallways.rmi.hallways.common.packages.ClientPackage;
import hallways.rmi.hallways.common.packages.ServerPackage;
import hallways.rmi.hallways.common.status.GameStatus;
import hallways.rmi.hallways.common.status.MoveStatus;

public class Controller {

    Server server;
    Game game;

    public Controller() {
    }

    public Controller(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void startGame(){
        game = new Game(this);
    }

    public void processMove(ClientPackage clientPackage) {
        if (!server.isReadyToPlay()) {
            server.sendMessage(new ServerPackage(null, null, null, null,
                    GameStatus.NEW_GAME, null), clientPackage.getPlayer());
            return;
        }

        try {
            game.processMove(clientPackage.getPlayer(), clientPackage.getClickedBoard());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (game.getMoveStatus() == MoveStatus.SUCCESS) {

            ServerPackage message = new ServerPackage(game.getNeighbourCellLeftOrTop(),
                    game.getNeighbourCellRightOrDown(), game.getCurrentBoard(),
                    game.getMoveStatus(), game.getGameStatus(), game.getCurrentPlayer());

            server.sendMessage(message, PlayerItem.player1);
            server.sendMessage(message, PlayerItem.player2);

        } else {
            server.sendMessage(new ServerPackage(null, null, null, game.getMoveStatus(),
                    game.getGameStatus(), game.getCurrentPlayer()), clientPackage.getPlayer());
        }
    }

    public void startServer(){
        server.start();
    }

    public static void main(String[] args) {
        Controller controller = new Controller();
        Server server = new Server(controller);
        controller.server = server;
        controller.startServer();
    }
}
