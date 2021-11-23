package org.taiuti.minoa.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import org.optaplanner.core.api.domain.lookup.PlanningId;

public class Vehicle implements Standstill {

    @PlanningId
    protected Integer id;
    protected String name;

    // Shadow variables
    protected Trip nextTrip;

    public Vehicle() {
    }

    public Vehicle(Integer id, String name) {
        this.id = id;
        this.name = name.trim();
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

    @JsonBackReference
    public Vehicle getVehicle() {
        return this;
    }

    public Trip getNextTrip() {
        return nextTrip;
    }

    public void setNextTrip(Trip nextTrip) {
        this.nextTrip = nextTrip;
    }

    @Override
    public String toString() {
        String message="Vehicle [name=" + getName();

        if (getNextTrip() != null){
            message += ", " + getNextTrip().toString();
        }

        return message + "]";
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
