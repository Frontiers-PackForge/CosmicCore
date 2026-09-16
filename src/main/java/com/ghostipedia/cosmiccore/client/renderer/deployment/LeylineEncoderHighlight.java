package com.ghostipedia.cosmiccore.client.renderer.deployment;

import net.minecraft.core.BlockPos;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;

import java.util.Map;
import java.util.WeakHashMap;

public final class LeylineEncoderHighlight {

    private static final long DURATION_NANOS = 700_000_000L;
    private static final Map<BaseSchemaRenderer, Flash> FLASHES = new WeakHashMap<>();

    private LeylineEncoderHighlight() {}

    public static void flash(BaseSchemaRenderer renderer, BlockPos pos) {
        if (renderer != null && pos != null) FLASHES.put(renderer, new Flash(pos.immutable(), System.nanoTime()));
    }

    public static BlockPos active(BaseSchemaRenderer renderer) {
        Flash flash = FLASHES.get(renderer);
        if (flash == null) return null;
        if (System.nanoTime() - flash.started() <= DURATION_NANOS) return flash.pos();
        FLASHES.remove(renderer);
        return null;
    }

    public static void clear(BaseSchemaRenderer renderer) {
        if (renderer != null) FLASHES.remove(renderer);
    }

    private record Flash(BlockPos pos, long started) {}
}
