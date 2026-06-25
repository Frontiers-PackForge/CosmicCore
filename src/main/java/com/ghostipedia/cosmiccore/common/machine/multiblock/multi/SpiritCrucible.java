package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.SOUL_FOUNDRY;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_ATOMIC;
import static com.gregtechceu.gtceu.common.data.GTMaterials.TungstenCarbide;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.BATCH_MODE;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class SpiritCrucible {

    public final static MultiblockMachineDefinition SPIRIT_CRUCIBLE = REGISTRATE
            .multiblock("spirit_crucible", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(CosmicRecipeTypes.SPIRIT_CRUCIBLE, SOUL_FOUNDRY)
            .recipeModifiers(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK), BATCH_MODE)
            .appearanceBlock(CASING_ATOMIC)
            .partAppearance((controller, part, side) -> CASING_ATOMIC.getDefaultState())
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAB       BAA", "  B       B  ", "  B       B  ", "  B       B  ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .slice("AA         AA", " A         A ", " AAB     BAA ", "   B     B   ", "   B     B   ", "   B     B   ", "             ", "             ", "             ", "   B     B   ", "   B     B   ", "   B     B   ", "   B     B   ", "   B     B   ")
                    .slice("B   CAAAC   B", "B   CAAAC   B", "BAA CAAAC AAB", "B A CAAAC A B", "  AAC   CAA  ", "  D C   C D  ", "  D C   C D  ", "  D C   C D  ", "  D C   C D  ", "  BBC   CBB  ", "    C   C    ", "             ", "             ", "             ")
                    .slice("     EEE     ", "     EEE     ", " B   EEE   B ", " B   EEE   B ", " BAAAAAAAAAB ", " B A     A B ", "   A     A   ", "             ", "             ", " BBC     CBB ", " B C     C B ", " B C     C B ", " B         B ", " B         B ")
                    .slice("  C EAAAE C  ", "  C EAAAE C  ", "  C EAAAE C  ", "  C EAAAE C  ", "  CAA   AAC  ", "  C       C  ", "  C       C  ", "  C       C  ", "  C       C  ", "  C       C  ", "  C       C  ", "             ", "             ", "             ")
                    .slice("  AEAAAAAEA  ", "  AEA   AEA  ", "  AEA   AEA  ", "  AEA   AEA  ", "   A     A   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .slice("  AEAAAAAEA  ", "  AEA   AEA  ", "  AEA   AEA  ", "  AEA   AEA  ", "   A     A   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .slice("  AEAAAAAEA  ", "  AEA   AEA  ", "  AEA   AEA  ", "  AEA   AEA  ", "   A     A   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .slice("  C EAAAE C  ", "  C EAAAE C  ", "  C EAAAE C  ", "  C EAAAE C  ", "  CAA   AAC  ", "  C       C  ", "  C       C  ", "  C       C  ", "  C       C  ", "  C       C  ", "  C       C  ", "             ", "             ", "             ")
                    .slice("     EEE     ", "     EEE     ", " B   EEE   B ", " B   EEE   B ", " BAAAAAAAAAB ", " B A       B ", "   A     A   ", "             ", "             ", " BBC     CBB ", " B C     C B ", " B C     C B ", " B         B ", " B         B ")
                    .slice("B   CAAAC   B", "B   CAAAC   B", "BAA CAQAC AAB", "B A CAAAC A B", "  AAC   CAA  ", "  D C   C D  ", "  D C   C D  ", "  D C   C D  ", "  D C   C D  ", "  BBC   CBB  ", "    C   C    ", "             ", "             ", "             ")
                    .slice("AA         AA", " A         A ", " AAB     BAA ", "   B     B   ", "   B     B   ", "   B     B   ", "             ", "             ", "             ", "   B     B   ", "   B     B   ", "   B     B   ", "   B     B   ", "   B     B   ")
                    .slice("AAB       BAA", "  B       B  ", "  B       B  ", "  B       B  ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CosmicBlocks.SELF_HEALING_PTHANTERUM.get()))
                    .where('A', blocks(CASING_ATOMIC.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(4))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1,1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1,1))
                    )
                    .where('D', frames(TungstenCarbide))
                    .where('E', blocks(CASING_ATOMIC.get()))
                    .where('B', blocks(CosmicBlocks.RESONANTLY_TUNED_VIRTUE_MELD_CASING.get()))
                    .build())
            // spotless:on
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/gcym/atomic_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(model -> model.addDynamicRenderer(CosmicDynamicRenderHelpers::getSpiritCrucibleRender)))
            .hasBER(true)
            .register();

    public static void init() {}
}
