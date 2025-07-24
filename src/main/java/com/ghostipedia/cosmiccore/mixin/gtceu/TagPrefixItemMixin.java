package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.data.material.property.CCoreMaterialIconSet;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TagPrefixItem.class, remap = false)
public class TagPrefixItemMixin extends Item implements IItemRendererProvider {

    @Shadow
    @Final
    public Material material;

    @Unique
    private IRenderer cosmicCore$customRenderer;

    public TagPrefixItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void cosmicCore$initRenderer(CallbackInfo ci) {
        if (GTCEu.isClientSide()) {
            if (material.getMaterialIconSet() instanceof CCoreMaterialIconSet iconSet) {
                cosmicCore$customRenderer = iconSet.getRenderer();
            }
        }
    }

    @Override
    public @Nullable IRenderer getRenderer(ItemStack stack) {
        return cosmicCore$customRenderer;
    }
}
