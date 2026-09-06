package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.common.data.CosmicMachines;
import com.ghostipedia.cosmiccore.common.transmission.PowerTowerChain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class LeylineDeploymentBehaviors {

    private static final Map<ResourceLocation, LeylineDeploymentBehavior> BEHAVIORS = new HashMap<>();

    private LeylineDeploymentBehaviors() {}

    public static void register(ResourceLocation machine, LeylineDeploymentBehavior behavior) {
        if (BEHAVIORS.putIfAbsent(Objects.requireNonNull(machine), Objects.requireNonNull(behavior)) != null)
            throw new IllegalArgumentException("Duplicate leyline behavior: " + machine);
    }

    public static LeylineDeploymentBehavior forMachine(ResourceLocation machine) {
        return BEHAVIORS.getOrDefault(machine, LeylineDeploymentBehavior.NONE);
    }

    public static void registerDefaults() {
        register(CosmicMachines.POWER_TOWER.getId(), new LeylineDeploymentBehavior() {

            @Override
            public Reservation prepare(ServerPlayer player, InteractionHand hand, LeylineDeploymentPlan plan) {
                return PowerTowerChain.prepare(player, hand);
            }

            @Override
            public void placed(ServerPlayer player, LeylineDeploymentPlan plan) {
                PowerTowerChain.placed(player, plan.controllerPos());
            }
        });
    }
}
