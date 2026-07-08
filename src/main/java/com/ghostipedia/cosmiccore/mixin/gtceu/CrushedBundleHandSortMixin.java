package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class CrushedBundleHandSortMixin {

    @Unique
    private static final int COSMICCORE$SORT_TICKS = 60;

    @Unique
    private static MaterialStack cosmiccore$handSortOutput(ItemStack stack) {
        if (!(stack.getItem() instanceof TagPrefixItem item) || item.tagPrefix != TagPrefix.crushed) {
            return null;
        }
        if (item.material == CosmicBundleMaterials.Cuprosiva) {
            return new MaterialStack(GTMaterials.Tin, 3);
        }
        if (item.material == CosmicBundleMaterials.Ferosine) {
            return new MaterialStack(GTMaterials.Gold, 2);
        }
        return null;
    }

    @Unique
    private static void cosmiccore$giveNuggets(Player player, Material material, int nuggets) {
        while (nuggets > 0) {
            int give = Math.min(nuggets, 64);
            ItemStack out = ChemicalHelper.get(TagPrefix.dustTiny, material, give);
            nuggets -= give;
            if (!player.getInventory().add(out)) {
                player.drop(out, false);
            }
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortAnim(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (cosmiccore$handSortOutput(stack) != null) {
            cir.setReturnValue(UseAnim.EAT);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (cosmiccore$handSortOutput(stack) != null) {
            cir.setReturnValue(COSMICCORE$SORT_TICKS);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortUse(Level level, Player player, InteractionHand hand,
                                    CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack held = player.getItemInHand(hand);
        if (cosmiccore$handSortOutput(held) != null) {
            player.startUsingItem(hand);
            cir.setReturnValue(InteractionResultHolder.consume(held));
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortFinish(ItemStack stack, Level level, LivingEntity entity,
                                       CallbackInfoReturnable<ItemStack> cir) {
        MaterialStack output = cosmiccore$handSortOutput(stack);
        if (output == null) return;
        if (!level.isClientSide && entity instanceof Player player) {
            int count = stack.getCount();
            stack.shrink(count);
            cosmiccore$giveNuggets(player, output.material(), count * (int) output.amount());
        }
        cir.setReturnValue(stack);
    }
}
