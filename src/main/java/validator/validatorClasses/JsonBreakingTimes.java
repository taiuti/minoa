package validator.validatorClasses;

public class JsonBreakingTimes {
    private final StoppingTime stoppingTime;

    public JsonBreakingTimes(StoppingTime stoppingTime){
        this.stoppingTime = stoppingTime;
    }

    public JsonBreakingTimes() {
        stoppingTime = null;
    }

    public StoppingTime getStoppingTime(){
        return stoppingTime;
    }
}
