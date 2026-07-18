package com.ghostipedia.cosmiccore.mixin.qualityfoodultimine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.core.attachments.LevelData;
import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(targets = "dev.ftb.mods.ftbultimine.crops.VanillaCropLikeHandler", remap = false)
public abstract class VanillaCropLikeHandlerQualityFixMixin {

    @ModifyArg(
               method = "doHarvesting",
               at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"),
               index = 5)
    private ItemStack cosmiccore$useHeldHarvestTool(ItemStack ignored, @Local(argsOnly = true) Player player) {
        return player.getMainHandItem();
    }

    @ModifyExpressionValue(
                           method = "doHarvesting",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private List<ItemStack> cosmiccore$applyCropQuality(List<ItemStack> drops,
                                                        @Local(argsOnly = true) Player player,
                                                        @Local(argsOnly = true) BlockPos pos,
                                                        @Local(argsOnly = true) BlockState state) {
        Level level = player.level();
        Quality quality = LevelData.get(level, pos, true);
        BlockState farmland = level.getBlockState(pos.below());
        RegistryAccess registries = level.registryAccess();
        for (ItemStack drop : drops) {
            QualityUtils.applyQuality(drop, state, quality, player, farmland, registries);
        }
        return drops;
    }
}
