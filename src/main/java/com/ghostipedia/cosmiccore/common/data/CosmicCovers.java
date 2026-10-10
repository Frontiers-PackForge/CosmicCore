package com.ghostipedia.cosmiccore.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.FluidRegulatorCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.gregtechceu.gtceu.common.data.GTCovers;

import net.minecraft.core.Holder;

import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public final class CosmicCovers {

    public static final int STEAM_ITEM_TRANSFER_RATE = 4;
    public static final int STEAM_FLUID_TRANSFER_RATE = 32;

    public static final Holder<CoverDefinition> STEAM_CONVEYOR = register("conveyor.steam",
            (definition, coverable, side) -> new ConveyorCover(definition, coverable, side, GTValues.ULV,
                    STEAM_ITEM_TRANSFER_RATE),
            stockRenderer(GTCovers.CONVEYORS[GTValues.LV]));

    public static final Holder<CoverDefinition> STEAM_PUMP = register("pump.steam",
            (definition, coverable, side) -> new PumpCover(definition, coverable, side, GTValues.ULV,
                    STEAM_FLUID_TRANSFER_RATE),
            stockRenderer(GTCovers.PUMPS[GTValues.LV]));

    public static final Holder<CoverDefinition> STEAM_ROBOT_ARM = register("robot_arm.steam",
            (definition, coverable, side) -> new RobotArmCover(definition, coverable, side, GTValues.ULV,
                    STEAM_ITEM_TRANSFER_RATE),
            stockRenderer(GTCovers.ROBOT_ARMS[GTValues.LV]));

    public static final Holder<CoverDefinition> STEAM_FLUID_REGULATOR = register("fluid_regulator.steam",
            (definition, coverable, side) -> new FluidRegulatorCover(definition, coverable, side, GTValues.ULV,
                    STEAM_FLUID_TRANSFER_RATE),
            stockRenderer(GTCovers.FLUID_REGULATORS[GTValues.LV]));

    private CosmicCovers() {}

    private static Holder<CoverDefinition> register(String id, CoverDefinition.CoverBehaviourProvider behavior,
                                                    Supplier<Supplier<ICoverRenderer>> renderer) {
        return REGISTRATE.cover(id, behavior, renderer);
    }

    private static Supplier<Supplier<ICoverRenderer>> stockRenderer(Holder<CoverDefinition> definition) {
        return () -> definition.value().getCoverRenderer();
    }

    public static void init() {}
}
