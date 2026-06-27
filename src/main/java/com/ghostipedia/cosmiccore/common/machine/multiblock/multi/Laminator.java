package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.sammy.malum.registry.common.block.MalumBlocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.ULTRA_POWERED_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class Laminator {

    public final static MultiblockMachineDefinition LARGE_LAMINATOR = REGISTRATE
            .multiblock("large_laminator",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§1Large Laminator")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(CosmicRecipeTypes.MANA_DIGITIZER, CosmicRecipeTypes.MANA_FLUIDIZER)
            .appearanceBlock(CYCLOZINE_CHEMICALLY_REPELLING_CASING)
            .partAppearance((controller, part, side) -> CYCLOZINE_CHEMICALLY_REPELLING_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.LEFT,RelativeDirection.UP,RelativeDirection.FRONT)
                    .slice(" AA   AA ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", " AA   AA ")
                    .slice("AAABBBAAA", " C     C ", " C     C ", " C     C ", " C     C ", " C     C ", " C     C ", " C     C ", "AAA   AAA")
                    .slice("AABAAABAA", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "AABAAABAA")
                    .slice(" BABBBAB ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "  ABBBA  ")
                    .slice(" BABBBAB ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "  ABBBA  ")
                    .slice(" BABBBAB ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "  ABBBA  ")
                    .slice("AABAAABAA", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "  D   D  ", "AABAAABAA")
                    .slice("AAABQBAAA", " C     C ", " C     C ", " C     C ", " C     C ", " C     C ", " C     C ", " C     C ", "AAA   AAA")
                    .slice(" AA   AA ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", " AA   AA ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(ULTRA_POWERED_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.MANA_DIGITIZER,CosmicRecipeTypes.MANA_FLUIDIZER))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))
                    .where('C', frames(GTMaterials.TungstenCarbide))
                    .where('D', blocks(MalumBlocks.AQUEOUS_SPIRITED_GLASS.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static void init() {}
}
