package com.samsung.health.multisensortracking;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;

import android.util.Log;
import androidx.annotation.NonNull;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.ValueKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpO2Listener extends BaseListener {
    private final static String APP_TAG = "SpO2Listener";

    // Firebase Database reference
    private final DatabaseReference spO2Ref;

    // Last push timestamp
    private long lastPushTime = 0;

    public SpO2Listener(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);

        // Initialize Firebase reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        spO2Ref = database.getReference("sensors/spO2");

        final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
            @Override
            public void onDataReceived(@NonNull List<DataPoint> list) {
                for (DataPoint data : list) {
                    updateSpo2(data);
                }
            }

            @Override
            public void onFlushCompleted() {
                Log.i(APP_TAG, "onFlushCompleted called");
            }

            @Override
            public void onError(HealthTracker.TrackerError trackerError) {
                Log.e(APP_TAG, "onError called: " + trackerError);
                setHandlerRunning(false);
                if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.NoPermission);
                }
                if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.SdkPolicyError);
                }
            }
        };
        setTrackerEventListener(trackerEventListener);
    }

    public void restartTracker() {
        Log.i(APP_TAG, "Restarting SpO2 tracker...");
        stopTracker();
        startTracker(); // Restart the tracker
    }

    public void updateSpo2(DataPoint dataPoint) {
        try {
            // Extract status and SpO2 value
            int status = dataPoint.getValue(ValueKey.SpO2Set.STATUS);
            int spo2Value = dataPoint.getValue(ValueKey.SpO2Set.SPO2);

            // Create an SpO2Data object
            SpO2Data spO2Data = new SpO2Data(status, spo2Value);

            // Log the data
            Log.d(APP_TAG, spO2Data.toString());

            // Skip if SpO2 value is 0
            if (spo2Value == 0) {
                Log.d(APP_TAG, "Skipping Firebase push for SpO2 value of 0");
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPushTime >= 5000) { // Check if 5 seconds have passed
                lastPushTime = currentTime;

                // Prepare data for Firebase
                Map<String, Object> spO2Map = new HashMap<>();
                spO2Map.put("status", spO2Data.getStatus());
                spO2Map.put("spo2_value", spO2Data.getSpo2Value());
                spO2Map.put("timestamp", currentTime);

                // Push data to Firebase
                spO2Ref.push().setValue(spO2Map)
                        .addOnSuccessListener(aVoid -> Log.d(APP_TAG, "SpO2 data successfully pushed to Firebase"))
                        .addOnFailureListener(e -> Log.e(APP_TAG, "Failed to push SpO2 data to Firebase", e));

                TrackerDataNotifier.getInstance().notifySpO2TrackerObservers(status, spo2Value);
            }
        } catch (Exception e) {
            Log.e(APP_TAG, "Error processing SpO2 data", e);
        }
    }
}
