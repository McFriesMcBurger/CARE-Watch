package com.samsung.health.multisensortracking;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.samsung.android.service.health.tracking.ConnectionListener;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.HealthTrackerException;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.android.service.health.tracking.data.HealthTrackerType;

import java.util.List;

public class ConnectionManager {
    private final static String TAG = "Connection Manager";
    private final ConnectionObserver connectionObserver;
    private HealthTrackingService healthTrackingService = null;
    private final ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            Log.i(TAG, "Connected");
            connectionObserver.onConnectionResult(R.string.ConnectedToHs);
            if (!isSpO2Available(healthTrackingService)) {
                Log.i(TAG, "Device does not support SpO2 tracking");
                connectionObserver.onConnectionResult(R.string.NoSpo2Support);
            }
            if (!isHeartRateAvailable(healthTrackingService)) {
                Log.i(TAG, "Device does not support Heart Rate tracking");
                connectionObserver.onConnectionResult(R.string.NoHrSupport);
            }
        }

        @Override
        public void onConnectionEnded() {
            Log.i(TAG, "Disconnected");
        }

        @Override
        public void onConnectionFailed(HealthTrackerException e) {
            connectionObserver.onError(e);
        }
    };

    ConnectionManager(ConnectionObserver observer) {
        connectionObserver = observer;
    }

    public void connect(Context context) {
        healthTrackingService = new HealthTrackingService(connectionListener, context);
        healthTrackingService.connectService();
    }

    public void disconnect() {
        if (healthTrackingService != null)
            healthTrackingService.disconnectService();
    }

    public void initSpO2(SpO2Listener spO2Listener) {
        // (1) Get HealthTracker object using SPO2_ON_DEMAND
        HealthTracker spO2Tracker = healthTrackingService.getHealthTracker(HealthTrackerType.SPO2_ON_DEMAND);

        // (2) Pass it to spO2Listener using setHealthTracker function
        spO2Listener.setHealthTracker(spO2Tracker);

        // Set up handler for spO2Listener (BaseListener setup)
        setHandlerForBaseListener(spO2Listener);
    }
    public void initHeartRate(HeartRateListener heartRateListener) {
        // (1) Get HealthTracker object using HEART_RATE_CONTINUOUS
        HealthTracker heartRateTracker = healthTrackingService.getHealthTracker(HealthTrackerType.HEART_RATE_CONTINUOUS);

        // (2) Pass it to heartRateListener using setHealthTracker function
        heartRateListener.setHealthTracker(heartRateTracker);

        // Set up handler for heartRateListener (BaseListener setup)
        setHandlerForBaseListener(heartRateListener);
    }

    private void setHandlerForBaseListener(BaseListener baseListener) {
        baseListener.setHandler(new Handler(Looper.getMainLooper()));
    }

    private boolean isSpO2Available(@NonNull HealthTrackingService healthTrackingService) {
        final List<HealthTrackerType> availableTrackers = healthTrackingService.getTrackingCapability().getSupportHealthTrackerTypes();
        return availableTrackers.contains(HealthTrackerType.SPO2_ON_DEMAND);
    }

    private boolean isHeartRateAvailable(@NonNull HealthTrackingService healthTrackingService) {
        final List<HealthTrackerType> availableTrackers = healthTrackingService.getTrackingCapability().getSupportHealthTrackerTypes();
        return availableTrackers.contains(HealthTrackerType.HEART_RATE_CONTINUOUS);
    }
}
