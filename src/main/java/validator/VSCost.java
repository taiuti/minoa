package validator;

import validator.validatorClasses.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VSCost {
    private static int availableElectricVehicle;
    public static boolean isAdmissible;

    public static double start(File outputFile){
    	//the total cost of the solution
        double vsCost = 0;
        //the object that represent the PTN
        Input input;
        //the file gave in input (the output.json file)

        try {

            ObjectMapper mapper = new ObjectMapper();
            input = mapper.readValue(outputFile, Input.class);

            availableElectricVehicle = input.getFleet().getVehicleList().get(1).getVehicleType().getElectricInfo().getNumberVehicle();

            double[] vehicleBlockCosts = new double[input.getVehicleBlockList().size()];
            System.out.println("Number of utilized vehicles: " + vehicleBlockCosts.length + "\n");

            isAdmissible = isAdmissible(input);
            //if all vehicle block are feasible
            if (isAdmissible){
                int i = 0;
                for (JsonVehicleBlock jsonVehicleBlock : input.getVehicleBlockList()) {
                    double tmpCost = computeVehicleBlockCost(jsonVehicleBlock.getVehicleBlock(), input);
                    vehicleBlockCosts[i] = tmpCost;
                    vsCost += tmpCost;
                    i++;
                }
                for (i = 0; i < vehicleBlockCosts.length; i++) {
                    System.out.println("Vehicle type: " + input.getVehicleBlockList().get(i).getVehicleBlock().getVehicleType().toUpperCase(Locale.ROOT)
                            + "\tVSCost:\t" + vehicleBlockCosts[i] + "\n");
                }

                System.out.println("Total cost:\t" + vsCost + "\n");
            }
            else{
                System.out.println("There is a not feasible vehicle block\n");
            }
        } catch (IOException e) {
            System.out.println("Exception occurred: " + e.getMessage());
        }

        return vsCost;
    }


    //method that return the cost of a single vehicle block
    private static double computeVehicleBlockCost(VehicleBlock vehicleBlock, Input input){
        //list that contains all the trips performed by the solution
    	List<JsonTrip> tripList = new ArrayList<>();
        for(JsonDirection jsonDirection : input.getDirections()){
            tripList.addAll(jsonDirection.getDirection().getTrips());
        }

        double usageCost = 0;
        double breakTimeCost =  input.getGlobalCost().getBreakCostCoefficient();
        double pullInPullOutCost = 0;
        double co2Cost = 0;
        double cost;

        // data i need to use to calculate the VS cost of the vehicle block
        long timeOfService = 0;
        long timeOfDeadhead = 0;
        long timeOfPaidBreak = 0;

        // set the usageCost, the pullInPullOutCost and the co2Cost according to the specification in the json file
        for(JsonVehicleType jsonVehicleType : input.getFleet().getVehicleList()){
            if(jsonVehicleType.getVehicleType().getVehicleTypeName().equalsIgnoreCase(vehicleBlock.getVehicleType())){
                usageCost = jsonVehicleType.getVehicleType().getUsageCost();
                pullInPullOutCost = jsonVehicleType.getVehicleType().getPullInOutCost();
                if(jsonVehicleType.getVehicleType().isElectric()){
                    co2Cost = 0;
                }
                else{
                    co2Cost = jsonVehicleType.getVehicleType().getIceInfo().getEmissionCoefficient();;
                }
            }
        }


        // for every activity in the vehicle block I need to increase the variables accordingly to the type of activity
        for(Activity elem : vehicleBlock.getActivityList()){

        	// if this activity is a trip I need to increase the timeOfService of the vehicleBlock
            if((elem.getClass() == ActivityTrip.class)){
                int tmp = ((ActivityTrip) elem).getTripId();
                for(JsonTrip jTrip : tripList){
                    if(jTrip.getTrip().getTripId() == tmp){
                        Trip tripTmp = jTrip.getTrip();
                        timeOfService += tripTmp.getEndTime() - tripTmp.getStartTime();
                    }
                }
            }
            
            // if this activity is a break I need to increase the timeOfPaidBreak only if the vehicle isn't charging
            else if(elem.getClass() == ActivityBreak.class){
                List<JsonBreak> tmpBreakList = ((ActivityBreak) elem).getBreakActivities();
                for (JsonBreak jsonBreak : tmpBreakList) {
                    if(jsonBreak.getBreakActivity().getTypeSpot().equalsIgnoreCase("parking") ||
                    !(jsonBreak.getBreakActivity().isCharging)){
                        BreakActivity ba = jsonBreak.getBreakActivity();
                        timeOfPaidBreak += ba.getEndTime() - ba.getStartTime();
                    }
                }
            }
            // otherwise the activity is a deadheadTrip and therefore I increase the time spent on deadheadTrips
            else{
                timeOfDeadhead += elem.getEndTime() - elem.getStartTime();
            }
        }

        cost = (usageCost) + (breakTimeCost * timeOfPaidBreak) + (pullInPullOutCost * timeOfDeadhead) + (co2Cost * timeOfService);
        return cost;
    }


    //method that return true in and only if all vehicle blocks in the solution are feasible
    private static boolean isAdmissible(Input input){

        //the list of nodes in the Public Trasportation network
        List<Node> nodes = new ArrayList<>();

        //list of deadhead arcs in the PTN
        List<JsonDeadheadArc> deadheadArcs = input.getDeadheadArcs();

        //map the tripId with the corresponding trip
        Map<Integer, Trip> tripHashMap= new HashMap<>();

        //map the node name with the number of available parking, slow cahrging and fast charging spots
        Map<String, List<Integer>> parkingSpotHashMap = new HashMap<>();
        Map<String, List<Integer>> slowChargeHashMap = new HashMap<>();
        Map<String, List<Integer>> fastChargeHashMap = new HashMap<>();

        //PTN timeHorizon array
        int[] timeHorizon = input.getTimeHorizon();


        //initialize tripHashMap
        for(JsonDirection jsonDirection : input.getDirections()) {
        	for(JsonTrip jTrip : jsonDirection.getDirection().getTrips()) {
                tripHashMap.put(jTrip.getTrip().getTripId(),jTrip.getTrip());
        	}
        }

        //for every node in the PTN initialize the list of parking, slow charging and fast charging spots
        for(JsonNetworkNode elem : input.getNodes()){
            nodes.add(elem.getNode());
            parkingSpotHashMap.put(elem.getNode().getName(), new ArrayList<Integer>());
            slowChargeHashMap.put(elem.getNode().getName(), new ArrayList<Integer>());
            fastChargeHashMap.put(elem.getNode().getName(), new ArrayList<Integer>());
        }

        //for each node in the PTN
        for (Node node : nodes) {
            //for every minute of service set the exact number of available parking, slow charging and fast charging spots
            for (int i = 0; i < (timeHorizon[timeHorizon.length - 1] - timeHorizon[0]) / 60; i++) {
                //set the value for minute i
                parkingSpotHashMap.get(node.getName()).add(i, node.getBreakCapacity());
                slowChargeHashMap.get(node.getName()).add(i, node.getSlowChargeCapacity());
                fastChargeHashMap.get(node.getName()).add(i, node.getFastChargeCapacity());
            }
        }

        //the vehicle block number i'm looking at
        int vehicleBlockNumber = 0;
        
        //for every vehicle block in the solution
        for (JsonVehicleBlock block : input.getVehicleBlockList()) {

            //parse the json object in a vehicle block object
            VehicleBlock vehicleBlock = block.getVehicleBlock();

            // if the selected vehicle block is electric
            if (vehicleBlock.getVehicleType().toLowerCase().contains("electric")) {
            	
            	//decrease the number of available electric vehicles
                availableElectricVehicle -= 1;

                //there is a fixed number of electric vehicle that the solution can contains
                if(availableElectricVehicle < 0){
                    System.out.println("Used to many electric vehicles");
                    return false;
                }

                //electric auotnomu
                double autonomy = 1;

                //residual autonomy of the vehicle, start equals the autonomy
                double residualAutonomy = autonomy;

                //complementary autonomy (starts at 0 -> autonomy - residualAutonomy = 0)
                double complementaryAutonomy = 0;

                double fastRechargeCoefficient = input.getFleet().getPhi();

                //the time that the vehicle need to take a full recharges (0% -> 100%)
                long maximumChargingTime = 1;

                //set the variables according to the values passed in the json file
                for(JsonVehicleType jsonVehicleType : input.getFleet().getVehicleList()){
                    if(jsonVehicleType.getVehicleType().getVehicleTypeName().equalsIgnoreCase(vehicleBlock.getVehicleType())){
                        autonomy = jsonVehicleType.getVehicleType().getElectricInfo().getVehicleAutonomy();
                        residualAutonomy = autonomy;
                        maximumChargingTime = jsonVehicleType.getVehicleType().getElectricInfo().getMaxChargingTime();
                    }
                }

                //for every activity in the selected vehicle block
                for (Activity activity : vehicleBlock.getActivityList()){

                    //if the activity is a trip the residual autonomy will decrease
                    if(activity.getClass() == ActivityTrip.class){
                        //get information about the selected trip
                        Trip tmpTrip = tripHashMap.get(((ActivityTrip) activity).getTripId());
                        //decrease the residual auotnomy accordingly to the length of the trip
                        residualAutonomy = residualAutonomy - tmpTrip.getLengthTrip();
                        //increase the complementary autonomy accordingly to the length of the trip
                        complementaryAutonomy += tmpTrip.getLengthTrip();
                    }

                    //if the activity is a break, and the vehicle is charging the residual autonomy will increase
                    if(activity.getClass() == ActivityBreak.class){
                    	//for every type of break in the break activity
                        for (int i = 0; i < ((ActivityBreak) activity).getBreakActivities().size(); i++) {
                            BreakActivity tmp = ((ActivityBreak) activity).getBreakActivities().get(i).getBreakActivity();

                            //if the break take place in a fast charge spot and the vehicle is charging
                            if(tmp.getTypeSpot().equalsIgnoreCase("fastCharging") && tmp.isCharging){
                            	//the number of km gained with the recharges
                                double partialRecharge = (autonomy * ((tmp.getEndTime()- tmp.getStartTime())/(fastRechargeCoefficient * (double)maximumChargingTime)));
                                //the vehicle recharged completely
                                if(partialRecharge >= complementaryAutonomy){
                                    complementaryAutonomy = 0;
                                    residualAutonomy = autonomy;
                                }
                                //the residual autonomy of the vehicle increase but not reach the full autonomy
                                else{
                                    complementaryAutonomy -= partialRecharge;
                                    residualAutonomy += partialRecharge;
                                }
                            }

                            //if the break take place in a slow charge spot and the vehicle is charging
                            if(tmp.getTypeSpot().equalsIgnoreCase("slowCharging") && tmp.isCharging){
                            	//the number of km gained with the recharges
                                double partialRecharge = autonomy * ((tmp.getEndTime()- tmp.getStartTime())/(double)maximumChargingTime);
                                //the vehicle recharged completely
                                if(partialRecharge >= complementaryAutonomy){
                                    complementaryAutonomy = 0;
                                    residualAutonomy = autonomy;
                                }
                                //the residual autonomy of the vehicle increase but not reach the full autonomy
                                else{
                                    complementaryAutonomy -= partialRecharge;
                                    residualAutonomy += partialRecharge;
                                }
                            }
                        }
                    }

                    //if the activity is a deadhead, the residual autonomy will decrease
                    if(activity.getClass() == Deadhead.class){
                        
                    	//for every deadhead arc in the PTN
                        for(JsonDeadheadArc jsonDeadheadArc : deadheadArcs){
                            //if the selected deadhead arc have the same code as the one in the vehicle block
                            if(jsonDeadheadArc.getDeadheadArc().getDeadheadArcCode() == ((Deadhead) activity).getDeadheadArcCode()){
                                //the residual autonomy decrease
                                residualAutonomy -= jsonDeadheadArc.getDeadheadArc().getArcLength();
                                complementaryAutonomy += jsonDeadheadArc.getDeadheadArc().getArcLength();
                            }
                        }
                    }
                    //the autonomy of the vehicle reached 0
                    if(residualAutonomy < 0){
                        System.out.println("Residual autonomy of vehicle block "+vehicleBlockNumber+ " reached 0");
                        return false;
                    }
                }
                System.out.println("\n");
            }
            
            //look at the next vehicle block
            vehicleBlockNumber++;
        }

        //check the parking, slow charge and fast charge spot for every vehicle block
        for(JsonVehicleBlock block : input.getVehicleBlockList()){
            VehicleBlock vehicleBlock = block.getVehicleBlock();


            //for each activity n the vehicle block
            for(Activity activity : vehicleBlock.getActivityList()){

                //if the selected activity is a break
                if(activity.getClass() == ActivityBreak.class){

                    //the node where the break take place, the same for every break activity
                    String node = activity.getStartNode();

                    //for every break in the break activity
                    for (int i = 0; i < ((ActivityBreak) activity).getBreakActivities().size(); i++) {
                    	//the start time of the break
                        int startTime = ((ActivityBreak) activity).getBreakActivities().get(i).getBreakActivity().getStartTime();
                        //the end time of the break
                        int endTime = ((ActivityBreak) activity).getBreakActivities().get(i).getBreakActivity().getEndTime();
                        //minute of break
                        int stopTime = (endTime-startTime) / 60;

                        //for every minute that the vehicle is taking a break decrease the right spot in the right minute
                        for (int j = 0; j < stopTime; j++) {

                            //if the vehicle is taking a break in a parking spot
                            if(((ActivityBreak) activity).getBreakActivities().get(i).getBreakActivity().getTypeSpot().equalsIgnoreCase("parking")){
                                int availableParkingSpot = parkingSpotHashMap.get(node).get(((startTime - timeHorizon[0])/60) + j);
                                parkingSpotHashMap.get(node).set((((startTime - timeHorizon[0])/60) + j), availableParkingSpot-1);
                            }
                            
                            //if the vehicle is taking a break in a slow charge spot
                            else if(((ActivityBreak) activity).getBreakActivities().get(i).getBreakActivity().getTypeSpot().equalsIgnoreCase("slowCharging")){
                                int availableSlowChargeSpot = slowChargeHashMap.get(node).get(((startTime - timeHorizon[0])/60) + j);
                                slowChargeHashMap.get(node).set((((startTime - timeHorizon[0])/60) + j), availableSlowChargeSpot-1);
                            }

                            //if the vehicle is taking a break in a fast charge spot
                            else{
                                int availableFastChargeSpot = fastChargeHashMap.get(node).get(((startTime - timeHorizon[0])/60) + j);
                                fastChargeHashMap.get(node).set((((startTime - timeHorizon[0])/60) + j), availableFastChargeSpot-1);
                            }


                        }
                    }
                }
            }
        }

        //for junior category the control of the parking spots are diasbled
        boolean isJuniorCategory = false;
        if(input.getGlobalCost().getAlpha0() == 0 && input.getGlobalCost().getAlpha1() == 0 && input.getGlobalCost().getAlpha2() == 0 && input.getFleet().getPhi() == 0){
            isJuniorCategory = true;
        }

        if(!isJuniorCategory){

        	//for every node in the PTN
            for(String node : parkingSpotHashMap.keySet()){
                List<Integer> parkingSpot = parkingSpotHashMap.get(node);
                List<Integer> slowChargeSpot = slowChargeHashMap.get(node);
                List<Integer> fastChargeSpot = fastChargeHashMap.get(node);

                //for every minute in the PTN
                for (int i = 0; i < parkingSpot.size(); i++) {
                	//if there number of vehicles stopped in a parking spot in the node in a determinate minute exceed the number of available parking spots
                    if(parkingSpot.get(i) < 0){
                        System.out.println("Exceeded parking capacity in  " + node + " at second: " +(timeHorizon[0] + (i*60)) + "\n");
                        return false;
                    }
                    
                    //if there number of vehicles stopped in a slow charge spot in the node in a determinate minute exceed the number of available slow charge spots
                    else if(slowChargeSpot.get(i) < 0){
                        System.out.println("Exceeded slow charge spot capacity in " + node + " at second: " +(timeHorizon[0] + (i*60)) + "\n");
                        return false;
                    }
                    
                    //if there number of vehicles stopped in a fast charge spot in the node in a determinate minute exceed the number of available fast charge spots
                    else if(fastChargeSpot.get(i) < 0){
                        System.out.println("Exceeded fast charge spot capacity in " + node + " at second: " +(timeHorizon[0] + (i*60)) + "\n");
                        return false;
                    }
                }
            }
        }

        //all vehicle blocks are feasible
        return true;
    }
}
