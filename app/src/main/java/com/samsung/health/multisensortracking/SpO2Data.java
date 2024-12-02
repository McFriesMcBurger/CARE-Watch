package com.samsung.health.multisensortracking;

import androidx.annotation.NonNull;

public class SpO2Data {
    int status; // Default status
    int spo2Value; // Default SpO2 value
    public SpO2Data(int status, int spo2Value) {
        this.status = status;
        this.spo2Value = spo2Value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSpo2Value() {
        return spo2Value;
    }

    @NonNull
    @Override
    public String toString() {
        return "SpO2Data{" +
                "status=" + status +
                ", spo2Value=" + spo2Value +
                '}';
    }
}
