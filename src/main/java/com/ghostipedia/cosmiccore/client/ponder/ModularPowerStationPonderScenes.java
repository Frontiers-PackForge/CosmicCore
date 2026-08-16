package com.ghostipedia.cosmiccore.client.ponder;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

public final class ModularPowerStationPonderScenes {

    private static final String HEADER = "cosmiccore.ponder.modular_power_station.header";

    private ModularPowerStationPonderScenes() {}

    public static void assembly(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("modular_power_station", Component.translatable(HEADER).getString());
        scene.configureBasePlate(0, 0, 21);
        scene.scaleSceneView(0.42f);
        scene.showBasePlate();
        scene.idle(10);

        Selection core = util.select().fromTo(14, 1, 3, 18, 4, 7);
        Selection firstModule = util.select().fromTo(12, 1, 3, 13, 4, 7);
        Selection secondModule = util.select().fromTo(10, 1, 3, 11, 4, 7);
        Selection terminal = util.select().fromTo(2, 1, 3, 9, 4, 7);
        Selection roof = util.select().fromTo(2, 5, 3, 18, 5, 7);
        Selection integralComponents = util.select().fromTo(10, 1, 4, 12, 4, 6);
        Selection stator = util.select().fromTo(3, 1, 4, 8, 4, 6);

        scene.world().showSection(core, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .sharedText(CosmicCore.id("modular_power_station.text_1"))
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(16, 3, 5));
        scene.idle(100);

        scene.world().showSection(firstModule, Direction.EAST);
        scene.idle(15);
        scene.overlay().showText(90)
                .sharedText(CosmicCore.id("modular_power_station.text_2"))
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(12, 3, 5));
        scene.idle(100);

        scene.world().showSection(secondModule, Direction.EAST);
        scene.idle(15);
        scene.overlay().showText(90)
                .sharedText(CosmicCore.id("modular_power_station.text_3"))
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(10, 3, 5));
        scene.idle(100);

        scene.overlay().showOutline(PonderPalette.RED, "integral_components", integralComponents, 100);
        scene.overlay().showText(90)
                .sharedText(CosmicCore.id("modular_power_station.text_4"))
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(11, 3, 5));
        scene.idle(100);

        scene.world().showSection(terminal, Direction.EAST);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.GREEN, "stator", stator, 100);
        scene.overlay().showText(100)
                .sharedText(CosmicCore.id("modular_power_station.text_5"))
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(5, 3, 5));
        scene.idle(100);
        scene.world().showSection(roof, Direction.DOWN);
        scene.idle(20);
    }
}
