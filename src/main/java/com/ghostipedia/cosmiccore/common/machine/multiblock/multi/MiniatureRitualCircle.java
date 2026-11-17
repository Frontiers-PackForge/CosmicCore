package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.klikli_dev.occultism.registry.OccultismBlocks;
import com.sammy.malum.registry.common.block.MalumBlocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.FULGORINTH_PRIME_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.PALE_ROSE_STRUCTURAL_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class MiniatureRitualCircle {

    public static final MachineEntry<MultiblockMachineDefinition> MINIATURE_RITUAL_CIRCLE = REGISTRATE
            .multiblock("miniature_ritual_circle", WorkableElectricMultiblockMachine::new)
            .langValue("Miniature Ritual Circle")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(FULGORINTH_PRIME_CASING)
            .partAppearance((controller, part, side) -> FULGORINTH_PRIME_CASING.getDefaultState())
            .recipeType(CosmicRecipeTypes.AUTOMATED_RITUALS)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            .pattern(definition -> MultiblockPatternBuilder.start(BACK, UP, LEFT)
                    .slice("  ALA  ", "  AAA  ", "  A A  ", "  B B  ", "  C C  ")
                    .slice(" AAAAA ", " ADDDA ", "  EEE  ", "       ", "       ")
                    .slice("AAAAAAA", "ADDDDDA", "AEE EEA", "B     B", "C     C")
                    .slice("AAAAAAA", "ADDDDDA", " E N E ", "       ", "       ")
                    .slice("AAAAAAA", "ADDDDDA", "AEE EEA", "B     B", "C     C")
                    .slice(" AAAAA ", " ADDDA ", "  EEE  ", "       ", "       ")
                    .slice("  AAA  ", "  AAA  ", "  A A  ", "  B B  ", "  C C  ")
                    .where(' ', any())
                    .where('L', controller(blocks(definition.getBlock())))
                    .where('A', blocks(FULGORINTH_PRIME_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(OccultismBlocks.OTHERROCK_BRICKS_WALL.get()))
                    .where('C', blocks(MalumBlocks.SOULWOOD_FENCE.get()))
                    .where('D', blocks(PALE_ROSE_STRUCTURAL_CASING.get()))
                    .where('E', blocks(OccultismBlocks.CHALK_GLYPH_RAINBOW.get()))
                    .where('N', blocks(OccultismBlocks.IESNIUM_SACRIFICIAL_BOWL.get(),
                            OccultismBlocks.DARK_IESNIUM_SACRIFICIAL_BOWL.get()))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/fulgorinth_prime_casing"),
                    CosmicCore.id("block/multiblock/dawnforge")))
            .register();

    public static void init() {}
}
