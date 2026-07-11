package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.PCBFoundryMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.*;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;

public class PCBFoundry {

    public final static MultiblockMachineDefinition PCB_FOUNDRY = REGISTRATE
            .multiblock("pcb_foundry",
                    PCBFoundryMachine::new)
            .langValue("§ePCB Foundry")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.PCB_FABRICATOR)
            .appearanceBlock(BICHROMAL_NEVRAMITE_CASING)
            .partAppearance((controller, part, side) -> BICHROMAL_NEVRAMITE_CASING.getDefaultState())
            .recipeModifiers(CosmicRecipeModifiers::innateParallel4x,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAAAAAAAA", "A A A A A", "A A A A A", "A A A A A", "         ", "         ", "         ")
                    .slice("ABBBBBBBA", "ACCCCCCCA", "ACCCCCCCA", "A A A A A", "         ", "         ", "         ")
                    .slice("ABBBBBBBA", "ACCCCCCCA", "ACCCCCCCA", "A A A A A", "         ", "         ", "         ")
                    .slice("AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "         ", "         ", "         ")
                    .slice("ADDDDDDDA", "ADEFFFEDA", "ADEFFFEDA", "ADEFFFEDA", "ADEEEEEDA", "ADDDDDDDA", "         ")
                    .slice("ADDDDDDDA", " E FFF E ", " E     E ", " E     E ", " E     E ", "AD     DA", "  BBBBB  ")
                    .slice("ADDDDDDDA", " E FFF E ", " E     E ", " E   G E ", " E   H E ", "ADHHHHHDA", "  BEEEB  ")
                    .slice("ADDDDDDDA", " E FFF E ", " E     E ", " E     E ", " E     E ", "AD     DA", "  BEEEB  ")
                    .slice("ADDDDDDDA", " E FFF E ", " E     E ", " E     E ", " E     E ", "AD     DA", "  BEEEB  ")
                    .slice("ADDDDDDDA", " E FFF E ", " E     E ", " E I   E ", " E H   E ", "ADHHHHHDA", "  BEEEB  ")
                    .slice("ADDDDDDDA", " E FFF E ", " E     E ", " E     E ", " E     E ", "AD     DA", "  BEEEB  ")
                    .slice("ADDDDDDDA", "AE FFF EA", "AE     EA", "AE     EA", "AE     EA", "AD     DA", "  BEEEB  ")
                    .slice("ADDDDDDDA", " D     D ", " D     D ", " D     D ", " D     D ", "ADDD DDDA", "  BBEBB  ")
                    .slice("ADDDDDDDA", "AEBBBBBEA", "AE     EA", "AE     EA", "AE     EA", "ABDDDDDBA", "  BBBBB  ")
                    .slice("ADDDDDDDA", " EBBBBBE ", " E BBB E ", " E BBB E ", " E BBB E ", "ABDBBBDBA", "   BBB   ")
                    .slice("ADDDDDDDA", " EBBBBBE ", " E BBB E ", " E BBB E ", " E BBB E ", "ABDBBBDBA", "   BBB   ")
                    .slice("ADDDDDDDA", " EBBBBBE ", " E BBB E ", " E BBB E ", " E BBB E ", "ABDBBBDBA", "   BBB   ")
                    .slice("ADDDDDDDA", " EBBBBBE ", " E     E ", "AAAAAAAAA", "AE     EA", "ABDDDDDBA", "         ")
                    .slice("ADDDDDDDA", " DEEAEED ", " DEEAEED ", "ADEEAEEDA", " DEEAEED ", "AAAAAAAAA", "         ")
                    .slice("AAAAAAAAA", "A  AAA  A", "A  AAA  A", "AAAAQAAAA", "   AAA   ", "   AAA   ", "         ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(BICHROMAL_NEVRAMITE_CASING.get())
                            .or(autoAbilities())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.HEAVY_ASSEMBLER))
                            .or(abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_4X,PartAbility.IMPORT_FLUIDS_9X))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH, CosmicPartAbility.COSMIC_PARALLEL_HATCH).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))) //Part IO go here
                    .where('B', blocks(SELF_HEALING_PTHANTERUM.get()))
                    .where('C', blocks(NAQUADAH_PRESSURE_RESISTANT_CASING.get()))
                    .where('D', blocks(OSCILLATING_GILDED_PTHANTERUM_CASING.get()))
                    .where('F',  blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.TungstenCarbide)))
                    .where('E', blocks(ZBLAN_REINFORCED_GLASS.get()))
                    .where('G', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                    .where('H', frames(GTMaterials.HSLASteel))
                    .where('I', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/bichromal_nevramite_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .register();

    public static void init() {}
}
