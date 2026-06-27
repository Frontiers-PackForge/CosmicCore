package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.VomahineShredder;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class ShredderMultiblock {

    // public final static MultiblockMachineDefinition VOMAHINE_SHREDDER = REGISTRATE
    // .multiblock("vomahine_shredder", WorkableElectricMultiblockMachine::new)
    // .rotationState(RotationState.NON_Y_AXIS)
    // .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
    // .appearanceBlock(CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING)
    // .recipeType(GTRecipeTypes.DUMMY_RECIPES)
    // .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP,
    // RelativeDirection.LEFT)
    // .slice("AAAAAAAAAAAAA", "ADDDDDDDDDDDA", "ADDDDDDDDDDDA", "AAAAAAADDDDDA", " AAAAAAA")
    // .slice("ACCCCCCCCCCCA", "ACCCCCCCCCCCB", "ACCCCCCCCCCCB", "ACCCCCCCCCCCB", " CCCCCCA")
    // .slice("ACCCCCCCCCCCA", "ACDDDDCEEEEC ", "ACDDDDCEEEEC ", "ACCCCCCEEEEC ", " C CA")
    // .slice("ACCCCCCCCCCCA", "ACCCCCCEEEEC ", "ACCCCCCEEEEC ", "ACCCCCCEEEEC ", " C CA")
    // .slice("AAAAAACCCCCCA", "AAAAAACEEEEC ", "AAAAAACEEEEC ", "ABBBBBCEEEEC ", " C CA")
    // .slice("AAAAAACCCCCCA", "AFFFFACCCCCCB", "AAAAAACCCCCCB", "A BCCCCCCB", " CCCCCCA")
    // .slice("AAAAAAAAAAAAA", "G AB BA", "A AB BA", "A AB BA", " AAAAAAA")
    // .where(' ', any())
    // .where('G', controller(blocks(definition.getBlock())))
    // .where('A', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))
    // .where('B', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
    // .where('C', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
    // .where('D', blocks(CASING_ATOMIC.get()))
    // .where('E', blocks(ULTRA_POWERED_CASING.get())
    // .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
    // .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1))
    // .or(Predicates.abilities(PartAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
    // .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(16)))
    // .where('F', blocks(definition.getBlock()))
    // .build())
    // .workableCasingRenderer(CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
    // GTCEu.id("block/multiblock/fusion_reactor"))
    // .tooltips(Component.translatable("cosmiccore.multiblock.shredder.tooltip.0"),
    // Component.translatable("cosmiccore.multiblock.shredder.tooltip.1"),
    // Component.translatable("cosmiccore.multiblock.shredder.tooltip.2"),
    // Component.translatable("cosmiccore.multiblock.shredder.tooltip.3"))
    // .hasTESR(true)
    // .register();
    //
    // // Shredder Modules
    //
    // public static final MultiblockMachineDefinition[] SHREDDER_MODULE = registerTieredMultis("shredder_module",
    // WorkableElectricMultiblockMachine::new, (tier, builder) -> builder
    // .rotationState(RotationState.ALL)
    // .langValue("Shredder Module MK %s".formatted(toRomanNumeral(tier - 5)))
    // .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
    // .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT,
    // FusionReactorMachine::recipeModifier)
    // .appearanceBlock(() -> FusionReactorMachine.getCasingState(tier))
    // .pattern((definition) -> {
    // var casing = blocks(FusionReactorMachine.getCasingState(tier));
    // return MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
    // .slice("AAAAAAAAAAAAA", "ADDDDDDDDDDDA", "ADDDDDDDDDDDA", "AAAAAAADDDDDA",
    // " AAAAAAA")
    // .slice("ACCCCCCCCCCCA", "ACCCCCCCCCCCB", "ACCCCCCCCCCCB", "ACCCCCCCCCCCB",
    // " CCCCCCA")
    // .slice("ACCCCCCCCCCCA", "ACDDDDCEEEEC ", "ACDDDDCEEEEC ", "ACCCCCCEEEEC ",
    // " C CA")
    // .slice("ACCCCCCCCCCCA", "ACCCCCCEEEEC ", "ACCCCCCEEEEC ", "ACCCCCCEEEEC ",
    // " C CA")
    // .slice("AAAAAACCCCCCA", "AAAAAACEEEEC ", "AAAAAACEEEEC ", "ABBBBBCEEEEC ",
    // " C CA")
    // .slice("AAAAAACCCCCCA", "AFFFFACCCCCCB", "AAAAAACCCCCCB", "A BCCCCCCB",
    // " CCCCCCA")
    // .slice("AAAAAAAAAAAAA", "G AB BA", "A AB BA", "A AB BA",
    // " AAAAAAA")
    // .where(' ', any())
    // .where('F', controller(blocks(definition.getBlock()))
    // .or(blocks(CASING_ATOMIC.get()).setMaxGlobalLimited(4)))
    // .where('A', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))
    // .where('B', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
    // .where('C', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
    // .where('D', blocks(CASING_ATOMIC.get()))
    // .where('E', blocks(ULTRA_POWERED_CASING.get())
    // .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
    // .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1))
    // .or(Predicates.abilities(PartAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
    // .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(16))
    // .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(16)))
    // .where('G', blocks(definition.getBlock()))
    // .build();
    // })
    // .workableCasingRenderer(
    // CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
    // GTCEu.id("block/multiblock/fusion_reactor"))
    // .hasTESR(true)
    // .register(),
    // ZPM, UV, UHV);

    public static MultiblockMachineDefinition[] registerTieredMultis(String name,
                                                                     BiFunction<BlockEntityCreationInfo, Integer, MultiblockControllerMachine> factory,
                                                                     BiFunction<Integer, MultiblockMachineBuilder, MultiblockMachineDefinition> builder,
                                                                     int... tiers) {
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .multiblock(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static void init() {}
}
