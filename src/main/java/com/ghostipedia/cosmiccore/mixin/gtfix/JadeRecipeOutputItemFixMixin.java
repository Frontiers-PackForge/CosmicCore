package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.integration.jade.provider.RecipeOutputProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;

@Mixin(value = RecipeOutputProvider.class, remap = false)
public abstract class JadeRecipeOutputItemFixMixin {

    @Inject(method = "addItemTooltips", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$renderEachItemOutputOnce(ITooltip tooltip, List<SizedIngredient> outputItems,
                                                     CallbackInfo ci) {
        IElementHelper helper = IElementHelper.get();
        for (SizedIngredient itemOutput : outputItems) {
            if (itemOutput == null || itemOutput.ingredient().hasNoItems()) {
                continue;
            }

            ItemStack output = itemOutput.getItems()[0];
            int count = itemOutput.count();
            ItemStack icon = output.copyWithCount(1);
            MutableComponent text = CommonComponents.space().append(String.valueOf(count));
            text.append(Component.translatable("gtceu.gui.content.times_item",
                    icon.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.WHITE));

            tooltip.add(helper.smallItem(icon));
            tooltip.append(text);
        }
        ci.cancel();
    }
}
