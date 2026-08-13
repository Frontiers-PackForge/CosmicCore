package com.ghostipedia.cosmiccore.common.power.telemetry;

public interface CablePowerTelemetry {

    int OVERAMPERAGE = 1;
    int OVERVOLTAGE = 2;

    int cosmiccore$getOverloadCause();

    void cosmiccore$markOveramperage();

    void cosmiccore$markOvervoltage();
}
