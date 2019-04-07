package dissert.dissert.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class LightReader implements SensorEventListener {

    private SensorManager sm;
    private Sensor light;
    private float lux;

    public LightReader(SensorManager sm) {
        this.sm = sm;
        this.light = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.lux = -1f;
        sm.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Returns last light level
     * @return last light level, in lux
     */
    public float getLight() {
        return this.lux;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.lux = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
