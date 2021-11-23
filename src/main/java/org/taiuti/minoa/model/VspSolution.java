package org.taiuti.minoa.model;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

@PlanningSolution
public class VspSolution {

    protected List<Trip> tripList;

    protected List<Vehicle> vehicleList;

    protected HardSoftScore score;

    // Ignored by OptaPlanner, used by the UI to display solve or stop solving
    // button
    private SolverStatus solverStatus;

    public VspSolution() {
    }

    public VspSolution(List<Trip> tripList, List<Vehicle> vehicleList) {
        this.tripList = tripList;
        this.vehicleList = vehicleList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "tripRange")
    public List<Trip> getTripList() {
        return tripList;
    }

    public void setTripList(List<Trip> tripList) {
        this.tripList = tripList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "vehicleRange")
    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    @Override
    public String toString() {

        String route = "";
        for (Vehicle vehicle : vehicleList) {
            route += "[" + vehicle.getName();
            Trip next = vehicle.getNextTrip();
            while (next != null) {
                route += "->" + next.getId() + " " + next.getStartNode().getName() + "/" + next.getEndNode().getName()
                        + " (" + next.getStartTime() + "," + next.getEndTime() + ")";
                next = next.getNextTrip();
            }
            route += "]\n";
        }

        return route;
    }
}
