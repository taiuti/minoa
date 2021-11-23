package validator.validatorClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Node{
    private final String nodeName;
    private final int breakCapacity;
    private final int slowChargeCapacity;
    private final int fastChargeCapacity;
    private final List<JsonBreakingTimes> breakingTimes;

    @JsonCreator
    public Node(@JsonProperty("nodeName") String name,
                @JsonProperty("breakCapacity") int breakCapacity,
                @JsonProperty("fastChargeCapacity") int fastChargeCapacity,
                @JsonProperty("slowChargeCapacity") int slowChargeCapacity,
                @JsonProperty("stoppingTimes") List<JsonBreakingTimes> breakingTimes){

        if(name == null)
            throw new NullPointerException();

        if(breakCapacity < 0 || fastChargeCapacity < 0 || slowChargeCapacity < 0)
            throw new NegativeArgumentException();

        this.nodeName = name;
        this.breakCapacity = breakCapacity;
        this.fastChargeCapacity = fastChargeCapacity;
        this.slowChargeCapacity = slowChargeCapacity;
        this.breakingTimes = breakingTimes;
    }


    @JsonProperty("nodeName")
    public String getName(){
        return this.nodeName;
    }

    public int getBreakCapacity(){
        return this.breakCapacity;
    }

    public int getFastChargeCapacity(){
        return this.fastChargeCapacity;
    }

    public int getSlowChargeCapacity(){
        return this.slowChargeCapacity;
    }

    public List<JsonBreakingTimes> getStoppingTimes(){
        return this.breakingTimes;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this)
            return true;

        if(!(obj instanceof Node))
            return false;

        Node tmp = (Node) obj;

        return tmp.getName().equals(this.nodeName) &&
               tmp.getBreakCapacity() == this.breakCapacity &&
               tmp.getFastChargeCapacity() == this.fastChargeCapacity &&
               tmp.getSlowChargeCapacity() == this.slowChargeCapacity;
    }

    @Override
    public String toString(){
        return "Node:{\n" +
               "Name : " + this.nodeName + "\n" +
               "BreakCapacity: " + this.breakCapacity + "\n" +
               "fastChargeCapacity: " + this.fastChargeCapacity + "\n" +
               "slowChargeCapacity: " + this.slowChargeCapacity + "\n";
    }

}

class NegativeArgumentException extends RuntimeException{
    public NegativeArgumentException(){
        super();
    }

    public NegativeArgumentException(String s){
        super(s);
    }
}
