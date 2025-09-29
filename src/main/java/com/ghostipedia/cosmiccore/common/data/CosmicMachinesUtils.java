package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ExoticCombustionEngineMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.V;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.workableTiered;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

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

    public static MachineDefinition[] registerSimpleGenerator(String name,
                                                              GTRecipeType recipeType,
                                                              Int2IntFunction tankScalingFunction,
                                                              float hazardStrengthPerOperation,
                                                              int... tiers) {
        return CosmicMachinesUtils.registerTieredMachines(name,
                (holder, tier) -> new SimpleGeneratorMachine(holder, tier, hazardStrengthPerOperation * tier,
                        tankScalingFunction),
                (tier, builder) -> builder
                        .langValue("%s %s Generator %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .rotationState(RotationState.ALL)
                        .recipeType(recipeType)
                        .recipeModifier(SimpleGeneratorMachine::recipeModifier, true)
                        .addOutputLimit(ItemRecipeCapability.CAP, 0)
                        .addOutputLimit(FluidRecipeCapability.CAP, 0)
                        .simpleGeneratorModel(GTCEu.id("block/generators/" + name))
                        .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                tankScalingFunction.applyAsInt(tier), false))
                        .register(),
                tiers);
    }

    public static MachineDefinition[] registerTieredSingleBlockMachines(String name,
                                                                        BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                        BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                                        int... tiers) {
        return registerTieredMachines(name, factory, builder, tiers);
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSteamMachines(String name,
                                                                                   BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
                                                                                   BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
        MachineDefinition lowTier = builder.apply(false,
                REGISTRATE.machine("lp_%s".formatted(name), holder -> factory.apply(holder, false))
                        .langValue("I DO NOT EXIST")
                        .tier(0));
        MachineDefinition highTier = builder.apply(true,
                REGISTRATE.machine("hp_%s".formatted(name), holder -> factory.apply(holder, true))
                        .langValue("High Pressure " + FormattingUtil.toEnglishName(name))
                        .tier(1));
        return Pair.of(lowTier, highTier);
    }
}
