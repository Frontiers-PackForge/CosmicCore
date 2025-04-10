package com.ghostipedia.cosmiccore.client.renderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public abstract class DelegatingVertexConsumer implements ISpriteAwareVertexConsumer {

    protected final VertexConsumer delegate;

    protected DelegatingVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    public void sprite(TextureAtlasSprite sprite) {
        VertexConsumer var3 = this.delegate;
        if (var3 instanceof ISpriteAwareVertexConsumer spriteConsumer) {
            spriteConsumer.sprite(sprite);
        }
    }

    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        this.delegate.vertex(x, y, z);
        return this;
    }

    public @NotNull VertexConsumer color(int r, int g, int b, int a) {
        this.delegate.color(r, g, b, a);
        return this;
    }

    public @NotNull VertexConsumer uv(float u, float v) {
        this.delegate.uv(u, v);
        return this;
    }

    public @NotNull VertexConsumer overlayCoords(int u, int v) {
        this.delegate.overlayCoords(u, v);
        return this;
    }

    public @NotNull VertexConsumer uv2(int u, int v) {
        this.delegate.uv2(u, v);
        return this;
    }

    public @NotNull VertexConsumer normal(float x, float y, float z) {
        this.delegate.normal(x, y, z);
        return this;
    }

    public void endVertex() {
        this.delegate.endVertex();
    }

    public void defaultColor(int r, int g, int b, int a) {
        this.delegate.defaultColor(r, g, b, a);
    }

    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }
}
