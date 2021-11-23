package org.taiuti.minoa.solver;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.taiuti.minoa.model.Trip;

public class TripDifficultyComparator implements Comparator<Trip> {

    public int compare(Trip a, Trip b) {
        return new CompareToBuilder().append(a.getStartTime(), b.getStartTime()).toComparison();
    }

}