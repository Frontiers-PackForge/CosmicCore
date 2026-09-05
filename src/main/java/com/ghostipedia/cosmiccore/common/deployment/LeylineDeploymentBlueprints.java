package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class LeylineDeploymentBlueprints {

    private static final Map<BlueprintKey, LeylineDeploymentBlueprint> BLUEPRINTS = new HashMap<>();

    private LeylineDeploymentBlueprints() {}

    private static MultiblockMachineDefinition deploymentMachine() {
        return CosmicMachines.POWER_TOWER;
    }

    public static ResourceLocation selectedId() {
        return deploymentMachine().getId();
    }

    public static boolean isPackage(ItemStack stack) {
        return stack.is(CosmicItems.POWER_TOWER_DEPLOYMENT_PACKAGE.get());
    }

    public static synchronized LeylineDeploymentBlueprint resolve(ResourceLocation id, Direction facing) {
        MultiblockMachineDefinition machine = deploymentMachine();
        if (!id.equals(machine.getId()) || facing.getAxis().isVertical()) {
            throw new IllegalArgumentException("Unknown deployment blueprint or invalid facing");
        }
        return BLUEPRINTS.computeIfAbsent(new BlueprintKey(id, facing),
                key -> GtmPatternDeploymentBlueprintResolver.resolve(
                        key.id(), machine, key.facing(), Direction.UP, false));
    }

    public static LeylineDeploymentBlueprint forPackage(ItemStack stack, Direction facing) {
        if (!isPackage(stack)) throw new IllegalArgumentException("Item is not a leyline package");
        return resolve(deploymentMachine().getId(), facing);
    }

    private record BlueprintKey(ResourceLocation id, Direction facing) {}
}
