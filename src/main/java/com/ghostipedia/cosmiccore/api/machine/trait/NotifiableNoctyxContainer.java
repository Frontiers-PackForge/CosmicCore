package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.INoctyxHandler;
import com.ghostipedia.cosmiccore.api.capability.recipe.NoctyxRecipeCapability;
import com.ghostipedia.cosmiccore.api.noctyx.NoctyxStack;
import com.ghostipedia.cosmiccore.api.transfer.noctyx.NoctyxContainer;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotifiableNoctyxContainer extends NotifiableRecipeHandlerTrait<NoctyxStack>
                                       implements INoctyxHandler, ICapabilityTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableNoctyxContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Persisted
    protected final NoctyxContainer[] storage;
    @Getter
    protected boolean allowSameTypes;
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;

    public NotifiableNoctyxContainer(MetaMachine machine, int slots, int capacity, IO handlerIO, IO capabilityIO) {
        super(machine);
        storage = new NoctyxContainer[slots];
        for (int i = 0; i < slots; i++) {
            storage[i] = new NoctyxContainer(capacity);
        }
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
        this.allowSameTypes = handlerIO == IO.IN;
    }

    @Override
    public int getSlots() {
        return storage.length;
    }

    @Override
    public @NotNull NoctyxStack getNoctyxInContainer(int slot) {
        return storage[slot].getNoctyx();
    }

    @Override
    public int getContainerCapacity(int slot) {
        return storage[slot].getCapacity();
    }

    @Override
    public boolean isNoctyxValid(int slot, @NotNull NoctyxStack stack) {
        return storage[slot].isNoctyxValid(stack);
    }

    @Override
    public int fill(@NotNull NoctyxStack resource, boolean simulate) {
        if (!canCapInput()) return 0;
        return fillInternal(resource, simulate);
    }

    public int fillInternal(NoctyxStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        var copied = resource.copy();
        NoctyxContainer existingStorage = null;
        if (!allowSameTypes) {
            for (var container : storage) {
                if (!container.getNoctyx().isEmpty() && container.getNoctyx().isSameType(resource)) {
                    existingStorage = container;
                    break;
                }
            }
        }
        if (existingStorage == null) {
            for (var container : storage) {
                var filled = container.fill(copied.copy(), simulate);
                if (filled > 0) {
                    copied.shrink(filled);
                    if (!allowSameTypes) {
                        break;
                    }
                }
                if (copied.isEmpty()) break;
            }
        } else {
            copied.shrink(existingStorage.fill(copied.copy(), simulate));
        }
        return resource.getAmount() - copied.getAmount();
    }

    @Override
    public @NotNull NoctyxStack drain(int maxDrain, boolean simulate) {
        if (canCapOutput()) {
            return drainInternal(maxDrain, simulate);
        }
        return NoctyxStack.EMPTY;
    }

    public NoctyxStack drainInternal(int maxDrain, boolean simulate) {
        if (maxDrain == 0) {
            return NoctyxStack.EMPTY;
        }
        NoctyxStack totalDrained = null;
        for (var container : storage) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = container.drain(maxDrain, simulate);
                if (totalDrained.isEmpty()) {
                    totalDrained = null;
                } else {
                    maxDrain -= totalDrained.getAmount();
                }
            } else {
                var copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                var drain = container.drain(copy, simulate);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? NoctyxStack.EMPTY : totalDrained;
    }

    @Override
    public @NotNull NoctyxStack drain(@NotNull NoctyxStack resource, boolean simulate) {
        if (canCapOutput()) {
            return drainInternal(resource, simulate);
        }
        return NoctyxStack.EMPTY;
    }

    public NoctyxStack drainInternal(NoctyxStack resource, boolean simulate) {
        if (!resource.isEmpty()) {
            var copied = resource.copy();
            for (var container : storage) {
                var candidate = copied.copy();
                copied.shrink(container.drain(candidate, simulate).getAmount());
                if (copied.isEmpty()) break;
            }
            copied.setAmount(resource.getAmount() - copied.getAmount());
            return copied;
        }
        return NoctyxStack.EMPTY;
    }

    protected static boolean testIngredient(@Nullable NoctyxStack stack, @NotNull NoctyxStack ingredient) {
        if (stack == null) {
            return false;
        }
        if (ingredient.isEmpty()) {
            return stack.isEmpty();
        }
        return stack.isSameType(ingredient);
    }

    @Override
    public List<NoctyxStack> handleRecipeInner(IO io, GTRecipe recipe, List<NoctyxStack> left,
                                               @Nullable String slotName, boolean simulate) {
        if (io != handlerIO) return left;
        if (io != IO.IN && io != IO.OUT) return left.isEmpty() ? null : left;
        // Store the NoctyxStack in each slot after an operation
        // Necessary for simulation since we don't actually modify the slot's contents
        // Doesn't hurt for execution, and definitely cheaper than copying the entire storage
        var visited = new NoctyxStack[storage.length];
        for (var it = left.iterator(); it.hasNext();) {
            var ingredient = it.next();
            if (ingredient.isEmpty()) {
                it.remove();
                continue;
            }
            if (ingredient.isEmpty()) {
                it.remove();
                continue;
            }
            if (io == IO.OUT && !allowSameTypes) {
                NoctyxContainer existing = null;
                for (var container : storage) {
                    if (!container.getNoctyx().isEmpty() && container.getNoctyx().isSameType(ingredient)) {
                        existing = container;
                        break;
                    }
                }
                if (existing != null) {
                    var filled = existing.fill(ingredient, simulate);
                    ingredient.shrink(filled);
                    if (ingredient.getAmount() <= 0) {
                        it.remove();
                    }
                    // Continue to next ingredient regardless of if we filled this ingredient completely
                    continue;
                }
            }
            for (int slot = 0; slot < storage.length; ++slot) {
                var stored = getNoctyxInContainer(slot);
                int amount = (visited[slot] == null ? stored.getAmount() : visited[slot].getAmount());
                if (io == IO.IN) {
                    if (amount == 0) continue;
                    if ((visited[slot] == null && testIngredient(stored, ingredient)) ||
                            testIngredient(visited[slot], ingredient)) {
                        var drained = storage[slot].drain(ingredient.getAmount(), simulate);
                        if (drained.getAmount() > 0) {
                            visited[slot] = drained.copy();
                            visited[slot].setAmount(amount - drained.getAmount());
                            ingredient.shrink(drained.getAmount());
                        }
                    }
                } else {
                    // IO.OUT && No slot already has this output
                    var output = ingredient.copy();
                    output.setAmount(ingredient.getAmount());
                    if (visited[slot] == null || visited[slot].isSameType(output)) {
                        int filled = storage[slot].fill(output, simulate);
                        if (filled > 0) {
                            visited[slot] = output.copy();
                            visited[slot].setAmount(filled);
                            ingredient.shrink(filled);
                            if (!allowSameTypes) {
                                if (ingredient.getAmount() <= 0) it.remove();
                                break;
                            }
                        }
                    }
                }
                if (ingredient.getAmount() <= 0) {
                    it.remove();
                    break;
                }
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public List<Object> getContents() {
        var ingredients = new ArrayList<NoctyxStack>();
        for (int i = 0; i < getSlots(); ++i) {
            var stack = getNoctyxInContainer(i);
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        return Arrays.asList(ingredients.toArray());
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (var container : storage) {
            if (container == null) {
                continue;
            }
            amount += container.getNoctyxAmount();
        }
        return amount;
    }

    @Override
    public RecipeCapability<NoctyxStack> getCapability() {
        return NoctyxRecipeCapability.CAP;
    }

    // boilerplate
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
