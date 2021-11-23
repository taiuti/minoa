package org.taiuti.minoa.model;

import java.util.ArrayList;
import java.util.List;

public class TimeHorizon {
    protected List<Integer> interval;

    public TimeHorizon(List<Integer> interval) {
        this.interval = interval;
    }

    public TimeHorizon(int[] to) {

        interval = new ArrayList<Integer>();
        for (int index = 0; index < to.length; index++) {
            interval.add(to[index]);
        }
    }

    public List<Integer> getInterval() {
        return interval;
    }

}
