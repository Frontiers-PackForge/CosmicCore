package com.ghostipedia.cosmiccore.api.item.armor;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import earth.terrarium.adastra.common.tags.ModFluidTags;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidItem;
import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import earth.terrarium.botarium.common.fluid.utils.ClientFluidHooks;
import earth.terrarium.botarium.common.item.ItemStackHolder;

public class SpaceArmorComponentItem extends ArmorComponentItem
                                     implements BotariumFluidItem<WrappedItemFluidContainer> {

    protected final long tankSize;

    public SpaceArmorComponentItem(ArmorMaterial material, Type type, long size, Properties properties) {
        super(material, type, properties);
        this.tankSize = size;
    }

    @Override
    public void attachComponents(IItemComponent... components) {
        super.attachComponents(components);

        IDurabilityBar durabilityBar = new IDurabilityBar() {

            @Override
            public int getBarColor(ItemStack stack) {
                return ClientFluidHooks.getFluidColor(FluidUtils.getTank(stack));
            }

            @Override
            public int getBarWidth(ItemStack stack) {
                var fluidContainer = getFluidContainer(stack);
                return (int) (((double) fluidContainer.getFirstFluid().getFluidAmount() /
                        fluidContainer.getTankCapacity(0)) * 13);
            }

            @Override
            public boolean isBarVisible(ItemStack stack) {
                return FluidUtils.hasFluid(stack);
            }

            @Override
            public boolean showEmptyBar(ItemStack itemStack) {
                return false;
            }
        };

        this.components.add(durabilityBar);
        durabilityBar.onAttached(this);
    }

    public SpaceArmorComponentItem setArmorLogic(IArmorLogic armorLogic) {
        return (SpaceArmorComponentItem) super.setArmorLogic(armorLogic);
    }

    @Override
    public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {
        return new WrappedItemFluidContainer(holder,
                new SimpleFluidContainer(FluidConstants.fromMillibuckets(tankSize), 1,
                        (t, f) -> f.is(ModFluidTags.OXYGEN)));
    }

    public long getOxygenAmount(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return 0;
        var stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (!(stack.getItem() instanceof SpaceArmorComponentItem suit)) return 0;
        return suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
    }

    public boolean hasOxygen(Entity entity) {
        return getOxygenAmount(entity) > FluidConstants.fromMillibuckets(1);
    }

    public void consumeOxygen(ItemStack stack, long amount) {
        ItemStackHolder holder = new ItemStackHolder(stack);
        var container = FluidContainer.of(holder);
        if (container == null) return;
        FluidHolder extracted = container
                .extractFluid(container.getFirstFluid().copyWithAmount(FluidConstants.fromMillibuckets(amount)), false);
        if (holder.isDirty() || extracted.getFluidAmount() > 0) stack.setTag(holder.getStack().getTag());
    }
}
