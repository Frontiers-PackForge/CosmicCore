package com.ghostipedia.cosmiccore.integration.journeymap;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.worldgen.survey.VeinSurveyUtil.VeinInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.MarkerOverlay;
import journeymap.client.api.model.MapImage;

import java.util.Map;

public class PredictedVeinRenderer {

    private static final Map<String, MarkerOverlay> predictedMarkers = new Object2ObjectOpenHashMap<>();
    private static final int PREDICTED_VEIN_COLOR = 0x80808080;
    private static NativeImage questionMarkImage;

    public static String getMarkerId(VeinInfo vein) {
        BlockPos center = vein.center();
        return "predicted_vein@[" + center.getX() + "," + center.getZ() + "]";
    }

    public static boolean addPredictedVein(ResourceKey<Level> dim, VeinInfo vein) {
        if (!CosmicJourneymapPlugin.isActive()) {
            return false;
        }

        IClientAPI api = CosmicJourneymapPlugin.getApi();
        if (api == null || !api.playerAccepts(CosmicCore.MOD_ID, DisplayType.Image)) {
            return false;
        }

        String id = getMarkerId(vein);
        if (predictedMarkers.containsKey(id)) {
            return false;
        }

        MarkerOverlay marker = createPredictedMarker(id, dim, vein);
        if (marker == null) {
            return false;
        }

        predictedMarkers.put(id, marker);

        try {
            api.show(marker);
        } catch (Exception e) {
            CosmicCore.LOGGER.error("Failed to show predicted vein marker: {}", vein.getVeinName(), e);
        }

        return true;
    }

    public static boolean removePredictedVein(VeinInfo vein) {
        return removePredictedVein(getMarkerId(vein));
    }

    public static boolean removePredictedVein(String id) {
        MarkerOverlay marker = predictedMarkers.remove(id);
        if (marker == null) {
            return false;
        }

        IClientAPI api = CosmicJourneymapPlugin.getApi();
        if (api != null) {
            api.remove(marker);
        }
        return true;
    }

    public static void removePredictedVeinsAt(BlockPos pos) {
        String idPrefix = "predicted_vein@[" + pos.getX() + "," + pos.getZ() + "]";
        var toRemove = predictedMarkers.keySet().stream()
                .filter(id -> id.equals(idPrefix))
                .toList();

        for (String id : toRemove) {
            removePredictedVein(id);
        }
    }

    public static void removePredictedVeinsInArea(BlockPos center, int radius) {
        var toRemove = predictedMarkers.entrySet().stream()
                .filter(entry -> {
                    BlockPos markerPos = entry.getValue().getPoint();
                    int dx = markerPos.getX() - center.getX();
                    int dz = markerPos.getZ() - center.getZ();
                    return Math.sqrt(dx * dx + dz * dz) <= radius;
                })
                .map(Map.Entry::getKey)
                .toList();

        for (String id : toRemove) {
            removePredictedVein(id);
        }
    }

    public static void clear() {
        IClientAPI api = CosmicJourneymapPlugin.getApi();
        if (api != null) {
            predictedMarkers.values().forEach(api::remove);
        }
        predictedMarkers.clear();
    }

    public static void hideAllMarkers() {
        IClientAPI api = CosmicJourneymapPlugin.getApi();
        if (api != null) {
            predictedMarkers.values().forEach(api::remove);
        }
    }

    public static void showAllMarkers() {
        IClientAPI api = CosmicJourneymapPlugin.getApi();
        if (api != null) {
            predictedMarkers.values().forEach(marker -> {
                try {
                    api.show(marker);
                } catch (Exception e) {
                    CosmicCore.LOGGER.error("Failed to show predicted vein marker", e);
                }
            });
        }
    }

    private static MarkerOverlay createPredictedMarker(String id, ResourceKey<Level> dim, VeinInfo vein) {
        BlockPos center = vein.center();

        NativeImage image = getOrCreateQuestionMarkImage();
        if (image == null) {
            return null;
        }

        MapImage mapImage = new MapImage(image)
                .centerAnchors()
                .setDisplayWidth(20)
                .setDisplayHeight(20)
                .setOpacity(0.7f);

        MarkerOverlay marker = new MarkerOverlay(CosmicCore.MOD_ID, id, center, mapImage);

        String tooltip = "? " + vein.getVeinName() + " (Predicted)\n" +
                "Position: " + center.getX() + ", " + center.getZ() + "\n" +
                "This vein has not been confirmed.\n" +
                "Visit the area to verify.";

        marker.setDimension(dim)
                .setLabel("?")
                .setTitle(tooltip);

        return marker;
    }

    private static NativeImage getOrCreateQuestionMarkImage() {
        if (questionMarkImage != null) {
            return questionMarkImage;
        }

        int size = 16;
        questionMarkImage = new NativeImage(NativeImage.Format.RGBA, size, size, false);

        int gray = 0xFF808080;
        int darkGray = 0xFF404040;
        int center = size / 2;
        int radius = size / 2 - 1;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int dx = x - center;
                int dy = y - center;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist <= radius && dist > radius - 2) {
                    questionMarkImage.setPixelRGBA(x, y, darkGray);
                } else if (dist <= radius - 2) {
                    questionMarkImage.setPixelRGBA(x, y, gray);
                } else {
                    questionMarkImage.setPixelRGBA(x, y, 0);
                }
            }
        }

        drawQuestionMark(questionMarkImage, center, center, 0xFFFFFFFF);

        return questionMarkImage;
    }

    private static void drawQuestionMark(NativeImage image, int cx, int cy, int color) {
        image.setPixelRGBA(cx - 1, cy - 4, color);
        image.setPixelRGBA(cx, cy - 4, color);
        image.setPixelRGBA(cx + 1, cy - 4, color);
        image.setPixelRGBA(cx + 2, cy - 3, color);
        image.setPixelRGBA(cx + 2, cy - 2, color);
        image.setPixelRGBA(cx + 1, cy - 1, color);
        image.setPixelRGBA(cx, cy, color);
        image.setPixelRGBA(cx, cy + 2, color);
    }

    public static int getMarkerCount() {
        return predictedMarkers.size();
    }

    public static boolean hasMarker(VeinInfo vein) {
        return predictedMarkers.containsKey(getMarkerId(vein));
    }

    public static boolean hasMarkerAt(BlockPos pos) {
        String id = "predicted_vein@[" + pos.getX() + "," + pos.getZ() + "]";
        return predictedMarkers.containsKey(id);
    }
}
