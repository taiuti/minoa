package org.taiuti.minoa.model;

public class BreakingTime {

    private final Integer minStoppingTime;
    private final Integer maxStoppingTime;

    public BreakingTime(Integer min, Integer max) {
        this.minStoppingTime = min;
        this.maxStoppingTime = max;
    }

    public BreakingTime() {
        this(0, 0);
    }

    public Integer getMinStoppingTime() {
        return this.minStoppingTime;
    }

    public Integer getMaxStoppingTime() {
        return this.maxStoppingTime;
    }

}
