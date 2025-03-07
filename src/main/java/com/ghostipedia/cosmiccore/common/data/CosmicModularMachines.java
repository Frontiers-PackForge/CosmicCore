package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.modules.StarLadderDummy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toRomanNumeral;

public class CosmicModularMachines {

    public static final MultiblockMachineDefinition[] STAR_LADDER_TEST_MODULE = registerTieredModules("name",
            (holder, tier) -> new StarLadderDummy(holder, tier, 1, 1),
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .langValue("Assembly Module MK %s".formatted(toRomanNumeral(tier - 5)))
                    .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
                    .appearanceBlock(() -> FusionReactorMachine.getCasingState(tier))
                    .pattern((definition) -> {
                        var casing = blocks(FusionReactorMachine.getCasingState(tier));
                        return FactoryBlockPattern.start()
                                .aisle("A", "A", "A", "A")
                                .aisle("A", "A", "B", "A")
                                .where("B", controller(blocks(definition.getBlock())))
                                .where('A', blocks(CosmicBlocks.VOMAHINE_CERTIFIED_CHEMICALLY_RESISTANT_CASING.get()))
                                .build();
                    })
                    .workableCasingRenderer(
                            CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                    .hasTESR(true)
                    .register(),
            ZPM, UV, UHV);

    public static MultiblockMachineDefinition[] registerTieredModules(
                                                                      String name,
                                                                      BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
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
