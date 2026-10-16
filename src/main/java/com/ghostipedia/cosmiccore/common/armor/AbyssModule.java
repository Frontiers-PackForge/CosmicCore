package com.ghostipedia.cosmiccore.common.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class AbyssModule extends ItemModule {

    public enum Role {
        STEALTH,
        PRESSURE,
        REBREATHER
    }

    private final Role role;
    private final int tier;
    private final EquipmentSlot slot;

    public AbyssModule(ResourceLocation id, Role role, int tier, EquipmentSlot slot) {
        super(id);
        this.role = role;
        this.tier = tier;
        this.slot = slot;
    }

    public Role role() {
        return role;
    }

    public int tier() {
        return tier;
    }

    public EquipmentSlot slot() {
        return slot;
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        if (role == Role.REBREATHER && tier == 1) return false;
        if (!(stack.getItem() instanceof ModularArmorItem armor) || armor.getEquipmentSlot() != slot) return false;
        var modular = GTCapabilityHelper.getModularItem(stack);
        return modular != null && modular.getAllModuleInstances().stream()
                .noneMatch(context -> context.getModule() instanceof AbyssModule module && module.role() == role);
    }
}
