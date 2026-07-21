package com.ghostipedia.cosmiccore.client.map.xaero.minimap;

import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.client.map.RevealedFieldStorage;
import com.ghostipedia.cosmiccore.client.map.RevealedFields;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FieldBlobElementRenderProvider
                                            extends
                                            MinimapElementRenderProvider<FieldBlobElement, MinimapElementMapRendererHandler> {

    private static final int VIEW_MARGIN_BLOCKS = 140;
    private static final int VIEW_FALLBACK_BLOCKS = 256;

    private Iterator<FieldBlobElement> iterator;

    public FieldBlobElementRenderProvider() {}

    @Override
    public void begin(MinimapElementRenderLocation location, MinimapElementMapRendererHandler context) {
        RevealedFieldStorage.ensureLoaded();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            this.iterator = null;
            return;
        }

        var dimension = mc.level.dimension();
        Collection<RevealedField> all = RevealedFields.INSTANCE.forDim(dimension);
        if (all.isEmpty()) {
            this.iterator = null;
            return;
        }

        int px = (int) player.getX();
        int pz = (int) player.getZ();
        long viewSqr = viewRadiusSqr();

        List<FieldBlobElement> elements = new ArrayList<>();
        for (RevealedField field : all) {
            if (distSqr(field, px, pz) <= viewSqr) {
                elements.add(new FieldBlobElement(dimension, field));
            }
        }
        this.iterator = elements.isEmpty() ? null : elements.iterator();
    }

    @Override
    public boolean hasNext(MinimapElementRenderLocation location, MinimapElementMapRendererHandler context) {
        return this.iterator != null && this.iterator.hasNext();
    }

    @Override
    public FieldBlobElement getNext(MinimapElementRenderLocation location, MinimapElementMapRendererHandler context) {
        return this.iterator.next();
    }

    @Override
    public void end(MinimapElementRenderLocation location, MinimapElementMapRendererHandler context) {}

    private static long viewRadiusSqr() {
        try {
            var processor = BuiltInHudModules.MINIMAP.getCurrentSession().getProcessor();
            double radius = (processor.getMinimapSize() / 2.0) / Math.max(0.01, processor.getMinimapZoom()) +
                    VIEW_MARGIN_BLOCKS;
            return (long) (radius * radius);
        } catch (Exception e) {
            return (long) VIEW_FALLBACK_BLOCKS * VIEW_FALLBACK_BLOCKS;
        }
    }

    private static long distSqr(RevealedField field, int px, int pz) {
        long dx = field.x() - px;
        long dz = field.z() - pz;
        return dx * dx + dz * dz;
    }
}
