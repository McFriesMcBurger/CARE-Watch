package com.samsung.health.multisensortracking;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.ValueKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeartRateListener extends BaseListener {
    private final static String APP_TAG = "HeartRateListener";

    private final DatabaseReference heartRateRef;
    private long lastPushTime = 0;

    // Counter to track the number of data blocks
    private int dataBlockCounter = 0;

    public HeartRateListener(FirebaseAnalytics firebaseAnalytics) {
        super(firebaseAnalytics);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        heartRateRef = database.getReference("sensors/heartRate");

        final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
            @Override
            public void onDataReceived(@NonNull List<DataPoint> list) {
                for (DataPoint dataPoint : list) {
                    readValuesFromDataPoint(dataPoint);
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

    public void readValuesFromDataPoint(DataPoint dataPoint) {
        try {
            final HeartRateData hrData = new HeartRateData();

            hrData.status = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS);
            hrData.hr = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE);
            List<Integer> hrIbiList = dataPoint.getValue(ValueKey.HeartRateSet.IBI_LIST);
            List<Integer> hrIbiStatus = dataPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST);

            if (hrIbiList != null && !hrIbiList.isEmpty()) {
                hrData.ibi = hrIbiList.get(hrIbiList.size() - 1);
            }
            if (hrIbiStatus != null && !hrIbiStatus.isEmpty()) {
                hrData.qIbi = hrIbiStatus.get(hrIbiStatus.size() - 1);
            }

            if (hrData.hr == 0) {
                Log.d(APP_TAG, "Skipping Firebase push for bpm value of 0");
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPushTime >= 2000) {
                lastPushTime = currentTime;

                // Generate a new unique key for this data block
                DatabaseReference newHeartRateRef = heartRateRef.push();
                String dataId = newHeartRateRef.getKey(); // Get the generated ID

                // Increment the counter
                dataBlockCounter++;

                // Include the ID and counter in the data
                Map<String, Object> heartRateData = new HashMap<>();
                heartRateData.put("id", dataId); // Add the unique ID
                heartRateData.put("counter", dataBlockCounter); // Add the counter
                heartRateData.put("bpm", hrData.hr);
                heartRateData.put("status", hrData.status);
                heartRateData.put("ibi", hrData.ibi);
                heartRateData.put("qualityIbi", hrData.qIbi);
                heartRateData.put("timestamp", currentTime);

                // Push the data to Firebase
                newHeartRateRef.setValue(heartRateData)
                        .addOnSuccessListener(aVoid -> Log.d(APP_TAG, "Data successfully pushed to Firebase with ID: " + dataId + " and Counter: " + dataBlockCounter))
                        .addOnFailureListener(e -> Log.e(APP_TAG, "Failed to push data to Firebase", e));

                TrackerDataNotifier.getInstance().notifyHeartRateTrackerObservers(hrData);
            }
        } catch (Exception e) {
            Log.e(APP_TAG, "Error reading values from DataPoint", e);
        }
    }
}
