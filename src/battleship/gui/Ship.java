package battleship.gui;

public class Ship {
    private final String name;
    private final int size;
    private int hits = 0;

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public void hit() {
        hits++;
    }

    public boolean isSunk() {
        return hits >= size;
    }

    public String getName() {
        return name;
    }
}