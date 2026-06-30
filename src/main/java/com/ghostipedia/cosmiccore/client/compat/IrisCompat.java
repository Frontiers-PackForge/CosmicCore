package com.ghostipedia.cosmiccore.client.compat;

import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public final class IrisCompat {

    private static boolean resolved;
    private static boolean available;
    private static Object api;
    private static Method isShaderPackInUse;

    private IrisCompat() {}

    public static boolean shadersActive() {
        if (!resolved) {
            resolve();
        }
        if (!available) {
            return false;
        }
        try {
            return (Boolean) isShaderPackInUse.invoke(api);
        } catch (ReflectiveOperationException e) {
            available = false;
            return false;
        }
    }

    private static void resolve() {
        resolved = true;
        if (!ModList.get().isLoaded("iris")) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            api = apiClass.getMethod("getInstance").invoke(null);
            isShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            available = true;
        } catch (ReflectiveOperationException e) {
            available = false;
        }
    }
}
