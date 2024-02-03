package com.ghostipedia.cosmiccore.gtbridge.pipenet;

import com.ghostipedia.cosmiccore.Statics;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import java.util.Objects;

public class PressurePipeData implements IAttachData {

    public static final PressurePipeData EMPTY = new PressurePipeData(Statics.DEFAULT_PRESSURE, Statics.DEFAULT_PRESSURE, 1.0, (byte) 0);

    @Getter
    private final double minPressure;
    @Getter
    private final double maxPressure;
    @Getter
    private final double volume;
    @Getter
    byte connections;

    public PressurePipeData(double minPressure, double maxPressure, double volume, byte connections) {
        this.minPressure = minPressure;
        this.maxPressure = maxPressure;
        this.volume = volume;
        this.connections = connections;
    }

    public boolean checkPressure(double pressure, @Nonnull PressurePipeNet net, @Nonnull BlockPos pos) {
        if (pressure > getMaxPressure()) {
            causePressureExplosion(net, pos);
            return false;
        } else if (pressure < getMinPressure()) {
            causePressureExplosion(net, pos);
            return false;
        }
        return true;
    }

    public void causePressureExplosion(PressurePipeNet net, BlockPos pos) {
        net.causePressureExplosion(net.getLevel(), pos);
    }

    @Override
    public boolean canAttachTo(Direction side) {
        return (connections & (1 << side.ordinal())) != 0;
    }

    @Override
    public boolean setAttached(Direction side, boolean attach) {
        var result = canAttachTo(side);
        if (result != attach) {
            if (attach) {
                connections |= (1 << side.ordinal());
            } else {
                connections &= ~(1 << side.ordinal());
            }
        }
        return result != attach;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PressurePipeData pipeData) {
            return pipeData.minPressure == minPressure && pipeData.maxPressure == maxPressure && pipeData.volume == volume && pipeData.connections == connections;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxPressure, maxPressure, volume, connections);
    }
}