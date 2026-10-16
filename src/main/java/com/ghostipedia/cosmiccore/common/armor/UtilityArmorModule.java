package com.ghostipedia.cosmiccore.common.armor;

import com.ghostipedia.cosmiccore.common.item.armor.boots.TravelerBootsItem;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ModuleData;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class UtilityArmorModule extends ItemModule {

    public enum Role {

        PDA(EquipmentSlot.HEAD),
        TRAVELER_BOOTS(EquipmentSlot.FEET);

        private final EquipmentSlot slot;

        Role(EquipmentSlot slot) {
            this.slot = slot;
        }

        public EquipmentSlot slot() {
            return slot;
        }

        public boolean accepts(ItemStack item) {
            return this == PDA ? WirelessPDABehavior.isPDA(item) : item.getItem() instanceof TravelerBootsItem;
        }
    }

    private final Role role;

    public UtilityArmorModule(ResourceLocation id, Role role) {
        super(id);
        this.role = role;
    }

    public Role role() {
        return role;
    }

    @Override
    public Component getInfo() {
        return Component.translatable(role == Role.PDA ? "cosmiccore.wireless_pda.tooltip.bind" :
                "tooltip.gtceu.armor.trait.fall");
    }

    @Override
    public Component getDisplayName(ModuleContext context) {
        return context.getData().getModuleItem().getHoverName();
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        if (!(stack.getItem() instanceof ModularArmorItem armor) || armor.getEquipmentSlot() != role.slot())
            return false;
        var modular = GTCapabilityHelper.getModularItem(stack);
        return modular != null && modular.getAllModuleInstances().stream().noneMatch(
                context -> context.getModule() instanceof UtilityArmorModule module && module.role() == role);
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack item) {
        return new ModuleData.BaseData(slot, module, item.copyWithCount(1), true);
    }

    @Override
    public InteractionResult useOn(ModuleContext module, UseOnContext context) {
        if (role != Role.PDA || !isEnabled(module)) return InteractionResult.PASS;
        var item = module.getData().getModuleItem().copy();
        var behavior = WirelessPDABehavior.behavior(item);
        if (behavior == null) return InteractionResult.PASS;
        var result = behavior.useOn(context, item);
        if (result.consumesAction() && !context.getLevel().isClientSide) {
            module.setData(module.getData().withModuleItem(item));
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ModuleContext module, Level level, Player player,
                                                  InteractionHand hand) {
        if (role != Role.PDA || !isEnabled(module)) return InteractionResultHolder.pass(player.getItemInHand(hand));
        var item = module.getData().getModuleItem().copy();
        var behavior = WirelessPDABehavior.behavior(item);
        if (behavior == null) return InteractionResultHolder.pass(player.getItemInHand(hand));
        var result = behavior.use(item, level, player, hand);
        if (result.getResult().consumesAction() && !level.isClientSide) {
            module.setData(module.getData().withModuleItem(result.getObject()));
        }
        return new InteractionResultHolder<>(result.getResult(), player.getItemInHand(hand));
    }
}
