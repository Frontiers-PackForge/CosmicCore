package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.misc.IMetaMachineMixin;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicToolDefinitions;
import com.ghostipedia.cosmiccore.common.item.tcon.TiconUtils;
import com.ghostipedia.cosmiccore.common.item.tcon.base.ChargableModifiableItem;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.WrenchModeSwitchModifier;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mixin(MetaMachine.class)
public class MetaMachineMixin implements IMetaMachineMixin {

    public Pair<ToolDefinition, InteractionResult> ccore$onToolClick(ModifiableItem ticonItem, UseOnContext context) {
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        if (gridSide == null) gridSide = hitResult.getDirection();
        if (context.getPlayer() == null) Pair.of(null, InteractionResult.PASS);
        if (ticonItem.getToolDefinition() == CosmicToolDefinitions.WRENCHES) {
            if (ticonItem instanceof ChargableModifiableItem electricItem) {
                ItemStack stack = context.getItemInHand();
                long energyCost = electricItem.ENERGY_COST;
                long available = electricItem.getCharge(stack);
                if (available >= energyCost) {
                    electricItem.discharge(stack, energyCost, false);
                    return Pair.of(ticonItem.getToolDefinition(),
                            onWrenchClick(context.getPlayer(), context.getHand(), gridSide, hitResult));
                } else {
                    return Pair.of(ticonItem.getToolDefinition(), InteractionResult.PASS);
                }
            }
            return Pair.of(ticonItem.getToolDefinition(),
                    onWrenchClick(context.getPlayer(), context.getHand(), gridSide, hitResult));
        }
        return Pair.of(null, InteractionResult.PASS);
    }

    @Unique
    private InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                            BlockHitResult hitResult) {
        ItemStack stack = playerIn.getItemInHand(hand);
        var tool = ToolStack.from(stack);
        var modList = TiconUtils.getModifierList(tool);

        if (gridSide == ((MetaMachine) (Object) this).getFrontFacing() &&
                ((MetaMachine) (Object) this).allowExtendedFacing()) {
            ((MetaMachine) (Object) this).setUpwardsFacing(
                    playerIn.isShiftKeyDown() ? ((MetaMachine) (Object) this).getUpwardsFacing().getCounterClockWise() :
                            ((MetaMachine) (Object) this).getUpwardsFacing().getClockWise());
            return InteractionResult.sidedSuccess(((MetaMachine) (Object) this).isRemote());
        }
        if (playerIn.isShiftKeyDown()) {
            if (gridSide == ((MetaMachine) (Object) this).getFrontFacing() ||
                    !((MetaMachine) (Object) this).isFacingValid(gridSide)) {
                return InteractionResult.FAIL;
            }
            ((MetaMachine) (Object) this).setFrontFacing(gridSide);
        } else {
            for (var mod : modList) {
                if (mod.getModifier() instanceof WrenchModeSwitchModifier wmsm) {
                    var type = wmsm.getType(tool);

                    if (type.isItem()) {
                        if (((MetaMachine) (Object) this) instanceof IAutoOutputItem autoOutputItem &&
                                (!((MetaMachine) (Object) this).hasFrontFacing() ||
                                        gridSide != ((MetaMachine) (Object) this).getFrontFacing())) {
                            autoOutputItem.setOutputFacingItems(gridSide);
                        }
                    }
                    if (type.isFluid()) {
                        if (((MetaMachine) (Object) this) instanceof IAutoOutputFluid autoOutputFluid &&
                                (!((MetaMachine) (Object) this).hasFrontFacing() ||
                                        gridSide != ((MetaMachine) (Object) this).getFrontFacing())) {
                            autoOutputFluid.setOutputFacingFluids(gridSide);
                        }
                    }

                }
            }
        }
        return InteractionResult.sidedSuccess(((MetaMachine) (Object) this).isRemote());
    }
}
