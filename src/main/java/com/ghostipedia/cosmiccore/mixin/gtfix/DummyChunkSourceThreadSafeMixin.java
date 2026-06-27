package com.ghostipedia.cosmiccore.mixin.gtfix;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import brachy.modularui.drawable.schema.DummyChunkSource;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = DummyChunkSource.class, remap = false)
public abstract class DummyChunkSourceThreadSafeMixin {

    @Unique
    private final Object cosmiccore$chunksLock = new Object();

    @WrapMethod(
                method = "getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
                remap = false)
    private ChunkAccess cosmiccore$lockGetChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load,
                                                Operation<ChunkAccess> original) {
        synchronized (this.cosmiccore$chunksLock) {
            return original.call(chunkX, chunkZ, requiredStatus, load);
        }
    }

    @WrapMethod(method = "clear()Z", remap = false)
    private boolean cosmiccore$lockClear(Operation<Boolean> original) {
        synchronized (this.cosmiccore$chunksLock) {
            return original.call();
        }
    }

    @WrapMethod(method = "getLoadedChunksCount()I", remap = false)
    private int cosmiccore$lockGetLoadedChunksCount(Operation<Integer> original) {
        synchronized (this.cosmiccore$chunksLock) {
            return original.call();
        }
    }
}
