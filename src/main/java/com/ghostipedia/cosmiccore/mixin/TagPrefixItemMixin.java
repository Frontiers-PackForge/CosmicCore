package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.api.data.material.property.CCoreMaterialIconSet;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.client.renderer.item.CosmicCoreItemRendererProvider;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TagPrefixItem.class, remap = false)
public class TagPrefixItemMixin extends Item implements CosmicCoreItemRendererProvider {

    @Shadow
    @Final
    public TagPrefix tagPrefix;
    @Shadow
    @Final
    public Material material;

    @Unique
    private ICustomRenderer cosmicCore$customRenderer;

    public TagPrefixItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/item/Item$Properties;Lcom/gregtechceu/gtceu/api/data/tag/TagPrefix;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;)V",
            at = @At(value = "RETURN"),
            remap = false)
    private void TagPrefixItem(Item.Properties properties, TagPrefix tagPrefix, Material material, CallbackInfo ci) {
        if (material.getMaterialIconSet() instanceof CCoreMaterialIconSet iconSet) {
            cosmicCore$customRenderer = iconSet.getCustomRender();
        }
    }

    @Override
    public ICustomRenderer getRenderInfo(ItemStack itemStack) {
        return cosmicCore$customRenderer;
    }
}
