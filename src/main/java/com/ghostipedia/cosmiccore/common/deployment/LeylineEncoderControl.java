package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LeylineCompressorMachine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.value.sync.SyncHandler;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class LeylineEncoderControl extends SyncHandler<LeylineEncoderControl> {

    private final LeylineCompressorMachine machine;
    public BiConsumer<Boolean, Boolean> result = (valid, encoded) -> {};
    public Consumer<ItemStack> loadDesign = stack -> {};
    public BooleanSupplier canWritePattern = () -> false;
    public List<Component> errors = List.of();
    private long lastRequest = Long.MIN_VALUE;

    public LeylineEncoderControl(LeylineCompressorMachine machine) {
        this.machine = machine;
    }

    public void submit(LeylinePrefab prefab, boolean encode) {
        syncToServer(encode ? 1 : 0, buf -> LeylinePrefab.writePayload(buf, prefab.save()));
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        if (!(machine.getLevel() instanceof ServerLevel level) || machine.isRemoved() ||
                getSyncManager().getPlayer().distanceToSqr(machine.getBlockPos().getCenter()) > 64)
            return;
        long tick = level.getGameTime();
        if (lastRequest != Long.MIN_VALUE && tick - lastRequest < 5) return;
        lastRequest = tick;
        if (id != 0 && id != 1) return;
        boolean valid = false;
        boolean encoded = false;
        errors = List.of();
        try {
            var prefab = LeylinePrefab.load(LeylinePrefab.readPayload(buf));
            var validation = LeylinePrefabValidation.check(level, prefab);
            valid = validation.valid();
            errors = validation.errors();
            if (valid && id == 1) encoded = machine.encode(prefab);
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.debug("Rejected leyline prefab", exception);
        }
        boolean accepted = valid;
        boolean produced = encoded;
        syncToClient(0, b -> {
            b.writeBoolean(accepted);
            b.writeBoolean(produced);
            b.writeVarInt(errors.size());
            for (var component : errors) ComponentSerialization.STREAM_CODEC.encode(b, component);
        });
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == 0) {
            boolean valid = buf.readBoolean();
            boolean encoded = buf.readBoolean();
            int count = buf.readVarInt();
            if (count < 0 || count > 80) throw new IllegalArgumentException("Too many validation errors");
            var received = new java.util.ArrayList<Component>();
            for (int i = 0; i < count; i++) received.add(ComponentSerialization.STREAM_CODEC.decode(buf));
            errors = List.copyOf(received);
            result.accept(valid, encoded);
        }
    }
}
