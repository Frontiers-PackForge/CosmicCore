package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.modules;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.block.IMultiblockProvider;
import com.ghostipedia.cosmiccore.api.machine.multiblock.ModularizedWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.client.renderer.machine.StarBallastMachineRenderer;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.StarLadder;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GCyMBlocks.CASING_ATOMIC;
import static com.gregtechceu.gtceu.common.data.GCyMBlocks.CASING_HIGH_TEMPERATURE_SMELTING;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toRomanNumeral;

public class StarLadderDummy extends ModularizedWorkableElectricMultiblockMachine {
    public StarLadderDummy(IMachineBlockEntity holder, int tier, int moduleTier, int minModuleTier) {
        super(holder, tier, moduleTier, minModuleTier);
    }

    @Override
    public void sendWorkingDisabled() {
        this.recipeLogic.setWorkingEnabled(false);
    }

    @Override
    public void sendWorkingEnabled() {
        this.recipeLogic.setWorkingEnabled(true);
    }

    @Override
    public String getNameForDisplays() {
        return this.getDefinition().getId().toLanguageKey("block", "display_count");
    }
    public static final MultiblockMachineDefinition[] STAR_LADDER_DUMMY_MODULE = registerTieredModules("star_ladder_dummy_module",
            (holder, tier) -> new StarLadderDummy(holder, tier, 0, 1)
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
                                .where('A', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).or(abilities()))
                                .build();
                    })
                    .workableCasingRenderer(CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
                    .hasTESR(true)
                    .compassNodeSelf()
                    .register(),
            ZPM, UV, UHV);

    public static MultiblockMachineDefinition[] registerTieredModules(String name,
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
}
