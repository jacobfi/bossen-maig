package maig.model;

import scr.SensorModel;

public class SimulationSensorModel implements SensorModel {

    private final double distance, drift, angle, speedX, speedY;
    private final boolean newlap;

    public SimulationSensorModel(double distance, double drift, double angle,
            double speedX, double speedY, boolean newlap) {
        this.distance = distance;
        this.drift = drift;
        this.angle = angle;
        this.speedX = speedX;
        this.speedY = speedY;
        this.newlap = newlap;
    }

    public boolean newLap() {
        return newlap;
    }

    @Override
    public double getSpeed() {
        return speedX;
    }

    @Override
    public double getAngleToTrackAxis() {
        return angle;
    }

    @Override
    public double[] getTrackEdgeSensors() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double[] getFocusSensors() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getTrackPosition() {
        return drift;
    }

    @Override
    public int getGear() {
        return 0;
    }

    @Override
    public double[] getOpponentSensors() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public int getRacePosition() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getLateralSpeed() {
        return speedY;
    }

    @Override
    public double getCurrentLapTime() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getDamage() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getDistanceFromStartLine() {
        return distance;
    }

    @Override
    public double getDistanceRaced() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getFuelLevel() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getLastLapTime() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getRPM() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double[] getWheelSpinVelocity() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getZSpeed() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public double getZ() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }

    @Override
    public String getMessage() {
        throw new UnsupportedOperationException("Not supported in simulation.");
    }
}
