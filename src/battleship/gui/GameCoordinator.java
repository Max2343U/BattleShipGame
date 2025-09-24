package battleship.gui;

public class GameCoordinator {
    private final Player player1;
    private final Player player2;
    private Player currentPlayer;
    private Player opponent;

    public GameCoordinator(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        this.opponent = p2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void switchTurn() {
        Player tmp = currentPlayer;
        currentPlayer = opponent;
        opponent = tmp;
    }

    public boolean isGameOver() {
        return opponent.getBoard().allShipsSunk();
    }
}