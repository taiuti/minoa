package validator.validatorClasses;

import java.util.ArrayList;
import java.util.List;

public class ActivityWrapper {

    private final List<Activity> activities;

    public ActivityWrapper(List<Activity> a){
        activities = new ArrayList<Activity>(a);
    }

    public ActivityWrapper(){ activities = new ArrayList<Activity>();}

    public List<Activity> getActivities() {
        return activities;
    }
}
