package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain.BargainAnswer;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementBargain;
import com.ghostipedia.cosmiccore.common.reflection.network.SyncQuakeMovementPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;

public class VoidUIPackets {

    public static void register() {
        CCoreNetwork.register(OpenVoidScreenPacket.class, OpenVoidScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        CCoreNetwork.register(BargainChoicePacket.class, BargainChoicePacket::new, NetworkDirection.PLAY_TO_SERVER);
        CCoreNetwork.register(ThresholdEncounterPacket.class, ThresholdEncounterPacket::new,
                NetworkDirection.PLAY_TO_CLIENT);
        CCoreNetwork.register(OpenHubPacket.class, OpenHubPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        CCoreNetwork.register(DefianceChoicePacket.class, DefianceChoicePacket::new, NetworkDirection.PLAY_TO_SERVER);
    }

    public static void sendOpenVoidScreen(ServerPlayer player, ResourceLocation bargainId) {
        ReflectionCapability.get(player).ifPresent(reflection -> {
            int erosion = reflection.getErosion();
            Set<ResourceLocation> activeBargains = reflection.getActiveBargains();
            int shardBalance = reflection.getShardBalance();
            int usedCapacity = reflection.getUsedCapacity();
            int totalCapacity = reflection.getTotalCapacity();
            CCoreNetwork.sendToPlayer(player, new OpenVoidScreenPacket(bargainId, erosion, activeBargains,
                    shardBalance, usedCapacity, totalCapacity));
        });
    }

    public static void sendOpenVoidScreen(ServerPlayer player) {
        ReflectionCapability.get(player).ifPresent(reflection -> {
            int erosion = reflection.getErosion();
            Set<ResourceLocation> activeBargains = reflection.getActiveBargains();
            int shardBalance = reflection.getShardBalance();
            int usedCapacity = reflection.getUsedCapacity();
            int totalCapacity = reflection.getTotalCapacity();
            CCoreNetwork.sendToPlayer(player, new OpenVoidScreenPacket(null, erosion, activeBargains,
                    shardBalance, usedCapacity, totalCapacity));
        });
    }

    public static void sendBargainChoice(ResourceLocation bargainId, String answerId) {
        CCoreNetwork.sendToServer(new BargainChoicePacket(bargainId, answerId));
    }

    public static void sendThresholdEncounter(ServerPlayer player, int thresholdIndex) {
        ReflectionCapability.get(player).ifPresent(reflection -> {
            int erosion = reflection.getErosion();
            Set<ResourceLocation> activeBargains = reflection.getActiveBargains();
            CCoreNetwork.sendToPlayer(player, new ThresholdEncounterPacket(thresholdIndex, erosion, activeBargains));
        });
    }

    public static void sendOpenHub(ServerPlayer player) {
        ReflectionCapability.get(player).ifPresent(reflection -> {
            int erosion = reflection.getErosion();
            Set<ResourceLocation> activeBargains = reflection.getActiveBargains();
            Set<ResourceLocation> defianceScars = reflection.getDefianceScars();
            int shardBalance = reflection.getShardBalance();
            int usedCapacity = reflection.getUsedCapacity();
            int totalCapacity = reflection.getTotalCapacity();
            CCoreNetwork.sendToPlayer(player, new OpenHubPacket(erosion, activeBargains, defianceScars,
                    shardBalance, usedCapacity, totalCapacity));
        });
    }

    public static void sendDefianceChoice(ResourceLocation bargainId) {
        CCoreNetwork.sendToServer(new DefianceChoicePacket(bargainId));
    }

    public static class OpenVoidScreenPacket implements CCoreNetwork.INetPacket {

        private final ResourceLocation bargainId;
        private final int erosion;
        private final Set<ResourceLocation> activeBargains;
        private final int shardBalance;
        private final int usedCapacity;
        private final int totalCapacity;

        public OpenVoidScreenPacket(ResourceLocation bargainId, int erosion, Set<ResourceLocation> activeBargains,
                                    int shardBalance, int usedCapacity, int totalCapacity) {
            this.bargainId = bargainId;
            this.erosion = erosion;
            this.activeBargains = activeBargains != null ? activeBargains : Set.of();
            this.shardBalance = shardBalance;
            this.usedCapacity = usedCapacity;
            this.totalCapacity = totalCapacity;
        }

        public OpenVoidScreenPacket(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                this.bargainId = buf.readResourceLocation();
            } else {
                this.bargainId = null;
            }
            this.erosion = buf.readVarInt();

            int count = buf.readVarInt();
            Set<ResourceLocation> bargains = new HashSet<>();
            for (int i = 0; i < count; i++) {
                bargains.add(buf.readResourceLocation());
            }
            this.activeBargains = bargains;

            this.shardBalance = buf.readVarInt();
            this.usedCapacity = buf.readVarInt();
            this.totalCapacity = buf.readVarInt();
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(bargainId != null);
            if (bargainId != null) {
                buf.writeResourceLocation(bargainId);
            }
            buf.writeVarInt(erosion);
            buf.writeVarInt(activeBargains.size());
            for (ResourceLocation id : activeBargains) {
                buf.writeResourceLocation(id);
            }

            buf.writeVarInt(shardBalance);
            buf.writeVarInt(usedCapacity);
            buf.writeVarInt(totalCapacity);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (bargainId != null) {
                    BargainRegistry.get(bargainId)
                            .ifPresent(bargain -> VoidScreen.openWithBargain(bargain, erosion, activeBargains,
                                    shardBalance, usedCapacity, totalCapacity));
                } else {
                    VoidScreen.openForReflection(erosion, activeBargains,
                            shardBalance, usedCapacity, totalCapacity);
                }
            });
        }
    }

    public static class BargainChoicePacket implements CCoreNetwork.INetPacket {

        private final ResourceLocation bargainId;
        private final String answerId;

        public BargainChoicePacket(ResourceLocation bargainId, String answerId) {
            this.bargainId = bargainId;
            this.answerId = answerId;
        }

        public BargainChoicePacket(FriendlyByteBuf buf) {
            this.bargainId = buf.readResourceLocation();
            this.answerId = buf.readUtf();
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(bargainId);
            buf.writeUtf(answerId);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            BargainRegistry.get(bargainId).ifPresent(bargain -> {
                processBargainChoice(player, bargain, answerId);
            });
        }

        private void processBargainChoice(ServerPlayer player, Bargain bargain, String answerId) {
            // Find the answer
            BargainAnswer foundAnswer = null;
            for (BargainAnswer answer : bargain.getAnswers()) {
                if (answer.id().equals(answerId)) {
                    foundAnswer = answer;
                    break;
                }
            }

            if (foundAnswer == null) return;

            // Make final for lambda
            final BargainAnswer selectedAnswer = foundAnswer;

            ReflectionCapability.get(player).ifPresent(reflection -> {
                boolean isAccept = answerId.equals("accept") ||
                        (!answerId.equals("refuse") && selectedAnswer.grantsFullPower());

                if (isAccept) {
                    if (!reflection.hasBargain(bargainId)) {
                        int shardCost = bargain.getShardCost();
                        int weight = bargain.getWeight();

                        if (shardCost > 0 && reflection.getShardBalance() < shardCost) {
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component
                                            .literal("\u00A7cInsufficient shards. You need " + shardCost + " shards."),
                                    false);
                            return;
                        }

                        if (weight > 0 && !reflection.canFitBargain(weight)) {
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal(
                                            "\u00A7cInsufficient soul capacity. Need " + weight + " weight, have " +
                                                    reflection.getRemainingCapacity() + " remaining."),
                                    false);
                            return;
                        }

                        if (shardCost > 0) {
                            reflection.spendShards(shardCost);
                        }

                        int erosionCost = bargain.getErosionCost();
                        if (erosionCost > 0) {
                            reflection.addErosion(erosionCost);
                        }

                        reflection.acceptBargain(bargainId);
                        bargain.onAccept(player, selectedAnswer);
                        syncBargainState(player, bargain, true);
                    }
                }
                // Note: Refusing a bargain offer does NOT call onDefy.
                // onDefy is only for breaking an existing bargain you've already accepted.
                // Refusing simply declines the offer with no mechanical effect.
            });
        }

        private void syncBargainState(ServerPlayer player, Bargain bargain, boolean active) {
            if (bargain.getId().equals(QuakeMovementBargain.INSTANCE.getId())) {
                CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(active));
            }
        }
    }

    public static class ThresholdEncounterPacket implements CCoreNetwork.INetPacket {

        private final int thresholdIndex;
        private final int erosion;
        private final Set<ResourceLocation> activeBargains;

        public ThresholdEncounterPacket(int thresholdIndex, int erosion, Set<ResourceLocation> activeBargains) {
            this.thresholdIndex = thresholdIndex;
            this.erosion = erosion;
            this.activeBargains = activeBargains != null ? activeBargains : Set.of();
        }

        public ThresholdEncounterPacket(FriendlyByteBuf buf) {
            this.thresholdIndex = buf.readVarInt();
            this.erosion = buf.readVarInt();

            int count = buf.readVarInt();
            Set<ResourceLocation> bargains = new HashSet<>();
            for (int i = 0; i < count; i++) {
                bargains.add(buf.readResourceLocation());
            }
            this.activeBargains = bargains;
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(thresholdIndex);
            buf.writeVarInt(erosion);

            buf.writeVarInt(activeBargains.size());
            for (ResourceLocation id : activeBargains) {
                buf.writeResourceLocation(id);
            }
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                VoidScreen.openForThreshold(thresholdIndex, erosion, activeBargains);
            });
        }
    }

    public static class OpenHubPacket implements CCoreNetwork.INetPacket {

        private final int erosion;
        private final Set<ResourceLocation> activeBargains;
        private final Set<ResourceLocation> defianceScars;
        private final int shardBalance;
        private final int usedCapacity;
        private final int totalCapacity;

        public OpenHubPacket(int erosion, Set<ResourceLocation> activeBargains, Set<ResourceLocation> defianceScars,
                             int shardBalance, int usedCapacity, int totalCapacity) {
            this.erosion = erosion;
            this.activeBargains = activeBargains != null ? activeBargains : Set.of();
            this.defianceScars = defianceScars != null ? defianceScars : Set.of();
            this.shardBalance = shardBalance;
            this.usedCapacity = usedCapacity;
            this.totalCapacity = totalCapacity;
        }

        public OpenHubPacket(FriendlyByteBuf buf) {
            this.erosion = buf.readVarInt();

            int activeCount = buf.readVarInt();
            Set<ResourceLocation> active = new HashSet<>();
            for (int i = 0; i < activeCount; i++) {
                active.add(buf.readResourceLocation());
            }
            this.activeBargains = active;

            int scarCount = buf.readVarInt();
            Set<ResourceLocation> scars = new HashSet<>();
            for (int i = 0; i < scarCount; i++) {
                scars.add(buf.readResourceLocation());
            }
            this.defianceScars = scars;

            this.shardBalance = buf.readVarInt();
            this.usedCapacity = buf.readVarInt();
            this.totalCapacity = buf.readVarInt();
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(erosion);

            buf.writeVarInt(activeBargains.size());
            for (ResourceLocation id : activeBargains) {
                buf.writeResourceLocation(id);
            }

            buf.writeVarInt(defianceScars.size());
            for (ResourceLocation id : defianceScars) {
                buf.writeResourceLocation(id);
            }

            buf.writeVarInt(shardBalance);
            buf.writeVarInt(usedCapacity);
            buf.writeVarInt(totalCapacity);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                VoidScreen.openForHub(erosion, activeBargains, defianceScars,
                        shardBalance, usedCapacity, totalCapacity);
            });
        }
    }

    public static class DefianceChoicePacket implements CCoreNetwork.INetPacket {

        private final ResourceLocation bargainId;

        public DefianceChoicePacket(ResourceLocation bargainId) {
            this.bargainId = bargainId;
        }

        public DefianceChoicePacket(FriendlyByteBuf buf) {
            this.bargainId = buf.readResourceLocation();
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(bargainId);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            BargainRegistry.get(bargainId).ifPresent(bargain -> {
                ReflectionCapability.get(player).ifPresent(reflection -> {
                    if (!reflection.hasBargain(bargainId)) return;

                    int cost = BargainRegistry.calculateDefianceCost(player, bargain);
                    reflection.addErosion(cost);
                    reflection.defy(bargainId);
                    bargain.onDefy(player);

                    if (bargainId.equals(QuakeMovementBargain.INSTANCE.getId())) {
                        CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(false));
                    }
                });
            });
        }
    }
}
