package validator;

import validator.validatorClasses.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import java.util.*;

public class TTCost {
    private static Input outputFile = null;
    private static Input inputFile = null;
    public static boolean isAdmissible;

    public static double start(File inputFileSrc, File outputFileSrc) {
    	isAdmissible = false;
        ObjectMapper mapper = new ObjectMapper();

        double ttCost = 0d;
        
        if(outputFileSrc != null && inputFileSrc != null){
            try {
                
            	outputFile = mapper.readValue(outputFileSrc, Input.class);
                inputFile = mapper.readValue(inputFileSrc, Input.class);
                
                String category = getCategory();
                switch (category){
                    case "junior" :
                    	isAdmissible = isJuniorAdmissible();
                        System.out.println("Is feasible: " + isAdmissible + "\n\n");
                        break;

                    case "senior" :
                    	isAdmissible = isSeniorAdmissible();
                        System.out.println("Is feasible: " + isAdmissible + "\n\n");
                        break;

                    case "professional" :
                        isAdmissible = isProfessionalAdmissible();
                        System.out.println("Is feasible: " +  isAdmissible);
                        if(isAdmissible) {
                        	ttCost = computeTTCost();
                            System.out.println("TT cost: " + ttCost + "\n\n");
                            
                        }
                        else {
                        	System.out.println("Solution wasn't feasible!\n");
                        }
                }
            } catch (IOException e) {
            	System.out.println("Exception occurred: " + e.getMessage());
            }
        }
        return ttCost;
    }

    
    /**
     * method that compute the TT cost of the solution
     * @return 
     * @throws IOException
     */
    private static double computeTTCost() throws IOException {
    	
    	//the cost of a single direction
        double directionTTCost;
        //the total cost of the TT
        double totalTTCost = 0;
        
        //for every direction in the list of directions in the output file
        for(JsonDirection direction : outputFile.getDirections()){
            System.out.println("Direction: " + direction.getDirection().getLineName() + "\tType: " + direction.getDirection().getDirectionType() + "\n");
            
            //the number of achieved headway different from ideal headway
            int nonIdealHeadway = 0;
            directionTTCost = 0;
            //the list of trips in the direction
            List<JsonTrip> jsonTrips = direction.getDirection().getTrips();

            //for each couple of trips in the direction
            for (int i = 0; i < jsonTrips.size()-1; i++){
            	//the achieved headway is the difference between the main stop arrival time of two consecutive trips
                int achievedHeadway = (int)(jsonTrips.get(i + 1).getTrip().getMainStopArrivalTime() - jsonTrips.get(i).getTrip().getMainStopArrivalTime());
                int idealHeadway = achievedHeadway;

                //the time timeWindow where the trips belongs
                int firstTripTimeHorizonIndex = tripTimeHorizonIndex(jsonTrips.get(i));
                int secondTripTimeHorizonIndex = tripTimeHorizonIndex(jsonTrips.get(i+1));

                //if both trips have their main stop arrival time in the same timeWindow the ideal headway is the one write in the json file
                if(firstTripTimeHorizonIndex == secondTripTimeHorizonIndex && firstTripTimeHorizonIndex != -1){
                    idealHeadway = direction.getDirection().getHeadways().get(firstTripTimeHorizonIndex).getHeadway().getIdealHeadway();
                }
                //if the two consecutive trips have their main stop arrival times in different time window the ideal headway is the convex combination
                //between the two adjacent ideals headway of the direction 
                else if(firstTripTimeHorizonIndex != -1 && secondTripTimeHorizonIndex != -1){
                    double firstTripConv = ((double)outputFile.getTimeHorizon()[firstTripTimeHorizonIndex+1] - (double)jsonTrips.get(i).getTrip().getMainStopArrivalTime())/(double) achievedHeadway;
                    double firstTripIdealHeadway = direction.getDirection().getHeadways().get(firstTripTimeHorizonIndex).getHeadway().getIdealHeadway();

                    double secondTripConv = ((double)jsonTrips.get(i+1).getTrip().getMainStopArrivalTime() - (double) outputFile.getTimeHorizon()[secondTripTimeHorizonIndex])/(double) achievedHeadway;
                    double secondTripIdealHeadway = direction.getDirection().getHeadways().get(secondTripTimeHorizonIndex).getHeadway().getIdealHeadway();

                    double value = (firstTripConv*firstTripIdealHeadway) + (secondTripConv*secondTripIdealHeadway);

                    idealHeadway = (int) Math.round(value);
                    }
                //the method tripTimeHorizonIndex(...) returned -1 therefore at least one trip have a wrong main stop arrival time
                else{
                	System.out.println("trip " + jsonTrips.get(i) + " has mainStopArrival time at" + jsonTrips.get(i).getTrip().getMainStopArrivalTime() + " therefore in index " + firstTripTimeHorizonIndex +
                			" in the timeHorizon\n" + 
                			"trip " + jsonTrips.get(i+1) + " has mainStopArrival time at" + jsonTrips.get(i).getTrip().getMainStopArrivalTime() + " therefore in index " + firstTripTimeHorizonIndex +
                			" in the timeHorizon");
                    return 0d;
                }
                
                //error != 0 if and only the achieved headway is different from the ideal headway
                double error = (double)abs(achievedHeadway - idealHeadway) / (double) idealHeadway;

                if(error != 0)
                    nonIdealHeadway ++;

                //increase the cost of the direction
                directionTTCost = directionTTCost + directionCost(error);
                System.out.println("IdealHeadway:\t" + idealHeadway/60 + " (" + idealHeadway + " sec)" + "\tAchievedHeadway: " + achievedHeadway/60 +" (" + achievedHeadway + " sec)");
            }

            System.out.println("TT cost of the direction:\t" + directionTTCost + "\nNon ideal headways in the direction\\Total headways:\t" + nonIdealHeadway +  "\\" + (jsonTrips.size()-1) + "\n\n");
            //increase the total cost of the TT
            totalTTCost = totalTTCost + directionTTCost;
        }

        return totalTTCost;
    }

    //method that compute the index of the time horizon where a trip belongs
    private static int tripTimeHorizonIndex(JsonTrip trip){
    	//for every index in the timeHorizon
        for (int i = 1; i < outputFile.getTimeHorizon().length; i++) {
        	//if timeHorizon[i-1] <= trip.mainStopArrivalTime <= timeHorizon[i] then the trip belongs to timeHorizon[i-1] 
            if(trip.getTrip().getMainStopArrivalTime() <= outputFile.getTimeHorizon()[i] && trip.getTrip().getMainStopArrivalTime() >= outputFile.getTimeHorizon()[i-1])
                return i-1;
        }

        System.out.println("trip " + trip.getTrip().getTripId() + " main stop arrival time does not belong to the time horizon, impossible compute directions TT cost ");

        return -1;
    }

    //given the error between the achieved headway and the ideal headway this method return the cost of the violation
    private static double directionCost(double error){
    	//error = 0 if and only if the achieved headway between two consecutive trips is exactly the ideal headway
        if(error == 0)
            return 0;

        double alpha0 = outputFile.getGlobalCost().getAlpha0();
        double alpha1 = outputFile.getGlobalCost().getAlpha1();
        double alpha2 = outputFile.getGlobalCost().getAlpha2();

        //otherwise the cost for an achieved headway not equal to the ideal headway is given with the formula below
        return alpha2*(error*error) + alpha1*error + alpha0;
    }

    //return the absolute value of n
    private static int abs(int n){
        if(n < 0)
            return -n;
        return n;
    }

    //class that implement the quick sort algorithm
    private static class  QuickSort {
        private static int partition(int[] arr, int low, int high) {
            int pivot = arr[high];
            int index = (low - 1);
            for (int j = low; j < high; j++) {
                if (arr[j] < pivot) {
                    index++;
                    int temp = arr[index];
                    arr[index] = arr[j];
                    arr[j] = temp;
                }
            }
            int temp = arr[index + 1];
            arr[index + 1] = arr[high];
            arr[high] = temp;
            return index + 1;
        }

        public static void sort(int[] arr, int low, int high) {
            if (low < high) {
                int pi = partition(arr, low, high);
                sort(arr, low, pi - 1);
                sort(arr, pi + 1, high);
            }
        }
    }

    //method that return true if an integer n belongs to an array a, false otherwise
    private static boolean binarySearch(int[] a, int n, int sx, int dx){
        if(dx >= sx){
            int mid = sx + ((dx-sx)/2);
            if(a[mid] == n)
                return true;
            else if(a[mid] > n)
                return binarySearch(a, n, sx, mid - 1);
            else
                return binarySearch(a, n, mid + 1, dx);
        }
        return false;
    }

    //method that return the position of an integer n if it belongs to an array a, -1 otherwise
    private static int positionBinarySearch(int[] a, int n, int sx, int dx){
        if(dx >= sx){
            int mid = sx + ((dx-sx)/2);
            if(a[mid] == n)
                return mid;
            else if(a[mid] > n)
                return positionBinarySearch(a, n, sx, mid - 1);
            else
                return positionBinarySearch(a, n, mid + 1, dx);
        }
        return -1;
    }

    //given a direction this method return true if and only if the trips of the direction in the output file are a 
    //subset of the ones in the input file
    private static boolean inputOutputSameTrip(Direction direction){
        if(direction == null)
            throw new NullPointerException();

        //array that contains the trip ids of the direction given from the output.json file
        int[] directionTrip = new int[direction.getTrips().size()];

        int i = 0;
        for(JsonTrip trip: direction.getTrips()){
            directionTrip[i] = trip.getTrip().getTripId();
            i++;
        }
        
        //sort the array of trip ids
        QuickSort.sort(directionTrip, 0, directionTrip.length-1);

        //the array of trips ids of the direction given in input
        int[] inputFileDirectionTrip;
        for(JsonDirection inputDirection : inputFile.getDirections()){
        	//if inputDirection is the same as direction (the one given in the output file)
            if(inputDirection.getDirection().getStartNode().equalsIgnoreCase(direction.getStartNode()) && inputDirection.getDirection().getEndNode().equalsIgnoreCase(direction.getEndNode())){
                inputFileDirectionTrip = new int[inputDirection.getDirection().getTrips().size()];
                i = 0;
                //fill inputFileDirectionTrip with the trips id
                for(JsonTrip trip : inputDirection.getDirection().getTrips()){
                    inputFileDirectionTrip[i] = trip.getTrip().getTripId();
                    i++;
                }
                //sort the array of trip ids of the direction given by the input file
                QuickSort.sort(inputFileDirectionTrip, 0, inputFileDirectionTrip.length-1);

                int n;
                //for every trip id in the array that contains the trip of the direction given by the output file
                for (int j = 0; j < directionTrip.length; j++) {
                    n = directionTrip[j];
                    //if the trip with id = n isn't in the array that contains all the potential trip for the direction
                    //the solution isn't admissible
                    if (!binarySearch(inputFileDirectionTrip, n, 0, inputFileDirectionTrip.length - 1)){
                        System.out.println("Trip " + n + " isn't in the input file!\n");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //method that return true if and only if every trip of every direction is been performed by exactly one vehicle block
    private static boolean performedTrip(){
    	//the number of total trips
        int count = 0;
        
        for(JsonDirection jsonDirection : outputFile.getDirections()){
            count += jsonDirection.getDirection().getTrips().size();
        }
        
        //array that contains all the trip ids given in the solution
        int[] tripPerformed = new int[count];
        //array that associate at every trip the number of times is been performed
        int[] howManyTimes = new int[count];

        count -= 1;
        //initialize the array that contains all the trip ids
        for(JsonDirection jsonDirection : outputFile.getDirections()){
            for (int i = 0; i < jsonDirection.getDirection().getTrips().size(); i++) {
                tripPerformed[count-i] = jsonDirection.getDirection().getTrips().get(i).getTrip().getTripId();
                howManyTimes[count-i] = 0;
            }
            count -= jsonDirection.getDirection().getTrips().size();
        }

        //sort the array
        QuickSort.sort(tripPerformed, 0, tripPerformed.length-1);
        
        //for every vehicle block in the output file
        for(JsonVehicleBlock jsonVehicleBlock : outputFile.getVehicleBlockList()){
        	//for every activity of the selected vehicle block
            for(Activity activity : jsonVehicleBlock.getVehicleBlock().getActivityList()){
            	//if the activity is a trip
                if(activity.getClass() == ActivityTrip.class){
                	//increase the number of times the trip was performed
                    howManyTimes[positionBinarySearch(tripPerformed, ((ActivityTrip) activity).getTripId(), 0, tripPerformed.length-1)] += 1;
                }
            }
        }

        for (int howManyTime : howManyTimes){
            if (howManyTime > 1){
                System.out.println("Trip " + tripPerformed[howManyTime] + " was performed by more than one vehicle block\n");
                return false;
            }
            else if(howManyTime < 1){
                System.out.println("Trip " + tripPerformed[howManyTime] + " was not performed by any vehicle block\n");
                return false;
            }
        }

        return true;
    }

    /*
    possible couples:
        deadhead - trip
        deadhead - break
        deadhead - deadhead
        trip - deadhead (Ok)
        trip - break (Ok)
        trip - trip (Ok)
        break - trip
        break - deadhead
        break - break
     */
    private static boolean compatible(Activity activity1, Activity activity2, Map<Direction, List<Trip>> tripDirectionMap, Map<Integer, Trip> tripHashMap){

        //trip - ...
        if(activity1.getClass() == ActivityTrip.class){

            //trip - trip
            if(activity2.getClass() == ActivityTrip.class){
                String endNodeTrip1 = "", startNodeTrip2 = "";
                long endTimeTrip1 = -1, startTimeTrip2 = -1;

                //looking for each direction information about the trips
                for(Direction direction : tripDirectionMap.keySet()){
                    //if in this direction there is the trip equals the first one of the couple in the vehicle block
                	if(tripDirectionMap.get(direction).contains(tripHashMap.get(((ActivityTrip) activity1).getTripId()))){
                        endNodeTrip1 = direction.getEndNode();
                        endTimeTrip1 = tripHashMap.get(((ActivityTrip) activity1).getTripId()).getEndTime();
                    }
                    //if in this direction there is the trip equals the second one of the couple in the vehicle block
                    if(tripDirectionMap.get(direction).contains(tripHashMap.get(((ActivityTrip) activity2).getTripId()))){
                        startNodeTrip2 = direction.getStartNode();
                        startTimeTrip2 = tripHashMap.get(((ActivityTrip) activity2).getTripId()).getStartTime();
                    }
                }

                //if I couldn't find any information about the trips
                if(endNodeTrip1 == null || startNodeTrip2 == null){
                    System.out.println("Node error with a the trip\n");
                    return false;
                }
                //if the second trip does not start in the same spot as the first trip end
                else if(!endNodeTrip1.equalsIgnoreCase(startNodeTrip2)) {
                    System.out.println("There is a couple of adjacent trip such that endNodeTrip1 != startNodeTrip2\t\n");
                    return false;
                }

                //if one of the trips does not belong at the time horizon
                if(!(endTimeTrip1 >= 0 && startTimeTrip2 >= 0)){
                    System.out.println("Time error with a trip\n");
                    return false;
                }
                //if the second trip does not start when the first one ends
                else if(!(endTimeTrip1 == startTimeTrip2)) {
                    System.out.println("endTimeTrip1 != startTimeTrip2\n");
                    return false;
                }

                return true;
            }

            //trip - break
            else if(activity2.getClass() == ActivityBreak.class){

            	//for each direction
                for(Direction direction : tripDirectionMap.keySet()){
                	//if the selected direction contains the trip with tripId equals the one I am looking at
                    if(tripDirectionMap.get(direction).contains(tripHashMap.get(((ActivityTrip) activity1).getTripId()))){
                        //if the end node of the trip is not the same as the start node of the break
                        if(!(direction.getEndNode().equalsIgnoreCase(activity2.getStartNode()))){
                            System.out.println("There is a couple (trip, break) such that endNodeTrip != startNodeBreak\n");
                            return false;
                        }
                        
                        //if the break does not start when the trip end
                        if(tripHashMap.get(((ActivityTrip) activity1).getTripId()).getEndTime() != ((ActivityBreak) activity2).getBreakActivities().get(0).getBreakActivity().getStartTime()){
                            System.out.println("There is a couple (trip, break) such that endTimeTrip != startTimeBreak\n");
                            return false;
                        }
                    }
                }

                return true;
            }

            //trip - deadhead
            else if(activity2.getClass() == Deadhead.class){
            	//for each direction
                for(Direction direction : tripDirectionMap.keySet()){
                	//if the selected direction contains the trip with tripId equals the one I am looking at
                    if(tripDirectionMap.get(direction).contains(tripHashMap.get(((ActivityTrip) activity1).getTripId()))){
                        //if the deadhead trip does not start when the trip ends
                        if(tripHashMap.get(((ActivityTrip) activity1).getTripId()).getEndTime() != activity2.getStartTime()){
                            System.out.println("There is a couple (trip, deadheadTrip) such that endTimeTrip != startTimeDeadhead\n");
                            return false;
                        }

                        //for every deadhead arc in the PTN
                        for(JsonDeadheadArc deadheadArc : inputFile.getDeadheadArcs()){
                            //if the deadhead arc I'm looking at have the same code as the one in the vehicle block
                            if(deadheadArc.getDeadheadArc().getDeadheadArcCode() == ((Deadhead) activity2).getDeadheadArcCode()){
                            	//if the deadhead does not start where the trip ends and the deadhead type isn't "pullIn"
                                if(!(deadheadArc.getDeadheadArc().getTerminalNode().equalsIgnoreCase(direction.getEndNode()) && deadheadArc.getDeadheadArc().getDeadheadType().equalsIgnoreCase("pullIn"))){
                                    System.out.println("There is a couple (trip, deadheadTrip) such that endNodeTrip != endNodeDeadhead || deadheadType != \"pullIn\"");
                                    return false;
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }

        //break - ...
        else if(activity1.getClass() == ActivityBreak.class){

            //break - trip
            if(activity2.getClass() == ActivityTrip.class){
            	//for every possible direction
                for(Direction direction : tripDirectionMap.keySet()){
                	//if the list of trips associated at the selected direction contains the information about the trip
                    if(tripDirectionMap.get(direction).contains(tripHashMap.get(((ActivityTrip) activity2).getTripId()))){
                    	//if the trip does not start where the break take place
                    	if(!(direction.getStartNode().equalsIgnoreCase(activity1.getEndNode()))){
                            System.out.println("There is a couple (break, trip) such that breakEndNode != tripStartNode\n");
                            return false;
                        }
                    	
                    	//if the trip does not start when the break activity finish
                        if(((ActivityBreak) activity1).getBreakActivities().get(((ActivityBreak) activity1).getBreakActivities().size()-1).getBreakActivity().getEndTime()
                                !=
                            tripHashMap.get(((ActivityTrip) activity2).getTripId()).getStartTime()){
                            System.out.println("There is a couple (break, trip) such that breakEndTime != tripStartTime\n");
                            return false;
                        }
                    }
                }

                return true;
            }

            //break - break 
            else if(activity2.getClass() == ActivityBreak.class){
            	//if the node where the second break take place isn't the same as the first one
                if(!activity1.getEndNode().equalsIgnoreCase(activity2.getStartNode())){
                    System.out.println("There is a couple (break, break) such that break1EndNode != break2StartNode\n");
                    return false;
                }

                //if the first break in the second break activity does not start when the last one of the first break activity ends
                if(((ActivityBreak) activity1).getBreakActivities().get(((ActivityBreak) activity1).getBreakActivities().size()-1).getBreakActivity().getEndTime()
                        !=
                    ((ActivityBreak) activity2).getBreakActivities().get(0).getBreakActivity().getStartTime()){
                    System.out.println("There is a couple (break, break) such that break1EndTime != break2StartTime\n");
                    return false;
                }

                return true;
            }

            //break - deadhead
            else if(activity2.getClass() == Deadhead.class){
            	//for every deadhead arc
                for(JsonDeadheadArc jsonDeadheadArc : inputFile.getDeadheadArcs()){
                	//if the selected deadhead arc have the same arcCode as the one in the vehicle block
                    if(jsonDeadheadArc.getDeadheadArc().getDeadheadArcCode() == ((Deadhead) activity2).getDeadheadArcCode()){
                    	//if the deadhead is a pull in, hence the vehicle is leaving the depot
                        if(jsonDeadheadArc.getDeadheadArc().getDeadheadType().equalsIgnoreCase("pullIn")){
                        	//is the break did not take place in the depot
                            if(!activity1.getEndNode().equalsIgnoreCase("dep")){
                                System.out.println("There is a couple (break, deadheadTrip) such that breakNode != \"dep\" ^ deadheadType = \"pullIn\"");
                                return false;
                            }
                        }
                        
                        //in this case the deadhead is a pull out, hence the vehicle is going to the depot
                        //if the break did not take place in the same node in the other terminal of the deadhead arc
                        else if (!jsonDeadheadArc.getDeadheadArc().getTerminalNode().equalsIgnoreCase(activity1.getEndNode())){
                            System.out.println("There is a couple (break, deadheadTrip) such that breakEndNode != deadheadTerminalNode\n");
                            return false;
                        }
                    }
                }

                //if the deadhead does not start when the break ends
                if(((ActivityBreak) activity1).getBreakActivities().get(((ActivityBreak) activity1).getBreakActivities().size()-1).getBreakActivity().getEndTime()
                        !=
                    activity2.getStartTime()){
                    System.out.println("There is a couple (break, deadheadTrip) such that breakEndTime != deadheadStartTime\n");
                    return false;
                }

                return true;
            }
        }

        //deadhead - ...
        else if(activity1.getClass() == Deadhead.class){
        	//since in the vehicle block there only is the deadheadArcCode and some information about times there is a need
        	//to store every information in a new deadheadArc, in this case I can search it just on time
            DeadheadArc deadheadArc = null;
            
            //initialize deadheadArc
            for(JsonDeadheadArc jsonDeadheadArc : inputFile.getDeadheadArcs()){
                if(((Deadhead) activity1).getDeadheadArcCode() == jsonDeadheadArc.getDeadheadArc().getDeadheadArcCode())
                    deadheadArc = jsonDeadheadArc.getDeadheadArc();
            }

            //deadhead - trip
            if(activity2.getClass() == ActivityTrip.class){
                for(Direction direction : tripDirectionMap.keySet()){
                    if(tripDirectionMap.get(direction).contains(tripHashMap.get(((ActivityTrip) activity2).getTripId()))){
                        //if deadheadArc is a pull out, hence the vehicle is leaving the depot
                    	//and the terminal node of the deadhead is the same as the start node of the trip
                        if(deadheadArc.getDeadheadType().equalsIgnoreCase("pullOut") && deadheadArc.getTerminalNode().equalsIgnoreCase(direction.getStartNode())){
                            return true;
                        }
                        
                        //if deadheadArc is a pull in, hence the vehicle is returning to the depot, and the terminal node of the deadhead
                    	//is the same as the end node of the trip
                        else if(deadheadArc.getDeadheadType().equalsIgnoreCase("pullIn") && deadheadArc.getTerminalNode().equalsIgnoreCase(direction.getEndNode())){
                            return true;
                        }
                    }
                }
                return false;
            }

            //deadhead - break
            else if(activity2.getClass() == ActivityBreak.class){
                //if the deadhead is a pull out, hence the vehicle is leaving the depot
            	//and the node where the break took place isn't the depot
                if(deadheadArc.getDeadheadType().equalsIgnoreCase("pullOut") && !activity2.getEndNode().equalsIgnoreCase("dep")){
                    System.out.println("There is a couple (deadheadTrip, break) such that deadhead type -> pullOut, break node != dep\n");
                    return false;
                }
                //the deadhead is a pull in, hence the vehicle is returning to the depot
                else if(deadheadArc.getDeadheadType().equalsIgnoreCase("pullIn")){
                    //if the node where the break take place isn't the same as the terminal node of the deadhead
                    if(!activity2.getStartNode().equalsIgnoreCase(deadheadArc.getTerminalNode())){
                        System.out.println("There is a couple (deadheadTrip, break) such that deadhead type -> pullIn, break node != deadhead terminal node\n");
                        return false;
                    }

                    //if the break does not start when the deadhead trip ends
                    if(activity1.getEndTime() != ((ActivityBreak) activity2).getBreakActivities().get(0).getBreakActivity().getStartTime()){
                        System.out.println("There is a couple (deadheadTrip, break) such that deadhead endTime != break startTime\n");
                        return false;
                    }
                }

                return true;
            }

            //deadhead - deadhead
            else if(activity2.getClass() == Deadhead.class){
                //looking upon all posible deadhead arcs
                for(JsonDeadheadArc jsonDeadheadArc : inputFile.getDeadheadArcs()){
                    //if the selected deadhead arc have the same code as the one in the vehicle block
                    if(jsonDeadheadArc.getDeadheadArc().getDeadheadArcCode() == ((Deadhead) activity2).getDeadheadArcCode()){
                        //if both deadhead arc have the same type (pullIn - pullIn or pullOut - pullOut)
                        if(deadheadArc.getDeadheadType().equalsIgnoreCase(jsonDeadheadArc.getDeadheadArc().getDeadheadType())){
                            System.out.println("There is a couple (deadheadTrip, deadheadTrip) that have the same type (pullIn - pullIn || pullOut - pullOut)\n");
                            return false;
                        }
                        else if(activity1.getEndTime() != activity2.getStartTime()){
                                System.out.println("There is a couple (deadheadTrip, deadheadTrip) such that deadhead1 endTime != deadhead2 startTime\n");
                                return false;
                            }
                        return true;
                    }
                }
            }
        }

        System.out.println("One activity have illegal type, activity class -> " + activity1.getClass().toString() + "other activity class -> " + activity2.getClass().toString());
        return false;
    }

    private static boolean initialFinalTrips(Direction direction){
        if(!direction.getTrips().get(0).getTrip().getIsInitialFinalTT().equalsIgnoreCase("initial")){
            System.out.println("First trip of the direction " + direction.getLineName() + " " + direction.getDirectionType() + " is not signed as \"intial\"\n");
            return false;
        }
        if(!direction.getTrips().get(direction.getTrips().size()-1).getTrip().getIsInitialFinalTT().equalsIgnoreCase("final")){
            System.out.println("Last trip of the direction " + direction.getLineName() + " " + direction.getDirectionType() + " is not signed as \"final\"\n");
            return false;
        }
        return true;
    }

    //method that return true if and only if the solution has feasible headways
    private static boolean headwayAdmissible(){
        List<JsonTrip> trips;
        
        //for every direction in the output file
        for(JsonDirection direction : outputFile.getDirections()){

        	//the list with all the trips
            trips = direction.getDirection().getTrips();
            int timeHorizonTrip1, timeHorizonTrip2;
            long actualheadway;

            //for every trip in the tripList of the direction
            for (int i = 1; i < trips.size(); i++) {
            	//index of the time horizon where the first trip belongs (aka the time window where the trip belongs)
                timeHorizonTrip1 = tripTimeHorizonIndex(trips.get(i-1));
            	//index of the time horizon where the second trip belongs (aka the time window where the trip belongs)
                timeHorizonTrip2 = tripTimeHorizonIndex(trips.get(i));
                //the achieved headway
                actualheadway = trips.get(i).getTrip().getMainStopArrivalTime() - trips.get(i-1).getTrip().getMainStopArrivalTime();

                //if both trips belong to the same time window
                if(timeHorizonTrip1 == timeHorizonTrip2){
                    //the achieved headway must be less than the max headway for that direction in the time window where the trips belongs
                    if(actualheadway > direction.getDirection().getHeadways().get(timeHorizonTrip1).getHeadway().getMaxHeadway()){
                        System.out.println("hHeadway between two consecutive trips grater than max headway\n" +
                                "First trip:\t" + trips.get(i-1).toString() + "\tsecond trip:\t" + trips.get(i).toString());
                        return false;
                    }
                    //for professional participants the achieved headway must be grater than min headway for that direction in the time window where the trips belongs
                    if(getCategory().equalsIgnoreCase("professional")){
                        if(actualheadway < direction.getDirection().getHeadways().get(timeHorizonTrip1).getHeadway().getMinHeadway()){
                            System.out.println("Headway between two consecutive trips less than minimum headway\n" +
                                        "First trip:\t" + trips.get(i-1).toString() + "\tsecond trip:\t" + trips.get(i).toString());
                            return false;
                        }
                    }
                }
                
                //the two trips does not belong to the same time window, hence their index in the time horizon aren't the same
                else{
                    //the achieved headway must be less than the max between the max headway of the two time windows
                	//achievedHeadway < max(maxHeadway(timeWindow1), maxHeadway(timeWindow2))
                    if(actualheadway > Math.max(direction.getDirection().getHeadways().get(timeHorizonTrip1).getHeadway().getMaxHeadway(), direction.getDirection().getHeadways().get(timeHorizonTrip2).getHeadway().getMaxHeadway())){
                        System.out.println("Headway between two consecutive trips grater than max headway\n" +
                                    "First trip:\t" + trips.get(i-1) + "\tsecond trip:\t" + trips.get(i));
                        return false;
                    }
                    //for professional participants the achieved headway must be grater than the max between the min headway of the two time windows
                    //achievedHeadway > max(minHeadway(timeWindow1), minHeadway(timeWindow2))
                    if(getCategory().equalsIgnoreCase("professional")){
                        if (actualheadway < Math.max(direction.getDirection().getHeadways().get(timeHorizonTrip1).getHeadway().getMinHeadway(), direction.getDirection().getHeadways().get(timeHorizonTrip2).getHeadway().getMinHeadway())){
                            System.out.println("Headway between two consecutive trips less than minimum headway\nFirst trip:\t" + trips.get(i - 1) + "\tsecond trip:\t" + trips.get(i));
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    //method that returns true if the output file contains an admissible solution, for the junior category of participants, for the problem in the input file 
    private static boolean isJuniorAdmissible(){
        Map<Direction, List<Trip>> tripDirectionHashMap = new HashMap<>();
        Map<Integer, Trip> tripHashMap = new HashMap<>();
        for(JsonDirection direction : outputFile.getDirections()){
            if(!inputOutputSameTrip(direction.getDirection()))
                return false;

            if(!initialFinalTrips(direction.getDirection()))
                return false;

            tripDirectionHashMap.put(direction.getDirection(), new ArrayList<Trip>());

            for (JsonTrip jsonTrip : direction.getDirection().getTrips()){
                tripHashMap.put(jsonTrip.getTrip().getTripId(), jsonTrip.getTrip());
                tripDirectionHashMap.get(direction.getDirection()).add(jsonTrip.getTrip());
            }
        }

        for(JsonVehicleBlock vehicleBlock : outputFile.getVehicleBlockList()){
            List<Activity> activities = vehicleBlock.getVehicleBlock().getActivityList();
            for (int i = 1; i < activities.size(); i++) {
                if(!compatible(activities.get(i-1), activities.get(i), tripDirectionHashMap, tripHashMap))
                    return false;
            }
        }

        return headwayAdmissible() && performedTrip();
    }

    //method that returns ture if the output file contains an admissible solution, for the junior category of participants, for the problem in the input file
    private static boolean isSeniorAdmissible(){
        Map<Direction, List<Trip>> tripDirectionHashMap = new HashMap<>();
        Map<Integer, Trip> tripHashMap = new HashMap<>();
        for(JsonDirection direction : outputFile.getDirections()){
            if(!inputOutputSameTrip(direction.getDirection()))
                return false;

            if(!initialFinalTrips(direction.getDirection()))
                return false;

            tripDirectionHashMap.put(direction.getDirection(), new ArrayList<Trip>());

            for (JsonTrip jsonTrip : direction.getDirection().getTrips()){
                tripHashMap.put(jsonTrip.getTrip().getTripId(), jsonTrip.getTrip());
                tripDirectionHashMap.get(direction.getDirection()).add(jsonTrip.getTrip());
            }
        }

        for(JsonVehicleBlock vehicleBlock : outputFile.getVehicleBlockList()){
            List<Activity> activities = vehicleBlock.getVehicleBlock().getActivityList();
            for (int i = 1; i < activities.size(); i++) {
                if(!compatible(activities.get(i-1), activities.get(i), tripDirectionHashMap, tripHashMap))
                    return false;
            }
        }

        return headwayAdmissible() && performedTrip();
    }

    //method that returns ture if the output file contains an admissible solution, for the junior category of participants, for the problem in the input file
    private static boolean isProfessionalAdmissible(){
        Map<Direction, List<Trip>> tripDirectionHashMap = new HashMap<>();
        Map<Integer, Trip> tripHashMap = new HashMap<>();
        for(JsonDirection direction : outputFile.getDirections()){
            if(!inputOutputSameTrip(direction.getDirection()))
                return false;

            if(!initialFinalTrips(direction.getDirection()))
                return false;

            tripDirectionHashMap.put(direction.getDirection(), new ArrayList<Trip>());

            for (JsonTrip jsonTrip : direction.getDirection().getTrips()){
                tripHashMap.put(jsonTrip.getTrip().getTripId(), jsonTrip.getTrip());
                tripDirectionHashMap.get(direction.getDirection()).add(jsonTrip.getTrip());
            }
        }

        for(JsonVehicleBlock vehicleBlock : outputFile.getVehicleBlockList()){
            List<Activity> activities = vehicleBlock.getVehicleBlock().getActivityList();
            for (int i = 1; i < activities.size(); i++) {
                if(!compatible(activities.get(i-1), activities.get(i), tripDirectionHashMap, tripHashMap))
                    return false;
            }
        }

        return headwayAdmissible() && performedTrip();
    }

    //method that compute the category of the participant
    public static String getCategory(){
        if(outputFile.getFleet().getPhi() != 0){
            if(outputFile.getGlobalCost().getAlpha0() != 0 || outputFile.getGlobalCost().getAlpha1() != 0 || outputFile.getGlobalCost().getAlpha2() != 0){
                return "professional";
            }
            else{
                return "senior";
            }
        }
        else{
            return "junior";
        }
    }
}
