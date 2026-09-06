package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface LeylineDeploymentBehavior {

    LeylineDeploymentBehavior NONE = new LeylineDeploymentBehavior() {};

    default Reservation prepare(ServerPlayer player, InteractionHand hand, LeylineDeploymentPlan plan) {
        return Reservation.NONE;
    }

    default void placed(ServerPlayer player, LeylineDeploymentPlan plan) {}

    interface Reservation {

        Reservation NONE = new Reservation() {};

        default boolean validate(ServerPlayer player, LeylineDeploymentPlan plan, Direction facing) {
            return true;
        }

        default List<ItemStack> reservedItems() {
            return List.of();
        }

        default void consume() {}

        default boolean afterFormation(ServerPlayer player, LeylineDeploymentPlan plan) {
            return true;
        }

        default void rollback(ServerLevel level) {}
    }
}
