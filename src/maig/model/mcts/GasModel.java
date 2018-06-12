package maig.model.mcts;

import maig.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class GasModel {
    private static HashMap<Integer,HashMap<Integer, Function<Double,Double>>> model = new HashMap<>();
    static {
        loadData("GasForwardModel.csv");
    }

    private static void loadData(String filename) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (int i = 0; i < lines.size(); i+=4) {
                String[] split = lines.get(i).split(",");
                double gas = Double.parseDouble( split[0] );
                int gear = (int) Double.parseDouble( split[1] );
                double max = Double.parseDouble(lines.get(i+1));
                double min = Double.parseDouble(lines.get(i+2));
                Function<Double,Double> f = readFunction(lines.get(i+3), min, max);
                addToModel(gear, gas, f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static Function<Double, Double> readFunction(String data, double min, double max) {
        String[] split = data.split(",");
        double a = Double.parseDouble(split[0]);
        //this shit is necessary because rapidminer is gay.
        double b1 = Double.parseDouble(split[1]);
        double b2 = Double.parseDouble(split[2]);
        double b = Math.max(b1,b2);

        return x -> {
            x = Math.min(x,max);
            x = Math.max(x,min);
            return a * x + b;
        };
    }
    private static void addToModel(int gear, double gas, Function<Double,Double> function) {
        HashMap<Integer, Function<Double,Double>> map;
        int gasi = (int)(gas * 10.0);
        if(model.containsKey(gasi)) {
            map = model.get(gasi);
        } else {
            map = new HashMap<>();
            model.put(gasi,map);
        }
        map.put(gear, function);
    }
    public static double getAcceleration(int gear, double gas, double speed) {
        if(gas <= 0) return speed * gas * Constants.BRAKE_FACTOR;
        int gasi = (int)(gas * 10.0);
        if(!model.get(gasi).containsKey(gear)) {
            for (int i = gear; i > 0; i--) {
                for (int j = gasi; j > 0 ; j--) {
                    if(model.get(j).containsKey(i)) return model.get(j).get(i).apply(speed);
                }
            }
        } else {
            return model.get(gasi).get(gear).apply(speed);
        }
        throw new RuntimeException("No data was available to predict speed. Gear: " + gear + " Gas: " + gas + " Speed: " + speed);
    }
}
