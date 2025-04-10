package com.ghostipedia.cosmiccore.client.gui;

import com.ghostipedia.cosmiccore.client.renderer.DelegatingVertexConsumer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class AlphaOverrideVertexConsumer extends DelegatingVertexConsumer {

    private final int alpha;

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, double alpha) {
        this(delegate, (int) ((double) 255.0F * alpha));
    }

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, int alpha) {
        super(delegate);
        this.alpha = alpha;
    }

    public @NotNull VertexConsumer color(int r, int g, int b, int a) {
        return super.color(r, g, b, this.alpha);
    }
}
