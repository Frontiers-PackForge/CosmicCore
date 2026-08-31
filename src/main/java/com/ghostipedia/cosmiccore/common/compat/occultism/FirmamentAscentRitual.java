package com.ghostipedia.cosmiccore.common.compat.occultism;

import com.ghostipedia.cosmiccore.common.dimension.FirmamentAscentLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity;
import com.klikli_dev.occultism.common.ritual.Ritual;
import com.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import org.jetbrains.annotations.Nullable;

public final class FirmamentAscentRitual extends Ritual {

    public FirmamentAscentRitual(RitualRecipe recipe) {
        super(recipe);
    }

    @Override
    public boolean start(
                         Level level,
                         BlockPos goldenBowlPosition,
                         GoldenSacrificialBowlBlockEntity blockEntity,
                         @Nullable ServerPlayer castingPlayer,
                         ItemStack activationItem) {
        if (castingPlayer == null || !level.dimension().equals(Level.OVERWORLD)) {
            if (castingPlayer != null) {
                castingPlayer.displayClientMessage(
                        Component.translatable("cosmiccore.firmament.ritual.overworld_only"), true);
            }
            return false;
        }
        // TODO(firmament): require the firmament deed when it is obtainable
        if (FirmamentAscentLogic.isAscending(castingPlayer)) {
            return false;
        }
        return super.start(level, goldenBowlPosition, blockEntity, castingPlayer, activationItem);
    }

    @Override
    public void finish(
                       Level level,
                       BlockPos goldenBowlPosition,
                       GoldenSacrificialBowlBlockEntity blockEntity,
                       @Nullable ServerPlayer castingPlayer,
                       ItemStack activationItem) {
        if (castingPlayer == null || !FirmamentAscentLogic.begin(castingPlayer)) return;
        activationItem.shrink(1);
        super.finish(level, goldenBowlPosition, blockEntity, castingPlayer, activationItem);
    }
}
