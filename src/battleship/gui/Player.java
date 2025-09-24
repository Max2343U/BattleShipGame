package battleship.gui;

public class Player {
    private final String name;
    private final GameBoard board;

    public Player(String name) {
        this.name = name;
        this.board = new GameBoard();
    }

    public String getName() {
        return name;
    }

    public GameBoard getBoard() {
        return board;
    }
}