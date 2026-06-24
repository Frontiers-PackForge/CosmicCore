package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.EXPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_STRESS_PROOF;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class SufferingChamber {

    public static final MultiblockMachineDefinition SUFFERING_CHAMBER = REGISTRATE
            .multiblock("suffering_chamber", WorkableElectricMultiblockMachine::new)
            .langValue("§cSuffering Chamber")
            .recipeType(CosmicRecipeTypes.SUFFERING_CHAMBER)
            .rotationState(RotationState.NON_Y_AXIS)
            .partAppearance((controller, part, side) -> CASING_STRESS_PROOF.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK))
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("AAA     AAA", "AA       AA", "A         A", "A         A", "           ", "           ", "           ", "           ")
                    .slice("A ABBBBBA A", "A ABBBBBA A", "  AB   BA  ", "  AD   DA  ", "  A D D A  ", "  A  E  A  ", "           ", "           ")
                    .slice("AAAAAAAAAAA", " AAAAAAAAA ", " A       A ", " AF     FA ", " A       A ", " AA     AA ", "  A     A  ", "  A     A  ")
                    .slice(" BAAAAAAAB ", " BAAAAAAAB ", " B       B ", " D G H G D ", "           ", "           ", "           ", "           ")
                    .slice(" BAAAAAAAB ", " BAAAAAAAB ", "           ", "           ", " D  G G  D ", "           ", "           ", "           ")
                    .slice(" BAAAAAAAB ", " BAAAXAAAB ", "           ", "   H   H   ", "     I     ", " E       E ", "           ", "           ")
                    .slice(" BAAAAAAAB ", " BAAAAAAAB ", "           ", "           ", " D  G G  D ", "           ", "           ", "           ")
                    .slice(" BAAAAAAAB ", " BAAAAAAAB ", " B       B ", " D G H G D ", "           ", "           ", "           ", "           ")
                    .slice("AAAAAAAAAAA", " AAAAAAAAA ", " A       A ", " AF     FA ", " A       A ", " AA     AA ", "  A     A  ", "  A     A  ")
                    .slice("A ABBBBBA A", "A ABBQBBA A", "  AB   BA  ", "  AD   DA  ", "  A D D A  ", "  A  E  A  ", "           ", "           ")
                    .slice("AAA     AAA", "AA       AA", "A         A", "A         A", "           ", "           ", "           ", "           ")
                    .where('Q', Predicates.controller(Predicates.blocks(definition.get())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(CASING_STRESS_PROOF.get()).setMinGlobalLimited(185)
                            .or(autoAbilities(CosmicRecipeTypes.SUFFERING_CHAMBER))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('B', blocks(BLANK_RUNE.get()))
                    .where('D', blocks(RITUAL_STONE.get()))
                    .where('E', blocks(LIGHT_RITUAL_STONE.get()))
                    .where('F', blocks(RITUAL_STONE.get()))
                    .where('G', blocks(LIGHT_RITUAL_STONE.get()))
                    .where('H', blocks(RITUAL_STONE.get()))
                    .where('I', blocks(LIGHT_RITUAL_STONE.get()))
                    .where('X', abilities(EXPORT_SOUL).setMinGlobalLimited(1, 1).setMaxGlobalLimited(1))
                    .build())
            // spotless:on
            .model(createSeparateControllerCasingMachineModel(CosmicCore.id("block/casings/solid/soul_muted_casing"),
                    GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/network_switch"))
                    .andThen(model -> model
                            .addDynamicRenderer(CosmicDynamicRenderHelpers::getSufferingChamberRenderer)))
            .hasBER(true)
            .register();

    public static void init() {}
}
