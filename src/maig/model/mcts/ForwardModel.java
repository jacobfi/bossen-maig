package maig.model.mcts;

import static maig.Constants.GEAR_LIMIT;
import static maig.Constants.TIME_STEP;
import static maig.Constants.R_FACTOR;

import maig.Constants;
import maig.model.LineSegment;
import maig.model.Point;
import maig.model.Vector;

public class ForwardModel {

    public static LineSegment forwardStep(LineSegment in, Action a) {
        Vector outv;
        Point outp;
        double offset = in.v.angle();
        double inSpeed = in.v.size();
        double outSpeed = inSpeed + gasApprox(a.gas, inSpeed) * TIME_STEP;
        double avgSpeed = (inSpeed + outSpeed) * 0.5;
        if (a.steering == 0.0) {
            outv = new Vector.Polar(outSpeed, offset);
            Vector disp = new Vector.Polar(avgSpeed, offset);
            outp = in.p.displace(disp.scale(TIME_STEP));
        } else {
            double r = steeringApprox(a.steering, avgSpeed);
            r *= Math.signum(a.steering);
            double angv = avgSpeed / r; // Angular velocity.
            double angle = angv * TIME_STEP; // Angular distance.
            outv = new Vector.Polar(outSpeed, offset + angle);
            outp = in.p.rotate(angle, r, offset - Math.PI * 0.5);
        }
        return new LineSegment(outp, outv);
    }

    // Radius of the rotation circle.
    private static double steeringApprox(double steering, double speed) {
        //return 9.5588 * Math.pow(Math.abs(steering), -0.938);
        return 176.8927 * Math.pow(0.000272, Math.abs(steering)) * Math.pow(1.0099 * R_FACTOR, speed);
    }

    // Acceleration.
    private static double gasApprox(double gas, double speed) {
        int gear = transmission(speed);
        return GasModel.getAcceleration(gear, gas, speed);
    }

    private static int transmission(double speed) {
        switch (GEAR_LIMIT) {
            case 6:
                if (speed > 240) {
                    return 6;
                }
            case 5:
                if (speed > 200) {
                    return 5;
                }
            case 4:
                if (speed > 155) {
                    return 4;
                }
            case 3:
                if (speed > 140) {
                    return 3;
                }
            case 2:
                if (speed > 60) {
                    return 2;
                }
            default:
                return 1;
        }
    }
}
