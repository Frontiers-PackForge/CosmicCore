package com.ghostipedia.cosmiccore.common.transmission.geometry;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class TwinWireSagCurve {

    private static final double EPSILON = 1.0E-9;

    private final Vec3 start;
    private final Vec3 end;
    private final Vec3 lateral;
    private final Vec3 sagDirection;
    private final double halfSeparation;
    private final double sagDepth;
    private final AABB bounds;

    private TwinWireSagCurve(Vec3 start, Vec3 end, AttachmentBasis basis, double halfSeparation, double sagDepth) {
        if (!Double.isFinite(halfSeparation) || halfSeparation < 0.0 || !Double.isFinite(sagDepth) ||
                sagDepth < 0.0) {
            throw new IllegalArgumentException("Wire separation and sag must be finite and non-negative");
        }
        Vec3 span = end.subtract(start);
        if (span.lengthSqr() < EPSILON) {
            throw new IllegalArgumentException("Wire endpoints must be distinct");
        }
        Vec3 direction = span.normalize();
        // Canon wire handling so we don't freaking twist wire renders in dumb ways lol
        Vec3 canonicalDirection = compare(start, end) <= 0 ? direction : direction.scale(-1.0);
        Vec3 projectedHint = basis.up().cross(canonicalDirection);
        if (projectedHint.lengthSqr() >= EPSILON && projectedHint.dot(basis.lateralHint()) < 0.0) {
            projectedHint = projectedHint.scale(-1.0);
        }
        if (projectedHint.lengthSqr() < EPSILON) {
            projectedHint = basis.lateralHint()
                    .subtract(canonicalDirection.scale(basis.lateralHint().dot(canonicalDirection)));
        }
        if (projectedHint.lengthSqr() < EPSILON) {
            Vec3 axis = leastAlignedAxis(canonicalDirection);
            projectedHint = axis.subtract(canonicalDirection.scale(axis.dot(canonicalDirection)));
        }
        this.start = start;
        this.end = end;
        this.lateral = projectedHint.normalize();
        Vec3 projectedUp = basis.up().subtract(direction.scale(basis.up().dot(direction)));
        this.sagDirection = projectedUp.lengthSqr() < EPSILON ? direction.cross(lateral).normalize().scale(-1.0) :
                projectedUp.normalize().scale(-1.0);
        this.halfSeparation = halfSeparation;
        this.sagDepth = sagDepth;
        this.bounds = calculateBounds();
    }

    public static TwinWireSagCurve of(Vec3 start, Vec3 end, AttachmentBasis basis, double halfSeparation,
                                      double sagDepth) {
        return new TwinWireSagCurve(start, end, basis, halfSeparation, sagDepth);
    }

    public static TwinWireSagCurve of(Vec3 start, Vec3 end, double halfSeparation, double sagDepth) {
        return of(start, end, AttachmentBasis.worldAligned(), halfSeparation, sagDepth);
    }

    public Vec3 start() {
        return start;
    }

    public Vec3 end() {
        return end;
    }

    public Vec3 lateral() {
        return lateral;
    }

    public Vec3 sagDirection() {
        return sagDirection;
    }

    public double halfSeparation() {
        return halfSeparation;
    }

    public double sagDepth() {
        return sagDepth;
    }

    public TwinWireSample sample(double t) {
        if (!Double.isFinite(t)) throw new IllegalArgumentException("Sample parameter must be finite");
        double clamped = Math.max(0.0, Math.min(1.0, t));
        Vec3 center = start.lerp(end, clamped)
                .add(sagDirection.scale(4.0 * sagDepth * clamped * (1.0 - clamped)));
        Vec3 offset = lateral.scale(halfSeparation);
        return new TwinWireSample(center.add(offset), center.subtract(offset));
    }

    public AABB bounds() {
        return bounds;
    }

    public List<ChunkPos> coveredChunks() {
        int minX = floorChunk(bounds.minX);
        int maxX = floorChunk(bounds.maxX);
        int minZ = floorChunk(bounds.minZ);
        int maxZ = floorChunk(bounds.maxZ);
        List<ChunkPos> result = new ArrayList<>((maxX - minX + 1) * (maxZ - minZ + 1));
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) result.add(new ChunkPos(x, z));
        }
        return List.copyOf(result);
    }

    public List<SectionPos> coveredSections(int minBuildHeight, int maxBuildHeight) {
        int minX = floorSection(bounds.minX);
        int maxX = floorSection(bounds.maxX);
        int minY = Math.max(floorSection(bounds.minY), SectionPos.blockToSectionCoord(minBuildHeight));
        int maxY = Math.min(floorSection(bounds.maxY), SectionPos.blockToSectionCoord(maxBuildHeight - 1));
        int minZ = floorSection(bounds.minZ);
        int maxZ = floorSection(bounds.maxZ);
        if (minY > maxY) return List.of();
        List<SectionPos> result = new ArrayList<>((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1));
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) result.add(SectionPos.of(x, y, z));
            }
        }
        return List.copyOf(result);
    }

    private AABB calculateBounds() {
        List<Vec3> points = new ArrayList<>();
        for (double wire : new double[] { -halfSeparation, halfSeparation }) {
            Vec3 offset = lateral.scale(wire);
            for (double t : extremaParameters()) points.add(point(t).add(offset));
        }
        double minX = points.stream().mapToDouble(Vec3::x).min().orElseThrow();
        double minY = points.stream().mapToDouble(Vec3::y).min().orElseThrow();
        double minZ = points.stream().mapToDouble(Vec3::z).min().orElseThrow();
        double maxX = points.stream().mapToDouble(Vec3::x).max().orElseThrow();
        double maxY = points.stream().mapToDouble(Vec3::y).max().orElseThrow();
        double maxZ = points.stream().mapToDouble(Vec3::z).max().orElseThrow();
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(EPSILON);
    }

    private double[] extremaParameters() {
        double[] values = new double[] { 0.0, 1.0, 0.5 };
        Vec3 linear = end.subtract(start);
        for (int axis = 0; axis < 3; axis++) {
            double a = -4.0 * sagDepth * component(sagDirection, axis);
            double b = component(linear, axis) + 4.0 * sagDepth * component(sagDirection, axis);
            if (Math.abs(a) > EPSILON) {
                double t = -b / (2.0 * a);
                if (t > 0.0 && t < 1.0) values = append(values, t);
            }
        }
        return values;
    }

    private Vec3 point(double t) {
        return start.lerp(end, t).add(sagDirection.scale(4.0 * sagDepth * t * (1.0 - t)));
    }

    private static double component(Vec3 vector, int axis) {
        return axis == 0 ? vector.x : axis == 1 ? vector.y : vector.z;
    }

    private static double[] append(double[] values, double value) {
        double[] result = java.util.Arrays.copyOf(values, values.length + 1);
        result[result.length - 1] = value;
        return result;
    }

    private static Vec3 leastAlignedAxis(Vec3 direction) {
        double x = Math.abs(direction.x), y = Math.abs(direction.y), z = Math.abs(direction.z);
        if (x <= y && x <= z) return new Vec3(1.0, 0.0, 0.0);
        if (y <= z) return new Vec3(0.0, 1.0, 0.0);
        return new Vec3(0.0, 0.0, 1.0);
    }

    private static int compare(Vec3 first, Vec3 second) {
        int x = Double.compare(first.x, second.x);
        if (x != 0) return x;
        int y = Double.compare(first.y, second.y);
        return y != 0 ? y : Double.compare(first.z, second.z);
    }

    private static int floorChunk(double coordinate) {
        return Math.floorDiv((int) Math.floor(coordinate), 16);
    }

    private static int floorSection(double coordinate) {
        return Math.floorDiv((int) Math.floor(coordinate), 16);
    }
}
