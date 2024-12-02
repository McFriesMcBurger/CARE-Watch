package com.samsung.health.multisensortracking;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;

import android.os.Bundle;
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

    // Modify constructor to accept FirebaseAnalytics
    public SpO2Listener(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);  // Pass FirebaseAnalytics to the BaseListener constructor

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

    public void updateSpo2(DataPoint dataPoint) {
        try {
            // (1) Extract status from the DataPoint using the correct ValueKey
            int status = dataPoint.getValue(ValueKey.SpO2Set.STATUS);

            // (2) Extract SpO2 value only if the status is MEASUREMENT_COMPLETED
            int spo2Value = 0;
            if (status == SpO2Status.MEASUREMENT_COMPLETED) {
                spo2Value = dataPoint.getValue(ValueKey.SpO2Set.SPO2);
            }

            // (3) Log the data for debugging purposes
            Log.d(APP_TAG, "Status: " + status + ", SpO2 Value: " + spo2Value);

            // (4) Push the SpO2 data to Firebase Realtime Database
            Map<String, Object> spO2Data = new HashMap<>();
            spO2Data.put("status", status);
            spO2Data.put("spo2_value", spo2Value);
            spO2Data.put("timestamp", System.currentTimeMillis());

            // Push data to Firebase
            spO2Ref.push().setValue(spO2Data)
                    .addOnSuccessListener(aVoid -> Log.d(APP_TAG, "SpO2 data successfully pushed to Firebase"))
                    .addOnFailureListener(e -> Log.e(APP_TAG, "Failed to push SpO2 data to Firebase", e));

            // (5) Notify SpO2 observers (if needed)
            TrackerDataNotifier.getInstance().notifySpO2TrackerObservers(status, spo2Value);

            // (6) Optionally, log the event to Firebase Analytics
            if (firebaseAnalytics != null) {
                Log.d(APP_TAG, "Logging Firebase event: SpO2 Data");
                Bundle bundle = new Bundle();
                bundle.putInt("spO2_status", status);
                bundle.putInt("spO2_value", spo2Value);
                firebaseAnalytics.logEvent("spo2_data", bundle);
            }

        } catch (Exception e) {
            Log.e(APP_TAG, "Error processing SpO2 data", e);
        }
    }
}
