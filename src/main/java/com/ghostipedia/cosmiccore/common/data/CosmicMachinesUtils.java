package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.common.machine.multiblock.ExoticCombustionEngineMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.V;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;


public class CosmicMachinesUtils {

    // Verbaitm the LCE/ECE Code Logic used in GTCEU; Thanks Guys!
    public static MultiblockMachineDefinition registerCosmicLargeCombustionEngine(String name, int tier,
                                                                                  Supplier<? extends Block> casing,
                                                                                  Supplier<? extends Block> gear,
                                                                                  Supplier<? extends Block> intake,
                                                                                  ResourceLocation casingTexture,
                                                                                  ResourceLocation overlayModel) {
        return REGISTRATE.multiblock(name, holder -> new ExoticCombustionEngineMachine(holder, tier))
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
                .generator(true)
                .recipeModifier(ExoticCombustionEngineMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("XXX", "XDX", "XXX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("AAA", "AYA", "AAA")
                        .where('X', blocks(casing.get()))
                        .where('G', blocks(gear.get()))
                        .where('C', blocks(casing.get()).setMinGlobalLimited(3)
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, true, false)))
                        .where('D',
                                ability(PartAbility.OUTPUT_ENERGY,
                                        Stream.of(ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV).filter(t -> t >= tier)
                                                .mapToInt(Integer::intValue).toArray())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                                GTValues.VN[tier])))
                        .where('A',
                                blocks(intake.get())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_1")))
                        .where('Y', controller(blocks(definition.getBlock())))
                        .build())
                .recoveryItems(
                        () -> new ItemLike[] {
                                GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingModel(casingTexture, overlayModel)
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", V[tier]),
                        Component.translatable("cosmiccore.universal.tooltip.lube_info.0"),
                        Component.translatable("cosmiccore.universal.tooltip.lube_info.1"),
                        Component.translatable("cosmiccore.universal.tooltip.lube_info.2"),
                        Component.translatable("cosmiccore.universal.tooltip.lube_info.3"),
                        Component.translatable("cosmiccore.universal.boosting_agents.0"),
                        Component.translatable("cosmiccore.universal.boosting_agents.1"),
                        Component.translatable("cosmiccore.universal.boosting_agents.2"),
                        Component.translatable("cosmiccore.universal.boosting_agents.3"))
                .register();
    }
}
