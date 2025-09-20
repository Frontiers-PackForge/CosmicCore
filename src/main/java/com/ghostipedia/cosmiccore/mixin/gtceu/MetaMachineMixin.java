package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.misc.IMetaMachineMixin;
import com.ghostipedia.cosmiccore.common.item.tcon.TiconUtils;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.WrenchModeSwitchModifier;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mixin(MetaMachine.class)
public class MetaMachineMixin implements IMetaMachineMixin {

    public InteractionResult ccore$onToolClick(ModifiableItem ticonItem, UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        var tool = ToolStack.from(stack);
        var modList = TiconUtils.getModifierList(tool);
        for (var mod : modList) {
            if (mod.getModifier() instanceof WrenchModeSwitchModifier wmsm) {
                var type = wmsm.getType(tool);

                var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                        context.getClickedPos(), false);
                Direction gridSide = ICoverable.determineGridSideHit(hitResult);
                if (gridSide == null) gridSide = hitResult.getDirection();

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
                return InteractionResult.sidedSuccess(((MetaMachine) (Object) this).isRemote());
            }
        }
        return InteractionResult.PASS;
    }
}
