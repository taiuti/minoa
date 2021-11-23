/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.taiuti.minoa.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.taiuti.minoa.model.Depot;
import org.taiuti.minoa.model.Direction;
import org.taiuti.minoa.model.Headway;
import org.taiuti.minoa.model.Node;
import org.taiuti.minoa.model.TimeHorizon;
import org.taiuti.minoa.model.Trip;
import org.taiuti.minoa.model.VspSolution;

import validator.validatorClasses.Input;
import validator.validatorClasses.JsonDirection;
import validator.validatorClasses.JsonHeadway;
import validator.validatorClasses.JsonNetworkNode;
import validator.validatorClasses.JsonTrip;

public class JsonDataBuilder {

    private Input input;

    private JsonDataBuilder() {
    }

    public static JsonDataBuilder builder() {
        return new JsonDataBuilder();
    }

    public JsonDataBuilder setInput(Input input) {

        this.input = input;
        return this;

    }

    public VspSolution build() {

        TimeHorizon timeHorizon = new TimeHorizon(input.getTimeHorizon());

        // the list of nodes in the Public Trasportation network
        Map<String, validator.validatorClasses.Node> nodeHashMap = new HashMap<>();

        List<Trip> tripList = new ArrayList<>();

        // for every node in the PTN initialize the list of parking, slow charging and
        // fast charging spots
        for (JsonNetworkNode elem : input.getNodes()) {
            nodeHashMap.put(elem.getNode().getName(), elem.getNode());
        }

        for (JsonDirection jsonDirection : input.getDirections()) {

            validator.validatorClasses.Node startNode = nodeHashMap.get(jsonDirection.getDirection().getStartNode());

            Node s;
            if ("dep".equals(startNode.getName())) {
                s = new Depot();
            } else {
                s = new Node();
            }

            s.setName(startNode.getName());

            validator.validatorClasses.Node endNode = nodeHashMap.get(jsonDirection.getDirection().getEndNode());
            Node e;
            if ("dep".equals(endNode.getName())) {
                e = new Depot();
            } else {
                e = new Node();
            }
            e.setName(endNode.getName());

            List<Headway> headway = new ArrayList<Headway>();
            for (JsonHeadway h : jsonDirection.getDirection().getHeadways()) {

                int minHeadway = h.getHeadway().getMinHeadway();
                int idealHeadway = h.getHeadway().getIdealHeadway();
                int maxHeadway = h.getHeadway().getMaxHeadway();
                headway.add(new Headway(minHeadway, idealHeadway, maxHeadway));

            }

            Direction direction = new Direction(jsonDirection.getDirection().getLineName(),
                    jsonDirection.getDirection().getDirectionType(), s, e, timeHorizon, headway);

            for (JsonTrip trip : jsonDirection.getDirection().getTrips()) {
                Trip t = new Trip();
                t.setId(trip.getTrip().getTripId());
                t.setDirection(direction);
                t.setStartTime((int)trip.getTrip().getStartTime());
                t.setEndTime((int)trip.getTrip().getEndTime());
                t.setLengthTrip((trip.getTrip().getLengthTrip()));
                tripList.add(t);
            }
        }

        VspSolution initialSolution = Utility.restrictedFirstInFirstOut(tripList);

        return initialSolution;
    }
}
