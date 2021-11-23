package validator.validatorClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class StoppingTime{
    private final int minStoppingTime;
    private final int maxStoppingTime;

    @JsonCreator
    public StoppingTime(@JsonProperty("minStoppingTime") int min,
                        @JsonProperty("maxStoppingTime") int max){
        this.minStoppingTime = min;
        this.maxStoppingTime = max;
    }

    public StoppingTime(){
        this(0, 0);
    }

    public int getMinStoppingTime(){
        return this.minStoppingTime;
    }

    public int getMaxStoppingTime(){
        return this.maxStoppingTime;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == this)
            return true;

        if(!(obj instanceof StoppingTime))
            return false;

        StoppingTime tmp = (StoppingTime) obj;

        return tmp.getMinStoppingTime() == this.minStoppingTime &&
               tmp.getMaxStoppingTime() == this.maxStoppingTime;
    }

    @Override
    public String toString(){
        return "StoppingTime: {" + "\n" +
               "MinStoppingTime: " + this.minStoppingTime + "\n" +
               "MaxStoppingTime: " + this.maxStoppingTime + "\n";
    }
}
