package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.OreExtractionDrillMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.BlockEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class OreExtractionDrill {

    // LV: Solid Steel, HV: Stainless Steel, IV: Titanium, ZPM: TungstenSteel
    public static final MultiblockMachineDefinition ORE_EXTRACTION_DRILL_LV = registerDrill("ore_extraction_drill_lv",
            GTValues.LV, GTBlocks.CASING_STEEL_SOLID,
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"));

    public static final MultiblockMachineDefinition ORE_EXTRACTION_DRILL_HV = registerDrill("ore_extraction_drill_hv",
            GTValues.HV, GTBlocks.CASING_STAINLESS_CLEAN,
            GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"));

    public static final MultiblockMachineDefinition ORE_EXTRACTION_DRILL_IV = registerDrill("ore_extraction_drill_iv",
            GTValues.IV, GTBlocks.CASING_TITANIUM_STABLE,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));

    public static final MultiblockMachineDefinition ORE_EXTRACTION_DRILL_ZPM = registerDrill("ore_extraction_drill_zpm",
            GTValues.ZPM, GTBlocks.CASING_TUNGSTENSTEEL_ROBUST,
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));

    private static MultiblockMachineDefinition registerDrill(String id, int tier, BlockEntry<Block> casing,
                                                             ResourceLocation casingTexture) {
        float removalChance = switch (tier) {
            case GTValues.LV -> 0.50f;
            case GTValues.HV -> 0.25f;
            case GTValues.IV -> 0.125f;
            case GTValues.ZPM -> 0.0625f;
            default -> 0.50f;
        };
        int yieldMultiplier = Math.round(1.0f / removalChance);

        return REGISTRATE
                .multiblock(id, holder -> new OreExtractionDrillMachine(holder, tier))
                .langValue(GTValues.VNF[tier] + " Ore Extraction Drill")
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .noRecipeModifier()
                .appearanceBlock(casing)
                .tooltips(
                        Component.translatable("cosmiccore.machine.ore_extraction_drill.tooltip.0"),
                        Component.translatable("cosmiccore.machine.ore_extraction_drill.tooltip.1",
                                String.format("%.1f%%", removalChance * 100)),
                        Component.translatable("cosmiccore.machine.ore_extraction_drill.tooltip.2",
                                yieldMultiplier),
                        Component.translatable("cosmiccore.machine.ore_extraction_drill.tooltip.3"))
                .pattern(definition -> {
                    // Tier-specific materials - deferred to pattern build time
                    var decorativeCasing = switch (tier) {
                        case GTValues.HV -> GTBlocks.CASING_STAINLESS_CLEAN.get();
                        case GTValues.IV -> GTBlocks.CASING_TITANIUM_STABLE.get();
                        case GTValues.ZPM -> GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
                        default -> CosmicBlocks.STEEL_PLATED_BRONZE.get();
                    };
                    var frameMaterial = switch (tier) {
                        case GTValues.HV -> GTMaterials.StainlessSteel;
                        case GTValues.IV -> GTMaterials.Titanium;
                        case GTValues.ZPM -> GTMaterials.TungstenSteel;
                        default -> GTMaterials.Steel;
                    };
                    var pipeCasing = switch (tier) {
                        case GTValues.IV -> GTBlocks.CASING_TITANIUM_PIPE.get();
                        case GTValues.ZPM -> GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get();
                        default -> GTBlocks.CASING_STEEL_PIPE.get();
                    };

                    return MultiblockPatternBuilder.start()
                            .slice("AA AA", " A A ", " BCB ", "     ", "     ", "     ", "     ", "     ", "     ",
                                    "     ")
                            .slice("A   A", "A D A", "BCCCB", " CCC ", "  B  ", "  B  ", "  B  ", "     ", "     ",
                                    "     ")
                            .slice("  D  ", " DDD ", "CCCCC", " CCC ", " BCB ", " BCB ", " BCB ", "  B  ", "  B  ",
                                    "  B  ")
                            .slice("A   A", "A D A", "BCCCB", " CCC ", "  B  ", "  B  ", "  B  ", "     ", "     ",
                                    "     ")
                            .slice("AA AA", " A A ", " BSB ", "     ", "     ", "     ", "     ", "     ", "     ",
                                    "     ")
                            .where('S', controller(blocks(definition.getBlock())))
                            .where('A', blocks(decorativeCasing))
                            .where('B', Predicates.frames(frameMaterial))
                            .where('C', blocks(casing.get())
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                            .where('D', blocks(pipeCasing))
                            .where(' ', Predicates.any())
                            .build();
                })
                .workableCasingModel(casingTexture, GTCEu.id("block/multiblock/large_miner"))
                .register();
    }

    public static void init() {}
}
