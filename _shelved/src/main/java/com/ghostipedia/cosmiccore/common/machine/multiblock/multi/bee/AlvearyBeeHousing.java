package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.core.HumidityType;
import forestry.api.core.IError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.TemperatureType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Per-thread IBeeHousing implementation.
 * Delegates climate/modifiers to the parent machine, inventory to AlvearyBeeInventory.
 * Does NOT use BeekeepingLogic — we own the tick loop ourselves.
 */
public class AlvearyBeeHousing implements IBeeHousing {

    private final MechanicalAlvearyMachine machine;
    private final AlvearyBeeInventory beeInventory;

    public AlvearyBeeHousing(MechanicalAlvearyMachine machine, AlvearyBeeInventory inventory) {
        this.machine = machine;
        this.beeInventory = inventory;
    }

    @Override
    public Iterable<IBeeModifier> getBeeModifiers() {
        var composite = machine.getModifierComposite();
        if (composite == null) return Collections.emptyList();
        return Collections.singletonList(composite);
    }

    @Override
    public Iterable<IBeeListener> getBeeListeners() {
        return Collections.emptyList();
    }

    @Override
    public AlvearyBeeInventory getBeeInventory() {
        return beeInventory;
    }

    @Override
    public IBeekeepingLogic getBeekeepingLogic() {
        throw new UnsupportedOperationException("Mechanical Alveary uses custom tick logic");
    }

    @Override
    public int getBlockLightValue() {
        var level = machine.getLevel();
        if (level == null) return 15;
        return level.getMaxLocalRawBrightness(machine.getPos().above());
    }

    @Override
    public boolean canBlockSeeTheSky() {
        var composite = machine.getModifierComposite();
        if (composite != null && composite.isSunlightSimulated()) return true;
        var level = machine.getLevel();
        if (level == null) return false;
        return level.canSeeSkyFromBelowWater(machine.getPos().above(3));
    }

    @Override
    public boolean isRaining() {
        var composite = machine.getModifierComposite();
        if (composite != null && composite.isSealed()) return false;
        var level = machine.getLevel();
        if (level == null) return false;
        return level.isRainingAt(machine.getPos().above(3));
    }

    @Override
    public Level getWorldObj() {
        return machine.getLevel();
    }

    @Override
    public Holder<Biome> getBiome() {
        var level = machine.getLevel();
        if (level == null) throw new IllegalStateException("Level not available");
        return level.getBiome(machine.getPos());
    }

    @Override
    public TemperatureType temperature() {
        var climate = machine.getClimateState();
        if (climate == null) return TemperatureType.NORMAL;
        return climate.getEffectiveTemperature();
    }

    @Override
    public HumidityType humidity() {
        var climate = machine.getClimateState();
        if (climate == null) return HumidityType.NORMAL;
        return climate.getEffectiveHumidity();
    }

    @Nullable
    @Override
    public GameProfile getOwner() {
        return null;
    }

    @Override
    public Vec3 getBeeFXCoordinates() {
        var pos = machine.getPos();
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
    }

    @Override
    public BlockPos getCoordinates() {
        return machine.getPos();
    }

    @Override
    public IErrorLogic getErrorLogic() {
        return DUMMY_ERROR_LOGIC;
    }

    private static final IErrorLogic DUMMY_ERROR_LOGIC = new IErrorLogic() {

        @Override
        public boolean setCondition(boolean condition, IError error) {
            return false;
        }

        @Override
        public boolean contains(IError error) {
            return false;
        }

        @Override
        public Set<IError> getErrors() {
            return Collections.emptySet();
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public void clearErrors() {}
    };
}
