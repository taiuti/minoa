package org.taiuti.minoa.bootstrap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.taiuti.minoa.model.Trip;
import org.taiuti.minoa.model.Vehicle;
import org.taiuti.minoa.model.VspSolution;

import validator.validatorClasses.ActivityTrip;
import validator.validatorClasses.Direction;
import validator.validatorClasses.Input;
import validator.validatorClasses.JsonDirection;
import validator.validatorClasses.JsonVehicleBlock;

public class Utility {

    private static final Logger LOG = Logger.getLogger(Utility.class);

    public static Input toInput(VspSolution solution) {

        return null;

    }

    public static VspSolution toVspSolution(Input input) {

        return null;

    }

    public static void analizeSolution(Input outputFile) {

        Set<Integer> in = new HashSet<>();
        Set<Integer> out = new HashSet<>();

        for (JsonDirection jdirection : outputFile.getDirections()) {
            Direction direction = jdirection.getDirection();

            Set<Integer> trips = direction.getTrips().stream().map(t -> t.getTrip().getTripId())
                    .collect(Collectors.toSet());

            in.addAll(trips);

        }

        for (JsonVehicleBlock block : outputFile.getVehicleBlockList()) {

            Set<Integer> trips = block.getVehicleBlock().getActivityList().stream()
                    .filter(t -> t.getClass().getSimpleName().contains("ActivityTrip"))
                    .map(t -> ((ActivityTrip) t).getTripId()).collect(Collectors.toSet());

            out.addAll(trips);

        }

        boolean t1 = in.containsAll(out);

        LOG.info("in.containsAll(out):" + t1);
        boolean t2 = out.containsAll(in);
        LOG.info("out.containsAll(in):" + t2);

    }

    public static VspSolution restrictedFirstInFirstOut(List<Trip> tripList) {

        Comparator<Trip> tripStartTimeComparator = Comparator.comparing(Trip::getStartTime);
        Comparator<Trip> tripEndTimeComparator = Comparator.comparing(Trip::getEndTime);

        Comparator<Trip> multipleFieldsComparator = tripStartTimeComparator.thenComparing(tripEndTimeComparator);

        List<Trip> orderedList = tripList.stream().sorted(multipleFieldsComparator).collect(Collectors.toList());

        // try {
        //     printOrderedTrips(orderedList);
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        // Vehicle
        List<Vehicle> vehicleList = new ArrayList<>();

        int i = 0;
        Optional<Trip> tripFromDepot;
        do {
            i++;

            tripFromDepot = orderedList.stream().filter(t -> (t.getVehicle() == null)).findFirst();

            if (!tripFromDepot.isEmpty()) {

                Vehicle vehicle = new Vehicle(i, "vehicle-" + i);

                vehicleList.add(vehicle);

                vehicle.setNextTrip(tripFromDepot.get());
                tripFromDepot.get().setPreviousStandstill(vehicle);
                tripFromDepot.get().setVehicle(vehicle);

                Trip previousTrip = tripFromDepot.get();
                Optional<Trip> nextTrip;
                do {

                    nextTrip = getFirstCompatibleTrip(previousTrip, orderedList);

                    if (!nextTrip.isEmpty()) {
                        previousTrip.setNextTrip(nextTrip.get());
                        nextTrip.get().setPreviousStandstill(previousTrip);
                        nextTrip.get().setVehicle(vehicle);

                        previousTrip = nextTrip.get();
                    }

                } while (!nextTrip.isEmpty());

            }
        } while (!tripFromDepot.isEmpty());

        return new VspSolution(orderedList, vehicleList);

    }

    public static Optional<Trip> getFirstCompatibleTrip(Trip trip, List<Trip> tripList) {

        Comparator<Trip> tripDelayComparator = new Comparator<Trip>() {
            @Override
            public int compare(Trip i1, Trip i2) {
                return i1.delay(i2).orElse(1);
            }
        };

        Optional<Trip> tripFromDepot = tripList.stream()
                .filter(t -> ((t.getVehicle() == null) && (trip.isCompatible(t)))).sorted(tripDelayComparator)
                .findFirst();

        return tripFromDepot;
    }

    // public static void printOrderedTrips(List<Trip> tripList) throws IOException {

    //     Comparator<Trip> tripStartTimeComparator = Comparator.comparing(Trip::getStartTime);
    //     Comparator<Trip> tripEndTimeComparator = Comparator.comparing(Trip::getEndTime);

    //     Comparator<Trip> multipleFieldsComparator = tripStartTimeComparator.thenComparing(tripEndTimeComparator);

    //     List<Trip> orderedList = tripList.stream().sorted(multipleFieldsComparator).collect(Collectors.toList());

    //     String fileName = "/home/marco/tmp/minoa.csv";
    //     BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
    //     writer.append("From,To,Start,End\r\n");

    //     for (Trip trip : orderedList) {

    //         writer.append(
    //                 trip.getDirection().getStartNode().getName() + "," + trip.getDirection().getEndNode().getName()
    //                         + "," + trip.getStartTime() + "," + trip.getEndTime() + "\r\n");

    //     }

    //     writer.close();

    // }
}
