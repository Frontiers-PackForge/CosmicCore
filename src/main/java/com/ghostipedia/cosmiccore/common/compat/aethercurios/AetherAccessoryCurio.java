package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class AetherAccessoryCurio implements ICurioItem {

    private final ResourceLocation equipSound;

    public AetherAccessoryCurio(ResourceLocation equipSound) {
        this.equipSound = equipSound;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getOptional(equipSound)
                .orElseGet(() -> SoundEvents.ARMOR_EQUIP_GENERIC.value());
        return new ICurio.SoundInfo(sound, 1.0f, 1.0f);
    }
}
