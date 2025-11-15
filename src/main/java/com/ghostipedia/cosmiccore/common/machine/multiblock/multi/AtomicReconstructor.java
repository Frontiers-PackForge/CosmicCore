
package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.sammy.malum.registry.common.block.BlockRegistry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.HIGH_TEMP_FISSION_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public class AtomicReconstructor {

    public final static MultiblockMachineDefinition ATOMIC_RECONSTRUCTOR = REGISTRATE
            .multiblock("atomic_reconstructor",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§5Voidtouched Salt Fission Reactor")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.VILE_FISSION)
            .appearanceBlock(HIGH_TEMP_FISSION_CASING)
            .partAppearance((controller, part, side) -> HIGH_TEMP_FISSION_CASING.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless: off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("      AAAAAAA      ", "      DAAAAAD      ", "      DA   AD      ", "      DA   AD      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      DA   AD      ", "      DA   AD      ", "      DAAAAAD      ", "      AAAAAAA      ", "                   ")
                    .aisle("AAAAA ADAAADA  DDD ", "A   A ADDDDDA  GGG ", "A   A A     A  DDD ", "A   A A     A  GGG ", "A   A          DDD ", "A   A       A      ", "A   A       A      ", "A   A       A      ", "A   A              ", "A   A A     A      ", "AAAAA A     A      ", "      ADDDDDA      ", "      ADAAADA      ", "       AAAAA       ")
                    .aisle("AAAAA AAAAAAA DDDDD", " BBB  ADDDDDA GFFFG", " BBB    C     DFFFD", " BBB    C     GFFFG", " BBB    C     DD DD", " BBBCCCCC   A  DAD ", " BBBCCCCC  DAAAAAD ", " BBBCCCCC   A      ", " BBB    C          ", " BBB    C          ", "AAAAA   C          ", "      ADDDDDA      ", "      AAAAAAA      ", "       AAAAA       ")
                    .aisle("AAAAA AAAAAAA DDDDD", " B B  ADDDDDA GF FG", " B B    C     DF FD", " B B    C     GF FG", " B B    C     D   D", " B BCCCCC  DDDD  D ", " B BCCCCC FFFFF  D ", " B BCCCCC  DDDDDDD ", " B B    C          ", " B B    C          ", "AAAAA   C          ", "      ADDDDDA      ", "      AAAAAAA      ", "       AAAAA       ")
                    .aisle("AAAAA AAAAAAA DDDDD", " BBB  ADDDDDA GFFFG", " BBB    C     DFFFD", " BBB    C     GFFFG", " BBB    C     DD DD", " BBBCCCCC   A  DAD ", " BBBCCCCC  DAAAAAD ", " BBBCCCCC   A      ", " BBB    C          ", " BBB    C          ", "AAAAA   C          ", "      ADDDDDA      ", "      AAAAAAA      ", "       AAAAA       ")
                    .aisle("AAAAA ADAAADA  DDD ", "A   A ADDDDDA  GGG ", "A   A A     A  DDD ", "A   A A     A  GGG ", "A   A          DDD ", "A   A       A      ", "A   A       A      ", "A   A       A      ", "A   A              ", "A   A A     A      ", "AAAAA A     A      ", "      ADDDDDA      ", "      ADAAADA      ", "       AAAAA       ")
                    .aisle("      AAAAAAA      ", "      DAAQAAD      ", "      DA   AD      ", "      DA   AD      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      DA   AD      ", "      DA   AD      ", "      DAAAAAD      ", "      AAAAAAA      ", "                   ")

                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(HIGH_TEMP_FISSION_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.VOID_SALT_FISSION))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2,2))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(BlockRegistry.BLOCK_OF_SOULSTONE.get()))
                    .where('C', blocks(CosmicBlocks.HIGHLY_CONDUCTIVE_FISSION_CASING.get()))
                    .where('D', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('E', blocks(BlockRegistry.BLOCK_OF_VOID_SALTS.get()))
                    .where('F', blocks(BlockRegistry.BLOCK_OF_MALIGNANT_LEAD.get()))
                    .where('G', blocks(BlockRegistry.BLOCK_OF_MALIGNANT_PEWTER.get()))
                    .build())
            // spotless: on
            .workableCasingModel(CosmicCore.id("block/casings/solid/high_temperature_fission_casing"),
                    CosmicCore.id("block/overlay/machine/roaster"))
            .register();

    public static void init() {}
}
