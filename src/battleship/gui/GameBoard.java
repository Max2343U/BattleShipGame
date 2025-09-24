package battleship.gui;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameBoard {
    private final char[][] grid = new char[10][10];
    private final Map<String,Ship> ships = new LinkedHashMap<String,Ship>();
    private final Map<Character,Ship> shipMap = new LinkedHashMap<Character,Ship>();

    public GameBoard() {
        for (int r = 0; r < 10; r++) {
            Arrays.fill(grid[r], '~');
        }
    }

    public boolean placeShip(String name, int size,
                             int row, int col, boolean vertical) {
        int dr = vertical ? 1 : 0;
        int dc = vertical ? 0 : 1;
        for (int i = 0; i < size; i++) {
            int rr = row + dr * i;
            int cc = col + dc * i;
            if (rr < 0 || rr >= 10 || cc < 0 || cc >= 10
                || grid[rr][cc] != '~') {
                return false;
            }
        }
        for (int i = 0; i < size; i++) {
            int rr = row + dr * i;
            int cc = col + dc * i;
            grid[rr][cc] = name.charAt(0);
        }
        Ship ship = new Ship(name, size);
        ships.put(name, ship);
        shipMap.put(name.charAt(0), ship);
        return true;
    }

    public FireResult fireAt(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return new FireResult(false, false, null);
        }
        char cell = grid[row][col];
        if (cell == 'M' || cell == 'X') {
            return new FireResult(false, false, null);
        }
        if (cell == '~') {
            grid[row][col] = 'M';
            return new FireResult(false, false, null);
        }
        Ship ship = shipMap.get(cell);
        ship.hit();
        grid[row][col] = 'X';
        boolean sunk = ship.isSunk();
        return new FireResult(true, sunk,
                              sunk ? ship.getName() : null);
    }

    public boolean allShipsSunk() {
        for (Ship s : ships.values()) {
            if (!s.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public char getCell(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return '~';
        }
        return grid[row][col];
    }

    public static class FireResult {
        public final boolean hit;
        public final boolean sunk;
        public final String shipName;
        public FireResult(boolean hit, boolean sunk,
                          String shipName) {
            this.hit = hit;
            this.sunk = sunk;
            this.shipName = shipName;
        }
    }
}