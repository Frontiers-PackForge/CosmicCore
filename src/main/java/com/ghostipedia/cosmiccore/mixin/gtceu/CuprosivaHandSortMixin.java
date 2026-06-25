package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
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
public abstract class CuprosivaHandSortMixin {

    @Unique
    private static final int COSMICCORE$SORT_TICKS = 60;

    @Unique
    private boolean cosmiccore$isCuprosivaCrushed(ItemStack stack) {
        return stack.getItem() instanceof TagPrefixItem tagPrefixItem &&
                tagPrefixItem.tagPrefix == TagPrefix.crushed &&
                tagPrefixItem.material == CosmicBundleMaterials.Cuprosiva;
    }

    @Unique
    private void cosmiccore$giveTin(Player player, int nuggets) {
        while (nuggets > 0) {
            int give = Math.min(nuggets, 64);
            ItemStack tin = ChemicalHelper.get(TagPrefix.nugget, GTMaterials.Tin, give);
            nuggets -= give;
            if (!player.getInventory().add(tin)) {
                player.drop(tin, false);
            }
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortAnim(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (cosmiccore$isCuprosivaCrushed(stack)) {
            cir.setReturnValue(UseAnim.EAT);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (cosmiccore$isCuprosivaCrushed(stack)) {
            cir.setReturnValue(COSMICCORE$SORT_TICKS);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortUse(Level level, Player player, InteractionHand hand,
                                    CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack held = player.getItemInHand(hand);
        if (cosmiccore$isCuprosivaCrushed(held)) {
            player.startUsingItem(hand);
            cir.setReturnValue(InteractionResultHolder.consume(held));
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$sortFinish(ItemStack stack, Level level, LivingEntity entity,
                                       CallbackInfoReturnable<ItemStack> cir) {
        if (!cosmiccore$isCuprosivaCrushed(stack)) return;
        if (!level.isClientSide && entity instanceof Player player) {
            int count = stack.getCount();
            stack.shrink(count);
            cosmiccore$giveTin(player, count * 3);
        }
        cir.setReturnValue(stack);
    }
}
