package com.ghostipedia.cosmiccore.api.capability;

public class HeatCapability {

    public static float adjustTempTowards(float temp, float target, float delta) {
        return adjustTempTowards(temp, target, delta, delta);
    }

    public static float adjustTempTowards(float temp, float target, float deltaPositive, float deltaNegative) {   // Get
                                                                                                                  // the
                                                                                                                  // Delta
                                                                                                                  // of
                                                                                                                  // the
                                                                                                                  // particular
                                                                                                                  // dimension,
                                                                                                                  // not
                                                                                                                  // sure
                                                                                                                  // how
                                                                                                                  // I
                                                                                                                  // want
                                                                                                                  // to
                                                                                                                  // do
                                                                                                                  // this
                                                                                                                  // yet,
                                                                                                                  // so
                                                                                                                  // let's
                                                                                                                  // just
                                                                                                                  // leave
                                                                                                                  // it
                                                                                                                  // as
                                                                                                                  // 1
        final float delta = 1;
        if (temp < target) {
            return Math.min(temp + delta * deltaPositive, target);
        } else if (temp > target) {
            return Math.max(temp - delta * deltaNegative, target);
        } else {
            return target;
        }
    }
}
