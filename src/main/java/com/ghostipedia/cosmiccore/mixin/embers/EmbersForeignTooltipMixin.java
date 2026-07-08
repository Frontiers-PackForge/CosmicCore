package com.ghostipedia.cosmiccore.mixin.embers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import com.rekindled.embers.EmbersClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmbersClientEvents.class, remap = false)
public class EmbersForeignTooltipMixin {

    @Inject(method = "addTranslatedDescriptionTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$onlyDescribeEmbersItems(RenderTooltipEvent.GatherComponents event,
                                                           CallbackInfo ci) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        if (!"embers".equals(namespace)) {
            ci.cancel();
        }
    }
}
