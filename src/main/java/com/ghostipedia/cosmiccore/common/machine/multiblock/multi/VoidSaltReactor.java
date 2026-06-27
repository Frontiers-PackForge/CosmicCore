package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
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

public class VoidSaltReactor {

    public final static MultiblockMachineDefinition VILE_FISSION_REACTOR = REGISTRATE
            .multiblock("void_salt_fissiom",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§5Voidtouched Salt Fission Reactor")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.VOID_SALT_FISSION)
            .appearanceBlock(HIGH_TEMP_FISSION_CASING)
            .partAppearance((controller, part, side) -> HIGH_TEMP_FISSION_CASING.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AA AA", "BA AB", "B   B", "B   B", "B   B", "BA AB", "AA AA")
                    .slice("AAAAA", "ACCCA", " CDC ", " CDC ", " CDC ", "ACCCA", "AAAAA")
                    .slice(" AAA ", " CCC ", " DED ", " DED ", " DED ", " CCC ", " AAA ")
                    .slice("AAAAA", "ACQCA", " CDC ", " CDC ", " CDC ", "ACCCA", "AAAAA")
                    .slice("AA AA", "BA AB", "B   B", "B   B", "B   B", "BA AB", "AA AA")

                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(HIGH_TEMP_FISSION_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.VOID_SALT_FISSION))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2,2))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(MalumBlocks.BLOCK_OF_SOULSTONE.get()))
                    .where('C', blocks(CosmicBlocks.HIGHLY_CONDUCTIVE_FISSION_CASING.get()))
                    .where('D', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('E', blocks(MalumBlocks.BLOCK_OF_VOID_SALTS.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/high_temperature_fission_casing"),
                    CosmicCore.id("block/overlay/machine/roaster"))
            .register();

    public static void init() {}
}
