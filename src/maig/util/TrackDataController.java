package maig.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.stream.Collectors;
import maig.model.TrackPoint;
import maig.model.VectorSpace;
import scr.Controller;
import scr.maig.DataGathering.TrackData;

public abstract class TrackDataController extends Controller {

    protected VectorSpace vs;
    protected TrackData td;

    public TrackDataController() {
        super();
        vs = createVectorSpace("track.mdl");
        //vs = createVectorSpace("new_track.mdl");
        //vs = createVectorSpace("best_track.mdl");
    }

    private VectorSpace createVectorSpace(String trackName) {
        try {
            FileInputStream fi = new FileInputStream(trackName);
            ObjectInputStream i = new ObjectInputStream(fi);
            td = (TrackData) i.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            vs = null;
            return null;
        }
        ArrayList<TrackPoint> arr = td.getNextPoints(0, 50_000);
        double magicConstant = Math.abs((Math.PI*2) / arr.stream().mapToDouble(tp -> tp.angle).sum());
        return new VectorSpace(arr.stream().map(tp
                -> new TrackPoint(tp.dist, tp.angle * magicConstant, tp.hi, tp.lo))
                .collect(Collectors.toList()));
    }

    @Override
    public void reset() {
        System.out.println("Reset");
    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown");
    }
}
