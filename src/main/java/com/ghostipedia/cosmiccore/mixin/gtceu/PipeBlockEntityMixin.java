package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.misc.IPipeBlockEntityMixin;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicToolDefinitions;
import com.ghostipedia.cosmiccore.common.item.tcon.TiconUtils;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.pipenet.PipeCoverContainer;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

@Mixin(value = PipeBlockEntity.class, remap = false)
public class PipeBlockEntityMixin implements IPipeBlockEntityMixin {

    @Shadow
    public PipeCoverContainer coverContainer;

    @Override
    public Pair<ToolDefinition, InteractionResult> ccore$onToolClick(ModifiableItem ticonItem, UseOnContext context) {
        if (((PipeBlockEntity<?, ?>) (Object) this) == null || context.getPlayer() == null) {
            return Pair.of(null, InteractionResult.FAIL);
        }

        var playerIn = context.getPlayer();
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        CoverBehavior coverBehavior = gridSide == null ? null : coverContainer.getCoverAtSide(gridSide);
        var gtToolType = TiconUtils.getGTToolType(ticonItem.getToolDefinition());
        if (((PipeBlockEntity<?, ?>) (Object) this).getPipeTuneTool() == gtToolType) {
            return Pair.of(ticonItem.getToolDefinition(), onTuneClick(playerIn, context.getHand(), hitResult));
        } else if (ticonItem.getToolDefinition() == CosmicToolDefinitions.SCREWDRIVERS) {
            if (coverBehavior != null) {
                return Pair.of(CosmicToolDefinitions.SCREWDRIVERS,
                        coverBehavior.onScrewdriverClick(playerIn, context.getHand(), hitResult));
            }
        }
        return Pair.of(null, InteractionResult.sidedSuccess(playerIn.level().isClientSide));
    }

    public InteractionResult onTuneClick(Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        if (gridSide == null) gridSide = hitResult.getDirection();
        if (player.isShiftKeyDown() && ((PipeBlockEntity<?, ?>) (Object) this).canHaveBlockedFaces()) {
            boolean isBlocked = ((PipeBlockEntity) (Object) this).isBlocked(gridSide);
            ((PipeBlockEntity) (Object) this).setBlocked(gridSide, !isBlocked);
        } else {
            boolean isOpen = ((PipeBlockEntity) (Object) this).isConnected(gridSide);
            ((PipeBlockEntity) (Object) this).setConnection(gridSide, !isOpen, false);
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
