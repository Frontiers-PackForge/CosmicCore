package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.sammy.malum.registry.common.block.MalumBlocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.HIGH_TEMP_FISSION_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class VileFissionReactor {

    public final static MultiblockMachineDefinition VILE_FISSION_REACTOR = REGISTRATE
            .multiblock("vile_fission",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§cFestering Fission Reactor")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.VILE_FISSION)
            .appearanceBlock(HIGH_TEMP_FISSION_CASING)
            .partAppearance((controller, part, side) -> HIGH_TEMP_FISSION_CASING.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAAAAAA", " AA AA ", " A   A ", "       ", "       ", "   A   ", "   A   ", "  ABA  ", "   A   ", "   A   ", "       ", "       ", " A   A ", " AA AA ", "AAAAAAA")
                    .slice("AAAAAAA", "AABBBAA", "AABCBAA", " ABCBA ", "  BCB  ", "  BBB  ", "  AAA  ", " AA AA ", "  AAA  ", "  BBB  ", "  BCB  ", " ABCBA ", "AABCBAA", "AABBBAA", "AAAAAAA")
                    .slice("AAAAAAA", "ABDDDBA", " BDDDB ", " BDDDB ", " BDDDB ", " BDDDB ", " AADAA ", "AAADAAA", " AADAA ", " BDDDB ", " BDDDB ", " BDDDB ", " BDDDB ", "ABDDDBA", "AAAAAAA")
                    .slice("AAAAAAA", " BD DB ", " CD DC ", " CD DC ", " CD DC ", "ABD DBA", "AAD DAA", "B D D B", "AAD DAA", "ABD DBA", " CD DC ", " CD DC ", " CD DC ", " BD DB ", "AAAAAAA")
                    .slice("AAAAAAA", "ABDDDBA", " BDDDB ", " BDDDB ", " BDDDB ", " BDDDB ", " AADAA ", "AAADAAA", " AADAA ", " BDDDB ", " BDDDB ", " BDDDB ", " BDDDB ", "ABDDDBA", "AAAAAAA")
                    .slice("AAAAAAA", "AABBBAA", "AABCBAA", " ABCBA ", "  BCB  ", "  BBB  ", "  AAA  ", " AA AA ", "  AAA  ", "  BBB  ", "  BCB  ", " ABCBA ", "AABCBAA", "AABBBAA", "AAAAAAA")
                    .slice("AAAQAAA", " AA AA ", " A   A ", "       ", "       ", "   A   ", "   A   ", "  ABA  ", "   A   ", "   A   ", "       ", "       ", " A   A ", " AA AA ", "AAAAAAA")

                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(HIGH_TEMP_FISSION_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.VILE_FISSION))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2,2))
                            .or(abilities(CosmicPartAbility.IMPORT_EMBER).setMaxGlobalLimited(1,1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(CosmicBlocks.HIGHLY_CONDUCTIVE_FISSION_CASING.get()))
                    .where('C', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('D', blocks(MalumBlocks.BLOCK_OF_LIVING_FLESH.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/high_temperature_fission_casing"),
                    CosmicCore.id("block/overlay/machine/roaster"))
            .register();

    public static void init() {}
}
