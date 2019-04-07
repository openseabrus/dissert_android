package dissert.dissert.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryReader extends BroadcastReceiver {

    private IntentFilter ifilter;
    private Context c;
    private boolean isCharging;

    public BatteryReader(Context c) {
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.c = c;
    }

    /**
     * Calculates and returns the current battery level
     * @return battery level (float)
     */
    public float getBatteryLevel() {
        Intent batteryStatus = c.registerReceiver(this, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (level / (float)scale) * 100f;
    }

    /**
     * Returns the charging state of the phone
     * @return boolean charging
     */
    public boolean isCharging() {
        return isCharging;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager
                .BATTERY_STATUS_FULL;

    }
}
