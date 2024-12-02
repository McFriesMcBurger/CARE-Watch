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
        Log.i(APP_TAG, "startTracker called ");

        // Null checks before calling toString
        if (healthTracker != null) {
            Log.d(APP_TAG, "healthTracker: " + healthTracker);
        } else {
            Log.e(APP_TAG, "healthTracker is null");
        }

        if (trackerEventListener != null) {
            Log.d(APP_TAG, "trackerEventListener: " + trackerEventListener);
        } else {
            Log.e(APP_TAG, "trackerEventListener is null");
        }

        if (!isHandlerRunning) {
            handler.post(() -> {
                // Practice 3: Set the event listener on the HealthTracker object
                if (healthTracker != null && trackerEventListener != null) {
                    healthTracker.setEventListener(trackerEventListener);

                    // Log event to Firebase for start of tracker
                    if (firebaseAnalytics != null) {
                        Log.d(APP_TAG, "Logging Firebase event: Tracker Started");
                        Bundle bundle = new Bundle();
                        bundle.putString("tracker_status", "started");
                        firebaseAnalytics.logEvent("tracker_event", bundle);
                    }

                    // Mark handler as running
                    setHandlerRunning(true);
                }
            });
        }
    }

    public void stopTracker() {
        Log.i(APP_TAG, "stopTracker called ");

        // Null checks before calling toString
        if (healthTracker != null) {
            Log.d(APP_TAG, "healthTracker: " + healthTracker);
        } else {
            Log.e(APP_TAG, "healthTracker is null");
        }

        if (trackerEventListener != null) {
            Log.d(APP_TAG, "trackerEventListener: " + trackerEventListener);
        } else {
            Log.e(APP_TAG, "trackerEventListener is null");
        }

        if (isHandlerRunning) {
            handler.post(() -> {
                // Practice 4: Try to remove the event listener by setting it to null
                if (healthTracker != null) {
                    healthTracker.setEventListener(null);
                }

                // Log event to Firebase for stop of tracker
                if (firebaseAnalytics != null) {
                    Log.d(APP_TAG, "Logging Firebase event: Tracker Stopped");
                    Bundle bundle = new Bundle();
                    bundle.putString("tracker_status", "stopped");
                    firebaseAnalytics.logEvent("tracker_event", bundle);
                }

                // Mark handler as not running
                setHandlerRunning(false);
                handler.removeCallbacksAndMessages(null);
            });
        }
    }
}

