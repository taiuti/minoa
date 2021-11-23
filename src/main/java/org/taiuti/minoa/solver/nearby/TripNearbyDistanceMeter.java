package org.taiuti.minoa.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.taiuti.minoa.model.Standstill;
import org.taiuti.minoa.model.Trip;

public class TripNearbyDistanceMeter implements NearbyDistanceMeter<Trip, Standstill> {

    @Override
    public double getNearbyDistance(Trip origin, Standstill other) {
        double distance;
        if (origin.isInterchangeable(other)) {
            distance = 0.0;
        } else{
            distance = Double.MAX_VALUE;
        }
        return distance;
    }
}
