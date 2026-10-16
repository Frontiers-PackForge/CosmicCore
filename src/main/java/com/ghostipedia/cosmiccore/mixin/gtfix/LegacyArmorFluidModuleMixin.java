package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.module.FluidStorageModule;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FluidStorageModule.class, remap = false)
public abstract class LegacyArmorFluidModuleMixin {

    @Inject(method = "getInfo", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$legacyRemovalInfo(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.translatable("cosmiccore.armor.legacy_fluid_storage"));
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$legacyContentsInfo(ModuleContext module,
                                               Item.TooltipContext context,
                                               List<Component> tooltips,
                                               TooltipFlag flag,
                                               CallbackInfo ci) {
        tooltips.add(Component.translatable("cosmiccore.armor.legacy_fluid_storage"));
        var content = module.getData().getModuleItem()
                .get(GTDataComponents.FLUID_CONTENT);
        if (content != null && !content.isEmpty())
            tooltips.add(Component.translatable("cosmiccore.armor.legacy_fluid_contents",
                    content.getAmount(), content.copy().getHoverName()));
        ci.cancel();
    }

    @Inject(method = "createCapabilityForStack", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$retireArmorStorage(ModuleContext context, ItemStack stack,
                                               CallbackInfoReturnable<IFluidHandlerItem> cir) {
        cir.setReturnValue(null);
    }
}
