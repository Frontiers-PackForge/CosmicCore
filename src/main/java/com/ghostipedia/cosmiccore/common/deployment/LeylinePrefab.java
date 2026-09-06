package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class LeylinePrefab {

    private final ResourceLocation machine;
    private final String name;
    private final ResourceLocation icon;
    private final List<LeylineBlockPlacement> blocks;
    private final List<Integer> repeats;
    private final List<Integer> dimensions;
    private final UUID id;
    private final CompoundTag serialized;
    private final byte[] compressed;

    public static final int MAX_BLOCKS = 32768;
    public static final int MAX_PAYLOAD_BYTES = 28000;
    public static final String DATA = "leyline_prefab";

    public LeylinePrefab(ResourceLocation machine, String name, ResourceLocation icon,
                         List<LeylineBlockPlacement> blocks,
                         List<Integer> repeats, List<Integer> dimensions) {
        if (!(GTRegistries.MACHINES.get(machine) instanceof MultiblockMachineDefinition))
            throw new IllegalArgumentException("Unknown multiblock");
        name = name.strip();
        if (name.isEmpty() || name.length() > 80 || blocks.isEmpty() || blocks.size() > MAX_BLOCKS)
            throw new IllegalArgumentException("Invalid prefab size or name");
        blocks = blocks.stream().filter(p -> !p.state().isAir())
                .sorted(Comparator.comparingLong(p -> p.relativeOffset().asLong())).toList();
        Set<BlockPos> seen = new HashSet<>();
        for (var block : blocks) {
            var pos = block.relativeOffset();
            if (!seen.add(pos) || Math.abs(pos.getX()) > 256 || Math.abs(pos.getY()) > 256 ||
                    Math.abs(pos.getZ()) > 256 ||
                    new ItemStack(block.state().getBlock()).isEmpty())
                throw new IllegalArgumentException("Invalid prefab block");
        }
        var definition = GTRegistries.MACHINES.get(machine);
        if (blocks.stream().filter(p -> p.state().is(definition.getBlock())).count() != 1 ||
                blocks.stream().noneMatch(
                        p -> p.relativeOffset().equals(BlockPos.ZERO) && p.state().is(definition.getBlock())))
            throw new IllegalArgumentException("Invalid controller anchor");
        this.machine = machine;
        this.name = name;
        this.icon = icon;
        this.blocks = blocks;
        this.repeats = List.copyOf(repeats);
        this.dimensions = List.copyOf(dimensions);
        if (repeats.size() > 256 || dimensions.size() > 16 ||
                repeats.stream().anyMatch(i -> i < 0 || i > 256) || dimensions.stream().anyMatch(i -> i < 0 || i > 256))
            throw new IllegalArgumentException("Invalid editor dimensions");
        this.serialized = serialize();
        this.compressed = compress(serialized);
        this.id = UUID.nameUUIDFromBytes(serialized.toString().getBytes(StandardCharsets.UTF_8));
    }

    public ResourceLocation machine() {
        return machine;
    }

    public String name() {
        return name;
    }

    public ResourceLocation icon() {
        return icon;
    }

    public List<LeylineBlockPlacement> blocks() {
        return blocks;
    }

    public List<Integer> repeats() {
        return repeats;
    }

    public List<Integer> dimensions() {
        return dimensions;
    }

    public UUID id() {
        return id;
    }

    public ResourceLocation blueprintId() {
        return CosmicCore.id("prefab/" + id());
    }

    public LeylineDeploymentBlueprint blueprint(Direction facing) {
        Rotation rotation = switch (facing) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.CLOCKWISE_90;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        return new LeylineDeploymentBlueprint(blueprintId(), machine, BlockPos.ZERO, blocks.stream()
                .map(p -> new LeylineBlockPlacement(p.relativeOffset().rotate(rotation), p.state().rotate(rotation)))
                .toList());
    }

    public Map<Item, Integer> ingredients() {
        Map<Item, Integer> result = new LinkedHashMap<>();
        blocks.forEach(p -> result.merge(p.state().getBlock().asItem(), 1, Integer::sum));
        return Collections.unmodifiableMap(result);
    }

    public CompoundTag save() {
        return serialized.copy();
    }

    public static void writePayload(FriendlyByteBuf buffer, CompoundTag tag) {
        buffer.writeByteArray(compress(tag));
    }

    private static byte[] compress(CompoundTag tag) {
        try {
            var output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, output);
            byte[] bytes = output.toByteArray();
            if (bytes.length > MAX_PAYLOAD_BYTES)
                throw new IllegalArgumentException("Prefab payload exceeds transport limit");
            return bytes;
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public static CompoundTag readPayload(FriendlyByteBuf buffer) {
        return decompress(buffer.readByteArray(MAX_PAYLOAD_BYTES));
    }

    private static CompoundTag decompress(byte[] bytes) {
        if (bytes.length > MAX_PAYLOAD_BYTES)
            throw new IllegalArgumentException("Prefab payload exceeds transport limit");
        try {
            return NbtIo.readCompressed(new ByteArrayInputStream(bytes),
                    NbtAccounter.create(8 * 1024 * 1024));
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("machine", machine.toString());
        tag.putString("name", name);
        tag.putString("icon", icon.toString());
        tag.putIntArray("repeats", repeats);
        tag.putIntArray("dimensions", dimensions);
        List<BlockState> palette = new ArrayList<>();
        ListTag states = new ListTag();
        long[] positions = new long[blocks.size()];
        int[] indexes = new int[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            var block = blocks.get(i);
            int index = palette.indexOf(block.state());
            if (index < 0) {
                index = palette.size();
                palette.add(block.state());
                states.add(NbtUtils.writeBlockState(block.state()));
            }
            positions[i] = block.relativeOffset().asLong();
            indexes[i] = index;
        }
        tag.put("palette", states);
        tag.putLongArray("positions", positions);
        tag.putIntArray("states", indexes);
        return tag;
    }

    public static LeylinePrefab load(CompoundTag tag) {
        long[] positions = tag.getLongArray("positions");
        int[] indexes = tag.getIntArray("states");
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        if (positions.length == 0 || positions.length > MAX_BLOCKS || indexes.length != positions.length ||
                palette.size() > MAX_BLOCKS)
            throw new IllegalArgumentException("Invalid prefab payload");
        List<BlockState> states = new ArrayList<>();
        for (int i = 0; i < palette.size(); i++)
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(i)));
        List<LeylineBlockPlacement> blocks = new ArrayList<>();
        for (int i = 0; i < positions.length; i++)
            blocks.add(new LeylineBlockPlacement(BlockPos.of(positions[i]), states.get(indexes[i])));
        return new LeylinePrefab(ResourceLocation.parse(tag.getString("machine")), tag.getString("name"),
                ResourceLocation.parse(tag.getString("icon")), blocks,
                Arrays.stream(tag.getIntArray("repeats")).boxed().toList(),
                Arrays.stream(tag.getIntArray("dimensions")).boxed().toList());
    }

    public static CompoundTag descriptor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(DATA);
    }

    public static UUID reference(ItemStack stack) {
        var descriptor = descriptor(stack);
        return descriptor.hasUUID("id") ? descriptor.getUUID("id") : null;
    }

    public static Component machineName(ItemStack stack) {
        var machine = ResourceLocation.tryParse(descriptor(stack).getString("machine"));
        var definition = machine == null ? null : GTRegistries.MACHINES.get(machine);
        if (stack.is(CosmicItems.POWER_TOWER_DEPLOYMENT_PACKAGE.get()))
            definition = com.ghostipedia.cosmiccore.common.data.CosmicMachines.POWER_TOWER;
        return definition == null ? Component.translatable("cosmiccore.leyline.unknown_machine") :
                new ItemStack(definition.getItem()).getHoverName();
    }

    public static LeylinePrefab fromStack(ItemStack stack, Level level) {
        var descriptor = descriptor(stack);
        if (descriptor.isEmpty()) return null;
        if (level instanceof ServerLevel serverLevel) {
            var library = LeylineFabricationLibrary.get(serverLevel);
            if (descriptor.contains("payload") || descriptor.contains("positions")) {
                var prefab = load(
                        descriptor.contains("payload") ? decompress(descriptor.getByteArray("payload")) : descriptor);
                library.add(prefab);
                return prefab;
            }
            return descriptor.hasUUID("id") ? library.find(descriptor.getUUID("id")) : null;
        }
        if (level != null && level.isClientSide() && descriptor.hasUUID("id"))
            return com.ghostipedia.cosmiccore.client.renderer.deployment.LeylinePrefabClient
                    .get(descriptor.getUUID("id"));
        return null;
    }

    public static boolean migrate(ItemStack stack, ServerLevel level) {
        var descriptor = descriptor(stack);
        if (!descriptor.contains("payload") && !descriptor.contains("positions")) return false;
        var prefab = fromStack(stack, level);
        if (prefab == null) return false;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(DATA, prefab.referenceTag()));
        return true;
    }

    public CompoundTag referenceTag() {
        CompoundTag descriptor = new CompoundTag();
        descriptor.putUUID("id", id);
        descriptor.putString("machine", machine.toString());
        descriptor.putString("icon", icon.toString());
        descriptor.putInt("blocks", blocks.size());
        return descriptor;
    }

    public byte[] payload() {
        return compressed.clone();
    }

    public static LeylinePrefab fromPayload(byte[] bytes) {
        return load(decompress(bytes));
    }

    public ItemStack stack(boolean pattern) {
        var stack = new ItemStack(pattern ? CosmicItems.LEYLINE_PATTERN.get() : CosmicItems.LEYLINE_PACKAGE.get());
        CompoundTag tag = new CompoundTag();
        tag.put(DATA, referenceTag());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }
}
