package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.VoraxReactorMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import net.minecraft.world.level.block.Blocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_ATOMIC;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class VoraxReactor {

    public final static MultiblockMachineDefinition VORAX_REACTOR = REGISTRATE
            .multiblock("vorax_reactor", VoraxReactorMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.VORAX)
            .recipeModifier(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .appearanceBlock(CASING_ATOMIC)
            .partAppearance((controller, part, side) -> TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.getDefaultState())
            .generator(true)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(" AAAAAAAAA ", "    A A    ", "  AAA AAA  ", "    AAA    ", "   BBABB   ", "    BBB    ",
                            "     B     ", "           ", "           ", "           ", "     B     ", "    BBB    ",
                            "   BBABB   ", "    AAA    ", "  AAA AAA  ", "    A A    ", " AAAAAAAAA ")
                    .slice("AA       AA", "  CCCACCC  ", " A  A A  A ", "   BAAAB   ", "  BB A BB  ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "  BB A BB  ", "   BAAAB   ", " A  A A  A ", "  CCCACCC  ", "AA       AA")
                    .slice("A         A", " CCAAAAACC ", "A D A A D A", "  DBAAABD  ", " BD  A  DB ", "  D     D  ",
                            "  D     D  ", "  D     D  ", "  D     D  ", "  D     D  ", "  D     D  ", "  D     D  ",
                            " BD  A  DB ", "  DBAAABD  ", "A D A A D A", " CCAAAAACC ", "A         A")
                    .slice("A         A", " CAAAAAAAC ", "A  AA AA  A", " BBBAAABBB ", "BB  AEA  BB", "    AEA    ",
                            "    AEA    ", "     E     ", "           ", "     E     ", "    AEA    ", "    AEA    ",
                            "BB  AEA  BB", " BBBAAABBB ", "A  AA AA  A", " CAAAAAAAC ", "A         A")
                    .slice("A         A", "ACAABBBAACA", "AAAAAAAAAAA", "AAAAAAAAAAA", "B  AFEFA  B", "B  AFEFA  B",
                            "B  AFEFA  B", "B   EEE   B", "B         B", "B   EEE   B", "B  AFEFA  B", "B  AFEFA  B",
                            "B  AFEFA  B", "AAAAAAAAAAA", "AAAAAAAAAAA", "ACAABBBAACA", "A         A")
                    .slice("A         A", " AAABBBAAA ", "    AEA    ", "AAAAAEAAAAA", "AAAEEEEEAAA", "B  EEEEE  B",
                            "B  EEEEE  B", "B  EEEEE  B", "B         B", "B  EEEEE  B", "B  EEEEE  B", "B  EEEEE  B",
                            "AAAEEEEEAAA", "AAAAAEAAAAA", "    AEA    ", " AAABBBAAA ", "A         A")
                    .slice("A         A", "ACAABBBAACA", "AAAAAAAAAAA", "AAAAAAAAAAA", "B  AFEFA  B", "B  AFEFA  B",
                            "B  AFEFA  B", "B   EEE   B", "B         B", "B   EEE   B", "B  AFEFA  B", "B  AFEFA  B",
                            "B  AFEFA  B", "AAAAAAAAAAA", "AAAAAAAAAAA", "ACAABBBAACA", "A         A")
                    .slice("A         A", " CAAAAAAAC ", "A  AA AA  A", " BBBAAABBB ", "BB  AEA  BB", "    AEA    ",
                            "    AEA    ", "     E     ", "           ", "     E     ", "    AEA    ", "    AEA    ",
                            "BB  AEA  BB", " BBBAAABBB ", "A  AA AA  A", " CAAAAAAAC ", "A         A")
                    .slice("A         A", " CCAAAAACC ", "A D A A D A", "  DBAAABD  ", " BD  A  DB ", "  D     D  ",
                            "  D     D  ", "  D     D  ", "  D     D  ", "  D     D  ", "  D     D  ", "  D     D  ",
                            " BD  A  DB ", "  DBAAABD  ", "A D A A D A", " CCAAAAACC ", "A         A")
                    .slice("AA       AA", "  CCCACCC  ", " A  A A  A ", "   BAAAB   ", "  BB A BB  ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "  BB A BB  ", "   BAAAB   ", " A  A A  A ", "  CCCACCC  ", "AA       AA")
                    .slice(" AAAAAAAAA ", "    A A    ", "  AAA AAA  ", "    AAA    ", "   BBQBB   ", "    BBB    ",
                            "     B     ", "           ", "           ", "           ", "     B     ", "    BBB    ",
                            "   BBABB   ", "    AAA    ", "  AAA AAA  ", "    A A    ", " AAAAAAAAA ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CosmicBlocks.ULTRA_POWERED_CASING.get()))
                    .where('A', blocks(CASING_ATOMIC.get()))
                    .where('D', frames(CosmicMaterials.Trinavine))
                    .where('E', blocks(Blocks.SCULK))
                    .where('F', blocks(FUSION_COIL.get()))
                    .where('B', blocks(CosmicBlocks.TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get())
                            .or(Predicates.abilities(PartAbility.OUTPUT_LASER, PartAbility.SUBSTATION_OUTPUT_ENERGY)
                                    .setMaxGlobalLimited(1, 1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1, 1)
                                    .setMaxGlobalLimited(3))
                            .or(Predicates.abilities(CosmicPartAbility.STERILIZE_HATCH).setExactLimit(1)))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/tritanium_lined_heavy_bolted_neutronium_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(model -> model.addDynamicRenderer(CosmicDynamicRenderHelpers::getConceptIncinerator)))
            .hasBER(true)
            .register();

    public static void init() {}
}
