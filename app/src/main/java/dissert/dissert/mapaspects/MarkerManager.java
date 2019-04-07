package dissert.dissert.mapaspects;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import dissert.dissert.MapViewActivity;
import dissert.dissert.R;
import dissert.dissert.rule.action.AbstractAction;

public class MarkerManager {

    private Context ctx;

    private MapView map;
    private Map<String, Distance> markerDistances;
    private Set<String> allIds;
    private Set<String> nears;

    private Drawable relevantMarker;
    private Drawable notRelevantMarker;

    private int nearDistance;
    private TextView t;

    public MarkerManager(MapView map, Context ctx, TextView t) {
        this.ctx = ctx;
        this.map = map;
        this.t = t;
        nearDistance = 50;
    }


    public void setMarkerOpacity(String id, int alpha) {
        markerDistances.get(id).getMarker().setAlpha(alpha / 100f);
    }

    public void setMarkerRotation(String id, int degrees) {
        markerDistances.get(id).getMarker().setRotation((float) degrees);
    }

    public void setOpacities(Set<String> ids, int alpha) {
        if (ids == null)
            return;
        for (String id : ids) {
            setMarkerOpacity(id, alpha);
        }
    }

    public void setRotations(Set<String> ids, int degrees) {
        if (ids == null)
            return;
        for (String id : ids) {
            setMarkerRotation(id, degrees);
        }
    }

    /**
     * Determines if the user is approaching a certain POI, given its ID.
     * @param user User location, as a GeoPoint.
     * @param id POI's ID.
     * @return user approaching the POI.
     */
    public boolean isUserApproaching(GeoPoint user, String id) {
        if (user == null)
            return false;
        Distance d = markerDistances.get(id);

        boolean res = d.isUserApproaching(user);
        return res;
    }

    public Set<String> isUserApproachingAll(GeoPoint user) {
        Set<String> approaching = new HashSet<>(markerDistances.size());

        for (String id : allIds) {
            if (isUserApproaching(user, id))
                approaching.add(id);
        }

        return approaching;
    }

    public boolean isUserNear(GeoPoint user, String id) {
        return isUserNear(user, id, nearDistance);
    }

    public Set<String> isUserNearAll(GeoPoint user) {
        Set<String> near = new HashSet<>(markerDistances.size());

        for (String id : allIds) {
            if (isUserNear(user, id, nearDistance))
                near.add(id);
        }

        return near;
    }

    public boolean isUserAt(GeoPoint user, GeoPoint second) {
        return user.distanceToAsDouble(second) <= 30;
    }

    public void setNearDistance(int nearDistance) {
        this.nearDistance = nearDistance;
    }

    public int getNearDistance() {
        return nearDistance;
    }

    public void updateDistance(GeoPoint user, Set<String> ids) {
        if (user == null || ids == null)
            return;
        for (String id : ids) {
            markerDistances.get(id).updateDistance(user);
        }
    }

    public void updateAllDistances(GeoPoint user) {
        if (user == null)
            return;

        for (String id : allIds)
            markerDistances.get(id).updateDistance(user);
    }

    public void setRelevancies(Set<String> ids, boolean relevant) {
        if (ids == null)
            return;
        for (String id : ids)
            setRelevant(relevant, id);
    }

    public void setRelevant(boolean relevant, String id) {
        Marker m = markerDistances.get(id).getMarker();
        m.setIcon(relevant ? relevantMarker : notRelevantMarker);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    }

    public void setInfoWindows(Set<String> ids, boolean open) {
        if (ids == null)
            return;
        for (String id : ids)
            setInfoWindow(id, open);
    }

    public void setInfoWindow(String id, boolean open) {
        if (open)
            markerDistances.get(id).getMarker().showInfoWindow();
        else
            markerDistances.get(id).getMarker().closeInfoWindow();
    }


    public void setNearestOpacity(Set<String> idsAllIndividuals, int opacity, GeoPoint user) {
        String nearest = getNearestPoint(idsAllIndividuals, user);
        if (nearest != null)
            setMarkerOpacity(nearest, opacity);
    }

    public void setNearestRotation(Set<String> idsAllIndividuals, int rotation, GeoPoint user) {
        String nearest = getNearestPoint(idsAllIndividuals, user);
        if (nearest != null)
            setMarkerRotation(nearest, rotation);
    }

    public void setNearestRelevancy(Set<String> idsAllIndividuals, boolean relevant, GeoPoint user) {
        String nearest = getNearestPoint(idsAllIndividuals, user);
        if (nearest != null)
            setRelevant(relevant, nearest);
    }

    public void setNearestInfoWindow(Set<String> idsAllIndividuals, boolean open, GeoPoint user) {
        String nearest = getNearestPoint(idsAllIndividuals, user);
        if (nearest != null)
            setInfoWindow(nearest, open);
    }

    private boolean isUserNear(GeoPoint user, String id, double maxDistance) {
        if (user == null)
            return false;

        Distance d = markerDistances.get(id);
        boolean res = d.getDistanceTo(user) <= maxDistance;
        String markerName = d.getMarker().getTitle();
        if (res) {
            boolean added = nears.add(markerName);
            if (added) {
                Toast.makeText(ctx, "You are getting near " + markerName, Toast.LENGTH_SHORT).show();
            }
        } else {
            nears.remove(markerName);
        }

        t.setText(Arrays.toString(nears.toArray()));

        return res;
    }

    private String getNearestPoint(Set<String> ids, GeoPoint user) {
        if (ids == null)
            return null;
        String nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (String id : ids){
            double dist = markerDistances.get(id).getDistanceTo(user);
            if (dist < nearestDist) {
                nearest = id;
                nearestDist = dist;
            }
        }
        return nearest;
    }

    /**
     * Receives an array of points from the database and adds them to the map.
     *
     * @param points array of points (markers).
     */
    public void addMarkers(JSONArray points, Drawable relevant, Drawable notRelevant) {
        markerDistances = new HashMap<>(points.length());
        allIds = new HashSet<>(points.length());
        relevantMarker = relevant;
        notRelevantMarker = notRelevant;
        try {
            for (int i = 0; i < points.length(); i++) {
                JSONObject point = points.getJSONObject(i);
                String name = point.getString("name");
                double latitude = point.getDouble("latitude");
                double longitude = point.getDouble("longitude");
                String description = point.getString("description");
                int id = point.getInt("id");
                this.addMarker(latitude, longitude, name, description, id + "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        instantiateMap();
    }

    /**
     * Initializes variable markers, which keeps record of every Marker present on the map, for
     * an easier fetching by Marker ID.
     */
    public void instantiateMap() {
        for (Overlay o : map.getOverlays()) {
            if (o instanceof Marker) {
                Marker m = (Marker) o;
                Distance d = new Distance(m);
                markerDistances.put(m.getId(), d);
                allIds.add(m.getId());
            }
        }
        nears = new HashSet<>(allIds.size());
    }

    /**
     * Adds a marker to the map, after setting its ID, position, Title and snippet.
     * @param latitude Marker's latitude.
     * @param longitude Marker's longitude.
     * @param title Marker's title.
     * @param description Marker's description.
     * @param id Marker's ID.
     */
    private void addMarker(double latitude, double longitude, String title, String description,
                           String id) {
        GeoPoint point = new GeoPoint(latitude, longitude);
        Marker m = new Marker(this.map);
        m.setId(id);
        m.setPosition(point);
        m.setTitle(title);
        m.setSnippet("Latitude: " + point.getLatitude() + "\nLongitude: "
                + point.getLongitude() + (description != null && !description.equals("null") ?
                "\n" +
                        description : ""));
        m.setIcon(notRelevantMarker);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);


        map.getOverlays().add(m);
    }

}
