package org.taiuti.minoa.model;

public class Headway {

    private final int minHeadway;
    private final int idealHeadway;
    private final int maxHeadway;

    public Headway(int minHeadway, int idealHeadway, int maxHeadway) {
        this.minHeadway = minHeadway;
        this.idealHeadway = idealHeadway;
        this.maxHeadway = maxHeadway;
    }

    public int getMinHeadway() {
        return minHeadway;
    }

    public int getIdealHeadway() {
        return idealHeadway;
    }

    public int getMaxHeadway() {
        return maxHeadway;
    }
}
