package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockPreview;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.BuildTieredMultiblockPacket;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = TerminalBehavior.class, remap = false)
public class TieredTerminalBehaviorMixin {

    @Shadow
    private MultiblockMachineDefinition multiblockDefinition;
    @Shadow
    private MultiblockSchemaInfo multiblockSchemaInfo;

    @Unique
    private int cosmiccore$capturedStructureTier;
    @Unique
    private boolean cosmiccore$capturedTierPending;

    @Inject(method = "onItemUseFirst", at = @At("RETURN"))
    private void cosmiccore$captureStructureTier(net.minecraft.world.item.ItemStack itemStack,
                                                 UseOnContext context,
                                                 CallbackInfoReturnable<InteractionResult> cir) {
        if (MetaMachine.getMachine(context.getLevel(),
                context.getClickedPos()) instanceof ITieredMultiblockMachine tiered) {
            cosmiccore$capturedStructureTier = tiered.getStructureTier();
            cosmiccore$capturedTierPending = true;
        } else {
            cosmiccore$capturedStructureTier = 0;
            cosmiccore$capturedTierPending = false;
        }
    }

    @ModifyExpressionValue(
                           method = "clientPanel",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/gregtechceu/gtceu/integration/recipeviewer/widgets/MultiblockPreviewWidget;setFlipped(Z)Lcom/gregtechceu/gtceu/integration/recipeviewer/widgets/MultiblockPreviewWidget;"),
                           require = 1)
    private MultiblockPreviewWidget cosmiccore$initializeTerminalPreviewTier(MultiblockPreviewWidget widget) {
        if (TieredMultiblockPatterns.isTiered(multiblockDefinition) && cosmiccore$capturedTierPending) {
            ((ITieredMultiblockPreview) widget.getMultiblockSchemaInfo())
                    .cosmiccore$setPreviewTier(cosmiccore$capturedStructureTier);
            cosmiccore$capturedTierPending = false;
        }
        return widget;
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$buildSelectedTier(UseOnContext context,
                                              CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown() || !player.isCreative()) return;
        if (!(MetaMachine.getMachine(context.getLevel(),
                context.getClickedPos()) instanceof MultiblockControllerMachine controller) ||
                !(controller instanceof ITieredMultiblockMachine tiered) ||
                !TieredMultiblockPatterns.isTiered(controller.getDefinition()) || controller.isFormed()) {
            return;
        }
        if (!context.getLevel().isClientSide() && !MachineOwner.canOpenOwnerMachine(player, controller)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }
        if (context.getLevel().isClientSide()) {
            int selectedTier = tiered.getStructureTier();
            Map<BlockPos, BlockState> preferences = Map.of();
            if (multiblockSchemaInfo != null && multiblockDefinition == controller.getDefinition()) {
                multiblockSchemaInfo.refreshSchema(multiblockDefinition, controller.getFrontFacing(),
                        controller.getUpwardsFacing(), controller.isFlipped(), null);
                selectedTier = ((ITieredMultiblockPreview) multiblockSchemaInfo).cosmiccore$getPreviewTier();
                preferences = new HashMap<>();
                for (var entry : multiblockSchemaInfo.getStructureBlocks().entrySet()) {
                    preferences.put(entry.getKey(), entry.getValue().getBlockState());
                }
            }
            CCoreNetwork.sendToServer(new BuildTieredMultiblockPacket(controller.getBlockPos(), selectedTier,
                    preferences));
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(context.getLevel().isClientSide()));
    }
}
