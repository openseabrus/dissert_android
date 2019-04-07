package dissert.dissert.utilities;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import dissert.dissert.MapViewActivity;
import dissert.dissert.R;

public class ActivityRecognizer extends IntentService {

    private static final String TAG = "ActivityRecognizer";
    private String lastState;

    public ActivityRecognizer() {
        super(TAG);
        lastState = "UNKNOWN";
    }

    /**
     * Called whenever an activity detection is available
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleActivityResult(result.getProbableActivities());
            publishActivity(result.getMostProbableActivity());
        }
    }

    private void publishActivity(DetectedActivity activity) {
        Log.d(TAG, "PUBLISH");
        Intent in = new Intent("activity-update");
        switch (activity.getType()) {
            case DetectedActivity.IN_VEHICLE: {
                in.putExtra("type", "IN_VEHICLE");
                lastState = "IN_VEHICLE";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                in.putExtra("type", "ON_BICYCLE");
                lastState = "ON_BICYCLE";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                in.putExtra("type", "ON_FOOT");
                lastState = "ON_FOOT";
                break;
            }
            case DetectedActivity.RUNNING: {
                in.putExtra("type", "RUNNING");
                lastState = "RUNNING";
                break;
            }
            case DetectedActivity.STILL: {
                in.putExtra("type", "STILL");
                lastState = "STILL";
                break;
            }
            case DetectedActivity.TILTING: {
                in.putExtra("type", "TILTING");
                lastState = "TILTING";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                in.putExtra("type", "UNKNOWN");
                lastState = "UNKNOWN";
                break;
            }
            case DetectedActivity.WALKING: {
                in.putExtra("type", "WALKING");
                lastState = "WALKING";
                break;
            }
        }

        in.putExtra("confidence", activity.getConfidence() + "");

        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }

    private void handleActivityResult(List<DetectedActivity> activities) {
        for (DetectedActivity activity : activities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.d(TAG, "IN_VEHICLE: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.d(TAG, "ON_BICYCLE: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.d(TAG, "ON_FOOT: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.d(TAG, "RUNNING: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.d(TAG, "STILL: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.d(TAG, "TILTING: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.d(TAG, "UNKNOWN: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.d(TAG, "WALKING: " + activity.getConfidence());
                    break;
                }
            }
        }
    }

}
