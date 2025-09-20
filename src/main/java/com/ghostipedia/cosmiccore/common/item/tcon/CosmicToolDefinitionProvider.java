package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicCoreModifiers;


import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicCoreModifiers;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.PackOutput;

import slimeknights.tconstruct.library.data.tinkering.AbstractToolDefinitionDataProvider;
import slimeknights.tconstruct.library.materials.RandomMaterial;
import slimeknights.tconstruct.library.tools.definition.module.build.SetStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolTraitsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.DefaultMaterialsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.PartStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveModule;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import static com.ghostipedia.cosmiccore.common.item.tcon.CosmicTinkerToolPart.wrenchHead;
import static slimeknights.tconstruct.tools.TinkerToolParts.*;

public class CosmicToolDefinitionProvider extends AbstractToolDefinitionDataProvider {

    public CosmicToolDefinitionProvider(PackOutput packOutput) {
        super(packOutput, CosmicCore.MOD_ID);
    }

    @Override
    protected void addToolDefinitions() {
        RandomMaterial tier1Material = RandomMaterial.random().tier(1).build();
        RandomMaterial randomMaterial = RandomMaterial.random().allowHidden().build();
        DefaultMaterialsModule defaultTwoParts = DefaultMaterialsModule.builder().material(tier1Material, tier1Material)
                .build();
        DefaultMaterialsModule defaultThreeParts = DefaultMaterialsModule.builder()
                .material(tier1Material, tier1Material, tier1Material).build();
        DefaultMaterialsModule defaultFourParts = DefaultMaterialsModule.builder()
                .material(tier1Material, tier1Material, tier1Material, tier1Material).build();
        DefaultMaterialsModule defaultFiveParts = DefaultMaterialsModule.builder()
                .material(tier1Material, tier1Material, tier1Material, tier1Material, tier1Material).build();
        DefaultMaterialsModule ancientTwoParts = DefaultMaterialsModule.builder()
                .material(randomMaterial, randomMaterial).build();
        DefaultMaterialsModule ancientThreeParts = DefaultMaterialsModule.builder()
                .material(randomMaterial, randomMaterial, randomMaterial).build();

        // pickaxes

        define(CosmicToolDefinitions.WIRE_CUTTERS)
                .module(PartStatsModule.parts()
                        .part(smallBlade)
                        .part(smallBlade)
                        .part(toolBinding)
                        .part(toolHandle)
                        .part(toolHandle).build())
                .module(defaultFourParts)
                .module(new SetStatsModule(StatsNBT.builder()
                        .set(ToolStats.ATTACK_SPEED, 0.9f)
                        .build()))
                .module(IsEffectiveModule.tag(CustomTags.MINEABLE_WITH_WIRE_CUTTER));

        define(CosmicToolDefinitions.WRENCHES)
                .module(PartStatsModule.parts()
                        .part(wrenchHead)
                        .part(toughBinding)
                        .part(toolHandle).build())
                .module(defaultThreeParts)
                .module(new SetStatsModule(StatsNBT.builder()
                        .set(ToolStats.ATTACK_SPEED, 0.9f)
                        .build()))
                .module(IsEffectiveModule.tag(CustomTags.MINEABLE_WITH_WRENCH))
                .module(ToolTraitsModule.builder().trait(CosmicCoreModifiers.wrenchModeSwitch).build());;




    }

    @Override
    public String getName() {
        return "Cosmic Core Tinker's Construct Tool Definition Data Generator";
    }
}
