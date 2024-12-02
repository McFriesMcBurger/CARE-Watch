package com.samsung.health.multisensortracking;

import android.os.Handler;
import android.util.Log;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import android.os.Bundle;

public class BaseListener {

    private final static String APP_TAG = "BaseListener";

    private Handler handler;
    private HealthTracker healthTracker;
    private boolean isHandlerRunning = false;
    private HealthTracker.TrackerEventListener trackerEventListener = null;
    public final FirebaseAnalytics firebaseAnalytics;

    // Constructor to initialize FirebaseAnalytics
    public BaseListener(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public void setHealthTracker(HealthTracker tracker) {
        healthTracker = tracker;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setHandlerRunning(boolean handlerRunning) {
        isHandlerRunning = handlerRunning;
    }

    public void setTrackerEventListener(HealthTracker.TrackerEventListener tracker) {
        trackerEventListener = tracker;
    }

    public void startTracker() {
        Log.i(APP_TAG, "startTracker called");

        // Null checks before calling toString
        if (healthTracker == null) {
            Log.e(APP_TAG, "healthTracker is null");
            return;
        }

        if (trackerEventListener == null) {
            Log.e(APP_TAG, "trackerEventListener is null");
            return;
        }

        // Ensure the handler is not already running to avoid multiple start requests
        if (!isHandlerRunning) {
            handler.post(() -> {
                // Set the event listener on the HealthTracker object
                healthTracker.setEventListener(trackerEventListener);

                // Log event to Firebase for start of tracker
                logTrackerEvent("started");

                // Mark handler as running
                setHandlerRunning(true);
            });
        } else {
            Log.w(APP_TAG, "Tracker is already running. Ignoring start request.");
        }
    }

    public void stopTracker() {
        Log.i(APP_TAG, "stopTracker called");

        // Null checks before calling toString
        if (healthTracker == null) {
            Log.e(APP_TAG, "healthTracker is null");
            return;
        }

        if (trackerEventListener == null) {
            Log.e(APP_TAG, "trackerEventListener is null");
            return;
        }

        // Only stop tracker if it is running
        if (isHandlerRunning) {
            handler.post(() -> {
                // Use unsetEventListener instead of setEventListener(null)
                healthTracker.unsetEventListener();

                // Log event to Firebase for stop of tracker
                logTrackerEvent("stopped");

                // Mark handler as not running
                setHandlerRunning(false);

                // Optionally, clear any scheduled tasks or messages for the handler
                handler.removeCallbacksAndMessages(null);
            });
        } else {
            Log.w(APP_TAG, "Tracker is not running. Ignoring stop request.");
        }
    }

    private void logTrackerEvent(String status) {
        if (firebaseAnalytics != null) {
            Log.d(APP_TAG, "Logging Firebase event: Tracker " + status);
            Bundle bundle = new Bundle();
            bundle.putString("tracker_status", status);
            firebaseAnalytics.logEvent("tracker_event", bundle);
        } else {
            Log.e(APP_TAG, "FirebaseAnalytics is null. Unable to log event.");
        }
    }
}
