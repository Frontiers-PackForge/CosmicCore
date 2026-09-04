package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;
import com.ghostipedia.cosmiccore.common.deployment.GtmMultiblockFormationValidator;
import com.ghostipedia.cosmiccore.common.deployment.GtmPatternDeploymentBlueprintResolver;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentExecutor;
import com.ghostipedia.cosmiccore.common.machine.transmission.PowerTowerMachine;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.Locale;

// The Deployment behavior of specifically the power tower, for logical linking and other fun BS later on.
public final class PowerTowerDeploymentPackageBehavior implements IInteractionItem, IAddInformation {

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.FAIL;

        ItemStack stack = context.getItemInHand();
        BlockPos controllerPos = context.getClickedPos().relative(context.getClickedFace());
        Direction frontFacing = player.getDirection().getOpposite();
        if (!level.mayInteract(player, context.getClickedPos()) ||
                !player.mayUseItemAt(controllerPos, context.getClickedFace(), stack)) {
            return reportFailure(player, "permission");
        }

        try {
            var blueprint = GtmPatternDeploymentBlueprintResolver.resolve(
                    CosmicMachines.POWER_TOWER.getId(), CosmicMachines.POWER_TOWER, frontFacing, Direction.UP, false);
            var plan = blueprint.planAt(controllerPos);
            var result = LeylineDeploymentExecutor.deployAtomically(level, plan, player.getUUID(),
                    (serverLevel, pos, existing, placing) -> serverLevel.mayInteract(player, pos) &&
                            player.mayUseItemAt(pos, context.getClickedFace(), stack),
                    new GtmMultiblockFormationValidator());
            if (!result.committed()) {
                if (result.status() == LeylineDeploymentExecutor.Status.PREFLIGHT_FAILED &&
                        !result.failures().isEmpty()) {
                    var failure = result.failures().getFirst();
                    return reportFailure(player, "preflight." + failure.kind().name().toLowerCase(Locale.ROOT),
                            failure.pos().toShortString());
                }
                return reportFailure(player, errorStatusKey(result.status()));
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            verifyDeploymentAfterPlacementEvent(level, controllerPos, player);
            return InteractionResult.SUCCESS;
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.error("Failed to resolve or deploy the Power Tower package", exception);
            return reportFailure(player, "invalid");
        }
    }

    private static InteractionResult reportFailure(Player player, String status, Object... arguments) {
        player.displayClientMessage(Component.translatable(
                "cosmiccore.deployment.power_tower.error." + status, arguments)
                .withStyle(ChatFormatting.RED), true);
        return InteractionResult.FAIL;
    }

    private static void verifyDeploymentAfterPlacementEvent(ServerLevel level, BlockPos controllerPos,
                                                            Player player) {
        var server = level.getServer();
        // NeoForge may still cancel multi-block place even after useOn returns. Ugh.
        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            if (MetaMachine.getMachine(level, controllerPos) instanceof PowerTowerMachine tower && tower.isFormed()) {
                if (!player.isRemoved()) {
                    player.displayClientMessage(Component.translatable("cosmiccore.deployment.power_tower.success")
                            .withStyle(ChatFormatting.GREEN), true);
                }
                return;
            }
            PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(level);
            var node = data.graph().nodeAtController(controllerPos);
            if (node != null && data.graph().removeNode(node.id())) data.markGraphDirty();
        }));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines,
                                TooltipFlag flag) {
        lines.add(Component.translatable("cosmiccore.deployment.power_tower.tooltip.0")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("cosmiccore.deployment.power_tower.tooltip.1")
                .withStyle(ChatFormatting.DARK_AQUA));
        lines.add(Component.translatable("cosmiccore.deployment.power_tower.tooltip.2")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String errorStatusKey(LeylineDeploymentExecutor.Status status) {
        return status.name().toLowerCase(Locale.ROOT);
    }
}
