package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.client.renderer.machine.SufferingChamberRenderer;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BloodMagicBlocks;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_NONCONDUCTING;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_STRESS_PROOF;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class WelderMulti {

    public final static MultiblockMachineDefinition  SUBMERGED_WELDER= REGISTRATE
            .multiblock("submerged_welder",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§3Submerged Welder")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.HEMOPHAGIC_TRANSFUSER)
            .partAppearance((controller, part, side) -> CYCLOZINE_CHEMICALLY_REPELLING_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAA       AAAA", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle("AAAAA     AAAAA", " BB         BB ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ")
                    .aisle("AAAAAA   AAAAAA", " BBB       BBB ", " B           B ", " B           B ", " B           B ", " B           B ", " B           B ", " B           B ", " BBB       BBB ")
                    .aisle("AAAAAAAAAAAAAAA", "  BBB     BBB  ", "   B AAAAA B   ", "   EAAAAAAAE   ", "   E AAAAA E   ", "   B       B   ", "   E       E   ", "   B       B   ", "  BB       BB  ")
                    .aisle(" AAAAAAAAAAAAA ", "   B       B   ", "    GGGGGGG    ", "   AGGGGGGGA   ", "    GGGGGGG    ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("  AAAAAAAAAAA  ", "               ", "   AGGGGGGGA   ", "   AG     GA   ", "   AG     GA   ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG GGG GA   ", "   AG     GA   ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG GGG GA   ", "  CAG     GA   ", "  DF  H    F   ", "  DDDDD        ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG     GA   ", "   AG     GA   ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG GGG GA   ", "   AG     GAC  ", "   F    H  FD  ", "        DDDDD  ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG GGG GA   ", "   AG     GA   ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG     GA   ", "   AG     GA   ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG GGG GA   ", "   AG     GAC  ", "   F   H   FD  ", "       DDDDDD  ", "   E       E   ", "               ")
                    .aisle("   AAAAAAAAA   ", "               ", "   AGGGGGGGA   ", "   AG GGG GA   ", "   AG     GA   ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("  AAAAAAAAAAA  ", "               ", "   AGGGGGGGA   ", "   AG     GA   ", "   AG     GA   ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle(" AAAAAAAAAAAAA ", "   B       B   ", "    GGGGGGG    ", "   AGGGGGGGA   ", "    GGGGGGG    ", "   F       F   ", "               ", "   E       E   ", "               ")
                    .aisle("AAAAAAAAAAAAAAA", "  BBB     BBB  ", "   B AAAAA B   ", "   EAAAQAAAE   ", "   E AAAAA E   ", "   B       B   ", "   E       E   ", "   B       B   ", "  BB       BB  ")
                    .aisle("AAAAAA   AAAAAA", " BBB       BBB ", " B           B ", " B           B ", " B           B ", " B           B ", " B           B ", " B           B ", " BBB       BBB ")
                    .aisle("AAAAA     AAAAA", " BB         BB ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ", "  B         B  ")
                    .aisle("AAAA       AAAA", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(WEAR_RESISTANT_RURIDIT_CASING.get()))
                    .where('B', blocks(CASING_STRESS_PROOF.get()))
                    .where('C', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get()))
                    .where('E', blocks(CASING_STRESS_PROOF.get()))
                    .where('D', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()).setMinGlobalLimited(28)
                            // TODO see how to limit to 1 laser OR 1 energy, not 1 of each..
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(abilities(IMPORT_SOUL)))
                    .where('F', blocks(CASING_STRESS_PROOF.get()))
                    .where('G', blocks(CASING_NONCONDUCTING.get()))
                    .where('H', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .build())
            // spotless:on
            .model(
                createWorkableCasingMachineModel(
                        CosmicCore.id("block/casings/solid/naquadah_pressure_resistant_casing"),
                        GTCEu.id("block/multiblock/hpca")
                    ).andThen(model -> model.addDynamicRenderer(CosmicDynamicRenderHelpers::getWelderArmsRenderer))
            )
            .register();

    public static void init() {}


}
