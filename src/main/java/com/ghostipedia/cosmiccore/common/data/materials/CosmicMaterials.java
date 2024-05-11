package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTFluidBuilder;
import com.gregtechceu.gtceu.common.data.GTElements;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CosmicMaterials {
    public static Material Prisma;
    public static Material PrismaticTungstensteel;

    public static void register() {
        Prisma = new Material.Builder(CosmicCore.id("prisma"))
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .element(CosmicElements.Pi)
                .buildAndRegister();
        PrismaticTungstensteel = new Material.Builder(CosmicCore.id("prismatic_tungstensteel"))
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .color(0x6f42cf).secondaryColor(0xc71414)
                .flags(GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(Prisma, 1, TungstenSteel, 1)
                .rotorStats(10.0f, 2.0f, 128)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV])
                .buildAndRegister();

    }
}
