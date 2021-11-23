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
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.solver.SolverManager;
import org.taiuti.minoa.model.Direction;
import org.taiuti.minoa.model.Headway;
import org.taiuti.minoa.model.Node;
import org.taiuti.minoa.model.TimeHorizon;
import org.taiuti.minoa.model.Trip;
import org.taiuti.minoa.model.Vehicle;
import org.taiuti.minoa.model.VspSolution;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SolverManagerTest {

    @Inject
    SolverManager<VspSolution, Long> solverManager;

    @Test
    void solve() throws ExecutionException, InterruptedException {

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


        VspSolution solution = new VspSolution(trips, vehicles);
        solverManager.solve(1L, id -> solution, SolverManagerTest::printSolution).getFinalBestSolution();
    }

    static void printSolution(VspSolution solution) {

        // verifica soluzione
        VspSolution s = solution;
    }
}
