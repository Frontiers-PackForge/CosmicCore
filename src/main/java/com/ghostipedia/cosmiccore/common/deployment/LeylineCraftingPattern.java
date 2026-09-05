package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.*;
import appeng.api.stacks.*;

import java.util.*;

public final class LeylineCraftingPattern implements IPatternDetails {

    private final AEItemKey definition;
    private final LeylinePrefab prefab;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;

    public LeylineCraftingPattern(ItemStack stack, Level level) {
        definition = AEItemKey.of(stack);
        prefab = Objects.requireNonNull(LeylinePrefab.fromStack(stack, level));
        inputs = prefab.ingredients().entrySet().stream()
                .map(e -> new Input(AEItemKey.of(e.getKey()), e.getValue())).toArray(IInput[]::new);
        var output = prefab.stack(false);
        var descriptor = LeylinePrefab.descriptor(stack);
        if (descriptor.contains("payload") || descriptor.contains("positions")) {
            net.minecraft.world.item.component.CustomData.update(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    output, tag -> tag.put(LeylinePrefab.DATA, descriptor));
        }
        outputs = List.of(new GenericStack(AEItemKey.of(output), 1));
    }

    public LeylinePrefab prefab() {
        return prefab;
    }

    public ItemStack output() {
        return ((AEItemKey) outputs.getFirst().what()).toStack();
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LeylineCraftingPattern pattern && definition.equals(pattern.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    public static void register() {
        PatternDetailsHelper.registerDecoder(new IPatternDetailsDecoder() {

            @Override
            public boolean isEncodedPattern(ItemStack stack) {
                return stack.is(CosmicItems.LEYLINE_PATTERN.get());
            }

            @Override
            public IPatternDetails decodePattern(AEItemKey key, Level level) {
                if (key == null || !isEncodedPattern(key.toStack())) return null;
                try {
                    return new LeylineCraftingPattern(key.toStack(), level);
                } catch (RuntimeException ignored) {
                    return null;
                }
            }
        });
    }

    private record Input(AEItemKey key, long count) implements IInput {

        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[] { new GenericStack(key, 1) };
        }

        @Override
        public long getMultiplier() {
            return count;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return key.equals(input);
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
