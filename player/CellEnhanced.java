package player;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;

public class CellEnhanced implements Comparable {
    public int i;
    public int j;
    public MNKCellState state;
    private int weight;

    public CellEnhanced(MNKCell c) {
        this.i = c.i;
        this.j = c.j;
        this.state = c.state;
        this.weight = 0;
    }

    public CellEnhanced(MNKCell c, int weight) {
        this(c);
        this.weight = weight;
    }

    @Override
    public int compareTo(Object o) {
        return ((CellEnhanced) o).weight - this.weight;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void addWeight(int weight) {
        this.weight += weight;
    }

    @Override
    public String toString() {
        return "[" + i + ", " + j + "] -> " + state + " : " + weight;
    }
}
