package com.ghostipedia.cosmiccore.mixin.repackaged;

import com.ghostipedia.cosmiccore.client.compat.create.FluidGaugeGhostSlot;
import com.ghostipedia.cosmiccore.client.compat.create.FluidGaugeSetItemScreenExtension;
import com.ghostipedia.cosmiccore.common.compat.create.FluidGaugeSetItemMenuExtension;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FactoryPanelSetItemScreen.class)
public abstract class FactoryPanelSetItemScreenFluidMixin
                                                          extends AbstractSimiContainerScreen<FactoryPanelSetItemMenu>
                                                          implements FluidGaugeSetItemScreenExtension {

    @Unique
    private FluidGaugeGhostSlot cosmiccore$fluidSlot;

    protected FactoryPanelSetItemScreenFluidMixin(
                                                  FactoryPanelSetItemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void cosmiccore$addFluidSlot(CallbackInfo ci) {
        FluidGaugeSetItemMenuExtension extension = (FluidGaugeSetItemMenuExtension) menu;
        if (!extension.cosmiccore$isFluidGauge()) return;
        cosmiccore$fluidSlot = new FluidGaugeGhostSlot(
                getGuiLeft() + 74,
                getGuiTop() + 28,
                menu,
                extension.cosmiccore$getFluid(),
                extension);
        addRenderableWidget(cosmiccore$fluidSlot);
    }

    @ModifyExpressionValue(
                           method = "renderBg",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/createmod/catnip/lang/LangBuilder;component()Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent cosmiccore$fluidTitle(MutableComponent original) {
        return ((FluidGaugeSetItemMenuExtension) menu).cosmiccore$isFluidGauge() ?
                Component.translatable("cosmiccore.gui.factory_gauge.place_fluid") : original;
    }

    @ModifyExpressionValue(
                           method = "renderBg",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/tterrag/registrate/util/entry/BlockEntry;asStack()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack cosmiccore$fluidGaugeIcon(ItemStack original) {
        if (!((FluidGaugeSetItemMenuExtension) menu).cosmiccore$isFluidGauge()) return original;
        return menu.contentHolder instanceof net.liukrast.deployer.lib.logistics.board.AbstractPanelBehaviour panel ?
                panel.getItem().getDefaultInstance() : original;
    }

    @Override
    public boolean cosmiccore$acceptFluidDrop(FluidStack fluid, int mouseX, int mouseY) {
        if (cosmiccore$fluidSlot == null || mouseX < cosmiccore$fluidSlot.getX() ||
                mouseX >= cosmiccore$fluidSlot.getX() + 18 || mouseY < cosmiccore$fluidSlot.getY() ||
                mouseY >= cosmiccore$fluidSlot.getY() + 18) {
            return false;
        }
        cosmiccore$fluidSlot.setGhostStack(fluid.copyWithAmount(1000));
        return true;
    }
}
