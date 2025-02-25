package com.ghostipedia.cosmiccore.common.block.noctyx;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public class NoctyxConnectorBlock extends NoctyxBlock {

    protected static final Map<Direction, Vector3f> offsetMap = new EnumMap<>(Direction.class);
    static {
        // todo: fill out actual values according to model (this is the offset to the laser target)
        for (var direction : Direction.values()) {
            offsetMap.put(direction, defaultOffset);
        }
    }

    public NoctyxConnectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Vector3f getLaserOffset(Direction attachedSide) {
        return offsetMap.get(attachedSide);
    }
}
