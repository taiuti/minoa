package org.taiuti.minoa.model;

import java.util.Optional;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.taiuti.minoa.solver.TripDifficultyComparator;

@PlanningEntity(difficultyComparatorClass = TripDifficultyComparator.class)
public class Trip implements Standstill {

    @PlanningId
    protected Integer id;
    protected String name;

    protected Direction direction;;
    protected Integer startTime;
    protected Integer endTime;
    protected Integer mainStopArrivalTime;
    protected double lengthTrip;
    protected String isInitialFinalTT;
    protected Headway headway;

    // Planning variables: changes during planning, between score calculations.
    @PlanningVariable(valueRangeProviderRefs = { "vehicleRange",
            "tripRange" }, graphType = PlanningVariableGraphType.CHAINED)
    protected Standstill previousStandstill;

    // Shadow variables
    protected Trip nextTrip;

    @AnchorShadowVariable(sourceVariableName = "previousStandstill")
    protected Vehicle vehicle;

    // No-arg constructor required for Hibernate
    public Trip() {
    }

    public Trip(Integer id, String name, Direction direction, Integer startTime, Integer endTime) {
        this.id = id;
        this.name = name;
        this.direction = direction;
        this.startTime = startTime;
        this.endTime = endTime;

        updateHeadway();
    }

    public Trip(Integer id, String name, Direction direction, Integer startTime, Integer endTime,
            Integer mainStopArrivalTime, double lengthTrip, String isInitialFinalTT) {
        this.id = id;
        this.name = name;
        this.direction = direction;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mainStopArrivalTime = mainStopArrivalTime;
        this.lengthTrip = lengthTrip;
        this.isInitialFinalTT = isInitialFinalTT;

        updateHeadway();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;

        // Aggiorno Headway
        updateHeadway();
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
        updateHeadway();
    }

    public Integer getMainStopArrivalTime() {
        return mainStopArrivalTime;
    }

    public void setMainStopArrivalTime(Integer mainStopArrivalTime) {
        this.mainStopArrivalTime = mainStopArrivalTime;
    }

    public double getLengthTrip() {
        return lengthTrip;
    }

    public void setLengthTrip(double lengthTrip) {
        this.lengthTrip = lengthTrip;
    }

    public String getIsInitialFinalTT() {
        return isInitialFinalTT;
    }

    public void setIsInitialFinalTT(String isInitialFinalTT) {
        this.isInitialFinalTT = isInitialFinalTT;
    }

    public Standstill getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(Standstill previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    @Override
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
    public Headway getHeadway() {
        return headway;
    }

    public void updateHeadway() {
        // set headway
        if (endTime != null) {
            int i = -1;
            for (Integer interval : direction.getTimeHorizon().interval) {
                if (endTime <= interval) {
                    headway = direction.getHeadways().get(i);
                    break;
                }
                i++;
            }
        }
    }

    public Node getStartNode() {

        if (direction == null) {
            return null;
        }
        return direction.getStartNode();
    }

    public Node getEndNode() {

        if (direction == null) {
            return null;
        }
        return direction.getEndNode();
    }

    @Override
    public Trip getNextTrip() {
        return nextTrip;
    }

    @Override
    public void setNextTrip(Trip nextTrip) {
        this.nextTrip = nextTrip;
    }

    public Integer getNextTripStartTime() {
        return (nextTrip == null) ? getEndTime() : nextTrip.getStartTime();
    }

    /*
     * l'intervallo di attesa (Headway) deve essere compreso tra minHeadway <=
     * attesa <= maxHeadway
     * 
     */
    public Optional<Integer> delay(Standstill other) {

        if (other == null) {
            return Optional.empty();
        }

        if (other instanceof Vehicle) {
            return Optional.empty();
        }

        Trip otherTrip = (Trip) other;

        if (!this.getEndNode().equals(otherTrip.getStartNode())) {
            return Optional.empty();
        }

        Optional<Integer> delay = Optional.of(otherTrip.startTime - this.endTime);

        return delay;

    }

    /*
     * l'intervallo di attesa (Headway) deve essere compreso tra minHeadway <=
     * attesa <= maxHeadway
     * 
     */

    public Boolean isCompatible(Standstill next) {

        if (next instanceof Vehicle) {
            return false;
        }

        if (next == null) {
            return true;
        }

        if (!(next instanceof Trip)) {
            return false;
        }

        Trip nextTrip = (Trip) next;

        if (!this.getEndNode().equals(nextTrip.getStartNode())) {
            return false;
        }

        Integer delay = nextTrip.startTime - this.endTime;

        return ((headway.getMinHeadway() <= delay) && (delay <= headway.getMaxHeadway()));

    }

    public Boolean isCompatibleWithPreviousStandstill() {

        if (previousStandstill == null) {
            return false;
        }

        if (previousStandstill instanceof Vehicle) {
            return true;
        }

        Trip previousTrip = (Trip) previousStandstill;

        return previousTrip.isCompatible(this);

    }

    public Boolean isCompatibleWithNextStandstill() {

        return isCompatible(nextTrip);

    }

    /*
     * l'intervallo di attesa (Headway) deve essere compreso tra minHeadway <=
     * attesa <= maxHeadway
     * 
     */
    public Boolean isInterchangeable(Standstill other) {

        Boolean ab = false;
        Boolean ba = false;

        if (other == null) {
            return false;
        }

        if (other instanceof Trip) {
            Trip otherTrip = (Trip) other;

            // controllo che il other.previousStandstill sia compatibile con trip
            Standstill previousDestination = otherTrip.getPreviousStandstill();

            if (previousDestination instanceof Vehicle) {
                ab = true;
            } else {
                // previousDestination is a trip
                Trip previousDestinationTrip = (Trip) previousDestination;
                if (previousDestinationTrip.isCompatible(this)) {
                    ab = true;
                }
            }

        }

        // controllo che il previousStandstill sia compatibile con other Trip
        if (getPreviousStandstill() instanceof Vehicle) {
            ba = true;
        } else {
            // previousDestination is a trip
            Trip previousTrip = (Trip) getPreviousStandstill();
            if (previousTrip.isCompatible(other)) {
                ba = true;
            }
        }

        return (ab && ba);
    }

    @Override
    public String toString() {

        String message = "Trip [id=" + id + ", " + getStartNode().getName() + "(" + startTime + ")->"
                + getEndNode().getName() + "(" + endTime + ")]";

        if (getNextTrip() != null) {
            message += ", " + getNextTrip().toString();
        }

        return message + "]";
    }

}