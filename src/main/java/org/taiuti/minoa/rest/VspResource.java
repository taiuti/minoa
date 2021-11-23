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

package org.taiuti.minoa.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.taiuti.minoa.bootstrap.CsvDataBuilder;
import org.taiuti.minoa.bootstrap.JsonDataBuilder;
import org.taiuti.minoa.bootstrap.Utility;
import org.taiuti.minoa.model.Direction;
import org.taiuti.minoa.model.Headway;
import org.taiuti.minoa.model.Node;
import org.taiuti.minoa.model.Standstill;
import org.taiuti.minoa.model.TimeHorizon;
import org.taiuti.minoa.model.Trip;
import org.taiuti.minoa.model.Vehicle;
import org.taiuti.minoa.model.VspSolution;

import validator.validatorClasses.Activity;
import validator.validatorClasses.ActivityTrip;
import validator.validatorClasses.Input;
import validator.validatorClasses.JsonVehicleBlock;
import validator.validatorClasses.VehicleBlock;

@Path("vsp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VspResource {

    private static final Logger LOG = Logger.getLogger(VspResource.class);
    public static final Long PROBLEM_ID = 1L;

    @Inject
    SolverManager<VspSolution, Long> solverManager;
    @Inject
    ScoreManager<VspSolution, HardSoftScore> scoreManager;

    // To try, open http://localhost:8080/VspSolution
    @GET
    public VspSolution getVspSolution() {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = getSolverStatus();
        VspSolution solution = findById(PROBLEM_ID);
        scoreManager.updateScore(solution); // Sets the score
        solution.setSolverStatus(solverStatus);
        return solution;
    }

    @POST
    @Path("solve")
    public VspSolution solve() {

        // Submit the problem to start solving
        SolverJob<VspSolution, Long> solverJob = solverManager.solve(PROBLEM_ID, findById(PROBLEM_ID));
        VspSolution solution;
        try {
            // Wait until the solving ends
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }
        return solution;

    }

    @POST
    @Path("solveJson")
    public void solveJson(Input input) {

        VspSolution solution = JsonDataBuilder.builder().setInput(input).build();

        // Submit the problem to start solving
        SolverJob<VspSolution, Long> solverJob = solverManager.solve(PROBLEM_ID, solution);
        try {
            // Wait until the solving ends
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }

        check(solution);
    }

    @POST
    @Path("test")
    public void test(Input input) {

        TimeHorizon th = new TimeHorizon(new int[] { 30000, 40000, 50000, 60000 });

        // Headway
        List<Headway> hws = new ArrayList<Headway>();
        hws.add(new Headway(0, 150, 200));
        hws.add(new Headway(0, 150, 200));
        hws.add(new Headway(0, 150, 200));

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


        VspSolution solution = new VspSolution(trips, vehicles);        // Submit the problem to start solving
        SolverJob<VspSolution, Long> solverJob = solverManager.solve(PROBLEM_ID, solution);
        try {
            // Wait until the solving ends
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }

        check(solution);
    }

    @POST
    @Path("solveCsv")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void solveCsv(String input) {

        VspSolution solution = CsvDataBuilder.builder().setInput(input).build();

        // Submit the problem to start solving
        SolverJob<VspSolution, Long> solverJob = solverManager.solve(PROBLEM_ID, solution);
        try {
            // Wait until the solving ends
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }

        check(solution);
    }

    @POST
    @Path("checkInput")
    public void checkInput(Input input) {

        // initialize tripHashMap
        VspSolution solution = JsonDataBuilder.builder().setInput(input).build();

        // map the tripId with the corresponding trip
        Map<Integer, Trip> tripHashMap = solution.getTripList().stream()
                .collect(Collectors.toMap(Trip::getId, trip -> trip));

        for (JsonVehicleBlock block : input.getVehicleBlockList()) {

            // parse the json object in a vehicle block object
            VehicleBlock vehicleBlock = block.getVehicleBlock();

            Trip lastTrip = null;
            // for every activity in the selected vehicle block
            for (Activity activity : vehicleBlock.getActivityList()) {

                // if the activity is a trip the residual autonomy will decrease
                if (activity.getClass() == ActivityTrip.class) {
                    // get information about the selected trip
                    int tripId = ((ActivityTrip) activity).getTripId();
                    Trip tmpTrip = tripHashMap.get(tripId);

                    if (lastTrip != null) {
                        if ((lastTrip.getEndTime() > tmpTrip.getStartTime())
                                || (!lastTrip.getEndNode().equals(tmpTrip.getStartNode()))) {
                            LOG.info(lastTrip.toString());
                            LOG.info(tmpTrip.toString());
                        }
                        LOG.info(lastTrip.getEndNode().getName() + "/" + tmpTrip.getStartNode().getName());
                    }
                    lastTrip = tmpTrip;
                }

            }
        }

    }

    public SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(PROBLEM_ID);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }

    protected VspSolution findById(Long id) {

        return JsonDataBuilder.builder().build();

    }

    protected void check(VspSolution solution) {

        for (Vehicle v : solution.getVehicleList()) {

            Standstill t = v;
            Trip lastTrip = null;
            while (t.getNextTrip() != null) {

                t = t.getNextTrip();
                if (lastTrip != null) {
                    if ((((Trip) lastTrip).getEndTime() > ((Trip) t).getStartTime())
                            || (!lastTrip.getEndNode().equals(((Trip) t).getStartNode()))) {

                        LOG.info(lastTrip.toString());
                        LOG.info(t.toString());
                        LOG.info(lastTrip.getEndNode().getName() + "/" + ((Trip) t).getStartNode().getName());
                    }
                }
                lastTrip = (Trip) t;
            }
        }

    }

    @POST
    @Path("analizeSolution")
    public void analizeSolution(Input input) {

        Utility.analizeSolution(input);

    }

}
