package org.taiuti.minoa.model;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
@PlanningEntity
public interface Standstill {

    Vehicle getVehicle();

    /**
     * @return sometimes null
     */
    @InverseRelationShadowVariable(sourceVariableName = "previousStandstill")
    Trip getNextTrip();

    void setNextTrip(Trip nextTrip);

}
