package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.item.datacomponents.LargeFluidContent;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

import net.minecraft.core.component.DataComponentMap;
import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = QuantumTankMachine.class, remap = false)
public abstract class QuantumTankComponentPersistenceFixMixin extends TieredMachine {

    @Shadow
    protected FluidStack stored;

    @Shadow
    protected long storedAmount;

    protected QuantumTankComponentPersistenceFixMixin(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        LargeFluidContent content = componentInput.getOrDefault(
                GTDataComponents.LARGE_FLUID_CONTENT, LargeFluidContent.EMPTY);
        stored = content.stored();
        storedAmount = content.amount();
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!stored.isEmpty()) {
            components.set(GTDataComponents.LARGE_FLUID_CONTENT, new LargeFluidContent(stored, storedAmount));
        }
    }
}
