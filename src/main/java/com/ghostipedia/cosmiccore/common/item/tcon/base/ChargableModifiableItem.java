package com.ghostipedia.cosmiccore.common.item.tcon.base;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

public class ChargableModifiableItem extends ModifiableItem {



    public ChargableModifiableItem(Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
                if(capability == GTCapability.CAPABILITY_ELECTRIC_ITEM) {
                    return LazyOptional.of(() -> {
                        return (T)new ElectricCapability(stack, );
                    });
                }
                return null;
            }
        };
    }

    public class ElectricCapability implements ICapabilityProvider, IElectricItem {

        private final ItemStack itemStack;

        private final long maxCharge;
        private final int tier;
        private final boolean chargeable;

        public ElectricCapability(ItemStack stack, long maxCharge, int tier, boolean chargeable) {
            this.itemStack = stack;
            this.maxCharge = maxCharge;
            this.tier = tier;
            this.chargeable = chargeable;
        }

        public void setCharge(long charge) {
            var nbt = itemStack.getOrCreateTag();
            nbt.putLong("charge", charge);
        }

        public void setMaxChargeOverride(long charge) {
            var nbt = itemStack.getOrCreateTag();
            nbt.putLong("maxCharge", charge);
        }

        public int getTier() {
            return this.tier;
        }

        @Override
        public boolean canProvideChargeExternally() {
            return false;
        }

        @Override
        public boolean chargeable() {
            return chargeable;
        }

        @Override
        public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
            return 0;
        }

        @Override
        public long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
            return 0;
        }

        @Override
        public long getTransferLimit() {
            return GTValues.V[getTier()];
        }

        @Override
        public long getMaxCharge() {
            var nbt = itemStack.getOrCreateTag();
            if(nbt.contains("maxCharge", Tag.TAG_LONG)) {
                return nbt.getLong("maxCharge");
            }
            return maxCharge;
        }

        @Override
        public long getCharge() {
            var nbt = itemStack.getOrCreateTag();
            if(nbt.getBoolean("infinite")) {
                return getMaxCharge();
            }
            if(nbt.contains("charge", Tag.TAG_LONG)) {
                return nbt.getLong("charge");
            }
            return maxCharge;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
            if(capability == GTCapability.CAPABILITY_ELECTRIC_ITEM) return LazyOptional.of(() -> (T)(IElectricItem)this);
            return LazyOptional.empty();
        }
    }
}
