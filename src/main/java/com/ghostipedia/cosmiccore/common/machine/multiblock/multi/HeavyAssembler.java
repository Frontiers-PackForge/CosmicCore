package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public class HeavyAssembler {

    public final static MultiblockMachineDefinition HEAVY_ASSEMBLER = REGISTRATE
            .multiblock("heavy_assembler",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§9Heavy Assembler")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.HEMOPHAGIC_TRANSFUSER)
            .appearanceBlock(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING)
            .partAppearance((controller, part, side) -> MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" BB  BBBBBBB", " BBBBBBBBBBB", "BB      BBBB", "B       BBBB", "B       BBB ", "B       BB  ", "BBBBBBBBBB  ")
                    .aisle(" BB  BBBBBBB", " BBBBBBBBBBB", "BB      BBBB", "B  G    BBBB", "BFFFFFFFBBB ", "B       BB  ", "BBBBBBBBBB  ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle(" BB  BBBBBBB", " BCCCCCBBBBB", "BB      BBBB", "B    G  BBBB", "BFFFFFFFBBB ", "B       BB  ", "BBBBBBBBBB  ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle(" BB  BBBBBBB", " BCCCCCBBBBB", "BB      BBBB", "B  G    BBBB", "BFFFFFFFBBB ", "B       BB  ", "BBBBBBBBBB  ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle(" BB  BBBBBBB", " BCCCCCBBBBB", "BB      BBBB", "B     G BBBB", "BFFFFFFFBBB ", "B       BB  ", "BBBBBBBBBB  ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle("       BBBBB", " BCCCCCBB  B", "EE         B", "E          B", "E       BCC ", "E       ECC ", "EEEEEEEEE   ")
                    .aisle(" BB  BBBBBBB", " BBBBBBBBBBB", "BB      BBBB", "B       BBBB", "B       BBBB", "B       BBBB", "BBBBBBBBB   ")
                    .aisle(" BB  BBBBBBB", " BBBBBBBBBBB", "BB      BBBB", "B       BBBB", "B       BBBB", "B       BBBB", "BBBBBBBBB   ")
                    .aisle("        BBBB", "        BBBB", "        BBBB", "        BBBB", "        BBBB", "        BBBB", "            ")
                    .aisle("        AAAA", "        AAAA", "        AAAA", "        AQAA", "        AAAA", "        AAAA", "            ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get())
                            .or(autoAbilities())
                            .or(autoAbilities(CosmicRecipeTypes.HEAVY_ASSEMBLER))
                            .or(abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_4X,PartAbility.IMPORT_FLUIDS_9X))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))) //Part IO go here
                    .where('B', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
                    .where('C', blocks(GCYMBlocks.CASING_LARGE_SCALE_ASSEMBLING.get()))
                    .where('F',  blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.TungstenCarbide)))
                    .where('E', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('G', blocks(GEARBOX_PTHANTERUM.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/vomahine_certified_interstellar_grade_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .register();

    public static void init() {}
}
