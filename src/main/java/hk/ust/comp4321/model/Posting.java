package hk.ust.comp4321.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Posting implements Serializable {
    private static final long serialVersionUID = 1L;

    private int frequency;
    private final List<Integer> positions;

    public Posting() {
        this.frequency = 0;
        this.positions = new ArrayList<>();
    }

    public void addPosition(int position) {
        frequency += 1;
        positions.add(position);
    }

    public int getFrequency() {
        return frequency;
    }

    public List<Integer> getPositions() {
        return positions;
    }
}
