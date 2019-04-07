package dissert.dissert;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dissert.dissert.mapaspects.MarkerManager;
import dissert.dissert.rule.Rule;
import dissert.dissert.rule.action.Action;
import dissert.dissert.rule.attribute.Attribute;
import dissert.dissert.rule.field.Field;
import dissert.dissert.rule.trigger.Trigger;
import dissert.dissert.sensors.SensorReader;
import dissert.dissert.utilities.SyncRequest;
import dissert.dissert.utilities.VolleyCallback;

public class RuleController {

    private Context ctx;
    private final String CONFIGURATION_URL = "https://gseabra.herokuapp.com/config";
    private final String POINTS_URL = "https://gseabra-pois.herokuapp.com/api/points";

    private SensorReader sensorReader;
    private SensorManager sm;
    private boolean initialized;
    private int interval;

    private MarkerManager markerManager;
    private boolean addedPoints;

    private String mapTheme;
    private String activityState;
    private boolean checkActivity;
    private boolean checkLight;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private Set<Rule> rules;

    RuleController(Context c) {
        ctx = c;
        initialized = false;
        addedPoints = false;
        activityState = "UNKNOWN";
        sm = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
        getJSONArray(result -> {
            JSONArray rules = result;
            initRules(rules, c);
        }, CONFIGURATION_URL);

        sensorReader = new SensorReader();
    }

    void addMap(MapView map, TextView t) {
        markerManager = new MarkerManager(map, ctx,t );
    }

    void addMarkers(Drawable relevant, Drawable notRelevant) {
        if (!addedPoints) {
            JSONArray points = this.obtainData(POINTS_URL);
            markerManager.addMarkers(points, relevant, notRelevant);
            addedPoints = true;
        }
    }


    public void checkChanges(IMapController mapController, MyLocationNewOverlay myLocationNewOverlay, MapView map) {
        checkObservees(mapController, myLocationNewOverlay, map);
    }

    void checkObservees(IMapController map, MyLocationNewOverlay myLocationNewOverlay, MapView mapView) {
        if (!initialized)
            return;

        GeoPoint user = myLocationNewOverlay.getMyLocation();
        Set<String> toUpdate = null;
        boolean containsAll = false;

        for (Rule rule : rules) {
            Trigger trigger = rule.getTrigger();
            Attribute triggerAttribute = trigger.getAttribute();
            boolean applyChanges = false;

            toUpdate = new HashSet<>(triggerAttribute.getFields().size());

            switch (trigger.getEntity()) {
                case RuleNames.USER: {
                    switch (triggerAttribute.getName()) {
                        case RuleNames.LOCATION: {
                            if (triggerAttribute.getFields().size() == 2) {
                                List<Field> fi = triggerAttribute.getFields();
                                GeoPoint d = new GeoPoint(Double.parseDouble(fi.get(0).getValue()), Double.parseDouble(fi.get(1).getValue()));

                                myLocationNewOverlay.getMyLocation().bearingTo(d);
                                applyChanges = markerManager.isUserAt(myLocationNewOverlay.getMyLocation(), d);
                            }
                            break;
                        }
                        case RuleNames.SPEED: {
                            double currentSpeed = -1;
                            if (myLocationNewOverlay != null)
                                if (myLocationNewOverlay.getLastFix() != null)
                                    currentSpeed = myLocationNewOverlay.getLastFix().getSpeed();

                            applyChanges = triggerAttribute.matchedValues(currentSpeed + "");
                            break;
                        }
                        case RuleNames.APPROACHING:{
                            List<String> ids = triggerAttribute.getIds();
                            applyChanges = true;

                            if (ids.contains(RuleNames.ALL_INDIVIDUALLY)) {
                                containsAll = true;
                                toUpdate = markerManager.isUserApproachingAll(user);
                            } else {
                                for (String id : ids) {
                                    toUpdate.add(id);

                                    applyChanges = applyChanges && markerManager.isUserApproaching(user, id);
                                }
                            }
                            break;
                        }
                        case RuleNames.NEAR: {
                            List<String> ids = triggerAttribute.getIds();
                            applyChanges = true;

                            if (ids.contains(RuleNames.ALL_INDIVIDUALLY)) {
                                containsAll = true;
                                toUpdate = markerManager.isUserNearAll(user);
                            } else {
                                for (String id : ids) {
                                    toUpdate.add(id);

                                    applyChanges = applyChanges && markerManager.isUserNear(user, id);
                                }
                            }
                            break;
                        }
                        case RuleNames.MOVEMENT: {
                            if (activityState != null)
                                applyChanges = triggerAttribute.matchedValues(activityState);
                            break;
                        }
                    }
                    break;
                }

                case RuleNames.BATTERY: {
                    switch (triggerAttribute.getName()) {
                        case RuleNames.LEVEL: {
                            int battery = getBatteryLevel();
                            applyChanges = triggerAttribute.matchedValues(battery + "");
                            break;
                        }
                    }
                    break;
                }

                case RuleNames.MAP: {
                    switch (triggerAttribute.getName()) {
                        case RuleNames.ZOOM: {
                            double zoom = mapView.getZoomLevelDouble();
                            applyChanges = triggerAttribute.matchedValues(zoom + "");
                            break;
                        }
                        case RuleNames.THEME: {
                            applyChanges = triggerAttribute.matchedValues(mapTheme);
                            break;
                        }
                    }
                    break;
                }

                case RuleNames.PROXIMITY: {
                    switch (triggerAttribute.getName()) {
                        case RuleNames.DISTANCE: {
                            applyChanges = triggerAttribute.matchedValues(markerManager.getNearDistance() + "");
                            break;
                        }
                    }
                    break;
                }

                case RuleNames.APPLICATION: {
                    switch (triggerAttribute.getName()) {
                        case RuleNames.UPDATE_INTERVAL: {
                            applyChanges = triggerAttribute.matchedValues(interval + "");
                            break;
                        }
                    }
                    break;
                }
                case RuleNames.AMBIENT: {
                    switch (triggerAttribute.getName()) {
                        case RuleNames.LIGHT: {
                            applyChanges = triggerAttribute.matchedValues(getLight() + "");
                            break;
                        }
                    }
                    break;
                }
            }

            if (applyChanges) {
                applyChanges(rule.getAction(), map, mapView, myLocationNewOverlay, toUpdate);
            }
        }

        if (!containsAll)
            markerManager.updateDistance(user, toUpdate);
        else
            markerManager.updateAllDistances(user);
    }

    private void applyChanges(Action action, IMapController map, MapView mapView, MyLocationNewOverlay myLocationNewOverlay, Set<String> idsAllIndividuals) {
        Attribute attribute = action.getAttribute();
        String value = attribute.getFields().get(0).getValue();
        switch (action.getEntity()) {
            case RuleNames.MAP: {
                switch (attribute.getName()) {
                    case RuleNames.ZOOM: {
                        //map.setZoom(Double.parseDouble(value));
                        map.zoomTo(Double.parseDouble(value));
                        break;
                    }
                    case RuleNames.THEME: {
                        if (!mapTheme.equals(value))
                            mapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
                        break;
                    }
                    case RuleNames.CENTER: {
                        map.animateTo(myLocationNewOverlay.getMyLocation());
                        break;
                    }
                }
                break;
            }
            case RuleNames.MARKER: {
                switch (attribute.getName()) {
                    case RuleNames.OPACITY: {
                        markerManager.setOpacities(idsAllIndividuals, Integer.parseInt(value));
                        break;
                    }
                    case RuleNames.ROTATION: {
                        markerManager.setRotations(idsAllIndividuals, Integer.parseInt(value));
                        break;
                    }
                    case RuleNames.COLOR: {
                        boolean relevant = value.equals(RuleNames.RELEVANT);
                        markerManager.setRelevancies(idsAllIndividuals, relevant);
                        break;
                    }
                    case RuleNames.INFOWINDOW: {
                        boolean open = value.equals(RuleNames.OPEN);
                        markerManager.setInfoWindows(idsAllIndividuals, open);
                        break;
                    }
                }
                break;
            }
            case RuleNames.NEAREST: {
                GeoPoint user = myLocationNewOverlay.getMyLocation();
                if (user == null)
                    break;
                switch (attribute.getName()) {
                    case RuleNames.OPACITY: {
                        markerManager.setNearestOpacity(idsAllIndividuals, Integer.parseInt(value), user);
                        break;
                    }
                    case RuleNames.ROTATION: {
                        markerManager.setNearestRotation(idsAllIndividuals, Integer.parseInt(value), user);
                        break;
                    }
                    case RuleNames.COLOR: {
                        boolean relevant = value.equals(RuleNames.RELEVANT);
                        markerManager.setNearestRelevancy(idsAllIndividuals, relevant, user);
                        break;
                    }
                    case RuleNames.INFOWINDOW: {
                        boolean open = value.equals(RuleNames.OPEN);
                        markerManager.setNearestInfoWindow(idsAllIndividuals, open, user);
                        break;
                    }
                }
                break;
            }
            case RuleNames.PROXIMITY: {
                switch (attribute.getName()) {
                    case RuleNames.DISTANCE: {
                        markerManager.setNearDistance(Integer.parseInt(value));
                        break;
                    }
                }
            }
            case RuleNames.APPLICATION: {
                switch (attribute.getName()) {
                    case RuleNames.UPDATE_INTERVAL: {
                        interval = Integer.parseInt(value);
                        break;
                    }
                }
                break;
            }
        }
    }

    public int checkInterval() {
        return interval;
    }

    public void setInterval(int interv) {
        interval = interv;
    }

    /**
     * Obtains Data from API endpoints.
     *
     * @param url url to be fecthed.
     * @return Array of data in JSON format
     */
    private JSONArray obtainData(String url) {
        SyncRequest sm = new SyncRequest();
        JSONArray data = null;
        try {
            data = new JSONArray(sm.execute(url).get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Initializes the needed sensors and features according to the rules previously obtained.
     * @param rules JSONArray containing the admin-defined rules.
     * @param c Context
     */
    private void initRules(JSONArray rules, Context c) {

        this.rules = new HashSet<>(rules.length());
        for(int i = 0; i < rules.length(); i++) {
            try {
                JSONObject rule = rules.getJSONObject(i);
                this.rules.add(new Rule(rule));

                String triggerEntity = rule.getJSONObject("trigger").getString("entity");
                String triggerAttribute = rule.getJSONObject("trigger").getJSONObject
                        ("attribute").getString("name");

                // What to initialize
                switch (triggerEntity) {
                    case RuleNames.BATTERY: {
                        switch(triggerAttribute) {
                            case RuleNames.LEVEL: {
                                this.sensorReader.initBattery(c);
                                break;
                            }
                        }
                        break;
                    }
                    case RuleNames.USER: {
                        switch (triggerAttribute) {
                            case RuleNames.MOVEMENT: {
                                checkActivity = true;
                                break;
                            }
                        }
                        break;
                    }
                    case RuleNames.AMBIENT: {
                        switch (triggerAttribute) {
                            case RuleNames.LIGHT: {
                                sensorReader.initLight(sm);
                                checkLight = true;
                                break;
                            }
                        }
                        break;
                    }
                }
            } catch (JSONException je) {
                Log.e("RuleCont.initRules", je.toString());
            }
        }


        initialized = true;
    }

    /**
     * Returns light level
     *
     * @return light level
     */
    public float getLight() {
        return sensorReader.getLight();
    }


    /**
     * Calculates and returns the current battery level
     *
     * @return battery level
     */
    private int getBatteryLevel() {
        return sensorReader.getBatteryLevel();
    }

    /**
     * Returns phone's charging state
     *
     * @return boolean charging state
     */
    public boolean isCharging() {
        return sensorReader.isCharging();
    }

    public void setMapTheme(String theme) {
        mapTheme = theme;
    }

    public void setActivityState(String activityState) {
        this.activityState = activityState;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean getCheckActivity() {
        return checkActivity;
    }

    public boolean getCheckLight() {
        return checkLight;
    }

    private void getJSONArray(final VolleyCallback callback, String url) {
        RequestQueue queue = Volley.newRequestQueue(ctx);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                callback.onSuccessResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ActivityRecognizer", "ERROR!");
            }
        });

        queue.add(jsonArrayRequest);
    }
}
