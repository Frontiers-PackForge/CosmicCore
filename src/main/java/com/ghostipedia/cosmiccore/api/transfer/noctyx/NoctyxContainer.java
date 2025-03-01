package com.ghostipedia.cosmiccore.api.transfer.noctyx;

import com.ghostipedia.cosmiccore.api.capability.INoctyxHandler;
import com.ghostipedia.cosmiccore.api.noctyx.INoctyxContainer;
import com.ghostipedia.cosmiccore.api.noctyx.NoctyxStack;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class NoctyxContainer implements INoctyxHandler, INoctyxContainer, ITagSerializable<CompoundTag>,
                             IContentChangeAware {

    protected Predicate<NoctyxStack> validator;
    @Getter
    protected @NotNull NoctyxStack noctyx = NoctyxStack.EMPTY;
    @Getter
    protected int capacity;
    @Getter
    @Setter
    protected Runnable onContentsChanged = () -> {};

    public NoctyxContainer(int capacity, Predicate<NoctyxStack> validator) {
        this.capacity = capacity;
        this.validator = validator;
    }

    public NoctyxContainer(int capacity) {
        this(capacity, s -> true);
    }

    public NoctyxContainer(NoctyxStack stack) {
        this(stack.getAmount());
    }

    public boolean isEmpty() {
        return noctyx.isEmpty();
    }

    public void setNoctyx(@NotNull NoctyxStack noctyx) {
        this.noctyx = noctyx;
        onContentsChanged();
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull NoctyxStack getNoctyxInContainer(int slot) {
        return getNoctyx();
    }

    @Override
    public int getContainerCapacity(int slot) {
        return capacity;
    }

    @Override
    public int getNoctyxAmount() {
        return noctyx.getAmount();
    }

    @Override
    public boolean isNoctyxValid(int slot, @NotNull NoctyxStack stack) {
        return validator.test(stack);
    }

    public boolean isNoctyxValid(@NotNull NoctyxStack stack) {
        return validator.test(stack);
    }

    @Override
    public int fill(NoctyxStack resource, boolean simulate) {
        if (resource.isEmpty() || !isNoctyxValid(resource)) {
            return 0;
        }
        if (simulate) {
            if (isEmpty()) {
                return Math.min(capacity, resource.getAmount());
            }
            if (!noctyx.isSameType(resource)) {
                return 0;
            }
            return Math.min(capacity - noctyx.getAmount(), resource.getAmount());
        }
        if (isEmpty()) {
            noctyx = NoctyxStack.of(resource.getType(), Math.min(capacity, resource.getAmount()));
            onContentsChanged();
            return noctyx.getAmount();
        }
        if (!noctyx.isSameType(resource)) {
            return 0;
        }
        var filled = capacity - noctyx.getAmount();
        if (resource.getAmount() < filled) {
            noctyx.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            noctyx.setAmount(capacity);
        }
        if (filled > 0) {
            onContentsChanged();
        }
        return filled;
    }

    @Override
    public @NotNull NoctyxStack drain(@NotNull NoctyxStack resource, boolean simulate) {
        if (resource.isEmpty() || !resource.isSameType(noctyx)) {
            return NoctyxStack.EMPTY;
        }
        return drain(resource.getAmount(), simulate);
    }

    @Override
    public @NotNull NoctyxStack drain(int maxDrain, boolean simulate) {
        var drained = maxDrain;
        if (noctyx.getAmount() < drained) {
            drained = noctyx.getAmount();
        }
        var stack = NoctyxStack.of(noctyx, drained);
        if (!simulate && drained > 0) {
            noctyx.shrink(drained);
            onContentsChanged();
        }
        return stack;
    }

    public void onContentsChanged() {
        onContentsChanged.run();
    }

    @Override
    public CompoundTag serializeNBT() {
        return noctyx.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        setNoctyx(NoctyxStack.of(tag));
    }
}
