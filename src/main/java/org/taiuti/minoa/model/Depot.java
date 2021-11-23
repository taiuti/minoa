package org.taiuti.minoa.model;

import java.util.List;

public class Depot extends Node {

    public Depot() {
        super();
    }

    public Depot(String name) {
        super(name);
    }

    public Depot(String name, Integer breakCapacity, Integer slowChargeCapacity, Integer fastChargeCapacity,
            List<BreakingTime> breakingTimes) {
        super(name, breakCapacity, slowChargeCapacity, fastChargeCapacity, breakingTimes);
    }
}