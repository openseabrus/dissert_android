package dissert.dissert.mapaspects;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class Distance {

    private double distance;
    private Marker marker;

    public Distance(Marker marker) {
        distance = -1d;
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker;
    }

    public boolean isUserApproaching(GeoPoint user) {
        double distanceInMeters = calculateDistanceTo(user);
        boolean significantDiff = Math.abs(distanceInMeters - distance) >= 10d;
        boolean res = distanceInMeters < distance;

        return res && significantDiff;
    }

    public double getDistanceTo(GeoPoint user) {
        return calculateDistanceTo(user);
    }

    public void updateDistance(GeoPoint user) {
        distance = calculateDistanceTo(user);
    }

    private double calculateDistanceTo(GeoPoint point) {
        double distanceInMeters = point.distanceToAsDouble(marker.getPosition());
        return distanceInMeters;
    }
}
