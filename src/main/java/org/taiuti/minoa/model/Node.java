package org.taiuti.minoa.model;

import java.util.List;

public class Node {

    private String name;
    private Integer breakCapacity;
    private Integer slowChargeCapacity;
    private Integer fastChargeCapacity;
    private List<BreakingTime> breakingTimes;

    public Node() {
    }

    public Node(String name) {
        this.name = name;
    }

    public Node(String name, Integer breakCapacity, Integer slowChargeCapacity, Integer fastChargeCapacity,
            List<BreakingTime> breakingTimes) {
        this.name = name;
        this.breakCapacity = breakCapacity;
        this.slowChargeCapacity = slowChargeCapacity;
        this.fastChargeCapacity = fastChargeCapacity;
        this.breakingTimes = breakingTimes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBreakCapacity() {
        return breakCapacity;
    }

    public void setBreakCapacity(Integer breakCapacity) {
        this.breakCapacity = breakCapacity;
    }

    public Integer getSlowChargeCapacity() {
        return slowChargeCapacity;
    }

    public void setSlowChargeCapacity(Integer slowChargeCapacity) {
        this.slowChargeCapacity = slowChargeCapacity;
    }

    public Integer getFastChargeCapacity() {
        return fastChargeCapacity;
    }

    public void setFastChargeCapacity(Integer fastChargeCapacity) {
        this.fastChargeCapacity = fastChargeCapacity;
    }

    public List<BreakingTime> getBreakingTimes() {
        return breakingTimes;
    }

    public void setBreakingTimes(List<BreakingTime> breakingTimes) {
        this.breakingTimes = breakingTimes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Node [name=" + name + "]";
    }

}