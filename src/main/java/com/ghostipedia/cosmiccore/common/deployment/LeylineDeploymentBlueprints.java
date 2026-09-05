package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class LeylineDeploymentBlueprints {

    private static final Map<BlueprintKey, LeylineDeploymentBlueprint> BLUEPRINTS = new java.util.LinkedHashMap<>(256,
            0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<BlueprintKey, LeylineDeploymentBlueprint> entry) {
            return size() > 256;
        }
    };

    private LeylineDeploymentBlueprints() {}

    private static MultiblockMachineDefinition deploymentMachine() {
        return CosmicMachines.POWER_TOWER;
    }

    public static ResourceLocation selectedId() {
        return deploymentMachine().getId();
    }

    public static boolean isPackage(ItemStack stack) {
        if (stack.is(CosmicItems.POWER_TOWER_DEPLOYMENT_PACKAGE.get())) return true;
        if (!stack.is(CosmicItems.LEYLINE_PACKAGE.get())) return false;
        try {
            return !LeylinePrefab.descriptor(stack).isEmpty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static synchronized LeylineDeploymentBlueprint register(LeylinePrefab prefab, Direction facing) {
        if (facing.getAxis().isVertical()) throw new IllegalArgumentException("Invalid blueprint facing");
        var blueprint = BLUEPRINTS.computeIfAbsent(new BlueprintKey(prefab.blueprintId(), facing),
                key -> prefab.blueprint(facing));
        int blocks = BLUEPRINTS.values().stream().mapToInt(value -> value.relativePlacements().size()).sum();
        var iterator = BLUEPRINTS.values().iterator();
        while (blocks > 131072 && BLUEPRINTS.size() > 1) {
            blocks -= iterator.next().relativePlacements().size();
            iterator.remove();
        }
        return blueprint;
    }

    public static synchronized LeylineDeploymentBlueprint resolve(ResourceLocation id, Direction facing) {
        var existing = BLUEPRINTS.get(new BlueprintKey(id, facing));
        if (existing != null) return existing;
        MultiblockMachineDefinition machine = deploymentMachine();
        if (!id.equals(machine.getId()) || facing.getAxis().isVertical()) {
            throw new IllegalArgumentException("Unknown deployment blueprint or invalid facing");
        }
        return BLUEPRINTS.computeIfAbsent(new BlueprintKey(id, facing),
                key -> GtmPatternDeploymentBlueprintResolver.resolve(
                        key.id(), machine, key.facing(), Direction.UP, false));
    }

    public static LeylineDeploymentBlueprint forPackage(ItemStack stack, Direction facing,
                                                        net.minecraft.world.level.Level level) {
        if (!isPackage(stack)) throw new IllegalArgumentException("Item is not a leyline package");
        if (stack.is(CosmicItems.LEYLINE_PACKAGE.get())) {
            var prefab = LeylinePrefab.fromStack(stack, level);
            if (prefab == null) return null;
            return register(prefab, facing);
        }
        return resolve(deploymentMachine().getId(), facing);
    }

    private record BlueprintKey(ResourceLocation id, Direction facing) {}
}
