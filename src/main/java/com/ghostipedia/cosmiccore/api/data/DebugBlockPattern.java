package com.ghostipedia.cosmiccore.api.data;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DebugBlockPattern {

    private static final String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&*+-./:;<=>?@[]^_";

    private WorldDirections directions;
    public String[][] pattern;
    public int[][] aisleRepetitions;
    public Map<Character, Set<String>> symbolMap;
    public Map<Character, ResourceLocation> charToBlockMap;

    public DebugBlockPattern() {
        symbolMap = new HashMap<>();
        charToBlockMap = new LinkedHashMap<>();
        directions = new WorldDirections(
                Direction.EAST,
                Direction.UP,
                Direction.SOUTH);
    }

    public DebugBlockPattern(
                             Level world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this();
        pattern = new String[1 + maxX - minX][1 + maxY - minY];
        aisleRepetitions = new int[pattern.length][2];
        for (int[] aisleRepetition : aisleRepetitions) {
            aisleRepetition[0] = 1;
            aisleRepetition[1] = 1;
        }

        Map<BlockState, Character> map = new HashMap<>();
        map.put(Blocks.AIR.defaultBlockState(), ' ');
        charToBlockMap.put(' ', BuiltInRegistries.BLOCK.getKey(Blocks.AIR));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                StringBuilder builder = new StringBuilder();
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (!map.containsKey(state)) {
                        char c = symbolAt(charToBlockMap.size() - 1);
                        map.put(state, c);
                        String name = String.valueOf(c);
                        symbolMap.computeIfAbsent(c, key -> new HashSet<>()).add(name); // any
                        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                        charToBlockMap.put(c, blockKey);
                    }
                    builder.append(map.get(state));
                }
                pattern[x - minX][y - minY] = builder.toString();
            }
        }
    }

    public static PatternDirections directionsFor(Direction facing) {
        return switch (facing) {
            case WEST -> new PatternDirections(
                    RelativeDirection.BACK,
                    RelativeDirection.UP,
                    RelativeDirection.LEFT);
            case EAST -> new PatternDirections(
                    RelativeDirection.FRONT,
                    RelativeDirection.UP,
                    RelativeDirection.RIGHT);
            case NORTH -> new PatternDirections(
                    RelativeDirection.RIGHT,
                    RelativeDirection.UP,
                    RelativeDirection.BACK);
            case SOUTH -> new PatternDirections(
                    RelativeDirection.LEFT,
                    RelativeDirection.UP,
                    RelativeDirection.FRONT);
            case DOWN -> new PatternDirections(
                    RelativeDirection.UP,
                    RelativeDirection.BACK,
                    RelativeDirection.RIGHT);
            case UP -> new PatternDirections(
                    RelativeDirection.UP,
                    RelativeDirection.FRONT,
                    RelativeDirection.LEFT);
        };
    }

    public static WorldDirections worldDirectionsFor(Direction facing) {
        PatternDirections patternDirections = directionsFor(facing);
        return new WorldDirections(
                patternDirections.slice().getDefaultFacing().getOpposite(),
                patternDirections.string().getDefaultFacing(),
                patternDirections.character().getDefaultFacing());
    }

    public static StructureOrientation orientationFor(Direction facing) {
        return new StructureOrientation(directionsFor(facing), worldDirectionsFor(facing));
    }

    public static ExportOrientation exportOrientationFor(Direction facing) {
        return exportOrientationFor(orientationFor(facing));
    }

    public static ExportOrientation exportOrientationFor(StructureOrientation orientation) {
        PatternDirections patternDirections = orientation.pattern();
        WorldDirections worldDirections = orientation.world();
        for (Direction front : Direction.values()) {
            for (Direction up : Direction.values()) {
                if (front.getAxis() == up.getAxis()) continue;
                for (int flipState = 0; flipState < 2; flipState++) {
                    boolean flipped = flipState == 1;
                    if (patternDirections.slice().getRelativeFacing(front, up, flipped) == worldDirections.slice() &&
                            patternDirections.string().getRelativeFacing(front, up, flipped) ==
                                    worldDirections.string() &&
                            patternDirections.character().getRelativeFacing(front, up, flipped) ==
                                    worldDirections.character()) {
                        return new ExportOrientation(front, up, flipped);
                    }
                }
            }
        }
        throw new IllegalStateException("Unable to resolve the structure writer export orientation");
    }

    public void orient(WorldDirections target) {
        validateDirections(target);
        char[][][] newPattern = new char[dimensionFor(target.slice())][dimensionFor(target.string())][dimensionFor(
                target.character())];
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[0].length; j++) {
                for (int k = 0; k < pattern[0][0].length(); k++) {
                    char c = pattern[i][j].charAt(k);
                    int slice = coordinateFor(target.slice(), i, j, k);
                    int string = coordinateFor(target.string(), i, j, k);
                    int character = coordinateFor(target.character(), i, j, k);
                    newPattern[slice][string][character] = c;
                }
            }
        }

        pattern = new String[newPattern.length][newPattern[0].length];
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[0].length; j++) {
                StringBuilder builder = new StringBuilder();
                for (char c : newPattern[i][j]) {
                    builder.append(c);
                }
                pattern[i][j] = builder.toString();
            }
        }

        aisleRepetitions = new int[pattern.length][2];
        for (int[] aisleRepetition : aisleRepetitions) {
            aisleRepetition[0] = 1;
            aisleRepetition[1] = 1;
        }

        directions = target;
    }

    private int dimensionFor(Direction target) {
        if (isSameAxis(directions.slice(), target)) return pattern.length;
        if (isSameAxis(directions.string(), target)) return pattern[0].length;
        return pattern[0][0].length();
    }

    private int coordinateFor(Direction target, int slice, int string, int character) {
        if (isSameAxis(directions.slice(), target)) {
            return directions.slice() == target ? slice : pattern.length - slice - 1;
        }
        if (isSameAxis(directions.string(), target)) {
            return directions.string() == target ? string : pattern[0].length - string - 1;
        }
        return directions.character() == target ? character : pattern[0][0].length() - character - 1;
    }

    private static void validateDirections(WorldDirections directions) {
        if (isSameAxis(directions.slice(), directions.string()) ||
                isSameAxis(directions.string(), directions.character()) ||
                isSameAxis(directions.character(), directions.slice())) {
            throw new IllegalArgumentException("The three pattern directions must use distinct axes");
        }
    }

    private static boolean isSameAxis(Direction first, Direction second) {
        return first.getAxis() == second.getAxis();
    }

    private static char symbolAt(int index) {
        if (index >= SYMBOLS.length()) {
            throw new IllegalStateException("Structure contains too many distinct block states");
        }
        return SYMBOLS.charAt(index);
    }

    public DebugBlockPattern copy() {
        DebugBlockPattern newPattern = new DebugBlockPattern();
        newPattern.directions = directions;

        newPattern.pattern = new String[pattern.length][pattern[0].length];
        for (int i = 0; i < pattern.length; i++) {
            System.arraycopy(pattern[i], 0, newPattern.pattern[i], 0, pattern[i].length);
        }

        newPattern.aisleRepetitions = new int[aisleRepetitions.length][2];
        for (int i = 0; i < aisleRepetitions.length; i++) {
            System.arraycopy(
                    aisleRepetitions[i], 0, newPattern.aisleRepetitions[i], 0, aisleRepetitions[i].length);
        }

        symbolMap.forEach((k, v) -> newPattern.symbolMap.put(k, new HashSet<>(v)));
        newPattern.charToBlockMap.putAll(this.charToBlockMap);

        return newPattern;
    }

    public record PatternDirections(
                                    RelativeDirection slice,
                                    RelativeDirection string,
                                    RelativeDirection character) {}

    public record WorldDirections(
                                  Direction slice,
                                  Direction string,
                                  Direction character) {}

    public record StructureOrientation(
                                       PatternDirections pattern,
                                       WorldDirections world) {

        public StructureOrientation rotate(Direction.Axis axis) {
            return new StructureOrientation(
                    pattern,
                    new WorldDirections(
                            world.slice().getClockWise(axis),
                            world.string().getClockWise(axis),
                            world.character().getClockWise(axis)));
        }
    }

    public record ExportOrientation(
                                    Direction front,
                                    Direction up,
                                    boolean flipped) {}
}
