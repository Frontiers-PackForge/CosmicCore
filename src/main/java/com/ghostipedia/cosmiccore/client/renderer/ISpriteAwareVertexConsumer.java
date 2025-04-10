package com.ghostipedia.cosmiccore.client.renderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface ISpriteAwareVertexConsumer extends VertexConsumer {

    void sprite(TextureAtlasSprite var1);
}
