package com.samsung.health.multisensortracking;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;

import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.Value;
import com.samsung.android.service.health.tracking.data.ValueKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class SpO2ListenerTest {
    private final static int SPO2_VALUE = 97;

    @Mock
    TrackerDataObserver trackerDataObserver;

    @InjectMocks
    SpO2Listener spO2Listener;

    @Test
    public void shouldUpdateSpo2ValuesFromDataPoint_P() {
        //given
        final int status = SpO2Status.MEASUREMENT_COMPLETED;

        @SuppressWarnings("rawtypes")
        final Map<ValueKey, Value> values = new HashMap<>();
        values.put(ValueKey.SpO2Set.STATUS, new Value<>(status));
        values.put(ValueKey.SpO2Set.SPO2, new Value<>(SPO2_VALUE));
        final DataPoint dataPoint = new DataPoint(values);

        //when
        doAnswer(invocation -> {
            final int arg0 = invocation.getArgument(0);
            final int arg1 = invocation.getArgument(1);

            assertEquals(status, arg0);
            assertEquals(SPO2_VALUE, arg1);
            return null;
        }).when(trackerDataObserver).onSpO2TrackerDataChanged(anyInt(), anyInt());

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver);
        spO2Listener.updateSpo2(dataPoint);

        //then
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver);

    }

    @Test
    public void shouldNotUpdateSpo2ValueFromDataPointWhenStatusOtherThenCompleted_N() {
        //given
        final int status = SpO2Status.DEVICE_MOVING;

        @SuppressWarnings("rawtypes")
        final Map<ValueKey, Value> values = new HashMap<>();
        values.put(ValueKey.SpO2Set.STATUS, new Value<>(status));
        values.put(ValueKey.SpO2Set.SPO2, new Value<>(SPO2_VALUE));
        final DataPoint dataPoint = new DataPoint(values);

        //when
        doAnswer(invocation -> {
            final int arg0 = invocation.getArgument(0);
            final int arg1 = invocation.getArgument(1);

            assertEquals(status, arg0);
            assertNotEquals(SPO2_VALUE, arg1);
            return null;
        }).when(trackerDataObserver).onSpO2TrackerDataChanged(anyInt(), anyInt());

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver);
        spO2Listener.updateSpo2(dataPoint);

        //then
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver);

    }
}
