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

package org.taiuti.minoa.domain;

import org.junit.jupiter.api.Test;
import org.taiuti.minoa.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TripTest {

    @Test
    void compatibility() {
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

        // test tAB1 incompatibil con se stesso
        assertFalse(tAB1.isCompatible(tAB1), "Trip incompatibile con se stesso");

        // test tAB1 e tBA2 incompatibili per tempo limite inferione alla sosta minima
        tBA2.setStartTime(tAB1.getEndTime() + hws.get(0).getMinHeadway() - 1);
        assertFalse(tAB1.isCompatible(tBA2), "tAB1 e tBA2 incompatibili per tempo limite inferione alla sosta minima");
        assertFalse(tBA2.isCompatibleWithPreviousStandstill(),
                "tAB1 e tBA2 incompatibili per tempo limite inferione alla sosta minima");

        // test tAB1 e tBA2 incompatibili per tempo limite maggiore della sosta massima
        tBA2.setStartTime(tAB1.getEndTime() + hws.get(0).getMaxHeadway() + 1);
        assertFalse(tAB1.isCompatible(tBA2), "tAB1 e tBA2 incompatibili per tempo limite maggiore della sosta massima");
        assertFalse(tBA2.isCompatibleWithPreviousStandstill(),
                "tAB1 e tBA2 incompatibili per tempo limite inferione alla sosta minima");

        // test tAB1 e tBA2 incompatibili per stessa direction
        Trip tBA3 = new Trip(3, "trip-3", dirAB, 28000, 28100, 28100, 1.0, null);
        tBA3.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway());
        tBA3.setPreviousStandstill(tAB1);
        assertFalse(tAB1.isCompatible(tBA3), "tAB1 e tBA2 incompatibili per stessa direction");
        assertFalse(tBA3.isCompatibleWithPreviousStandstill(),
                "tAB1 e tBA2 incompatibili per tempo limite inferione alla sosta minima");

        // test tAB1 e tBA2 compatibili sosta compresa tra sosta minima e max
        tBA2.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway());

        assertTrue(tAB1.isCompatible(tBA2), "Compatibile");
        assertFalse(tBA2.isCompatibleWithPreviousStandstill(), "Compatibile");
        tBA2.setPreviousStandstill(tAB1);

        assertTrue(tBA2.isCompatibleWithPreviousStandstill(), "Compatibile");

        // delay
        tBA2.setStartTime(tAB1.getEndTime());
        assertEquals(tAB1.delay(tBA2), Optional.of(0), "delay");

        // isCompatible
        tBA2.setStartTime(tAB1.getEndTime());
        assertFalse(tAB1.isCompatible(tBA2), "isCompatible");
        assertFalse(tBA2.isCompatibleWithPreviousStandstill(),
                "tAB1 e tBA2 incompatibili per tempo limite inferione alla sosta minima");

        tBA2.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway());
        assertTrue(tAB1.isCompatible(tBA2), "isCompatible");
        assertTrue(tBA2.isCompatibleWithPreviousStandstill(),
                "tAB1 e tBA2 incompatibili per tempo limite inferione alla sosta minima");

        tBC1.setStartTime(tAB1.getEndTime());
        assertFalse(tAB1.isCompatible(tBC1), "isCompatible");
        assertFalse(tBC1.isCompatibleWithPreviousStandstill());

        tBC1.setStartTime(tAB1.getEndTime() + hws.get(0).getIdealHeadway());
        assertTrue(tAB1.isCompatible(tBC1), "isCompatible");
        assertTrue(tBC1.isCompatibleWithPreviousStandstill(), "isCompatible");

    }

    @Test
    void isInterchangeable() {
        // TimeHorizon
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
         * Situazione iniziale A(30000)->B(31000) [100,150,200] B(31150)->A(32150)
         * C(30050)->B(31050) [100,150,200] B(31200)->C(32200)
         * 
         * Situazione finale A(30000)->B(31000) [100,150,200] B(31200)->C(32200)
         * C(30050)->B(31050) [100,150,200] B(31150)->A(32150)
         */

        // blocco1
        v1.setNextTrip(tAB1);

        tAB1.setVehicle(v1);
        tAB1.setPreviousStandstill(v1);
        tAB1.setNextTrip(tBA2);
        tAB1.setStartTime(30000);
        tAB1.setEndTime(tAB1.getStartTime() + 1000);

        tBA2.setVehicle(v1);
        tBA2.setPreviousStandstill(tAB1);
        tBA2.setNextTrip(null);
        tBA2.setStartTime(tAB1.getEndTime() + tAB1.getHeadway().getIdealHeadway());
        tBA2.setEndTime(tBA2.getStartTime() + 1000);

        // blocco2
        v2.setNextTrip(tCB3);

        tCB3.setVehicle(v2);
        tCB3.setPreviousStandstill(v2);
        tCB3.setNextTrip(tBC4);
        tCB3.setStartTime(tAB1.getStartTime() + 50);
        tCB3.setEndTime(tCB3.getStartTime() + 1000);

        tBC4.setVehicle(v2);
        tBC4.setPreviousStandstill(tCB3);
        tBC4.setNextTrip(null);
        tBC4.setStartTime(31200);
        tBC4.setEndTime(tBC4.getStartTime() + 1000);

        assertFalse(tAB1.isInterchangeable(tBA2), "isInterchangeable");
        assertFalse(tAB1.isInterchangeable(tBC4), "isInterchangeable");
        assertTrue(tAB1.isInterchangeable(tAB1), "isInterchangeable");
        assertTrue(tAB1.isInterchangeable(tCB3), "isInterchangeable");

        assertTrue(tBA2.isInterchangeable(tBA2), "isInterchangeable");
        assertTrue(tBA2.isInterchangeable(tBC4), "isInterchangeable");
        assertFalse(tBA2.isInterchangeable(tAB1), "isInterchangeable");
        assertFalse(tBA2.isInterchangeable(tCB3), "isInterchangeable");

        assertFalse(tCB3.isInterchangeable(tBA2), "isInterchangeable");
        assertFalse(tCB3.isInterchangeable(tBC4), "isInterchangeable");
        assertTrue(tCB3.isInterchangeable(tAB1), "isInterchangeable");
        assertTrue(tCB3.isInterchangeable(tCB3), "isInterchangeable");

        assertTrue(tBC4.isInterchangeable(tBC4), "isInterchangeable");
        assertTrue(tBC4.isInterchangeable(tBA2), "isInterchangeable");
        assertFalse(tBC4.isInterchangeable(tAB1), "isInterchangeable");
        assertFalse(tBC4.isInterchangeable(tCB3), "isInterchangeable");

        /*
         * Situazione iniziale A(30000)->B(31000) [100,150,200] B(31150)->A(32150)
         * C(30050)->B(31051) [100,150,200] B(31201)->C(32201)
         * 
         * Situazione finale A(30000)->B(31000) [100,150,200] B(31201)->C(32201)
         * C(30050)->B(31050) [100,150,200] B(31150)->A(32150)
         */
        tBC4.setStartTime(31201);
        tBC4.setEndTime(tBC4.getStartTime() + 1000);

        assertFalse(tAB1.isInterchangeable(tBA2), "isInterchangeable");
        assertFalse(tAB1.isInterchangeable(tBC4), "isInterchangeable");
        assertTrue(tAB1.isInterchangeable(tAB1), "isInterchangeable");
        assertTrue(tAB1.isInterchangeable(tCB3), "isInterchangeable");

        assertTrue(tBA2.isInterchangeable(tBA2), "isInterchangeable");
        assertFalse(tBA2.isInterchangeable(tBC4), "isInterchangeable");
        assertFalse(tBA2.isInterchangeable(tAB1), "isInterchangeable");
        assertFalse(tBA2.isInterchangeable(tCB3), "isInterchangeable");

        assertFalse(tCB3.isInterchangeable(tBA2), "isInterchangeable");
        assertFalse(tCB3.isInterchangeable(tBC4), "isInterchangeable");
        assertTrue(tCB3.isInterchangeable(tAB1), "isInterchangeable");
        assertTrue(tCB3.isInterchangeable(tCB3), "isInterchangeable");

        assertTrue(tBC4.isInterchangeable(tBC4), "isInterchangeable");
        assertFalse(tBC4.isInterchangeable(tBA2), "isInterchangeable");
        assertFalse(tBC4.isInterchangeable(tAB1), "isInterchangeable");
        assertFalse(tBC4.isInterchangeable(tCB3), "isInterchangeable");

    }
}
