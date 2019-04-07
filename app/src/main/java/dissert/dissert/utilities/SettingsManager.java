package dissert.dissert.utilities;

import android.support.v7.widget.Toolbar;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import dissert.dissert.R;

public class SettingsManager {

    private static final String url = "http://gseabra.herokuapp.com/api/settings";
    private int interval;
    private boolean startAdapting;
    private MyLocationNewOverlay mlno;
    private String theme;

    public SettingsManager() {
        interval = 5000;
        startAdapting = true;
        theme = "Light";
    }

    public void setSettings(Toolbar appToolbar, IMapController mapController, MapView map) {
        SyncRequest sr = new SyncRequest();
        JSONObject data = null;
        try {
            data = new JSONObject(sr.execute(url).get());

            if (data.has("min_zoom"))
                map.setMinZoomLevel(data.getDouble("min_zoom"));

            if (data.has("max_zoom"))
                map.setMaxZoomLevel(data.getDouble("max_zoom"));

            if (data.has("init_zoom"))
                mapController.setZoom(data.getDouble("init_zoom"));

            if (data.has("init_theme")) {
                String theme = data.getString("init_theme");
                this.theme = theme;
                if (theme.equals("Dark"))
                    map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
            }

            if (data.has("init_center")) {
                JSONObject init_center = data.getJSONObject("init_center");
                if (init_center.has("latitude") && init_center.has("longitude"))
                    mapController.setCenter(new GeoPoint(init_center.getDouble("latitude"), init_center.getDouble("longitude")));
            }

            if (data.has("app_theme"))
                appToolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark);

            if (data.has("update_interval"))
                interval = data.getInt("update_interval");

            if (data.has("toolbar_title"))
                appToolbar.setTitle(data.getString("toolbar_title"));

            if (data.has("start_adapting"))
                startAdapting = data.getBoolean("start_adapting");


            mlno = new MyLocationNewOverlay(map);
            mlno.enableMyLocation();
            mlno.setDrawAccuracyEnabled(true);
            map.getOverlays().add(mlno);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean startAdapting() {
        return startAdapting;
    }

    public int getInterval() {
        return interval;
    }

    public MyLocationNewOverlay getMlno() {
        return  mlno;
    }

    public String getTheme() {
        return theme;
    }
}
