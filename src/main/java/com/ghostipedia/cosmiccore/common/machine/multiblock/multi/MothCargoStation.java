package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MothCargoStationMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STEEL_SOLID;

/**
 * Moth Cargo Station - Sender multiblock for the Cargo Moths system.
 * Ships items and fluids to linked Moth Cargo Drop Off stations.
 */
public class MothCargoStation {

    // Forestry beehive blocks used as moth homes
    public static final ResourceLocation BEEHIVE_FOREST = ResourceLocation.fromNamespaceAndPath("forestry",
            "beehive_forest");
    public static final ResourceLocation BEEHIVE_LUSH = ResourceLocation.fromNamespaceAndPath("forestry",
            "beehive_lush");
    public static final ResourceLocation BEEHIVE_DESERT = ResourceLocation.fromNamespaceAndPath("forestry",
            "beehive_desert");
    public static final ResourceLocation BEEHIVE_END = ResourceLocation.fromNamespaceAndPath("forestry", "beehive_end");

    /**
     * Check if a block is a valid moth home (any tier of Forestry beehive).
     */
    public static boolean isMothHome(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        return blockId.equals(BEEHIVE_FOREST) ||
                blockId.equals(BEEHIVE_LUSH) ||
                blockId.equals(BEEHIVE_DESERT) ||
                blockId.equals(BEEHIVE_END);
    }

    public static final MultiblockMachineDefinition MOTH_CARGO_STATION = REGISTRATE
            .multiblock("moth_cargo_station", MothCargoStationMachine::new)
            .langValue("Moth Cargo Station")
            .tooltips(
                    Component.literal("Ships items and fluids using cargo moths"),
                    Component.literal("Link to Moth Cargo Drop Offs with a datastick"),
                    Component.literal("Add Moth Homes to increase capacity and speed"),
                    Component.literal("Feed moths honey or pale oil for bonuses!"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(CASING_STEEL_SOLID)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    // Tower structure: 3x3 footprint, 6 blocks tall
                    // Moth homes (beehives) in center column - up to 4 can be placed
                    // Open walls (air in center) so beehives are visible from all sides
                    .slice("CCC", "C C", "C C", "C C", "C C", "CCC")
                    .slice("CCC", " M ", " M ", " M ", " M ", "CCC")
                    .slice("CQC", "C C", "C C", "C C", "C C", "CCC")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(4))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4)))
                    .where('M', CosmicPredicates.mothHomes())
                    .build())
            // spotless:on
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void init() {}
}
