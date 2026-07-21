package com.ghostipedia.cosmiccore.client.map.xaero.worldmap;

import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.client.map.RevealedFieldStorage;
import com.ghostipedia.cosmiccore.client.map.RevealedFields;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobElement;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import xaero.map.element.MapElementRenderProvider;
import xaero.map.gui.GuiMap;
import xaero.map.world.MapWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FieldBlobElementRenderProvider extends MapElementRenderProvider<FieldBlobElement, Object> {

    private Iterator<FieldBlobElement> iterator;

    public FieldBlobElementRenderProvider() {}

    @Override
    public void begin(int location, Object context) {
        RevealedFieldStorage.ensureLoaded();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            this.iterator = null;
            return;
        }
        ResourceKey<Level> dimension = viewedDimension(mc);
        if (dimension == null) {
            this.iterator = null;
            return;
        }
        Collection<RevealedField> fields = RevealedFields.INSTANCE.forDim(dimension);
        if (fields.isEmpty()) {
            this.iterator = null;
            return;
        }
        List<FieldBlobElement> elements = new ArrayList<>(fields.size());
        for (RevealedField field : fields) {
            elements.add(new FieldBlobElement(dimension, field));
        }
        this.iterator = elements.iterator();
    }

    @Override
    public boolean hasNext(int location, Object context) {
        return this.iterator != null && this.iterator.hasNext();
    }

    @Override
    public FieldBlobElement getNext(int location, Object context) {
        return this.iterator.next();
    }

    @Override
    public void end(int location, Object context) {}

    private static ResourceKey<Level> viewedDimension(Minecraft mc) {
        if (mc.screen instanceof GuiMap map) {
            if (map.getMapProcessor() == null) return null;
            MapWorld mapWorld = map.getMapProcessor().getMapWorld();
            return mapWorld == null ? null : mapWorld.getCurrentDimensionId();
        }
        return mc.level.dimension();
    }
}
