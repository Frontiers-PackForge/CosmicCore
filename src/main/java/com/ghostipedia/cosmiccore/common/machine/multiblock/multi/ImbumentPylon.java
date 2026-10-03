package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.BLOODSTEEL_KUVITE_PLATING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SOUL_MUTED_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public final class ImbumentPylon {

    public static final MultiblockMachineDefinition IMBUMENT_PYLON = REGISTRATE
            .multiblock("imbument_pylon", ImbumentPylonMachine::new)
            .langValue("Imbument Pylon")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(SOUL_MUTED_CASING)
            .recipeType(CosmicRecipeTypes.IMBUMENT_PYLON)
            .partAppearance((controller, part, side) -> SOUL_MUTED_CASING.getDefaultState())
            .recipeModifiers(ImbumentPylonMachine::recipeModifier)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(" AAA ", " AEA ", "  A  ", "     ", "     ", "     ", "     ")
                    .slice("AAAAA", "ABBBA", " BCB ", " B B ", "     ", "     ", "     ")
                    .slice("AAAAA", "AB BA", "ACCCA", "     ", "     ", "     ", "  D  ")
                    .slice("AAAAA", "ABBBA", " BCB ", " B B ", "     ", "     ", "     ")
                    .slice(" AAA ", " AAA ", "  A  ", "     ", "     ", "     ", "     ")
                    .where(' ', any())
                    .where('A', blocks(SOUL_MUTED_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1)))
                    .where('B', blocks(BLOODSTEEL_KUVITE_PLATING.get()))
                    .where('C', any())
                    .where('D', any())
                    .where('E', controller(blocks(definition.getBlock())))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/soul_muted_casing"),
                    GTCEu.id("block/multiblock/network_switch"))
                    .andThen(model -> model.addDynamicRenderer(CosmicDynamicRenderHelpers::getImbumentPylonRender)))
            .hasBER(true)
            .register();

    private ImbumentPylon() {}

    public static void init() {}
}
