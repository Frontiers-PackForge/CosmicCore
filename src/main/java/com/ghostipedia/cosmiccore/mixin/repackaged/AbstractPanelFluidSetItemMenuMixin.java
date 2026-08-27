package com.ghostipedia.cosmiccore.mixin.repackaged;

import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;

import net.liukrast.deployer.lib.logistics.board.AbstractPanelBehaviour;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPanelBehaviour.class)
public abstract class AbstractPanelFluidSetItemMenuMixin {

    @Inject(method = "createMenu", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$createFluidMenu(
                                            int containerId,
                                            Inventory playerInventory,
                                            Player player,
                                            CallbackInfoReturnable<AbstractContainerMenu> cir) {
        FactoryPanelBehaviour behaviour = (FactoryPanelBehaviour) (Object) this;
        if (cir.getReturnValue() == null && FactoryGaugePromiseLimitSupport.isFluid(behaviour)) {
            cir.setReturnValue(FactoryPanelSetItemMenu.create(containerId, playerInventory, behaviour));
        }
    }
}
