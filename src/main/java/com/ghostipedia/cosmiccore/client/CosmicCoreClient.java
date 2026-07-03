package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.dev.AbyssDevView;
import com.ghostipedia.cosmiccore.client.dev.MurkbloomDevControls;
import com.ghostipedia.cosmiccore.client.keybind.BootsKeybinds;
import com.ghostipedia.cosmiccore.client.keybind.QuakeMovementKeybinds;
import com.ghostipedia.cosmiccore.client.keybind.SoulSuperKeybind;
import com.ghostipedia.cosmiccore.client.murkbloom.MurkParticle;
import com.ghostipedia.cosmiccore.client.murkbloom.MurkbloomOverlay;
import com.ghostipedia.cosmiccore.client.renderer.machine.*;
import com.ghostipedia.cosmiccore.client.tooltip.FoodTooltipClientComponent;
import com.ghostipedia.cosmiccore.client.tooltip.FoodTooltipComponent;
import com.ghostipedia.cosmiccore.common.data.CosmicParticleTypes;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;

import java.io.IOException;

public class CosmicCoreClient {

    private CosmicCoreClient() {}

    public static void init(IEventBus modBus) {
        modBus.register(CosmicCoreClient.class);

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
        DynamicRenderManager.register(CosmicCore.id("star_ladder_render"), StarLadderRender.TYPE);
    }

    @Getter
    private static ShaderInstance nebulaeShader;

    @Getter
    private static ShaderInstance soulAuraShader;

    @Getter
    private static ShaderInstance voidBgShader;

    @Getter
    private static ShaderInstance galaxyBgShader;

    @Getter
    private static ShaderInstance soulCoreShader;

    @Getter
    private static ShaderInstance soulThreadsShader;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("rendertype_nebulae"),
                    DefaultVertexFormat.POSITION), (shaderInstance) -> nebulaeShader = shaderInstance);

            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("soul_aura"),
                    DefaultVertexFormat.POSITION_TEX), (shaderInstance) -> soulAuraShader = shaderInstance);

            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("void_bg"),
                    DefaultVertexFormat.POSITION_TEX), (shaderInstance) -> voidBgShader = shaderInstance);

            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("galaxy_bg"),
                    DefaultVertexFormat.POSITION_TEX), (shaderInstance) -> galaxyBgShader = shaderInstance);

            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("soul_core"),
                    DefaultVertexFormat.POSITION_TEX), (shaderInstance) -> soulCoreShader = shaderInstance);

            event.registerShader(new ShaderInstance(event.getResourceProvider(), CosmicCore.id("soul_threads"),
                    DefaultVertexFormat.POSITION_TEX), (shaderInstance) -> soulThreadsShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onGUIRegisterUIOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(CosmicCore.id("cosmichud"), new CosmicHudGuiOverlay());
        event.registerAboveAll(CosmicCore.id("murkbloom"), new MurkbloomOverlay());
    }

    @SubscribeEvent
    public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(FoodTooltipComponent.class, FoodTooltipClientComponent::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CosmicParticleTypes.MURK.get(), MurkParticle.BodyProvider::new);
        event.registerSpriteSet(CosmicParticleTypes.MURK_PALE.get(), MurkParticle.PaleProvider::new);
        event.registerSpriteSet(CosmicParticleTypes.MURK_MOTE.get(), MurkParticle.MoteProvider::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        BootsKeybinds.registerKeyMappings(event);
        QuakeMovementKeybinds.registerKeyMappings(event);
        SoulSuperKeybind.registerKeyMappings(event);
        AbyssDevView.registerKeyMappings(event);
        MurkbloomDevControls.registerKeyMappings(event);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(StellarIrisRender.IRIS_MODEL_CORE));
        event.register(ModelResourceLocation.standalone(StellarIrisRender.IRIS_MODEL_RING));
        event.register(ModelResourceLocation.standalone(StellarIrisRender.IRIS_MODEL_RING_WHITE));

        event.register(ModelResourceLocation.standalone(ConceptIncineratorRender.IRIS_MODEL_CORE));
        event.register(ModelResourceLocation.standalone(ConceptIncineratorRender.IRIS_MODEL_RING));
        event.register(ModelResourceLocation.standalone(ConceptIncineratorRender.IRIS_MODEL_RING_WHITE));
        event.register(ModelResourceLocation.standalone(ConceptIncineratorRender.STAR_CORE));
        event.register(ModelResourceLocation.standalone(ConceptIncineratorRender.STAR_CORE_MIDDLE));
        event.register(ModelResourceLocation.standalone(ConceptIncineratorRender.STAR_CORE_OUTER));

        event.register(ModelResourceLocation.standalone(StarBallastRender.STAR_MODEL_CORE));
        event.register(ModelResourceLocation.standalone(StarBallastRender.STAR_MODEL_OUTER));
        event.register(ModelResourceLocation.standalone(StarBallastRender.STAR_MODEL_INNER));
        event.register(ModelResourceLocation.standalone(StarBallastRender.STAR_MODEL_BEAM));
    }

    /*
     * SHELVED bee client registration — Forestry dropped on 1.21.1 (bead cosmiccore-42.13)
     * public static class CosmicBeesClientRegistration implements Consumer<IClientRegistration> {
     * 
     * @Override
     * public void accept(IClientRegistration client) {
     * registerApiculture(client);
     * }
     * 
     * // Spotless: off
     * private static void registerApiculture(IClientRegistration client) {
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_OXYGEN, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_OXYGEN, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_OXYGEN, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_HYDROGEN, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_HYDROGEN, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_HYDROGEN, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_NITROGEN, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_NITROGEN, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_NITROGEN, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_ARGON, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_ARGON, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LOFTY_ARGON, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.ROSE_POLYMER, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ROSE_POLYMER, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ROSE_POLYMER, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.CITRUS_POLYMER, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.CITRUS_POLYMER, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.CITRUS_POLYMER, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.WAXY_POLYMER, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.WAXY_POLYMER, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.WAXY_POLYMER, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.BIOHAZARD, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.BIOHAZARD, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.BIOHAZARD, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.PALE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.PALE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.PALE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.SOUL, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SOUL, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SOUL, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.RUNIC, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.RUNIC, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.RUNIC, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.AMBROSIC, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.AMBROSIC, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.AMBROSIC, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.ABRASIVE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ABRASIVE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ABRASIVE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.ENERGIZED, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ENERGIZED, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ENERGIZED, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.SLICK, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SLICK, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SLICK, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.PYROLYTIC, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.PYROLYTIC, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.PYROLYTIC, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.LUNAR, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LUNAR, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.LUNAR, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.SOLAR, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SOLAR, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SOLAR, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * // NEW BEES
     * client.setCustomBeeModel(CosmicBeesSpecies.HADAL, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.HADAL, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.HADAL, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.SHAMAN, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SHAMAN, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.SHAMAN, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.COSMOS, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.ASHEN, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ASHEN, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ASHEN, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.FRACKING, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.FRACKING, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.FRACKING, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.FATE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.FATE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.FATE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.GRAND_GARDEN, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.GRAND_GARDEN, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.GRAND_GARDEN, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.ARCHITECT, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ARCHITECT, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ARCHITECT, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.INQUISITIVE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.INQUISITIVE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.INQUISITIVE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.HELLSMITH, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.HELLSMITH, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.HELLSMITH, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.RADOXIA, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.RADOXIA, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.RADOXIA, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.ABSENT, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ABSENT, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ABSENT, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.ILLUSIVE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ILLUSIVE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.ILLUSIVE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.CONSTRUCTIVE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.CONSTRUCTIVE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.CONSTRUCTIVE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.PRISMATIC, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.PRISMATIC, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.PRISMATIC, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.HYDRAULIC, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.HYDRAULIC, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.HYDRAULIC, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.COBBLED, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.COBBLED, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.COBBLED, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.EXHAUSTIVE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.EXHAUSTIVE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.EXHAUSTIVE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * 
     * client.setCustomBeeModel(CosmicBeesSpecies.VIRTUE, BeeLifeStage.DRONE,
     * CosmicCore.id("item/bee/bee_drone_fuzzy"));
     * client.setCustomBeeModel(CosmicBeesSpecies.VIRTUE, BeeLifeStage.PRINCESS,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_princess"));
     * client.setCustomBeeModel(CosmicBeesSpecies.VIRTUE, BeeLifeStage.QUEEN,
     * CosmicCore.id("item/bee/bee_drone_fuzzy_queen"));
     * }
     * }
     */

    @EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class HideVanillaOverlays {

        @SubscribeEvent
        public static void onOverlayPre(RenderGuiLayerEvent.Pre event) {
            if (event.getName().equals(VanillaGuiLayers.AIR_LEVEL)) {
                event.setCanceled(true);
            }
        }
    }
}
