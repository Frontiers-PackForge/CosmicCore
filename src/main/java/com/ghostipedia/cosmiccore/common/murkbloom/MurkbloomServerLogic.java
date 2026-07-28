package com.ghostipedia.cosmiccore.common.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.data.CosmicDamageTypes;
import com.ghostipedia.cosmiccore.common.data.worldgen.abyss.AbyssRegions;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.MurkbloomSyncPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncAbyssAttunementPacket;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class MurkbloomServerLogic {

    private MurkbloomServerLogic() {}

    public static final ResourceKey<Level> HOLLOW_DIM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("undergarden", "undergarden"));
    public static final int ENTRY_Y = -60;
    public static final int ATTUNEMENT_LAYER = 2;

    private static final double[] RISE_THRESHOLDS = { 25, 50, 70, 90 };
    private static final double FALL_HYSTERESIS = 8;
    private static final int FALL_COOLDOWN = 25;
    private static final int SYNC_INTERVAL = 10;

    public static final double NOISE_BREAK = 6;
    public static final double NOISE_PLACE = 4;
    public static final double NOISE_EAT = 6;
    public static final double NOISE_DEAL_HIT = 8;
    public static final double NOISE_TAKE_HIT = 5;
    public static final double SONAR_CAP = 80;

    private static final double TICK_SPRINT_EXPOSED = 0.22;
    private static final double TICK_SPRINT_SHELTERED = 0.12;
    private static final double TICK_SWIM_EXPOSED = 0.06;
    private static final double THRASH_SPEED_SQ = 0.0055;
    private static final double MOVE_SPEED_SQ = 0.0008;
    private static final double MASKED_MOVE_DECAY = 0.08;
    private static final double TICK_STILL_RESTLESS = 0.05;
    private static final double EXPOSURE_BASE = 0.02;
    private static final double EXPOSURE_MAX = 0.35;
    private static final int EXPOSURE_RAMP_TICKS = 1200;
    private static final double DECAY_CROUCH = 0.20;
    private static final double DECAY_DRY = 0.90;
    private static final double DECAY_OUTSIDE = 3.0;
    private static final int SHELTER_BELOW = 10;
    private static final int SHELTER_ABOVE = 6;
    private static final int[] SHELTER_HORIZONTAL = { 3, 6, 9 };

    private static final Map<UUID, Hunt> HUNTS = new HashMap<>();
    private static final Set<UUID> DEV_IMMUNE = new HashSet<>();

    private static final class Hunt {

        double noise = 0;
        int stir = 0;
        long lastFall = 0;
        long lastSync = 0;
        int lastSentStir = -1;
        BlockPos lastLoud = null;
        double flinchQueued = 0;
        boolean exposed = false;
        int exposedTicks = 0;
        byte impulseKind = 0;
        int dissolveTicks = 0;
        boolean dissolveWarned = false;
        boolean stalkWarned = false;
        int lastLayer = 0;
    }

    public static final int DISSOLVE_GRACE_TICKS = 200;
    public static final int DISSOLVE_INTERVAL = 10;
    public static final float DISSOLVE_DAMAGE = 10.0f;

    public static final byte KIND_BREAK = 1;
    public static final byte KIND_PLACE = 2;
    public static final byte KIND_EAT = 3;
    public static final byte KIND_HIT = 4;
    public static final byte KIND_SONAR = 5;

    public static boolean inHollow(ServerPlayer player) {
        return inHollow(player.level(), player.getY());
    }

    public static boolean inHollow(Level level, double y) {
        return level.dimension().equals(HOLLOW_DIM) && y <= ENTRY_Y;
    }

    public static boolean hunting(ServerPlayer player) {
        return inHollow(player) && player.isInWater();
    }

    public static boolean toggleDevImmunity(ServerPlayer player) {
        UUID id = player.getUUID();
        if (DEV_IMMUNE.remove(id)) return false;
        DEV_IMMUNE.add(id);
        HUNTS.remove(id);
        sync(player, new Hunt(), true);
        return true;
    }

    public static boolean devImmune(ServerPlayer player) {
        return DEV_IMMUNE.contains(player.getUUID());
    }

    public static final double STEALTH_FLOOR = 0.5;

    public static double noiseGainScale(ServerPlayer player) {
        int layer = AbyssRegions.layer(player.getBlockY());
        double aggro = Math.pow(5, Math.max(0, layer - 1));
        double scaled = aggro * StealthCoating.armorMultiplier(player) * StealthCoating.effectMultiplier(player);
        return Math.max(scaled, STEALTH_FLOOR);
    }

    public static void impulse(ServerPlayer player, double amount, boolean capped, byte kind) {
        if (devImmune(player)) return;
        if (!inHollow(player)) return;
        amount *= noiseGainScale(player);
        Hunt hunt = HUNTS.computeIfAbsent(player.getUUID(), u -> new Hunt());
        double next = hunt.noise + amount;
        if (capped) next = Math.max(hunt.noise, Math.min(next, SONAR_CAP));
        hunt.noise = Mth.clamp(next, 0, 100);
        hunt.lastLoud = player.blockPosition();
        hunt.flinchQueued = Math.max(hunt.flinchQueued, Math.min(amount / 12.0, 1.5));
        hunt.impulseKind = kind;
    }

    public static void sonarPing(ServerPlayer player) {
        if (devImmune(player)) return;
        if (!inHollow(player)) return;
        Hunt hunt = HUNTS.computeIfAbsent(player.getUUID(), u -> new Hunt());
        double target = hunt.stir < 4 ? RISE_THRESHOLDS[hunt.stir] + 2 : hunt.noise;
        hunt.noise = Mth.clamp(Math.max(hunt.noise, Math.min(target, SONAR_CAP)), 0, 100);
        hunt.lastLoud = player.blockPosition();
        hunt.flinchQueued = Math.max(hunt.flinchQueued, 1.2);
        hunt.impulseKind = KIND_SONAR;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (devImmune(player)) return;
        Hunt hunt = HUNTS.get(player.getUUID());

        if (!inHollow(player)) {
            if (hunt != null) {
                hunt.noise = Math.max(0, hunt.noise - DECAY_OUTSIDE);
                stepStir(hunt, player);
                if (hunt.noise <= 0 && hunt.stir == 0) {
                    sync(player, hunt, true);
                    HUNTS.remove(player.getUUID());
                } else {
                    sync(player, hunt, false);
                }
            }
            return;
        }

        if (hunt == null) {
            hunt = HUNTS.computeIfAbsent(player.getUUID(), u -> new Hunt());
        }

        if (!player.getData(CosmicAttachmentTypes.ABYSS_ATTUNED) &&
                AbyssRegions.layer(player.getBlockY()) >= ATTUNEMENT_LAYER) {
            player.setData(CosmicAttachmentTypes.ABYSS_ATTUNED, true);
            player.displayClientMessage(Component.translatable("cosmiccore.abyss.seal_broken")
                    .withStyle(style -> style.withColor(0xC9AEF5).withItalic(true)), true);
            CCoreNetwork.sendToPlayer(player, new SyncAbyssAttunementPacket(true));
        }

        int layer = AbyssRegions.layer(player.getBlockY());
        if (layer > hunt.lastLayer && layer >= 2) {
            player.displayClientMessage(Component.translatable("cosmiccore.abyss.descent")
                    .withStyle(style -> style.withColor(0x6E93A6).withItalic(true)), true);
        }
        hunt.lastLayer = layer;

        if (player.tickCount % 20 == 0) {
            hunt.exposed = isExposed(player);
        }

        if (hunting(player)) {
            if (hunt.exposed) {
                hunt.exposedTicks++;
            } else {
                hunt.exposedTicks = Math.max(0, hunt.exposedTicks - 3);
            }
            double exposure = hunt.exposed ? Math.min(EXPOSURE_MAX,
                    EXPOSURE_BASE + (EXPOSURE_MAX - EXPOSURE_BASE) * hunt.exposedTicks / (double) EXPOSURE_RAMP_TICKS) :
                    0.0;
            double speedSq = player.getDeltaMovement().lengthSqr();
            double movement;
            if (speedSq > THRASH_SPEED_SQ) {
                movement = hunt.exposed ? TICK_SPRINT_EXPOSED : TICK_SPRINT_SHELTERED;
            } else if (speedSq > MOVE_SPEED_SQ) {
                movement = hunt.exposed ? TICK_SWIM_EXPOSED : -MASKED_MOVE_DECAY;
            } else if (player.isShiftKeyDown()) {
                movement = -DECAY_CROUCH;
            } else {
                movement = TICK_STILL_RESTLESS;
            }
            double gain = exposure + movement;
            hunt.noise += gain > 0 ? gain * noiseGainScale(player) : gain;
        } else {
            hunt.exposedTicks = Math.max(0, hunt.exposedTicks - 6);
            hunt.noise -= DECAY_DRY;
        }
        hunt.noise = Mth.clamp(hunt.noise, 0, 100);

        stepStir(hunt, player);

        if (hunt.stir >= 3 && !hunt.stalkWarned) {
            hunt.stalkWarned = true;
            player.displayClientMessage(Component.translatable("cosmiccore.abyss.stalked")
                    .withStyle(style -> style.withColor(0xFF9E64).withItalic(true)), true);
        } else if (hunt.stir <= 1) {
            hunt.stalkWarned = false;
        }

        if (hunting(player) && hunt.stir >= 4) {
            hunt.dissolveTicks++;
            if (!hunt.dissolveWarned) {
                hunt.dissolveWarned = true;
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                player.connection.send(new ClientboundSetSubtitleTextPacket(
                        Component.translatable("cosmiccore.abyss.swarm_subtitle")
                                .withStyle(style -> style.withColor(0xDCE6EA))));
                player.connection.send(new ClientboundSetTitleTextPacket(
                        Component.translatable("cosmiccore.abyss.swarm_title")
                                .withStyle(style -> style.withColor(0xCF6679))));
            }
            if (hunt.dissolveTicks > DISSOLVE_GRACE_TICKS && hunt.dissolveTicks % DISSOLVE_INTERVAL == 0) {
                player.hurt(CosmicDamageTypes.source(player.level(), CosmicDamageTypes.MURKBLOOM), DISSOLVE_DAMAGE);
            }
        } else {
            hunt.dissolveTicks = 0;
            if (hunt.stir < 3) {
                hunt.dissolveWarned = false;
            }
        }

        sync(player, hunt, false);
    }

    private static void stepStir(Hunt hunt, ServerPlayer player) {
        while (hunt.stir < 4 && hunt.noise >= RISE_THRESHOLDS[hunt.stir]) {
            hunt.stir++;
        }
        if (hunt.stir > 0 && hunt.noise < RISE_THRESHOLDS[hunt.stir - 1] - FALL_HYSTERESIS &&
                player.tickCount - hunt.lastFall > FALL_COOLDOWN) {
            hunt.stir--;
            hunt.lastFall = player.tickCount;
        }
    }

    private static void sync(ServerPlayer player, Hunt hunt, boolean force) {
        boolean due = player.tickCount - hunt.lastSync >= SYNC_INTERVAL;
        boolean changed = hunt.stir != hunt.lastSentStir;
        boolean flinch = hunt.flinchQueued > 0;
        if (!force && !due && !changed && !flinch) return;

        float yaw = player.getYRot();
        if (hunt.lastLoud != null) {
            double dx = hunt.lastLoud.getX() + 0.5 - player.getX();
            double dz = hunt.lastLoud.getZ() + 0.5 - player.getZ();
            yaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90f;
        }
        CCoreNetwork.sendToPlayer(player, new MurkbloomSyncPacket(hunt.stir, (float) (hunt.noise / 100.0),
                yaw, (float) hunt.flinchQueued, hunt.impulseKind));
        hunt.lastSync = player.tickCount;
        hunt.lastSentStir = hunt.stir;
        hunt.flinchQueued = 0;
        hunt.impulseKind = 0;
    }

    private static boolean isExposed(ServerPlayer player) {
        BlockPos base = player.blockPosition();
        for (int i = 1; i <= SHELTER_BELOW; i++) {
            if (isSolid(player, base.below(i))) return false;
        }
        for (int i = 1; i <= SHELTER_ABOVE; i++) {
            if (isSolid(player, base.above(i))) return false;
        }
        for (int d : SHELTER_HORIZONTAL) {
            if (isSolid(player, base.offset(d, 0, 0)) || isSolid(player, base.offset(-d, 0, 0)) ||
                    isSolid(player, base.offset(0, 0, d)) || isSolid(player, base.offset(0, 0, -d)) ||
                    isSolid(player, base.offset(d, 0, d)) || isSolid(player, base.offset(d, 0, -d)) ||
                    isSolid(player, base.offset(-d, 0, d)) || isSolid(player, base.offset(-d, 0, -d))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSolid(ServerPlayer player, BlockPos pos) {
        var state = player.level().getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (ToolHelper.IS_AOE_BREAKING_BLOCKS.get() &&
                    !ToolHelper.getAoEDefinition(player.getMainHandItem()).isZero()) {
                return;
            }
            double muted = NOISE_BREAK * StealthCoating.toolMultiplier(player.getMainHandItem());
            impulse(player, muted, false, KIND_BREAK);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            impulse(player, NOISE_PLACE, false, KIND_PLACE);
        }
    }

    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            impulse(player, NOISE_EAT, false, KIND_EAT);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            impulse(attacker, NOISE_DEAL_HIT, false, KIND_HIT);
        }
        if (event.getEntity() instanceof ServerPlayer victim && event.getSource().getEntity() != null) {
            impulse(victim, NOISE_TAKE_HIT, false, KIND_HIT);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CCoreNetwork.sendToPlayer(player,
                    new SyncAbyssAttunementPacket(player.getData(CosmicAttachmentTypes.ABYSS_ATTUNED)));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HUNTS.remove(event.getEntity().getUUID());
        DEV_IMMUNE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HUNTS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HUNTS.remove(player.getUUID());
            CCoreNetwork.sendToPlayer(player, new MurkbloomSyncPacket(0, 0f, player.getYRot(), 0f, (byte) 0));
        }
    }
}
