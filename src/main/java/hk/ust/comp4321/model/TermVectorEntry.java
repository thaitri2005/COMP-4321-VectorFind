package hk.ust.comp4321.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TermVectorEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private int bodyFrequency;
    private int titleFrequency;
    private final List<Integer> bodyPositions;
    private final List<Integer> titlePositions;

    public TermVectorEntry() {
        this.bodyFrequency = 0;
        this.titleFrequency = 0;
        this.bodyPositions = new ArrayList<>();
        this.titlePositions = new ArrayList<>();
    }

    public void addBodyPosition(int position) {
        bodyFrequency += 1;
        bodyPositions.add(position);
    }

    public void addTitlePosition(int position) {
        titleFrequency += 1;
        titlePositions.add(position);
    }

    public int getBodyFrequency() {
        return bodyFrequency;
    }

    public int getTitleFrequency() {
        return titleFrequency;
    }

    public List<Integer> getBodyPositions() {
        return bodyPositions;
    }

    public List<Integer> getTitlePositions() {
        return titlePositions;
    }

    public int totalFrequency() {
        return bodyFrequency + titleFrequency;
    }
}
