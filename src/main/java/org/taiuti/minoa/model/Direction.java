package org.taiuti.minoa.model;

import java.util.List;

public class Direction {
    private final String lineName;
    private final String directionType;
    private final Node startNode;
    private final Node endNode;
    private final TimeHorizon timeHorizon;

    private final List<Headway> headways;

    public Direction(String lineName, String directionType, Node startNode, Node endNode, TimeHorizon timeHorizon,
            List<Headway> headways) {
        this.lineName = lineName;
        this.directionType = directionType;
        this.startNode = startNode;
        this.endNode = endNode;
        this.timeHorizon = timeHorizon;
        this.headways = headways;
    }

    public String getLineName() {
        return lineName;
    }

    public String getDirectionType() {
        return directionType;
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public TimeHorizon getTimeHorizon() {
        return timeHorizon;
    }

    public List<Headway> getHeadways() {
        return headways;
    }

}
