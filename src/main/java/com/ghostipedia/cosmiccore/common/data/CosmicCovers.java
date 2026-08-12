package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.FluidRegulatorCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.gregtechceu.gtceu.common.data.GTCovers;

import java.util.function.Supplier;

public final class CosmicCovers {

    public static final int STEAM_ITEM_TRANSFER_RATE = 4;
    public static final int STEAM_FLUID_TRANSFER_RATE = 32;

    public static final CoverDefinition STEAM_CONVEYOR = register("conveyor.steam",
            (definition, coverable, side) -> new ConveyorCover(definition, coverable, side, GTValues.ULV,
                    STEAM_ITEM_TRANSFER_RATE),
            stockRenderer(GTCovers.CONVEYORS[0]));

    public static final CoverDefinition STEAM_PUMP = register("pump.steam",
            (definition, coverable, side) -> new PumpCover(definition, coverable, side, GTValues.ULV,
                    STEAM_FLUID_TRANSFER_RATE),
            stockRenderer(GTCovers.PUMPS[0]));

    public static final CoverDefinition STEAM_ROBOT_ARM = register("robot_arm.steam",
            (definition, coverable, side) -> new RobotArmCover(definition, coverable, side, GTValues.ULV,
                    STEAM_ITEM_TRANSFER_RATE),
            stockRenderer(GTCovers.ROBOT_ARMS[0]));

    public static final CoverDefinition STEAM_FLUID_REGULATOR = register("fluid_regulator.steam",
            (definition, coverable, side) -> new FluidRegulatorCover(definition, coverable, side, GTValues.ULV,
                    STEAM_FLUID_TRANSFER_RATE),
            stockRenderer(GTCovers.FLUID_REGULATORS[0]));

    private CosmicCovers() {}

    private static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behavior,
                                            Supplier<Supplier<ICoverRenderer>> renderer) {
        CoverDefinition definition = new CoverDefinition(CosmicCore.id(id), behavior, renderer);
        GTRegistries.register(GTRegistries.COVERS, definition.getId(), definition);
        return definition;
    }

    private static Supplier<Supplier<ICoverRenderer>> stockRenderer(CoverDefinition definition) {
        return definition::getCoverRenderer;
    }

    public static void init() {}
}
