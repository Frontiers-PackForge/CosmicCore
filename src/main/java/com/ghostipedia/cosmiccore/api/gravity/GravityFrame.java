package com.ghostipedia.cosmiccore.api.gravity;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public record GravityFrame(
                           GravityMode mode,
                           Direction down,
                           double strength,
                           ResourceLocation sourceId,
                           int priority,
                           int transitionTicks,
                           double adhesionDistance,
                           long revision) {

    public static final double MAX_STRENGTH = 16.0;
    public static final GravityFrame NORMAL = new GravityFrame(
            GravityMode.NORMAL,
            Direction.DOWN,
            1.0,
            CosmicCore.id("normal"),
            0,
            0,
            0.0,
            0L);

    public static final Codec<GravityFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GravityMode.CODEC.fieldOf("mode").forGetter(GravityFrame::mode),
            Direction.CODEC.fieldOf("down").forGetter(GravityFrame::down),
            Codec.DOUBLE.fieldOf("strength").forGetter(GravityFrame::strength),
            ResourceLocation.CODEC.fieldOf("source_id").forGetter(GravityFrame::sourceId),
            Codec.INT.fieldOf("priority").forGetter(GravityFrame::priority),
            Codec.INT.fieldOf("transition_ticks").forGetter(GravityFrame::transitionTicks),
            Codec.DOUBLE.fieldOf("adhesion_distance").forGetter(GravityFrame::adhesionDistance),
            Codec.LONG.fieldOf("revision").forGetter(GravityFrame::revision))
            .apply(instance, GravityFrame::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GravityFrame> STREAM_CODEC = StreamCodec
            .ofMember(GravityFrame::encode, GravityFrame::new);

    public GravityFrame {
        mode = Objects.requireNonNull(mode);
        down = Objects.requireNonNull(down);
        sourceId = Objects.requireNonNull(sourceId);
        strength = clampStrength(strength);
        transitionTicks = Math.max(0, transitionTicks);
        adhesionDistance = finiteNonnegative(adhesionDistance);

        if (mode == GravityMode.NORMAL && (down != Direction.DOWN || Double.compare(strength, 1.0) != 0)) {
            throw new IllegalArgumentException("Normal gravity requires down direction and unit strength");
        }
        if (mode == GravityMode.FREE_DRIFT && (down != Direction.DOWN || Double.compare(strength, 0.0) != 0)) {
            throw new IllegalArgumentException("Free drift requires down direction and zero strength");
        }
        if (mode == GravityMode.DIRECTED && strength <= 0.0) {
            throw new IllegalArgumentException("Directed gravity requires positive strength");
        }
    }

    private GravityFrame(RegistryFriendlyByteBuf buffer) {
        this(
                buffer.readEnum(GravityMode.class),
                buffer.readEnum(Direction.class),
                buffer.readDouble(),
                buffer.readResourceLocation(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readLong());
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(mode);
        buffer.writeEnum(down);
        buffer.writeDouble(strength);
        buffer.writeResourceLocation(sourceId);
        buffer.writeVarInt(priority);
        buffer.writeVarInt(transitionTicks);
        buffer.writeDouble(adhesionDistance);
        buffer.writeLong(revision);
    }

    public static GravityFrame normal() {
        return NORMAL;
    }

    public GravityFrame withRevision(long revision) {
        return new GravityFrame(mode, down, strength, sourceId, priority, transitionTicks, adhesionDistance, revision);
    }

    public boolean sameTarget(GravityFrame other) {
        return other != null && mode == other.mode && down == other.down &&
                Double.compare(strength, other.strength) == 0 &&
                sourceId.equals(other.sourceId) && priority == other.priority &&
                transitionTicks == other.transitionTicks &&
                Double.compare(adhesionDistance, other.adhesionDistance) == 0;
    }

    private static double clampStrength(double strength) {
        if (Double.isNaN(strength)) return 0.0;
        return Math.clamp(strength, 0.0, MAX_STRENGTH);
    }

    private static double finiteNonnegative(double value) {
        if (!Double.isFinite(value)) return 0.0;
        return Math.max(0.0, value);
    }
}
