package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain.BargainAnswer;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementBargain;
import com.ghostipedia.cosmiccore.common.reflection.network.SyncQuakeMovementPacket;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class VoidUIPackets {

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
            SoulShape soulShape = reflection.getSoulShape();
            boolean hasMutilator = player.getInventory().contains(CosmicItems.SOUL_MUTILATOR.asStack());
            CCoreNetwork.sendToPlayer(player, new OpenHubPacket(erosion, activeBargains, defianceScars,
                    shardBalance, usedCapacity, totalCapacity, soulShape, hasMutilator));
        });
    }

    public static void sendSoulShapeChoice(SoulShape shape) {
        CCoreNetwork.sendToServer(new SoulShapeChoicePacket(shape));
    }

    public static void sendDefianceChoice(ResourceLocation bargainId) {
        CCoreNetwork.sendToServer(new DefianceChoicePacket(bargainId));
    }

    public static class OpenVoidScreenPacket implements CustomPacketPayload {

        public static final Type<OpenVoidScreenPacket> TYPE = new Type<>(CosmicCore.id("void_open_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenVoidScreenPacket> CODEC = StreamCodec
                .ofMember(OpenVoidScreenPacket::encode, OpenVoidScreenPacket::new);

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

        public void execute(IPayloadContext ctx) {
            if (bargainId != null) {
                BargainRegistry.get(bargainId)
                        .ifPresent(bargain -> VoidScreen.openWithBargain(bargain, erosion, activeBargains,
                                shardBalance, usedCapacity, totalCapacity));
            } else {
                VoidScreen.openForReflection(erosion, activeBargains,
                        shardBalance, usedCapacity, totalCapacity);
            }
        }

        @Override
        public @NotNull Type<OpenVoidScreenPacket> type() {
            return TYPE;
        }
    }

    public static class BargainChoicePacket implements CustomPacketPayload {

        public static final Type<BargainChoicePacket> TYPE = new Type<>(CosmicCore.id("void_bargain_choice"));
        public static final StreamCodec<FriendlyByteBuf, BargainChoicePacket> CODEC = StreamCodec
                .ofMember(BargainChoicePacket::encode, BargainChoicePacket::new);

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

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(bargainId);
            buf.writeUtf(answerId);
        }

        public void execute(IPayloadContext ctx) {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            BargainRegistry.get(bargainId).ifPresent(bargain -> {
                processBargainChoice(player, bargain, answerId);
            });
        }

        private void processBargainChoice(ServerPlayer player, Bargain bargain, String answerId) {
            BargainAnswer foundAnswer = null;
            for (BargainAnswer answer : bargain.getAnswers()) {
                if (answer.id().equals(answerId)) {
                    foundAnswer = answer;
                    break;
                }
            }

            if (foundAnswer == null) return;

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
                                    Component.literal("§cInsufficient shards. You need " + shardCost + " shards."),
                                    false);
                            return;
                        }

                        if (weight > 0 && !reflection.canFitBargain(weight)) {
                            player.displayClientMessage(
                                    Component.literal(
                                            "§cInsufficient soul capacity. Need " + weight + " weight, have " +
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
            });
        }

        private void syncBargainState(ServerPlayer player, Bargain bargain, boolean active) {
            if (bargain.getId().equals(QuakeMovementBargain.INSTANCE.getId())) {
                CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(active));
            }
        }

        @Override
        public @NotNull Type<BargainChoicePacket> type() {
            return TYPE;
        }
    }

    public static class ThresholdEncounterPacket implements CustomPacketPayload {

        public static final Type<ThresholdEncounterPacket> TYPE = new Type<>(CosmicCore.id("void_threshold_encounter"));
        public static final StreamCodec<FriendlyByteBuf, ThresholdEncounterPacket> CODEC = StreamCodec
                .ofMember(ThresholdEncounterPacket::encode, ThresholdEncounterPacket::new);

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

        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(thresholdIndex);
            buf.writeVarInt(erosion);

            buf.writeVarInt(activeBargains.size());
            for (ResourceLocation id : activeBargains) {
                buf.writeResourceLocation(id);
            }
        }

        public void execute(IPayloadContext ctx) {
            VoidScreen.openForThreshold(thresholdIndex, erosion, activeBargains);
        }

        @Override
        public @NotNull Type<ThresholdEncounterPacket> type() {
            return TYPE;
        }
    }

    public static class OpenHubPacket implements CustomPacketPayload {

        public static final Type<OpenHubPacket> TYPE = new Type<>(CosmicCore.id("void_open_hub"));
        public static final StreamCodec<FriendlyByteBuf, OpenHubPacket> CODEC = StreamCodec
                .ofMember(OpenHubPacket::encode, OpenHubPacket::new);

        private final int erosion;
        private final Set<ResourceLocation> activeBargains;
        private final Set<ResourceLocation> defianceScars;
        private final int shardBalance;
        private final int usedCapacity;
        private final int totalCapacity;
        private final SoulShape soulShape;
        private final boolean hasMutilator;

        public OpenHubPacket(int erosion, Set<ResourceLocation> activeBargains, Set<ResourceLocation> defianceScars,
                             int shardBalance, int usedCapacity, int totalCapacity,
                             SoulShape soulShape, boolean hasMutilator) {
            this.erosion = erosion;
            this.activeBargains = activeBargains != null ? activeBargains : Set.of();
            this.defianceScars = defianceScars != null ? defianceScars : Set.of();
            this.shardBalance = shardBalance;
            this.usedCapacity = usedCapacity;
            this.totalCapacity = totalCapacity;
            this.soulShape = soulShape != null ? soulShape : SoulShape.UNSHAPED;
            this.hasMutilator = hasMutilator;
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
            this.soulShape = SoulShape.fromId(buf.readUtf());
            this.hasMutilator = buf.readBoolean();
        }

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
            buf.writeUtf(soulShape.getId());
            buf.writeBoolean(hasMutilator);
        }

        public void execute(IPayloadContext ctx) {
            VoidScreen.openForHub(erosion, activeBargains, defianceScars,
                    shardBalance, usedCapacity, totalCapacity, soulShape, hasMutilator);
        }

        @Override
        public @NotNull Type<OpenHubPacket> type() {
            return TYPE;
        }
    }

    public static class DefianceChoicePacket implements CustomPacketPayload {

        public static final Type<DefianceChoicePacket> TYPE = new Type<>(CosmicCore.id("void_defiance_choice"));
        public static final StreamCodec<FriendlyByteBuf, DefianceChoicePacket> CODEC = StreamCodec
                .ofMember(DefianceChoicePacket::encode, DefianceChoicePacket::new);

        private final ResourceLocation bargainId;

        public DefianceChoicePacket(ResourceLocation bargainId) {
            this.bargainId = bargainId;
        }

        public DefianceChoicePacket(FriendlyByteBuf buf) {
            this.bargainId = buf.readResourceLocation();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(bargainId);
        }

        public void execute(IPayloadContext ctx) {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

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

        @Override
        public @NotNull Type<DefianceChoicePacket> type() {
            return TYPE;
        }
    }

    public static class SoulShapeChoicePacket implements CustomPacketPayload {

        public static final Type<SoulShapeChoicePacket> TYPE = new Type<>(CosmicCore.id("void_soul_shape_choice"));
        public static final StreamCodec<FriendlyByteBuf, SoulShapeChoicePacket> CODEC = StreamCodec
                .ofMember(SoulShapeChoicePacket::encode, SoulShapeChoicePacket::new);

        private final SoulShape shape;

        public SoulShapeChoicePacket(SoulShape shape) {
            this.shape = shape;
        }

        public SoulShapeChoicePacket(FriendlyByteBuf buf) {
            this.shape = SoulShape.fromId(buf.readUtf());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(shape.getId());
        }

        public void execute(IPayloadContext ctx) {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            if (shape == SoulShape.UNSHAPED) return;

            ReflectionCapability.get(player).ifPresent(reflection -> {
                if (!reflection.hasAwakened()) return;
                if (reflection.hasSoulShape()) return;

                var inventory = player.getInventory();
                int mutilatorSlot = -1;
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    if (inventory.getItem(i).is(CosmicItems.SOUL_MUTILATOR.get())) {
                        mutilatorSlot = i;
                        break;
                    }
                }
                if (mutilatorSlot == -1) return;

                inventory.removeItem(mutilatorSlot, 1);

                reflection.setSoulShape(shape);

                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 0.6f);
            });
        }

        @Override
        public @NotNull Type<SoulShapeChoicePacket> type() {
            return TYPE;
        }
    }
}
