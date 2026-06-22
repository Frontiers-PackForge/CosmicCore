package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.utils.StringUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Network packets for Cluster of Perpetuity scar removal.
 */
public class ScarSelectionPackets {

    public static void sendOpenScarSelection(ServerPlayer player, Set<ResourceLocation> scars) {
        CCoreNetwork.sendToPlayer(player, new OpenScarSelectionPacket(scars));
    }

    public static void sendScarRemoval(ResourceLocation scarId) {
        CCoreNetwork.sendToServer(new ScarRemovalPacket(scarId));
    }

    public static class OpenScarSelectionPacket implements CustomPacketPayload {

        public static final Type<OpenScarSelectionPacket> TYPE = new Type<>(CosmicCore.id("scar_open_selection"));
        public static final StreamCodec<FriendlyByteBuf, OpenScarSelectionPacket> CODEC = StreamCodec
                .ofMember(OpenScarSelectionPacket::encode, OpenScarSelectionPacket::new);

        private final Set<ResourceLocation> scars;

        public OpenScarSelectionPacket(Set<ResourceLocation> scars) {
            this.scars = scars != null ? scars : Set.of();
        }

        public OpenScarSelectionPacket(FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            Set<ResourceLocation> readScars = new HashSet<>();
            for (int i = 0; i < count; i++) {
                readScars.add(buf.readResourceLocation());
            }
            this.scars = readScars;
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(scars.size());
            for (ResourceLocation id : scars) {
                buf.writeResourceLocation(id);
            }
        }

        public void execute(IPayloadContext ctx) {
            ScarSelectionScreen.open(scars);
        }

        @Override
        public @NotNull Type<OpenScarSelectionPacket> type() {
            return TYPE;
        }
    }

    public static class ScarRemovalPacket implements CustomPacketPayload {

        public static final Type<ScarRemovalPacket> TYPE = new Type<>(CosmicCore.id("scar_removal"));
        public static final StreamCodec<FriendlyByteBuf, ScarRemovalPacket> CODEC = StreamCodec
                .ofMember(ScarRemovalPacket::encode, ScarRemovalPacket::new);

        private final ResourceLocation scarId;

        public ScarRemovalPacket(ResourceLocation scarId) {
            this.scarId = scarId;
        }

        public ScarRemovalPacket(FriendlyByteBuf buf) {
            this.scarId = buf.readResourceLocation();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(scarId);
        }

        public void execute(IPayloadContext ctx) {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ReflectionCapability.get(player).ifPresent(reflection -> {
                if (!reflection.hasDefianceScar(scarId)) {
                    player.displayClientMessage(
                            Component.literal("You don't have that scar.").withStyle(ChatFormatting.RED),
                            true);
                    return;
                }

                ItemStack cluster = findCluster(player);
                if (cluster.isEmpty()) {
                    player.displayClientMessage(
                            Component.literal("You need a Cluster of Perpetuity.").withStyle(ChatFormatting.RED),
                            true);
                    return;
                }

                cluster.shrink(1);
                reflection.removeScar(scarId);

                String name = StringUtil.toTitleCase(scarId.getPath());
                player.displayClientMessage(
                        Component.literal("The scar of '" + name + "' fades from your soul.")
                                .withStyle(ChatFormatting.LIGHT_PURPLE),
                        true);

                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6f, 1.2f);
            });
        }

        private ItemStack findCluster(ServerPlayer player) {
            if (isCluster(player.getMainHandItem())) return player.getMainHandItem();
            if (isCluster(player.getOffhandItem())) return player.getOffhandItem();

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (isCluster(stack)) return stack;
            }
            return ItemStack.EMPTY;
        }

        private boolean isCluster(ItemStack stack) {
            return !stack.isEmpty() && stack.is(CosmicItems.PERPETUITY_SHARD_MASSIVE.get());
        }

        @Override
        public @NotNull Type<ScarRemovalPacket> type() {
            return TYPE;
        }
    }
}
