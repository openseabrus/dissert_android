package dissert.dissert.sensors;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;

public class SensorReader {

    private LightReader lir;
    private InternetReader ir;
    private BatteryReader br;


    public SensorReader(Context c, SensorManager sm, ConnectivityManager cm) {
        lir = new LightReader(sm);
        ir = new InternetReader(cm);
        br = new BatteryReader(c);
    }

    public SensorReader() {

    }

    public void initLight(SensorManager sm) {
        this.lir = new LightReader(sm);
    }

    public void initBattery(Context c) {
        this.br = new BatteryReader(c);
    }


    /**
     * Returns light level
     * @return light level
     */
    public float getLight() {
        return lir.getLight();
    }

    /**
     * Identifies the current connection status from InternetReader and returns its String
     * translation
     * @return String connection - connection status
     */
    public String getConnection() {
        int connection = ir.getConnectionStatus();
        switch (connection) {
            case -2: return "Unknown";
            case -1: return "Disconnected";
            case 0: return "Disconnecting...";
            case 1: return "Connecting...";
            case 2: return "Connected (Mobile)";
            case 3: return "Connected (WiFi)";
            default: return null;
        }
    }

    /**
     * Calculates and returns the current battery level
     * @return battery level
     */
    public int getBatteryLevel() {
        return (int) br.getBatteryLevel();
    }

    /**
     * Returns phone's charging state
     * @return boolean charging state
     */
    public boolean isCharging() {
        return br.isCharging();
    }

}
