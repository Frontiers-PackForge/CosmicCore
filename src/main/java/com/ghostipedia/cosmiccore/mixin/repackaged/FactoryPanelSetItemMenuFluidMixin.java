package com.ghostipedia.cosmiccore.mixin.repackaged;

import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;
import com.ghostipedia.cosmiccore.common.compat.create.FluidGaugeSetItemMenuExtension;
import com.ghostipedia.cosmiccore.mixin.deployer.StockPanelBehaviourAccessor;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.fluids.FluidStack;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import com.simibubi.create.foundation.utility.CreateLang;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FactoryPanelSetItemMenu.class)
public abstract class FactoryPanelSetItemMenuFluidMixin implements FluidGaugeSetItemMenuExtension {

    @Unique
    private FluidStack cosmiccore$fluid = FluidStack.EMPTY;
    @Unique
    private boolean cosmiccore$fluidGauge;

    @Inject(method = "addSlots", at = @At("HEAD"))
    private void cosmiccore$identifyFluidGauge(CallbackInfo ci) {
        FactoryPanelSetItemMenu menu = (FactoryPanelSetItemMenu) (Object) this;
        FactoryPanelBehaviour behaviour = menu.contentHolder;
        cosmiccore$fluidGauge = behaviour != null && FactoryGaugePromiseLimitSupport.isFluid(behaviour);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cosmiccore$initializeFluid(CallbackInfo ci) {
        FactoryPanelSetItemMenu menu = (FactoryPanelSetItemMenu) (Object) this;
        FactoryPanelBehaviour behaviour = menu.contentHolder;
        cosmiccore$fluidGauge = behaviour != null && FactoryGaugePromiseLimitSupport.isFluid(behaviour);
        if (cosmiccore$fluidGauge) {
            cosmiccore$fluid = ((FluidStack) ((StockPanelBehaviourAccessor) behaviour).cosmiccore$getFilter()).copy();
        }
    }

    @ModifyArgs(
                method = "addSlots",
                at = @At(value = "INVOKE",
                         target = "Lnet/neoforged/neoforge/items/SlotItemHandler;<init>(Lnet/neoforged/neoforge/items/IItemHandler;III)V"))
    private void cosmiccore$hideItemSlot(Args args) {
        if (!cosmiccore$fluidGauge) return;
        args.set(2, -10_000);
        args.set(3, -10_000);
    }

    @Inject(method = "saveData", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$saveFluid(FactoryPanelBehaviour contentHolder, CallbackInfo ci) {
        if (!cosmiccore$fluidGauge) return;
        FactoryPanelSetItemMenu menu = (FactoryPanelSetItemMenu) (Object) this;
        if (cosmiccore$fluid.isEmpty()) {
            menu.player.displayClientMessage(CreateLang.translateDirect("logistics.filter.invalid_item"), true);
            AllSoundEvents.DENY.playOnServer(menu.player.level(), menu.player.blockPosition(), 1, 1);
            ci.cancel();
            return;
        }
        ((StockPanelBehaviourAccessor) contentHolder).cosmiccore$setFilter(cosmiccore$fluid.copyWithAmount(1000));
        contentHolder.blockEntity.setChanged();
        contentHolder.blockEntity.notifyUpdate();
        menu.player.level().playSound(
                null, contentHolder.getPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
        ci.cancel();
    }

    @Override
    public boolean cosmiccore$isFluidGauge() {
        return cosmiccore$fluidGauge;
    }

    @Override
    public FluidStack cosmiccore$getFluid() {
        return cosmiccore$fluid;
    }

    @Override
    public void cosmiccore$setFluid(FluidStack fluid) {
        cosmiccore$fluid = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(1000);
    }
}
