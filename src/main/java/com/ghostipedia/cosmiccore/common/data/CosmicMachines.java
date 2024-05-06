package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveWorkableMachine;
import com.ibm.icu.impl.ICUService;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public class CosmicMachines {


    public static final int[] HIGH_TIERS = GTValues.tiersBetween(GTValues.IV, GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UHV);

    public final static MachineDefinition[] SOUL_IMPORT_HATCH = registerSoulTieredHatch(
            "soul_input_hatch", "Soul Input Hatch", "soul_hatch.import",
            IO.IN, HIGH_TIERS, CosmicPartAbility.IMPORT_SOUL);

    public final static MachineDefinition[] SOUL_EXPORT_HATCH = registerSoulTieredHatch(
            "soul_output_hatch", "Soul Output Hatch", "soul_hatch.export",
            IO.OUT, HIGH_TIERS, CosmicPartAbility.EXPORT_SOUL);

    public final static MultiblockMachineDefinition SOUL_TESTER = REGISTRATE.multiblock("soul_tester", PrimitiveWorkableMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicCoreRecipeTypes.SOUL_TESTER_RECIPES)
            .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                .aisle("S", "C", "I")
                .where("C", controller(blocks(definition.getBlock())))
                .where("S", abilities(CosmicPartAbility.IMPORT_SOUL).or(abilities(CosmicPartAbility.EXPORT_SOUL)))
                .where("I", abilities(PartAbility.EXPORT_ITEMS).or(abilities(PartAbility.IMPORT_ITEMS)))
                .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"), GTCEu.id("block/multiblock/coke_oven"))
            .register();

    private static MachineDefinition[] registerSoulTieredHatch(String name, String displayName, String model, IO io, int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new SoulHatchPartMachine(holder, tier, io),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + ' ' + displayName)
                        .abilities(abilities)
                        .rotationState(RotationState.ALL)
                        .overlayTieredHullRenderer(model)
                        .compassNode("soul_hatch")
                        .register(), tiers);
    }

    private static MachineDefinition[] registerTieredMachines(String name, BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                               BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder, int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> factory.apply(holder, tier)).tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static void init() {}
}
