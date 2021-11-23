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

package org.taiuti.minoa.solver;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;
import org.taiuti.minoa.model.Direction;
import org.taiuti.minoa.model.Headway;
import org.taiuti.minoa.model.Node;
import org.taiuti.minoa.model.TimeHorizon;
import org.taiuti.minoa.model.Trip;
import org.taiuti.minoa.model.Vehicle;
import org.taiuti.minoa.model.VspSolution;

class VspConstraintProviderTest {

    private final ConstraintVerifier<VspConstraintProvider, VspSolution> constraintVerifier = ConstraintVerifier
            .build(new VspConstraintProvider(), VspSolution.class, Trip.class);

    @Test
    void isCompatible() {

        TimeHorizon th = new TimeHorizon(new int[] { 30000, 40000, 50000, 60000 });

        // Headway
        List<Headway> hws = new ArrayList<Headway>();
        hws.add(new Headway(100, 150, 200));
        hws.add(new Headway(100, 150, 200));
        hws.add(new Headway(100, 150, 200));

        // Node
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");

        // Direction
        Direction dirAB = new Direction("AB", "A->B", nodeA, nodeB, th, hws);
        Direction dirBA = new Direction("AB", "B->A", nodeB, nodeA, th, hws);
        Direction dirBC = new Direction("BC", "B->C", nodeB, nodeC, th, hws);
        Direction dirCB = new Direction("CB", "C->B", nodeC, nodeB, th, hws);

        // Vehicle
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        Vehicle v1 = new Vehicle(1, "vehicle-1");
        Vehicle v2 = new Vehicle(2, "vehicle-2");

        vehicles.add(v1);
        vehicles.add(v2);

        // Trip
        List<Trip> trips = new ArrayList<Trip>();
        Trip tAB1 = new Trip(1, "tAB1", dirAB, null, null);
        Trip tBA2 = new Trip(2, "tBA2", dirBA, null, null);
        Trip tCB3 = new Trip(3, "tCB2", dirCB, null, null);
        Trip tBC4 = new Trip(4, "tBC1", dirBC, null, null);

        trips.add(tAB1);
        trips.add(tBA2);
        trips.add(tCB3);
        trips.add(tBC4);

        /*
         * Situazione iniziale A(30000)->B(31000) [150] B(31150)->A(32150)
         * C(30050)->B(31050) [100,150,200] B(31200)->C(32200)
         *
         * Situazione finale A(30000)->B(31000) [100,150,200] B(31200)->C(32200)
         * C(30050)->B(31050) [100] B(31150)->A(32150)
         */

        // blocco1
        v1.setNextTrip(tAB1);

        tAB1.setVehicle(v1);
        tAB1.setPreviousStandstill(v1);
        tAB1.setNextTrip(tBA2);
        tAB1.setStartTime(30000);
        tAB1.setEndTime(31000);

        tBA2.setVehicle(v1);
        tBA2.setPreviousStandstill(tAB1);
        tBA2.setNextTrip(null);
        tBA2.setStartTime(31150);
        tBA2.setEndTime(32150);

        // blocco2
        v2.setNextTrip(tCB3);

        tCB3.setVehicle(v2);
        tCB3.setPreviousStandstill(v2);
        tCB3.setNextTrip(tBC4);
        tCB3.setStartTime(30050);
        tCB3.setEndTime(31050);

        tBC4.setVehicle(v2);
        tBC4.setPreviousStandstill(tCB3);
        tBC4.setNextTrip(null);
        tBC4.setStartTime(31150);
        tBC4.setEndTime(32150);


        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tAB1).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tBA2).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tCB3).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tBC4).penalizesBy(0L);

        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tAB1).penalizesBy(150L);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tBA2).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tCB3).penalizesBy(100L);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tBC4).penalizesBy(0L);


        // blocco1
        v1.setNextTrip(tAB1);

        tAB1.setVehicle(v1);
        tAB1.setPreviousStandstill(v1);
        tAB1.setNextTrip(tBA2);
        tAB1.setStartTime(30000);
        tAB1.setEndTime(30100);

        tBA2.setVehicle(v1);
        tBA2.setPreviousStandstill(tAB1);
        tBA2.setNextTrip(null);
        tBA2.setStartTime(30200);
        tBA2.setEndTime(30300);

        // blocco2
        v2.setNextTrip(tCB3);

        tCB3.setVehicle(v2);
        tCB3.setPreviousStandstill(v2);
        tCB3.setNextTrip(tBC4);
        tCB3.setStartTime(30050);
        tCB3.setEndTime(30150);

        tBC4.setVehicle(v2);
        tBC4.setPreviousStandstill(tCB3);
        tBC4.setNextTrip(null);
        tBC4.setStartTime(30100);
        tBC4.setEndTime(30200);

        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tAB1).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tBA2).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tCB3).penalizesBy(1L);
        constraintVerifier.verifyThat(VspConstraintProvider::isCompatibleWithNextStandstill).given(tBC4).penalizesBy(0L);

        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tAB1).penalizesBy(100L);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tBA2).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tCB3).penalizesBy(0L);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(tBC4).penalizesBy(0L);

    }

    // @Test
    // void endNodeEqualNextStartNode() {

    // Node node1 = new Node("node-1");
    // Node node2 = new Node("node-2");

    // // nodo-1 --> nodo-1 penalize 1
    // Direction dir1 = new Direction("lineName", "directionType", node1, node2,
    // null, null);
    // Trip trip1 = new Trip(1, "trip-1", dir1, 0, 10, 10, 1.0, null);
    // Trip trip2 = new Trip(2, "trip-2", dir1, 20, 30, 30, 1.0, null);

    // trip1.setNextTrip(trip2);
    // trip1.setPreviousStandstill(trip2);
    // constraintVerifier.verifyThat(VspConstraintProvider::endNodeEqualNextStartNode).given(trip1).penalizesBy(1L);

    // // nodo-1 --> nodo-2 penalize 0
    // Direction dir2 = new Direction("lineName", "directionType", node2, node1,
    // null, null);
    // trip2.setDirection(dir2);
    // trip1.setNextTrip(trip2);
    // trip1.setPreviousStandstill(trip2);
    // constraintVerifier.verifyThat(VspConstraintProvider::endNodeEqualNextStartNode).given(trip1).penalizesBy(0L);
    // }

    @Test
    void minStopTime() {

        Node node1 = new Node("node-1");
        Node node2 = new Node("node-2");

        // trip-1 --> trip-2 (0,10) --> (20,30) penalty 10
        Direction dir = new Direction("lineName", "directionType", node1, node2, null, null);
        Trip trip1 = new Trip(1, "trip-1", dir, 0, 10, 10, 1.0, null);
        Trip trip2 = new Trip(2, "trip-2", dir, 20, 30, 30, 1.0, null);

        trip1.setNextTrip(trip2);
        trip1.setPreviousStandstill(trip2);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(trip1).penalizesBy(10L);

        // trip-1 --> trip-2 (0,10) --> null no penalty
        trip1.setNextTrip(null);
        constraintVerifier.verifyThat(VspConstraintProvider::minStopTime).given(trip1).penalizesBy(0L);

    }

}
