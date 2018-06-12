package scr.maig.DataGathering;

import maig.model.TrackPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Mathias Vielwerth on 28-10-2014.
 */
public class TrackData implements Serializable {
    ArrayList<TrackPoint> data = new ArrayList<>();

    public void put(TrackPoint point) {
        data.add(point);
    }

    public ArrayList<TrackPoint> getNextPoints(int position, int pointCount) {
        TrackPoint tp = data.stream().filter(t -> t.dist - 1 < position && t.dist > position).findFirst().orElse(null);
        ArrayList<TrackPoint> arr = new ArrayList<>(pointCount);
        if(tp == null) return arr;

        int index = data.indexOf(tp);
        for(int i = index; i < index+pointCount; i++) {
            if(i >= data.size())
                break;
            arr.add(data.get(i));
        }
        return arr;
    }
}
