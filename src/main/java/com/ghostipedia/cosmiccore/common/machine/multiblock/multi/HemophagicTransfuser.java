package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.client.renderer.machine.HemophagicTransfuserRender;
import com.ghostipedia.cosmiccore.client.renderer.machine.StarBallastMachineRenderer;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_ATOMIC;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING;

public class HemophagicTransfuser {

    public final static MultiblockMachineDefinition HEMOPHAGIC_TRANSFUSER = REGISTRATE.multiblock("hemophagic_transfuser",
                    IrisMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.VOMAHINE_CORE_DRILL)
            .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAA   AAAA", "A  AAAAA  A", "A         A", "AA       AA", " A       A ", " A       A ", " A       A ", "AA       AA", "A         A", "A  AAAAA  A", "AAAA   AAAA")
                    .aisle("A  AAAAA  A", "   BCCCB   ", "  B     B  ", "A         A", "AC       CA", "AC       CA", "AC       CA", "A         A", "  B     B  ", "   BCCCB   ", "A  AAAAA  A")
                    .aisle("A         A", "  B     B  ", " B       B ", "           ", "D         D", "D         D", "D         D", "           ", " B       B ", "  B     B  ", "A         A")
                    .aisle("AA       AA", "AB       BA", "           ", "   EEEEE   ", "D  E   E  D", "D  E   E  D", "D  E   E  D", "   EEEEE   ", "           ", "AB       BA", "AA       AA")
                    .aisle(" A       A ", "AC       CA", "F         F", "F  E   E  F", "D         D", "D         D", "D         D", "F  E   E  F", "F         F", "AC       CA", " A       A ")
                    .aisle(" A       A ", "AC       CA", "           ", "   E   E   ", "D         D", "D         D", "D         D", "   E   E   ", "           ", "AC       CA", " A       A ")
                    .aisle(" A       A ", "AC       CA", "F         F", "F  E   E  F", "D         D", "D         D", "D         D", "F  E   E  F", "F         F", "AC       CA", " A       A ")
                    .aisle("AA       AA", "AB       BA", "           ", "   EEEEE   ", "D  E   E  D", "D  E   E  D", "D  E   E  D", "   EEEEE   ", "           ", "AB       BA", "AA       AA")
                    .aisle("A         A", "  B     B  ", " B       B ", "           ", "D         D", "D         D", "D         D", "           ", " B       B ", "  B     B  ", "A         A")
                    .aisle("A  AAAAA  A", "   BCCCB   ", "  B     B  ", "A         A", "AC       CA", "AC       CA", "AC       CA", "A         A", "  B     B  ", "   BCCCB   ", "A  AAAAA  A")
                    .aisle("AAAA   AAAA", "A  AAQAA  A", "A         A", "AA       AA", " A       A ", " A       A ", " A       A ", "AA       AA", "A         A", "A  AAAAA  A", "AAAA   AAAA")
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('B', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
                    .where('C', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
                    .where('D', blocks(CASING_ATOMIC.get()))
                    .where('E', blocks(ULTRA_POWERED_CASING.get()))
                    .where('F', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(16))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(16))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(16))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(16)))
                    .where('G', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .build())
            .renderer(HemophagicTransfuserRender::new)
            .tooltips(Component.translatable("cosmiccore.multiblock.iris.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.iris.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.iris.tooltip.2"),
                    Component.translatable("cosmiccore.multiblock.iris.tooltip.3"))
            .hasTESR(true)
            .register();

    public static void init() {}
}