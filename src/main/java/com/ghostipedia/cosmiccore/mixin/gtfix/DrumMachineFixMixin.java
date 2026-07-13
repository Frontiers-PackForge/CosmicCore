package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.machine.storage.DrumMachine;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.fluids.FluidUtil;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DrumMachine.class, remap = false)
public abstract class DrumMachineFixMixin extends MetaMachine {

    public DrumMachineFixMixin(BlockEntityCreationInfo info) {
        super(info);
    }

    @Shadow
    @Final
    public AutoOutputTrait autoOutput;

    @Shadow
    @Final
    protected NotifiableFluidTank cache;

    @Overwrite
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        var player = context.getPlayer();
        var hand = context.getHand();
        if (FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()) {
            if (isRemote()) {
                return InteractionResult.SUCCESS;
            }
            return FluidUtil.interactWithFluidHandler(player, hand, cache) ? InteractionResult.SUCCESS :
                    InteractionResult.CONSUME;
        }
        return super.onUseWithItem(context);
    }

    @Overwrite
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (autoOutput.getFluidOutputDirection() != context.getGridSide()) {
            return InteractionResult.PASS;
        }
        boolean enabled = !autoOutput.isAutoOutputFluids();
        autoOutput.setAllowAutoOutputFluids(enabled);
        var player = context.getPlayer();
        if (!isRemote() && player != null) {
            player.displayClientMessage(Component.translatable(
                    enabled ? "gtceu.gui.fluid_auto_output.enabled" : "gtceu.gui.fluid_auto_output.disabled"), true);
        }
        return InteractionResult.sidedSuccess(isRemote());
    }
}
