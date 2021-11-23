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
import java.util.List;

import org.taiuti.minoa.model.Node;
import org.taiuti.minoa.model.Trip;
import org.taiuti.minoa.model.Vehicle;
import org.taiuti.minoa.model.VspSolution;

public class CsvDataBuilder {

    private static final String COMMA_DELIMITER = ",";
    private String input;

    private CsvDataBuilder() {
    }

    public static CsvDataBuilder builder() {
        return new CsvDataBuilder();
    }

    public CsvDataBuilder setInput(String input) {

        this.input = input;
        return this;

    }

    public VspSolution build() {

        List<Trip> tripList = new ArrayList<>();

         String[] rows = input.split("/");

        if (rows.length > 1) {

            // recupero nodi
            String[] nodi = rows[0].split(COMMA_DELIMITER);
            Node[] nodes = new Node[rows.length];

            for (int i = 0; i < nodi.length; i++) {
                nodes[i] = new Node(nodi[i]);
            }

            for (int i = 1; i < rows.length; i++) {

                // accessing each element of array
                String[] trips = rows[i].split(COMMA_DELIMITER);

                for (int j = 0; j < trips.length / 2; j++) {
                    // Trip trip = new Trip(i * 10 + j * 1L, "Trip" + i * 10 + j, nodes[j * 2], nodes[j * 2 + 1],
                    //         Long.parseLong(trips[j * 2]), Long.parseLong(trips[j * 2 + 1]));
                    // tripList.add(trip);
                }

            }
        }

        List<Vehicle> vehicleList = new ArrayList<Vehicle>();
        for (int i = 0; i < 8; i++) {
            Vehicle vehicle = new Vehicle(i, "vehicle-" + i);
            vehicleList.add(vehicle);
        }

        return new VspSolution(tripList, vehicleList);
    }
}
