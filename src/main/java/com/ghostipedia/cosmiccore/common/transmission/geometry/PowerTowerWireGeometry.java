package com.ghostipedia.cosmiccore.common.transmission.geometry;

import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class PowerTowerWireGeometry {

    public static final double RADIUS = 0.0625;
    public static final double SEGMENT_LENGTH = 0.25;
    private List<Segment> segments;
    private final List<TwinWireSagCurve> curves;
    private final AABB bounds;
    private final Map<Long, List<Segment>> segmentsByChunk = new HashMap<>();

    public PowerTowerWireGeometry(List<Vec3> starts, List<Vec3> ends) {
        if (starts.size() != ends.size() || starts.isEmpty() || starts.size() > 4)
            throw new IllegalArgumentException("Invalid wire endpoints");
        List<TwinWireSagCurve> result = new ArrayList<>();
        AABB total = null;
        for (int wire = 0; wire < starts.size(); wire++) {
            Vec3 start = starts.get(wire), end = ends.get(wire);
            double distance = start.distanceTo(end);
            double sag = Math.clamp(distance * 0.06, 1.0, 4.0);
            var curve = TwinWireSagCurve.of(start, end, 0.0, sag);
            result.add(curve);
            AABB curveBounds = curve.bounds().inflate(RADIUS);
            total = total == null ? curveBounds : total.minmax(curveBounds);
        }
        curves = List.copyOf(result);
        bounds = total;
    }

    private void prepareSegments() {
        if (segments != null) return;
        List<Segment> result = new ArrayList<>();
        for (int wire = 0; wire < curves.size(); wire++) {
            var curve = curves.get(wire);
            Vec3 start = curve.start();
            int count = (int) Math.ceil((start.distanceTo(curve.end()) + 8 * curve.sagDepth()) / SEGMENT_LENGTH);
            Vec3 previous = start;
            for (int index = 1; index <= count; index++) {
                Vec3 next = curve.sample((double) index / count).positiveLateral();
                Segment segment = new Segment(previous, next, new AABB(previous, next).inflate(RADIUS), wire,
                        (double) (index - 1) / count, (double) index / count);
                result.add(segment);
                previous = next;
            }
        }
        segments = List.copyOf(result);
        for (Segment segment : segments) {
            AABB box = segment.bounds();
            for (int x = (int) Math.floor(box.minX) >> 4; x <= (int) Math.floor(box.maxX) >> 4; x++) {
                for (int z = (int) Math.floor(box.minZ) >> 4; z <= (int) Math.floor(box.maxZ) >> 4; z++) {
                    segmentsByChunk.computeIfAbsent(ChunkPos.asLong(x, z), ignored -> new ArrayList<>()).add(segment);
                }
            }
        }
    }

    public static Endpoints endpoints(PowerTowerNode first, PowerTowerNode second) {
        if (first.attachmentPoints().isEmpty() || second.attachmentPoints().isEmpty()) {
            var legacy = TwinWireSagCurve.of(first.wireAttachmentCenter(), second.wireAttachmentCenter(), 0.65, 0);
            var a = legacy.sample(0);
            var b = legacy.sample(1);
            return new Endpoints(List.of(a.positiveLateral(), a.negativeLateral()),
                    List.of(b.positiveLateral(), b.negativeLateral()));
        }
        List<Vec3> starts = first.attachmentPoints();
        List<Vec3> ends = new ArrayList<>(second.attachmentPoints());
        for (int pair = 0; pair < starts.size(); pair += 2) {
            Vec3 a = starts.get(pair), b = starts.get(pair + 1);
            Vec3 c = ends.get(pair), d = ends.get(pair + 1);
            if (a.distanceToSqr(d) + b.distanceToSqr(c) < a.distanceToSqr(c) + b.distanceToSqr(d)) {
                ends.set(pair, d);
                ends.set(pair + 1, c);
            }
        }
        return new Endpoints(starts, List.copyOf(ends));
    }

    public List<Segment> segments() {
        prepareSegments();
        return segments;
    }

    public AABB bounds() {
        return bounds;
    }

    public List<Segment> query(AABB box) {
        if (!bounds.intersects(box)) return List.of();
        prepareSegments();
        var result = new HashSet<Segment>();
        for (int x = (int) Math.floor(box.minX) >> 4; x <= (int) Math.floor(box.maxX) >> 4; x++) {
            for (int z = (int) Math.floor(box.minZ) >> 4; z <= (int) Math.floor(box.maxZ) >> 4; z++) {
                for (Segment segment : segmentsByChunk.getOrDefault(ChunkPos.asLong(x, z), List.of())) {
                    if (segment.bounds().intersects(box)) result.add(segment);
                }
            }
        }
        return List.copyOf(result);
    }

    public record Endpoints(List<Vec3> starts, List<Vec3> ends) {

        public PowerTowerWireGeometry geometry() {
            return new PowerTowerWireGeometry(starts, ends);
        }
    }

    public record Segment(Vec3 start, Vec3 end, AABB bounds, int wire, double from, double to) {}

    public Vec3 point(int wire, double progress) {
        return curves.get(wire).sample(progress).positiveLateral();
    }

    public int wireCount() {
        return curves.size();
    }

    public Vec3 tangent(int wire, double progress) {
        double low = Math.max(0, progress - 0.001), high = Math.min(1, progress + 0.001);
        return point(wire, high).subtract(point(wire, low)).scale(1.0 / (high - low));
    }

    public double advance(int wire, double progress, double distance) {
        return Math.clamp(progress + distance / tangent(wire, progress).length(), 0, 1);
    }

    public double closestProgress(int wire, Vec3 position) {
        double best = 0;
        double nearest = Double.MAX_VALUE;
        for (int i = 0; i <= 32; i++) {
            double t = i / 32.0;
            double distance = point(wire, t).distanceToSqr(position);
            if (distance < nearest) {
                nearest = distance;
                best = t;
            }
        }
        double low = Math.max(0, best - 1.0 / 32), high = Math.min(1, best + 1.0 / 32);
        for (int i = 0; i < 16; i++) {
            double a = low + (high - low) / 3, b = high - (high - low) / 3;
            if (point(wire, a).distanceToSqr(position) < point(wire, b).distanceToSqr(position)) high = b;
            else low = a;
        }
        return (low + high) / 2;
    }
}
