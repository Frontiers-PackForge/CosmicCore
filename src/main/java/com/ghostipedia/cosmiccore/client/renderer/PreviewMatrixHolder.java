package com.ghostipedia.cosmiccore.client.renderer;

import org.joml.Matrix4f;

public final class PreviewMatrixHolder {

    public static final Matrix4f FRUSTUM = new Matrix4f();
    public static final Matrix4f PROJECTION = new Matrix4f();

    public static void update(Matrix4f frustum, Matrix4f projection) {
        if (frustum != null) {
            FRUSTUM.set(frustum);
        }
        if (projection != null) {
            PROJECTION.set(projection);
        }
    }

    private PreviewMatrixHolder() {}
}
