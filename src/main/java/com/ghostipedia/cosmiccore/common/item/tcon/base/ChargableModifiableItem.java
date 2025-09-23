package com.ghostipedia.cosmiccore.common.item.tcon.base;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
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
        // Example defaults: 100k max charge, LV tier, chargeable
        return new ElectricCapability(stack, 100_000L, GTValues.LV, true);
    }

    public static class ElectricCapability implements ICapabilityProvider, IElectricItem {

        private final ItemStack itemStack;
        private final long baseMaxCharge;
        private final int tier;
        private final boolean chargeable;

        private final LazyOptional<IElectricItem> holder = LazyOptional.of(() -> this);

        public ElectricCapability(ItemStack stack, long maxCharge, int tier, boolean chargeable) {
            this.itemStack = stack;
            this.baseMaxCharge = maxCharge;
            this.tier = tier;
            this.chargeable = chargeable;
        }

        private CompoundTag getOrCreateTag() {
            return itemStack.getOrCreateTag();
        }

        public void setCharge(long charge) {
            getOrCreateTag().putLong("charge", Math.max(0, Math.min(charge, getMaxCharge())));
        }

        public void setMaxChargeOverride(long charge) {
            getOrCreateTag().putLong("maxCharge", charge);
        }
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
            return capability == GTCapability.CAPABILITY_ELECTRIC_ITEM ? holder.cast() : LazyOptional.empty();
        }

        @Override
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
            if (!chargeable) return 0;
            if (chargerTier < tier) return 0;

            long transferLimit = ignoreTransferLimit ? amount : Math.min(amount, getTransferLimit());
            long current = getCharge();
            long max = getMaxCharge();
            long accepted = Math.min(max - current, transferLimit);

            if (!simulate && accepted > 0) {
                setCharge(current + accepted);
            }
            return accepted;
        }

        @Override
        public long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
            if (dischargerTier < tier) return 0;

            long transferLimit = ignoreTransferLimit ? amount : Math.min(amount, getTransferLimit());
            long current = getCharge();
            long extracted = Math.min(current, transferLimit);

            if (!simulate && extracted > 0) {
                setCharge(current - extracted);
            }
            return extracted;
        }

        @Override
        public long getTransferLimit() {
            return GTValues.V[getTier()];
        }

        @Override
        public long getMaxCharge() {
            var nbt = getOrCreateTag();
            if (nbt.contains("maxCharge", Tag.TAG_LONG)) {
                return nbt.getLong("maxCharge");
            }
            return baseMaxCharge;
        }

        @Override
        public long getCharge() {
            var nbt = getOrCreateTag();
            if (nbt.getBoolean("infinite")) {
                return getMaxCharge();
            }
            if (nbt.contains("charge", Tag.TAG_LONG)) {
                return nbt.getLong("charge");
            }
            return 0; // default empty
        }
    }
}
