package com.samsung.health.multisensortracking;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Handler;

import com.samsung.android.service.health.tracking.HealthTracker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseListenerTest {

    @Rule
    public ExpectedException expectedException;
    @Mock
    Handler handler;
    @Mock
    HealthTracker healthTracker;
    @Mock
    HealthTracker.TrackerEventListener trackerEventListener;
    @InjectMocks
    BaseListener baseListener;

    @Test
    public void shouldInteractWithHealthTrackerWhenStartTracker_P() {
        //given
        baseListener.setHandler(handler);
        baseListener.setHealthTracker(healthTracker);
        baseListener.setTrackerEventListener(trackerEventListener);
        baseListener.setHandlerRunning(false);

        //when
        when(handler.post(any(Runnable.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        });
        baseListener.startTracker();

        //then
        verify(healthTracker).setEventListener(any());
    }

    @Test
    public void shouldNoInteractWithHealthTrackerWhenStartTrackerAndAlreadyStarted_N() {
        //given
        baseListener.setHandler(handler);
        baseListener.setHealthTracker(healthTracker);
        baseListener.setTrackerEventListener(trackerEventListener);
        baseListener.setHandlerRunning(true);

        //when
        baseListener.startTracker();

        //then
        verify(healthTracker, never()).setEventListener(any());
    }

    @Test
    public void shouldInteractWithHealthTrackerWhenStopTracker_P() {
        //given
        baseListener.setHandler(handler);
        baseListener.setHealthTracker(healthTracker);
        baseListener.setTrackerEventListener(trackerEventListener);
        baseListener.setHandlerRunning(true);

        //when
        baseListener.stopTracker();

        //then
        verify(healthTracker).unsetEventListener();
        verify(handler).removeCallbacksAndMessages(any());
    }

    @Test
    public void shouldNotInteractWithHealthTrackerWhenStopTrackerAndAlreadyStopped_N() {
        //given
        baseListener.setHandler(handler);
        baseListener.setHealthTracker(healthTracker);
        baseListener.setTrackerEventListener(trackerEventListener);
        baseListener.setHandlerRunning(false);

        //when
        baseListener.stopTracker();

        //then
        verify(healthTracker, never()).unsetEventListener();
        verify(handler, never()).removeCallbacksAndMessages(any());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenHealthTrackerIsSetToNull_N() {
        //given
        baseListener.setHandler(handler);
        baseListener.setTrackerEventListener(trackerEventListener);
        baseListener.setHandlerRunning(false);
        baseListener.setHealthTracker(null);

        expectedException.expect(NullPointerException.class);

        baseListener.startTracker();

    }
}
