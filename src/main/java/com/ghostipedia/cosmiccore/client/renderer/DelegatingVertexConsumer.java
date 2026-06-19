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

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
        this.delegate.setColor(r, g, b, a);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setNormal(float x, float y, float z) {
        this.delegate.setNormal(x, y, z);
        return this;
    }
}
