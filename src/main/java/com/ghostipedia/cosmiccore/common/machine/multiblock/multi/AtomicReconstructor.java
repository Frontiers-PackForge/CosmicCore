package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.ETHERSTEEL_PLATED_ASH_TILES;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class AtomicReconstructor {

    public final static MultiblockMachineDefinition ATOMIC_RECONSTRUCTOR = REGISTRATE
            .multiblock("atomic_reconstructor",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§6Radbolt Atomic Reconstructor")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.RADBOLT_RECONSTRUCTOR)
            .appearanceBlock(ETHERSTEEL_PLATED_ASH_TILES)
            .partAppearance((controller, part, side) -> ETHERSTEEL_PLATED_ASH_TILES.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("      AAAAAAA      ", "      DAAAAAD      ", "      DA   AD      ", "      DA   AD      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      DA   AD      ", "      DA   AD      ", "      DAAAAAD      ", "      AAAAAAA      ", "                   ")
                    .slice("AAAAA ADAAADA  DDD ", "A   A ADDDDDA  GGG ", "A   A A     A  DDD ", "A   A A     A  GGG ", "A   A          DDD ", "A   A       A      ", "A   A       A      ", "A   A       A      ", "A   A              ", "A   A A     A      ", "AAAAA A     A      ", "      ADDDDDA      ", "      ADAAADA      ", "       AAAAA       ")
                    .slice("AAAAA AAAAAAA DDDDD", " BBB  ADDDDDA GFFFG", " BBB    C     DFFFD", " BBB    C     GFFFG", " BBB    C     DD DD", " BBBCCCCC   A  DAD ", " BBBCCCCC  DAAAAAD ", " BBBCCCCC   A      ", " BBB    C          ", " BBB    C          ", "AAAAA   C          ", "      ADDDDDA      ", "      AAAAAAA      ", "       AAAAA       ")
                    .slice("AAAAA AAAAAAA DDDDD", " B B  ADDDDDA GF FG", " B B    C     DF FD", " B B    C     GF FG", " B B    C     D   D", " B BCCCCC  DDDD  D ", " B BCCCCC FFFFF  D ", " B BCCCCC  DDDDDDD ", " B B    C          ", " B B    C          ", "AAAAA   C          ", "      ADDDDDA      ", "      AAAAAAA      ", "       AAAAA       ")
                    .slice("AAAAA AAAAAAA DDDDD", " BBB  ADDDDDA GFFFG", " BBB    C     DFFFD", " BBB    C     GFFFG", " BBB    C     DD DD", " BBBCCCCC   A  DAD ", " BBBCCCCC  DAAAAAD ", " BBBCCCCC   A      ", " BBB    C          ", " BBB    C          ", "AAAAA   C          ", "      ADDDDDA      ", "      AAAAAAA      ", "       AAAAA       ")
                    .slice("AAAAA ADAAADA  DDD ", "A   A ADDDDDA  GGG ", "A   A A     A  DDD ", "A   A A     A  GGG ", "A   A          DDD ", "A   A       A      ", "A   A       A      ", "A   A       A      ", "A   A              ", "A   A A     A      ", "AAAAA A     A      ", "      ADDDDDA      ", "      ADAAADA      ", "       AAAAA       ")
                    .slice("      AAAAAAA      ", "      DAAQAAD      ", "      DA   AD      ", "      DA   AD      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      D     D      ", "      DA   AD      ", "      DA   AD      ", "      DAAAAAD      ", "      AAAAAAA      ", "                   ")

                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CosmicBlocks.SOUL_STAINED_STEEL_ALU_CASING.get()))
                    .where('B', blocks(ETHERSTEEL_PLATED_ASH_TILES.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.VOID_SALT_FISSION))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2,2))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where('D', blocks(ETHERSTEEL_PLATED_ASH_TILES.get()))
                    .where('F', blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                    .where('G', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .build())
            // spotless:on
            .model(createSeparateControllerCasingMachineModel(
                    CosmicCore.id("block/casings/solid/soul_stained_steel_aluminium_plated_casing"),
                    CosmicCore.id("block/casings/solid/ethersteel_plated_ash_tiles"),
                    GTCEu.id("block/multiblock/network_switch"))
                    .andThen(model -> model
                            .addDynamicRenderer(CosmicDynamicRenderHelpers::createHellfireFoundryPartRender)))
            .register();

    public static void init() {}
}
