package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockPreview;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.Direction;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(value = MultiblockSchemaInfo.class, remap = false)
public abstract class TieredMultiblockSchemaInfoMixin implements ITieredMultiblockPreview {

    @Shadow
    @Final
    private Int2IntMap userSliceRepeats;
    @Shadow
    @Final
    private IntList userDimensions;
    @Shadow
    @Final
    private Long2ObjectMap<BlockInfo> userGlobalBlockPreferences;
    @Shadow
    private @Nullable AbstractStructureHelper structureHelper;

    @Unique
    private int cosmiccore$previewTier;

    @Override
    public int cosmiccore$getPreviewTier() {
        return cosmiccore$previewTier;
    }

    @Override
    public void cosmiccore$setPreviewTier(int tier) {
        int selectedTier = Math.max(0, tier);
        if (selectedTier == cosmiccore$previewTier) return;
        cosmiccore$previewTier = selectedTier;
        userSliceRepeats.clear();
        userDimensions.clear();
        userGlobalBlockPreferences.clear();
        structureHelper = null;
    }

    @Redirect(
              method = "refreshSchema",
              at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"),
              require = 1)
    private Object cosmiccore$selectPreviewTierPattern(Supplier<IBlockPattern> original,
                                                       MultiblockMachineDefinition definition,
                                                       Direction frontFacing, Direction upFacing, boolean isFlipped,
                                                       @Nullable Runnable onSchemaRefresh) {
        if (TieredMultiblockPatterns.isTiered(definition)) {
            return TieredMultiblockPatterns.pattern(definition, cosmiccore$previewTier);
        }
        return original.get();
    }
}
