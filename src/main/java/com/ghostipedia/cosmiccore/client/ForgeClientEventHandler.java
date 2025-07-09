package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.client.renderer.StructureBoundingBox;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.Arrays;

import com.mojang.blaze3d.shaders.FogShape;

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
    public static void remapIds(MissingMappingsEvent event) {

        //beeg machines

        remapMultiMachine(event, "steam_caster", CosmicMachines.STEAM_CASTER);
        remapMultiMachine(event, "steam_mixer", CosmicMachines.STEAM_MIXER);
        remapMultiMachine(event, "industrial_primitive_blast_furnace",
                CosmicMachines.INDUSTRIAL_PRIMITIVE_BLAST_FURNACE);
        remapMultiMachine(event, "high_pressure_assembler", CosmicMachines.HIGH_PRESSURE_ASSEMBLER);
        remapMultiMachine(event, "large_combustion_engine_cc", CosmicMachines.LARGE_COMBUSTION_ENGINE);
        remapMultiMachine(event, "extreme_combustion_engine_cc", CosmicMachines.EXTREME_COMBUSTION_ENGINE);
        remapMultiMachine(event, "ludicrous_combustion_engine_cc", CosmicMachines.LUDICROUS_COMBUSTION_ENGINE);
        remapMultiMachine(event, "ultimate_combustion_engine_cc", CosmicMachines.ULTIMATE_COMBUSTION_ENGINE);


        //naq mini reactors
        for( MachineDefinition machine : CosmicMachines.NAQUAHINE_MINI_REACTOR){
            if(machine == null) continue;
            String name = (GTValues.VN[machine.getTier()] + "_naquahine_mini_reactor");
            remapSingleBLocks(event, name, machine);
        }

        //steam sbs

        remapSingleBLocks(event, "lp_steam_wiremill", CosmicMachines.STEAM_WIREMILL.first());
        remapSingleBLocks(event, "hp_steam_wiremill", CosmicMachines.STEAM_WIREMILL.second());
        remapSingleBLocks(event, "lp_steam_wiremill", CosmicMachines.STEAM_BENDER.first());
        remapSingleBLocks(event, "hp_steam_wiremill", CosmicMachines.STEAM_BENDER.second());

    }



    private static void remapMultiMachine(MissingMappingsEvent event, String id, MultiblockMachineDefinition machine) {
        ResourceLocation resourceId = GTCEu.id(id);

        event.getMappings(Registries.ITEM, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getItem());
            }
        });

        event.getMappings(Registries.BLOCK, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlock());
            }
        });

        event.getMappings(Registries.BLOCK_ENTITY_TYPE, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlockEntityType());
            }
        });
    }

    private static void remapSingleBLocks(MissingMappingsEvent event, String id, MachineDefinition machine) {
        ResourceLocation resourceId = GTCEu.id(id);

        event.getMappings(Registries.ITEM, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getItem());
            }
        });

        event.getMappings(Registries.BLOCK, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlock());
            }
        });

        event.getMappings(Registries.BLOCK_ENTITY_TYPE, GTCEu.MOD_ID).forEach(mapping -> {
            if (mapping.getKey().equals(resourceId)) {
                mapping.remap(machine.getBlockEntityType());
            }
        });
    }

}
