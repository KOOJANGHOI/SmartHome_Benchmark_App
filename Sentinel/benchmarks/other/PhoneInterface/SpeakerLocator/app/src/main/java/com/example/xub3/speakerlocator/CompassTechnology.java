package com.example.xub3.speakerlocator;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hadizadeh.positioning.controller.ExclusionTechnology;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.model.SignalInformation;

/**
 * Created by xubin on 4/28/16.
 */
public class CompassTechnology extends ExclusionTechnology {

    private float bearing;
    private Context context;
    private TextView compassTv;

    public CompassTechnology(Context context, String name, double allowedDelta) {
        super(name, allowedDelta / 2);
        this.context = context;
        compassTv = (TextView)((Activity)context).findViewById(R.id.hellotext);
        SensorManager mySensorManager = (SensorManager) context.
                getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> mySensors = mySensorManager.getSensorList(
                Sensor.TYPE_ORIENTATION);
        if (mySensors.size() > 0) {
            mySensorManager.registerListener(mySensorEventListener, mySensors.get(0),
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        signalData.put("compassSignal", new SignalInformation(bearing));
        return signalData;
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float compassBearing = (float) event.values[0];
            if (Math.abs(bearing - compassBearing) > 2) {
                compassTv.setText(String.valueOf(compassBearing));
            }
            bearing = compassBearing;

        }
    };

    @Override
    protected boolean isValueOutOfExclusionRange(
            Map<String, SignalInformation> signalData, double persistedValue) {
        boolean inRange = true;
        for (Map.Entry<String, SignalInformation> data : signalData.entrySet()) {
            double currentValue = data.getValue().getStrength();
            double min = persistedValue - allowedDelta;
            double max = persistedValue + allowedDelta;
            if (max >= 360) {
                max -= 360;
            }
            if (min <= 0) {
                min = 360 - min;
            }

            if (min > max) {
                if (!(currentValue >= min || currentValue <= max)) {
                    inRange = false;
                }
            } else {
                if (!(currentValue >= min && currentValue <= max)) {
                    inRange = false;
                }
            }
        }
        return inRange;
    }
}

