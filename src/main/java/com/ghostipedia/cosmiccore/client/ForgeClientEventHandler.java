package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.api.data.material.property.CosmicCorePropertyKeys;
import com.ghostipedia.cosmiccore.client.renderer.StructureBoundingBox;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.item.GTBucketItem;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEventHandler {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        var stage = event.getStage();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            StructureBoundingBox.renderStructureSelect(event.getPoseStack(), event.getCamera());
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (CosmicUtils.hasTheOneRing(event.getCamera().getEntity())) {
            event.setFogShape(FogShape.SPHERE);

            // Shrink the fog to be very close
            if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
                event.setFarPlaneDistance(16.0F);
                event.setNearPlaneDistance(0.0F);
            } else {
                event.setFarPlaneDistance(10.0F);
                event.setNearPlaneDistance(3.0F);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (CosmicUtils.hasTheOneRing(event.getCamera().getEntity())) {
            // and make the fog a blue mist.
            // #7CBADA
            event.setRed(0.671F);
            event.setGreen(0.792F);
            event.setBlue(0.855F);
        }
    }

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {

        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BucketItem bucket) {
            Fluid fluid = bucket.getFluid();
            if (fluid instanceof GTFluid attributeFluid) {
                var mat = ChemicalHelper.getMaterial(attributeFluid);

                if (mat.hasProperty(CosmicCorePropertyKeys.FLUID_TOOLTIPS)) {
                    var prop = mat.getProperty(CosmicCorePropertyKeys.FLUID_TOOLTIPS);
                    event.getToolTip().add(Component.translatable(prop.getKey(), FormattingUtil.formatNumber2Places(prop.getValue())));
                }
            }
        }

        //CosmicFluidTooltipAddon.appendFluidTooltip(event.getItemStack());
    }
}
