package com.ghostipedia.cosmiccore.gtbridge.pipenet;


import com.ghostipedia.cosmiccore.Statics;
import com.ghostipedia.cosmiccore.gtbridge.machines.IPressureContainer;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class PressurePipeNet extends PipeNet<PressurePipeData> implements IPressureContainer {

    private double netParticles = Statics.DEFAULT_PRESSURE;
    private double volume = 1.0D;
    private double minNetPressure = Double.MAX_VALUE;
    private double maxNetPressure = Double.MIN_VALUE;

    public PressurePipeNet(LevelPipeNet<PressurePipeData, ? extends PipeNet> world) {
        super(world);
    }

    @Override
    protected void writeNodeData(@Nonnull PressurePipeData pressurePipeData, @Nonnull CompoundTag nbt) {
        nbt.putDouble("MinP", pressurePipeData.getMinPressure());
        nbt.putDouble("MaxP", pressurePipeData.getMaxPressure());
        nbt.putDouble("Volume", pressurePipeData.getVolume());
        nbt.putByte("connections", pressurePipeData.getConnections());
    }

    @Override
    protected PressurePipeData readNodeData(@Nonnull CompoundTag nbt) {
        return new PressurePipeData(nbt.getDouble("MinP"), nbt.getDouble("MaxP"), nbt.getDouble("Volume"), nbt.getByte("connections"));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = super.serializeNBT();
        compound.putDouble("minNetP", minNetPressure);
        compound.putDouble("maxNetP", maxNetPressure);
        compound.putDouble("Volume", volume);
        compound.putDouble("Particles", netParticles);
        return compound;
    }


    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.minNetPressure = nbt.getDouble("minNetP");
        this.maxNetPressure = nbt.getDouble("maxNetP");
        this.volume = nbt.getDouble("Volume");
        this.netParticles = nbt.getDouble("Particles");
    }

    @Override
    protected void onNodeConnectionsUpdate() {
        super.onNodeConnectionsUpdate();
        this.minNetPressure = getAllNodes().values().stream().mapToDouble(node -> node.data.getMinPressure()).max().orElse(Double.MAX_VALUE);
        this.maxNetPressure = getAllNodes().values().stream().mapToDouble(node -> node.data.getMaxPressure()).min().orElse(Double.MIN_VALUE);
        final double oldVolume = getVolume();
        this.volume = Math.max(1, getAllNodes().values().stream().mapToDouble(node -> node.data.getVolume()).sum());
        this.netParticles *= getVolume() / oldVolume;
    }

    @Override
    public void onPipeConnectionsUpdate() {
        super.onPipeConnectionsUpdate();
    }

    @Override
    public double getParticles() {
        return netParticles;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public void setParticles(double amount) {
        this.netParticles = amount;
    }

    @Override
    public boolean changeParticles(double amount, boolean simulate) {
        if (simulate) return isPressureSafe(getPressureForParticles(getParticles() + amount));
        setParticles(getParticles() + amount);
        PressureNetWalker.checkPressure(this, getAllNodes().keySet().iterator().next(), getPressure());
        return isPressureSafe();
    }

    public void onLeak() {
        if (getPressure() < Statics.DEFAULT_PRESSURE) changeParticles(getLeakRate(), false);
        else if (getPressure() > Statics.DEFAULT_PRESSURE) changeParticles(-getLeakRate(), false);
    }

    public double getLeakRate() {
        return 1000.0D;
    }

    @Override
    public double getMinPressure() {
        return minNetPressure;
    }

    @Override
    public double getMaxPressure() {
        return maxNetPressure;
    }
}