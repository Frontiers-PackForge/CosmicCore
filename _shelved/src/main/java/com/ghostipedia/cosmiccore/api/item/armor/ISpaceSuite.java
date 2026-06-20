package com.ghostipedia.cosmiccore.api.item.armor;

import com.ghostipedia.cosmiccore.common.airControl.IOxygenProvider;
import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.constants.ConstantComponents;
import earth.terrarium.adastra.common.registry.ModFluids;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.adastra.common.utils.TooltipUtils;
import earth.terrarium.botarium.common.fluid.FluidConstants;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Interface for CosmicCore space suits that integrate with our oxygen system.
 * Oxygen consumption for breathing is now handled by OxygenLogic - this interface
 * provides the oxygen and marks items as oxygen providers.
 */
public interface ISpaceSuite extends IOxygenProvider {

    /**
     * Tick handler for space suit - handles freezing prevention only.
     * Oxygen consumption is handled by OxygenLogic via IOxygenProvider.
     */
    default void tickOxygen(Level level, Player player, ItemStack itemStack) {
        if (level.isClientSide) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!(itemStack.getItem() instanceof SpaceArmorComponentItem)) return;
        // Prevent freezing while wearing space suit
        player.setTicksFrozen(0);
        // NOTE: Oxygen consumption is now handled by OxygenLogic.drainFromOxygenProviders()
    }

    // --- IOxygenProvider implementation ---

    @Override
    default boolean hasOxygen(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof SpaceArmorComponentItem suit)) return false;
        return suit.hasOxygen(player);
    }

    @Override
    default long consumeOxygen(ItemStack stack, Player player, long amount) {
        if (!(stack.getItem() instanceof SpaceArmorComponentItem suit)) return 0;
        long before = suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
        suit.consumeOxygen(stack, amount);
        long after = suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
        return before - after;
    }

    @Override
    default long getOxygenAmount(ItemStack stack) {
        if (!(stack.getItem() instanceof SpaceArmorComponentItem suit)) return 0;
        return suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
    }

    @Override
    default long getMaxOxygenCapacity(ItemStack stack) {
        if (!(stack.getItem() instanceof SpaceArmorComponentItem suit)) return 0;
        return suit.getFluidContainer(stack).getTankCapacity(0);
    }

    static boolean hasFullNanoSet(LivingEntity entity) {
        return hasFullSet(entity, CosmicItemTags.NANOMUSCLE_SPACE_SUITE);
    }

    static boolean hasFullQuantumSet(LivingEntity entity) {
        return hasFullSet(entity, CosmicItemTags.QUARKTECH_SPACE_SUITE);
    }

    static boolean hasFullSet(LivingEntity entity, TagKey<Item> tagKey) {
        return StreamSupport.stream(entity.getArmorSlots().spliterator(), false)
                .allMatch(itemStack -> itemStack.is(tagKey));
    }

    default void onArmorTick(Level Level, Player player, ItemStack itemStack, ArmorItem.Type type) {
        if (type == ArmorItem.Type.CHESTPLATE) this.tickOxygen(Level, player, itemStack);
    }

    default void addInfo(ItemStack itemStack, List<Component> lines, ArmorItem.Type type) {
        if (type == ArmorItem.Type.CHESTPLATE && itemStack.getItem() instanceof SpaceArmorComponentItem suit) {
            lines.add(TooltipUtils.getFluidComponent(
                    FluidUtils.getTank(itemStack),
                    FluidConstants.fromMillibuckets(suit.getFluidContainer(itemStack).getTankCapacity(0)),
                    ModFluids.OXYGEN.get()));
            TooltipUtils.addDescriptionComponent(lines, ConstantComponents.SPACE_SUIT_INFO);
        }
    }
}
