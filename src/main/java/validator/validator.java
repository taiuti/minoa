package validator;

import java.io.File;
import java.nio.file.Paths;

public class validator {
    public static void main(String[] args) {
        File inputFile = null, outputFile = null;
        double ttCost = 0, vsCost = 0;
        if(args.length < 2){
            System.out.println("not enough files!");
        }
        else if(args.length > 2){
            System.out.println("too many files!");
        }
        else{
            for(String arg : args){
                if(arg.toLowerCase().contains("input") && arg.toLowerCase().contains(".json")){
                    inputFile = new File(Paths.get(arg).toUri());
                }
                else if(arg.toLowerCase().contains("output") && arg.toLowerCase().contains(".json")){
                    outputFile = new File(Paths.get(arg).toUri());
                }
            }

            if (inputFile != null && outputFile != null){
                ttCost = TTCost.start(inputFile, outputFile);
                if(TTCost.isAdmissible){
                    vsCost = VSCost.start(outputFile);
                    if(VSCost.isAdmissible){
                        if(TTCost.getCategory().equalsIgnoreCase("professional")){
                            System.out.println("ttCost: " + ttCost + "\tvsCost: " + vsCost);
                        }
                        else{
                            System.out.println("vsCost: " + vsCost);
                        }
                    }
                    else if(TTCost.getCategory().equalsIgnoreCase("professional")){
                        System.out.println("ttCost: " + ttCost);
                    }
                }
                else{
                    System.out.println("tt solution wasn't feasible!");
                }
            }
        }
    }
}
