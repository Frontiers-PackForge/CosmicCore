package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.api.item.armor.SpaceArmorComponentItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Debug(export = true)
@Mixin(SpaceSuitItem.class)
public abstract class AdAstraSpaceSuitItemMixin {

    /**
     * @author MrQuentinet
     * @reason In order for the GTArmor to work with AdAstra oxygen I needed to change the way the oxygen check is done.
     */
    @Overwrite(remap = false)
    public static long getOxygenAmount(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return 0;
        var stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof SpaceSuitItem suit) {
            return suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
        } else if (stack.getItem() instanceof SpaceArmorComponentItem suit) {
            return suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
        } else return 0;
    }
}
