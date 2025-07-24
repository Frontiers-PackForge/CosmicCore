package com.ghostipedia.cosmiccore.api.item.armor;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import earth.terrarium.adastra.common.tags.ModFluidTags;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidItem;
import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.ItemFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import earth.terrarium.botarium.common.fluid.utils.ClientFluidHooks;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import org.jetbrains.annotations.NotNull;

public class SpaceArmorComponentItem extends ArmorComponentItem
                                     implements BotariumFluidItem<WrappedItemFluidContainer> {

    protected final long tankSize;

    public SpaceArmorComponentItem(ArmorMaterial material, Type type, long size, Properties properties) {
        super(material, type, properties);
        this.tankSize = size;
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ClientFluidHooks.getFluidColor(FluidUtils.getTank(stack));
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        ItemFluidContainer fluidContainer = getFluidContainer(stack);
        return (int) (((double) fluidContainer.getFirstFluid().getFluidAmount() /
                fluidContainer.getTankCapacity(0)) * 13);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return FluidUtils.hasFluid(stack);
    }

    public @NotNull SpaceArmorComponentItem setArmorLogic(@NotNull IArmorLogic armorLogic) {
        return (SpaceArmorComponentItem) super.setArmorLogic(armorLogic);
    }

    @Override
    public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {
        return new WrappedItemFluidContainer(holder,
                new SimpleFluidContainer(tankSize, 1, (t, f) -> f.is(ModFluidTags.OXYGEN)));
    }

    public void consumeOxygen(ItemStack stack, long amount) {
        ItemStackHolder holder = new ItemStackHolder(stack);
        ItemFluidContainer container = FluidContainer.of(holder);
        if (container == null) return;

        FluidHolder extracted = container.extractFluid(container.getFirstFluid().copyWithAmount(amount), false);
        if (holder.isDirty() || extracted.getFluidAmount() > 0) {
            stack.setTag(holder.getStack().getTag());
        }
    }
}
