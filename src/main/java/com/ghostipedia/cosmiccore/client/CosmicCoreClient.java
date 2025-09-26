package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.bee.CosmicBeesSpecies;
import com.ghostipedia.cosmiccore.client.renderer.machine.*;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.client.plugin.IClientRegistration;
import lombok.Getter;

import java.io.IOException;
import java.util.function.Consumer;

import static dev.ftb.mods.ftblibrary.util.KnownServerRegistries.client;

public class CosmicCoreClient {

    private CosmicCoreClient() {}

    public static void init(IEventBus modBus) {
        modBus.register(CosmicCoreClient.class);

        DynamicRenderManager.register(CosmicCore.id("hpca_indicator"), HPCAIndicatorRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("hellfire_foundry_parts"), HellFireFoundryPartRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("hemographic_transfuser"), HemophagicTransfuserRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("suffering_chamber"), SufferingChamberRenderer.TYPE);
        DynamicRenderManager.register(CosmicCore.id("stellar_iris"), StellarIrisRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("star_ballast"), StarBallastRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("welder_arm_render"), WelderArmRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("concept_incinerator"), ConceptIncineratorRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("spirit_crucible"), SpiritCrucibleRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("biovat_render"), BioVatRender.TYPE);
        DynamicRenderManager.register(CosmicCore.id("tester_render"), RenderTesterHelper.TYPE);
    }

    @Getter
    private static ShaderInstance nebulaeShader;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("rendertype_nebulae"),
                    DefaultVertexFormat.POSITION), (shaderInstance) -> nebulaeShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onGUIRegisterUIOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("cosmichud", new CosmicHudGuiOverlay());
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(StellarIrisRender.IRIS_MODEL_CORE);
        event.register(StellarIrisRender.IRIS_MODEL_RING);
        event.register(StellarIrisRender.IRIS_MODEL_RING_WHITE);

        event.register(ConceptIncineratorRender.IRIS_MODEL_CORE);
        event.register(ConceptIncineratorRender.IRIS_MODEL_RING);
        event.register(ConceptIncineratorRender.IRIS_MODEL_RING_WHITE);
        event.register(ConceptIncineratorRender.STAR_CORE);
        event.register(ConceptIncineratorRender.STAR_CORE_MIDDLE);
        event.register(ConceptIncineratorRender.STAR_CORE_OUTER);

        event.register(StarBallastRender.STAR_MODEL_CORE);
        event.register(StarBallastRender.STAR_MODEL_OUTER);
        event.register(StarBallastRender.STAR_MODEL_INNER);
        event.register(StarBallastRender.STAR_MODEL_BEAM);
    }

    public static class CosmicBeesClientRegistration implements Consumer<IClientRegistration> {

        @Override
        public void accept(IClientRegistration client) {
            registerApiculture(client);
        }

        // Spotless: off
        private static void registerApiculture(IClientRegistration client) {
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_OXYGEN, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_OXYGEN, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_OXYGEN, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_HYDROGEN, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_HYDROGEN, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_HYDROGEN, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_NITROGEN, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_NITROGEN, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_NITROGEN, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_ARGON, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_ARGON, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_ARGON, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.ROSE_POLYMER, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.ROSE_POLYMER, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.ROSE_POLYMER, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.CITRUS_POLYMER, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.CITRUS_POLYMER, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.CITRUS_POLYMER, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.WAXY_POLYMER, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.WAXY_POLYMER, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.WAXY_POLYMER, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.BIOHAZARD, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.BIOHAZARD, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.BIOHAZARD, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.PALE, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.PALE, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.PALE, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.SOUL, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.SOUL, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.SOUL, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.RUNIC, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.RUNIC, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.RUNIC, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.AMBROSIC, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.AMBROSIC, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.AMBROSIC, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.ABRASIVE, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.ABRASIVE, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.ABRASIVE, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.ENERGIZED, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.ENERGIZED, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.ENERGIZED, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.SLICK, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.SLICK, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.SLICK, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.PYROLYTIC, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.PYROLYTIC, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.PYROLYTIC, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.LUNAR, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.LUNAR, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.LUNAR, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.SOLAR, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.SOLAR, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.SOLAR, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));

            client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.DRONE,
                    CosmicCore.id("item/bee/bee_drone_fuzzy"));
            client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.PRINCESS,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
            client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.QUEEN,
                    CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
        }
    }
}
