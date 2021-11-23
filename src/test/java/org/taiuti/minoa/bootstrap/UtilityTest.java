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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.taiuti.minoa.model.Direction;
import org.taiuti.minoa.model.Headway;
import org.taiuti.minoa.model.Node;
import org.taiuti.minoa.model.TimeHorizon;
import org.taiuti.minoa.model.Trip;
import org.taiuti.minoa.model.Vehicle;
import org.taiuti.minoa.model.VspSolution;

class UtilityTest {

    @Test
    void firstCompatibleTrip() {
        // TimeHorizon
        TimeHorizon th = new TimeHorizon(new int[] { 27960, 36000, 43200, 51360 });

        // Headway
        List<Headway> hws = new ArrayList<Headway>();
        hws.add(new Headway(480, 780, 1080));
        hws.add(new Headway(480, 780, 1320));
        hws.add(new Headway(480, 780, 1080));

        // Node
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");

        // Direction
        Direction dirAB = new Direction("AB", "A->B", nodeA, nodeB, th, hws);
        Direction dirBA = new Direction("AB", "B->A", nodeB, nodeA, th, hws);

        // Trip
        List<Trip> trips = new ArrayList<Trip>();
        Trip tAB1 = new Trip(1, "trip-1", dirAB, 27960, 28000, 28000, 1.0, null);
        Trip tBA2 = new Trip(2, "trip-2", dirBA, 28000, 28100, 28100, 1.0, null);
        Trip tBA3 = new Trip(3, "trip-3", dirBA, 28000, 28100, 28100, 1.0, null);
        Trip tBA4 = new Trip(4, "trip-4", dirBA, 28000, 28100, 28100, 1.0, null);

        trips.add(tAB1);
        trips.add(tBA2);
        trips.add(tBA3);
        trips.add(tBA4);

        tBA2.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway());
        tBA3.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway() + 1);

        Optional<Trip> t = Utility.getFirstCompatibleTrip(tAB1, trips);

        assertFalse(t.isEmpty());
        assertEquals(tBA2, t.get(), "getFirstCompatibleTrip");

        tBA2.setVehicle(new Vehicle());

        t = Utility.getFirstCompatibleTrip(tAB1, trips);

        assertFalse(t.isEmpty());
        assertEquals(tBA3, t.get(), "getFirstCompatibleTrip");

    }

    @Test
    void restrictedFirstInFirstOut() {
        // TimeHorizon
        TimeHorizon th = new TimeHorizon(new int[] { 27960, 36000, 43200, 51360 });

        // Headway
        List<Headway> hws = new ArrayList<Headway>();
        hws.add(new Headway(480, 780, 1080));
        hws.add(new Headway(480, 780, 1320));
        hws.add(new Headway(480, 780, 1080));

        // Node
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");

        // Direction
        Direction dirAB = new Direction("AB", "A->B", nodeA, nodeB, th, hws);
        Direction dirBA = new Direction("AB", "B->A", nodeB, nodeA, th, hws);
        Direction dirBC = new Direction("BC", "B->C", nodeB, nodeC, th, hws);
        Direction dirCB = new Direction("CB", "C->B", nodeC, nodeB, th, hws);

        // Trip
        List<Trip> trips = new ArrayList<Trip>();
        Trip tAB1 = new Trip(1, "tAB1", dirAB, 27960, 28000, 28000, 1.0, null);
        Trip tBA2 = new Trip(2, "tBA2", dirBA, 28000, 28100, 28100, 1.0, null);
        Trip tBC1 = new Trip(3, "tBC1", dirBC, 28000, 28100, 28100, 1.0, null);
        Trip tCB2 = new Trip(4, "tCB2", dirCB, 28000, 28100, 28100, 1.0, null);

        trips.add(tAB1);
        trips.add(tBA2);
        trips.add(tBC1);
        trips.add(tCB2);

        tBA2.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway() + 1);
        tBC1.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway());

        VspSolution solution = Utility.restrictedFirstInFirstOut(trips);


    }
}
