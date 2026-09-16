package com.ghostipedia.cosmiccore.common.machine.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record FoundryCampusSnapshot(GlobalPos core, FoundryTier tier, FoundryRenderAnchor sourceAnchor,
                                    List<Furnace> furnaces) {

    public FoundryCampusSnapshot {
        furnaces = List.copyOf(furnaces);
    }

    public void write(FriendlyByteBuf buffer) {
        writeGlobalPos(buffer, core);
        buffer.writeVarInt(tier.level());
        sourceAnchor.write(buffer);
        buffer.writeCollection(furnaces, (target, furnace) -> furnace.write(target));
    }

    public static FoundryCampusSnapshot read(FriendlyByteBuf buffer) {
        GlobalPos core = readGlobalPos(buffer);
        FoundryTier tier = FoundryTier.fromLevel(buffer.readVarInt());
        FoundryRenderAnchor sourceAnchor = FoundryRenderAnchor.read(buffer);
        List<Furnace> furnaces = buffer.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 16), Furnace::read);
        return new FoundryCampusSnapshot(core, tier, sourceAnchor, furnaces);
    }

    private static void writeGlobalPos(FriendlyByteBuf buffer, GlobalPos position) {
        buffer.writeResourceLocation(position.dimension().location());
        buffer.writeBlockPos(position.pos());
    }

    private static GlobalPos readGlobalPos(FriendlyByteBuf buffer) {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
        BlockPos position = buffer.readBlockPos();
        return GlobalPos.of(dimension, position);
    }

    public record Furnace(GlobalPos position, ResourceLocation type, FoundryRenderAnchor receivingAnchor,
                          Selection selection, Activity activity) {

        void write(FriendlyByteBuf buffer) {
            writeGlobalPos(buffer, position);
            buffer.writeResourceLocation(type);
            receivingAnchor.write(buffer);
            buffer.writeEnum(selection);
            buffer.writeEnum(activity);
        }

        static Furnace read(FriendlyByteBuf buffer) {
            return new Furnace(readGlobalPos(buffer), buffer.readResourceLocation(), FoundryRenderAnchor.read(buffer),
                    buffer.readEnum(Selection.class), buffer.readEnum(Activity.class));
        }
    }

    public enum Selection {
        ACTIVE,
        DORMANT
    }

    public enum Activity {
        UNAVAILABLE,
        IDLE,
        WORKING
    }
}
