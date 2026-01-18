package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.utils.StringUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Network packets for Cluster of Perpetuity scar removal.
 */
public class ScarSelectionPackets {

    public static void register() {
        CCoreNetwork.register(OpenScarSelectionPacket.class, OpenScarSelectionPacket::new,
                NetworkDirection.PLAY_TO_CLIENT);
        CCoreNetwork.register(ScarRemovalPacket.class, ScarRemovalPacket::new,
                NetworkDirection.PLAY_TO_SERVER);
    }

    public static void sendOpenScarSelection(ServerPlayer player, Set<ResourceLocation> scars) {
        CCoreNetwork.sendToPlayer(player, new OpenScarSelectionPacket(scars));
    }

    public static void sendScarRemoval(ResourceLocation scarId) {
        CCoreNetwork.sendToServer(new ScarRemovalPacket(scarId));
    }

    // Server -> Client: Open the scar selection screen
    public static class OpenScarSelectionPacket implements CCoreNetwork.INetPacket {

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

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(scars.size());
            for (ResourceLocation id : scars) {
                buf.writeResourceLocation(id);
            }
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ScarSelectionScreen.open(scars));
        }
    }

    // Client -> Server: Remove a specific scar (consumes Cluster from inventory)
    public static class ScarRemovalPacket implements CCoreNetwork.INetPacket {

        private final ResourceLocation scarId;

        public ScarRemovalPacket(ResourceLocation scarId) {
            this.scarId = scarId;
        }

        public ScarRemovalPacket(FriendlyByteBuf buf) {
            this.scarId = buf.readResourceLocation();
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(scarId);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

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
            // Check hands first
            if (isCluster(player.getMainHandItem())) return player.getMainHandItem();
            if (isCluster(player.getOffhandItem())) return player.getOffhandItem();

            // Search inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (isCluster(stack)) return stack;
            }
            return ItemStack.EMPTY;
        }

        private boolean isCluster(ItemStack stack) {
            return !stack.isEmpty() && stack.is(CosmicItems.PERPETUITY_SHARD_MASSIVE.get());
        }
    }
}
