package maig.model.test;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import maig.model.mcts.State;
import maig.util.TrackDataController;
import scr.Action;
import scr.SensorModel;

public class TestDriverHuman extends TrackDataController {

    private static final double SENSIVITY = 0.01;

    private double gas, steering;
    private boolean up, down, left, right;
    private int gear = 1;

    public TestDriverHuman() {
        super();
        JFrame cockpit = new JFrame();
        cockpit.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cockpit.setSize(100, 100);
        cockpit.setVisible(true);
        cockpit.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1:
                        gear = 1;
                        break;
                    case KeyEvent.VK_2:
                        gear = 2;
                        break;
                    case KeyEvent.VK_3:
                        gear = 3;
                        break;
                    case KeyEvent.VK_4:
                        gear = 4;
                        break;
                    case KeyEvent.VK_5:
                        gear = 5;
                        break;
                    case KeyEvent.VK_6:
                        gear = 6;
                        break;
                    case KeyEvent.VK_R:
                        gear = -1;
                        break;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        up = !down;
                        break;
                    case KeyEvent.VK_DOWN:
                        down = !up;
                        break;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        left = !right;
                        break;
                    case KeyEvent.VK_RIGHT:
                        right = !left;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        up = false;
                        gas = 0.0;
                        break;
                    case KeyEvent.VK_DOWN:
                        down = false;
                        gas = 0.0;
                        break;
                    case KeyEvent.VK_LEFT:
                        left = false;
                        steering = 0.0;
                        break;
                    case KeyEvent.VK_RIGHT:
                        right = false;
                        steering = 0.0;
                        break;
                }
            }
        });
    }

    @Override
    public Action control(SensorModel sensors) {
        if (up) {
            gas += SENSIVITY;
        }
        if (down) {
            gas -= SENSIVITY;
        }
        if (left) {
            steering += SENSIVITY;
        }
        if (right) {
            steering -= SENSIVITY;
        }
        Action a = new Action();
        a.accelerate = gas > 0.0 ? gas : 0.0;
        a.brake = gas < 0.0 ? gas * -1 : 0.0;
        a.steering = steering;
        a.gear = gear;
        echoVectorModel(sensors);
        return a;
    }

    @Override
    public float[] initAngles() {
        float[] f = new float[19];
        f[0] = -90;
        f[1] = 90;
        return f;
    }

    private void echoVectorModel(SensorModel s) {
        if (s.getDistanceFromStartLine() > 2000) {
            return;
        }
        State m = vs.transform(s);
        double discrepancy = m.value() - s.getTrackPosition();
        //System.out.println(discrepancy);
        //System.out.println(m.velocity());
        //System.out.printf("%.2f, %.2f\n", m.position().x, m.position().y);
    }
}
