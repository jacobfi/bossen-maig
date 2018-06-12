package maig.learning;

import maig.model.mcts.Action;
import maig.util.TrackDataController;
import scr.Client;
import scr.SensorModel;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccelerationLearningDriver extends TrackDataController {

    private static Action[] actions = new Action[] {
            new Action(1.0,0.00045),
            new Action(0.9,0.00045),
            new Action(0.8,0.00045),
            new Action(0.7,0.00045),
            new Action(0.6,0.0004),
            new Action(0.5,0.0004),
            new Action(0.4,0.0004),
            new Action(0.3,0.0004),
            new Action(0.2,0.0003),
            new Action(0.1,0.0002),
    };
    private static int actionIndex = 0;

    private static String SAVEFILE = "Acceleration";
    private static PrintWriter writer;
    static {
        try {
            SAVEFILE += "_" + new SimpleDateFormat("HH-mm-ss").format(new Date()) + ".csv";
            writer = new PrintWriter(SAVEFILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private int CC = 0;

    private double tStart, tEnd, vStart, vEnd, vTop;
    private double rpmStart, rpmEnd;
    @Override
    public scr.Action control(SensorModel sensors) {
        Action currentAction = actions[actionIndex];
        //Calculate result for the interval
        if(vStart > 0.01) {
            double acceleration = (vEnd-vStart) / (tEnd - tStart);
            double rmpAcceleration = (rpmEnd - rpmStart) / (tEnd - tStart);
            if(acceleration > 0.1 && vStart > vTop) {
                vTop = vStart;
                writer.append(String.format(Locale.US, "%.3f, %.3f, %d, %.3f, %.3f, %.3f\n", vStart, currentAction.gas, sensors.getGear(), rmpAcceleration, acceleration, sensors.getCurrentLapTime()));
                //System.out.printf("%.3f, %.3f, %d, %.3f \n", vStart, currentAction.gas, sensors.getGear(), acceleration);
            }
        }

        //Save data for the interval
        tStart = tEnd;
        tEnd = sensors.getCurrentLapTime();
        vStart = vEnd;
        vEnd = sensors.getSpeed() / 3.6;
        rpmStart = rpmEnd;
        rpmEnd = sensors.getRPM();

        //Control the car
        scr.Action a = new scr.Action();
        a.accelerate = currentAction.gas;
        a.steering = currentAction.steering;
        int gear = sensors.getGear();
        if(gear == 0) gear = 1;
        if(sensors.getRPM() > 8000 && CC == 0) {
            gear++;
            CC = 5;
        } else if (sensors.getRPM() < 2000 && CC == 0 && gear > 1) {
            gear--;
            CC = 5;
        }
        a.gear = gear;
        if(CC > 0) {
            a.clutch = 1;
            a.accelerate = 0;
            CC--;
        }
        if(tStart > 10 && sensors.getDistanceFromStartLine() > 1100
                || sensors.getCurrentLapTime() > 40) a.restartRace = true;
        return a;
    }

    @Override
    public void reset() {
        AccelerationLearningDriver.actionIndex++;
        if(actionIndex >= actions.length) return;
        new Thread(() -> {
            Client.main(new String[] {
                    "maig.learning.AccelerationLearningDriver"
            });
        }).start();
    }
}
