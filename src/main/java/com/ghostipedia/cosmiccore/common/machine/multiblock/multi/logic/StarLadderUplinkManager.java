package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.data.savedData.StarLadderSavedData;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets.WhisperStyle;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.Supplier;

public class StarLadderUplinkManager implements INBTSerializable<CompoundTag> {

    private static final int MAX_PROGRESS = 6000;

    private static final int TICKS_PER_SECOND = 20;

    private static final int PHASE_1_SOUL_DRAIN_PER_SECOND = 5000;
    private static final int PHASE_2_SOUL_DRAIN_PER_SECOND = 12000;
    private static final int PHASE_3_SOUL_DRAIN_PER_SECOND = 25000;

    private static final int PROGRESS_PER_TICK = 1;
    private static final int DEMAND_PROGRESS_BURST = 50;
    private static final int DEMAND_MISS_PENALTY = 30;

    private static final int FIRST_DEMAND_TICK = 200;
    private static final int DEMAND_INTERVAL_TICKS = 600;
    private static final int DEMAND_TIMER_TICKS = 1200;

    private static final int INTERRUPTED_WHISPER_1_TICK = 20;
    private static final int INTERRUPTED_WHISPER_2_TICK = 60;
    private static final int INTERRUPTED_WHISPER_3_TICK = 100;
    private static final int INTERRUPTED_DURATION = 120;

    private final StarLadderMachine machine;

    private StarLadderUplinkState state = StarLadderUplinkState.IDLE;
    private int tickCounter = 0;
    private int progress = 0;
    private ServerPlayer initiatingPlayer;

    private final DemandSlot bulkDemand = new DemandSlot();
    private final DemandSlot complexDemand = new DemandSlot();
    private int demandCount = 0;
    private int nextDemandTick = FIRST_DEMAND_TICK;

    private int syncCooldown = 0;

    private static final Random RANDOM = new Random();

    public StarLadderUplinkManager(StarLadderMachine machine) {
        this.machine = machine;
    }

    // ---- Public API ----

    public void initiate(ServerPlayer player) {
        if (state != StarLadderUplinkState.IDLE) return;
        if (!machine.isFormed()) return;
        if (machine.getLinkedPartners().isEmpty()) return;
        if (isTeamEstablished()) return;

        initiatingPlayer = player;
        state = StarLadderUplinkState.INTERRUPTED;
        tickCounter = 0;

        playSound(SoundEvents.WARDEN_AMBIENT, 0.6f, 0.5f);
        StarLadderUplinkPackets.sendCloseScreen(player);
    }

    public void abort(ServerPlayer player) {
        if (state == StarLadderUplinkState.IDLE || state == StarLadderUplinkState.COMPLETED) return;

        resetToIdle();
        syncToPlayer(player);
    }

    public void confirm(ServerPlayer player) {
        if (state != StarLadderUplinkState.AWAITING_CONFIRMATION) return;

        initiatingPlayer = player;
        state = StarLadderUplinkState.ACTIVE_PHASE_1;
        tickCounter = 0;
        progress = 0;
        demandCount = 0;
        nextDemandTick = FIRST_DEMAND_TICK;
        clearDemands();

        syncToPlayer(player);
    }

    public StarLadderUplinkState getState() {
        if (state == StarLadderUplinkState.IDLE && isTeamEstablished()) {
            return StarLadderUplinkState.COMPLETED;
        }
        return state;
    }

    public int getProgress() {
        return progress;
    }

    public DemandSlot getBulkDemand() {
        return bulkDemand;
    }

    public DemandSlot getComplexDemand() {
        return complexDemand;
    }

    // ---- Tick Logic ----

    public void tick() {
        if (state == StarLadderUplinkState.IDLE || state == StarLadderUplinkState.COMPLETED) return;

        tickCounter++;

        switch (state) {
            case INTERRUPTED -> tickInterrupted();
            case ACTIVE_PHASE_1, ACTIVE_PHASE_2, ACTIVE_PHASE_3 -> tickActivePhase();
            case FAILED -> tickFailed();
            default -> {}
        }

        if (syncCooldown > 0) syncCooldown--;
    }

    private void tickInterrupted() {
        if (initiatingPlayer == null || !initiatingPlayer.isAlive()) {
            resetToIdle();
            return;
        }

        if (tickCounter == INTERRUPTED_WHISPER_1_TICK) {
            StarLadderUplinkPackets.sendObserverWhisper(initiatingPlayer,
                    W + "interrupted.silence", WhisperStyle.AMBIENT);
            playSound(SoundEvents.SCULK_CLICKING, 0.3f, 0.4f);
        }
        if (tickCounter == INTERRUPTED_WHISPER_2_TICK) {
            StarLadderUplinkPackets.sendObserverWhisper(initiatingPlayer,
                    W + "interrupted.pressure", WhisperStyle.AMBIENT);
            playSound(SoundEvents.SCULK_CLICKING, 0.5f, 0.6f);
        }
        if (tickCounter == INTERRUPTED_WHISPER_3_TICK) {
            StarLadderUplinkPackets.sendObserverWhisper(initiatingPlayer,
                    W + "interrupted.no", WhisperStyle.OBSERVER);
            playSound(SoundEvents.WARDEN_HEARTBEAT, 0.8f, 0.8f);
        }
        if (tickCounter >= INTERRUPTED_DURATION) {
            state = StarLadderUplinkState.AWAITING_CONFIRMATION;
            tickCounter = 0;
        }
    }

    private void tickActivePhase() {
        if (!machine.isFormed()) {
            failUplink();
            return;
        }

        boolean drained = drainSoul();

        if (drained) {
            progress = Math.min(MAX_PROGRESS, progress + PROGRESS_PER_TICK);
        }

        tickDemandSlot(bulkDemand);
        tickDemandSlot(complexDemand);

        if (tickCounter >= nextDemandTick) {
            if (!bulkDemand.isActive()) generateDemand(bulkDemand, getBulkPool());
            if (!complexDemand.isActive()) generateDemand(complexDemand, getComplexPool());
        }

        checkInputBusForDemands();

        if (progress >= MAX_PROGRESS) {
            advancePhase();
            return;
        }

        if (tickCounter % 60 == 0) {
            playSound(SoundEvents.WARDEN_HEARTBEAT, 0.2f, 0.6f);
        }

        sendFightWhispers();
        syncIfNeeded();
    }

    private void tickDemandSlot(DemandSlot slot) {
        if (!slot.isActive()) return;
        slot.timer++;
        if (slot.timer >= slot.timerMax) {
            progress = Math.max(0, progress - DEMAND_MISS_PENALTY);
            slot.clear();
            nextDemandTick = tickCounter + DEMAND_INTERVAL_TICKS / 2;
            playSound(SoundEvents.ANVIL_LAND, 0.3f, 0.5f);
        }
    }

    private void tickFailed() {
        if (tickCounter >= 100) {
            resetToIdle();
        }
    }

    // ---- Phase Helpers ----

    private int getSoulDrainPerSecond() {
        return switch (state) {
            case ACTIVE_PHASE_1 -> PHASE_1_SOUL_DRAIN_PER_SECOND;
            case ACTIVE_PHASE_2 -> PHASE_2_SOUL_DRAIN_PER_SECOND;
            case ACTIVE_PHASE_3 -> PHASE_3_SOUL_DRAIN_PER_SECOND;
            default -> 0;
        };
    }

    private List<DemandEntry> getBulkPool() {
        return switch (state) {
            case ACTIVE_PHASE_1 -> PHASE_1_BULK;
            case ACTIVE_PHASE_2 -> PHASE_2_BULK;
            case ACTIVE_PHASE_3 -> PHASE_3_BULK;
            default -> PHASE_1_BULK;
        };
    }

    private List<DemandEntry> getComplexPool() {
        return switch (state) {
            case ACTIVE_PHASE_1 -> PHASE_1_COMPLEX;
            case ACTIVE_PHASE_2 -> PHASE_2_COMPLEX;
            case ACTIVE_PHASE_3 -> PHASE_3_COMPLEX;
            default -> PHASE_1_COMPLEX;
        };
    }

    // ---- Soul Drain ----

    private boolean drainSoul() {
        if (!(machine.getLevel() instanceof ServerLevel serverLevel)) return false;

        UUID ownerUUID = machine.getOwnerUUID();
        if (ownerUUID == null) return false;

        var team = machine.getOwner() instanceof FTBOwner ftbOwner ? ftbOwner.getPlayerTeam(ownerUUID) : null;
        UUID networkId = team != null ? team.getTeamId() : ownerUUID;

        int drainPerTick = getSoulDrainPerSecond() / TICKS_PER_SECOND;

        SoulNetwork network = SoulNetworkSavedData.getSoulNetwork(serverLevel, networkId);
        SoulStack drainStack = new SoulStack(SoulType.Refined, drainPerTick);
        SoulStack result = network.syphon(drainStack, false);

        return result.amount() >= drainPerTick;
    }

    // ---- Hardware Demands ----

    private void generateDemand(DemandSlot slot, List<DemandEntry> pool) {
        DemandEntry entry = pool.get(RANDOM.nextInt(pool.size()));
        ItemStack demandStack = entry.itemSupplier.get();
        if (demandStack.isEmpty()) return;

        int qty = entry.minQty + RANDOM.nextInt(entry.maxQty - entry.minQty + 1);

        slot.item = demandStack.copyWithCount(1);
        slot.qty = qty;
        slot.delivered = 0;
        slot.timer = 0;
        slot.timerMax = DEMAND_TIMER_TICKS;
        demandCount++;

        playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 0.6f, 0.5f);
        syncIfNeeded();
    }

    private void clearDemands() {
        bulkDemand.clear();
        complexDemand.clear();
    }

    private void checkInputBusForDemands() {
        var itemHandlers = machine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        checkSlotAgainstBus(bulkDemand, itemHandlers);
        checkSlotAgainstBus(complexDemand, itemHandlers);
    }

    private void checkSlotAgainstBus(DemandSlot slot, List<?> itemHandlers) {
        if (!slot.isActive()) return;
        if (slot.delivered >= slot.qty) return;

        for (var handler : itemHandlers) {
            if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;

            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack slotStack = itemHandler.getStackInSlot(i);
                if (slotStack.isEmpty()) continue;
                if (!slotStack.is(slot.item.getItem())) continue;

                int needed = slot.qty - slot.delivered;
                int toTake = Math.min(slotStack.getCount(), needed);
                ItemStack extracted = itemHandler.extractItemInternal(i, toTake, false);
                if (extracted.isEmpty()) continue;
                slot.delivered += extracted.getCount();

                if (slot.delivered >= slot.qty) {
                    progress = Math.min(MAX_PROGRESS, progress + DEMAND_PROGRESS_BURST);
                    slot.clear();
                    nextDemandTick = tickCounter + DEMAND_INTERVAL_TICKS;
                    playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
                    syncIfNeeded();
                    return;
                }
            }
        }
    }

    // ---- Fight Whispers ----

    private static final String W = "cosmiccore.star_ladder.whisper.";

    private record Whisper(String key, WhisperStyle style) {}

    private static Whisper ambient(String id) {
        return new Whisper(W + "ambient." + id, WhisperStyle.AMBIENT);
    }

    private static Whisper observer(String id) {
        return new Whisper(W + "observer." + id, WhisperStyle.OBSERVER);
    }

    private static Whisper reflection(String id) {
        return new Whisper(W + "reflection." + id, WhisperStyle.REFLECTION);
    }

    private static final Whisper[] PHASE_1_WHISPERS = {
            // Ambient — machine strain, sensory
            ambient("p1.conduits_heating"),
            ambient("p1.groaning_walls"),
            ambient("p1.air_tastes_iron"),
            ambient("p1.floor_vibrating"),
            ambient("p1.low_hum"),
            ambient("p1.sparks_corner"),
            ambient("p1.lights_flicker"),
            ambient("p1.smell_of_ozone"),
            ambient("p1.dust_falling"),
            ambient("p1.pipes_rattling"),
            ambient("p1.static_on_skin"),
            ambient("p1.shadows_wrong"),
            ambient("p1.metal_ticking"),
            ambient("p1.pressure_dropping"),
            ambient("p1.something_woke_up"),
            // Observer — terse, dismissive
            observer("p1.stop"),
            observer("p1.not_here"),
            observer("p1.leave"),
            observer("p1.no"),
            // Reflection — determined
            reflection("p1.keep_feeding"),
            reflection("p1.hold"),
            reflection("p1.its_working"),
    };

    private static final Whisper[] PHASE_2_WHISPERS = {
            // Ambient — escalating, structural
            ambient("p2.structure_resonating"),
            ambient("p2.heat_distortion"),
            ambient("p2.something_cracks"),
            ambient("p2.machine_screams"),
            ambient("p2.ears_ringing"),
            ambient("p2.metal_expanding"),
            ambient("p2.temperature_climbing"),
            ambient("p2.bolts_shearing"),
            ambient("p2.gravity_hiccup"),
            ambient("p2.light_bends"),
            ambient("p2.blood_in_mouth"),
            ambient("p2.walls_humming"),
            ambient("p2.floor_buckling"),
            ambient("p2.smell_of_burning"),
            ambient("p2.time_stutters"),
            ambient("p2.vision_doubles"),
            // Observer — warning, cold
            observer("p2.dont_understand"),
            observer("p2.not_yours"),
            observer("p2.i_was_patient"),
            observer("p2.you_were_warned"),
            observer("p2.still_time"),
            observer("p2.enough"),
            // Reflection — pushing through
            reflection("p2.channel_widening"),
            reflection("p2.dont_stop"),
            reflection("p2.halfway"),
    };

    private static final Whisper[] PHASE_3_WHISPERS = {
            // Ambient — reality failing
            ambient("p3.everything_shaking"),
            ambient("p3.veil_fraying"),
            ambient("p3.light_bending_wrong"),
            ambient("p3.hands_shaking"),
            ambient("p3.walls_breathing"),
            ambient("p3.static_all_frequencies"),
            ambient("p3.reality_thins"),
            ambient("p3.colors_wrong"),
            ambient("p3.sound_from_nowhere"),
            ambient("p3.edges_dissolving"),
            ambient("p3.gravity_uncertain"),
            ambient("p3.air_tastes_of_stars"),
            ambient("p3.heartbeat_in_walls"),
            ambient("p3.ground_not_solid"),
            ambient("p3.sky_too_close"),
            ambient("p3.something_looking_back"),
            ambient("p3.tinnitus_screaming"),
            // Observer — yielding, ominous
            observer("p3.fine"),
            observer("p3.remember_this"),
            observer("p3.you_chose_this"),
            observer("p3.will_not_forget"),
            observer("p3.see_what_happens"),
            observer("p3.door_opens_both_ways"),
            observer("p3.congratulations"),
            // Reflection — final push
            reflection("p3.almost_through"),
            reflection("p3.one_more_push"),
            reflection("p3.can_feel_it"),
    };

    private void sendFightWhispers() {
        if (initiatingPlayer == null) return;
        if (tickCounter % 200 != 0) return;

        Whisper[] whispers = switch (state) {
            case ACTIVE_PHASE_2 -> PHASE_2_WHISPERS;
            case ACTIVE_PHASE_3 -> PHASE_3_WHISPERS;
            default -> PHASE_1_WHISPERS;
        };
        Whisper whisper = whispers[RANDOM.nextInt(whispers.length)];
        StarLadderUplinkPackets.sendObserverWhisper(initiatingPlayer, whisper.key, whisper.style);
    }

    // ---- State Transitions ----

    private void advancePhase() {
        switch (state) {
            case ACTIVE_PHASE_1 -> {
                state = StarLadderUplinkState.ACTIVE_PHASE_2;
                tickCounter = 0;
                progress = 0;
                clearDemands();
                nextDemandTick = FIRST_DEMAND_TICK;
                playSound(SoundEvents.WARDEN_ROAR, 0.5f, 0.7f);
                if (initiatingPlayer != null) {
                    StarLadderUplinkPackets.sendObserverWhisper(initiatingPlayer,
                            W + "transition.phase_2", WhisperStyle.AMBIENT);
                }
            }
            case ACTIVE_PHASE_2 -> {
                state = StarLadderUplinkState.ACTIVE_PHASE_3;
                tickCounter = 0;
                progress = 0;
                clearDemands();
                nextDemandTick = FIRST_DEMAND_TICK;
                playSound(SoundEvents.WARDEN_ROAR, 0.7f, 0.5f);
                if (initiatingPlayer != null) {
                    StarLadderUplinkPackets.sendObserverWhisper(initiatingPlayer,
                            W + "transition.phase_3", WhisperStyle.AMBIENT);
                }
            }
            case ACTIVE_PHASE_3 -> {
                completeUplink();
            }
            default -> {}
        }
        syncIfNeeded();
    }

    private void completeUplink() {
        state = StarLadderUplinkState.IDLE;
        tickCounter = 0;
        clearDemands();

        markTeamEstablished();

        playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
        if (initiatingPlayer != null) {
            StarLadderUplinkPackets.sendObserverWhisper(initiatingPlayer,
                    W + "transition.complete", WhisperStyle.REFLECTION);
        }
        syncIfNeeded();
    }

    private void failUplink() {
        state = StarLadderUplinkState.FAILED;
        tickCounter = 0;
        clearDemands();
        progress = 0;
        playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 0.8f, 0.6f);
        syncIfNeeded();
    }

    private void resetToIdle() {
        state = StarLadderUplinkState.IDLE;
        tickCounter = 0;
        progress = 0;
        initiatingPlayer = null;
        clearDemands();
        demandCount = 0;
        syncIfNeeded();
    }

    // ---- Team Persistence ----

    private UUID resolveTeamId() {
        UUID ownerUUID = machine.getOwnerUUID();
        if (ownerUUID == null) return null;
        if (machine.getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(ownerUUID);
            if (team != null) return team.getTeamId();
        }
        return ownerUUID;
    }

    private boolean isTeamEstablished() {
        if (!(machine.getLevel() instanceof ServerLevel serverLevel)) return false;
        UUID teamId = resolveTeamId();
        if (teamId == null) return false;
        return StarLadderSavedData.getOrCreate(serverLevel).isEstablished(teamId);
    }

    private void markTeamEstablished() {
        if (!(machine.getLevel() instanceof ServerLevel serverLevel)) return;
        UUID teamId = resolveTeamId();
        if (teamId == null) return;
        StarLadderSavedData.getOrCreate(serverLevel).setEstablished(teamId);
    }

    // ---- Sync ----

    private void syncIfNeeded() {
        if (initiatingPlayer == null) return;
        if (syncCooldown > 0 && (bulkDemand.isActive() || complexDemand.isActive())) return;

        syncToPlayer(initiatingPlayer);
        syncCooldown = 5;
    }

    private void syncToPlayer(ServerPlayer player) {
        StarLadderUplinkPackets.sendUplinkSync(player, getState(), progress,
                getSoulDrainPerSecond(), bulkDemand, complexDemand);
    }

    // ---- NBT ----

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString("state", state.name());
        tag.putInt("progress", progress);
        tag.putInt("tickCounter", tickCounter);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        try {
            state = StarLadderUplinkState.valueOf(tag.getString("state"));
        } catch (IllegalArgumentException e) {
            state = StarLadderUplinkState.IDLE;
        }
        progress = tag.getInt("progress");
        tickCounter = tag.getInt("tickCounter");

        if (state != StarLadderUplinkState.IDLE) {
            state = StarLadderUplinkState.IDLE;
            progress = 0;
            tickCounter = 0;
        }
    }

    // ---- Sound ----

    private void playSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        if (initiatingPlayer != null) {
            initiatingPlayer.playNotifySound(sound, SoundSource.BLOCKS, volume, pitch);
        }
    }

    // ---- Demand Slot ----

    public static class DemandSlot {

        public ItemStack item = ItemStack.EMPTY;
        public int qty = 0;
        public int delivered = 0;
        public int timer = 0;
        public int timerMax = 0;

        public boolean isActive() {
            return !item.isEmpty();
        }

        public int remaining() {
            return Math.max(0, qty - delivered);
        }

        public void clear() {
            item = ItemStack.EMPTY;
            qty = 0;
            delivered = 0;
            timer = 0;
            timerMax = 0;
        }
    }

    // ---- Demand Pools ----

    private record DemandEntry(Supplier<ItemStack> itemSupplier, int minQty, int maxQty) {}

    private static DemandEntry material(TagPrefix prefix, com.gregtechceu.gtceu.api.data.chemical.material.Material mat,
                                        int min, int max) {
        return new DemandEntry(() -> ChemicalHelper.get(prefix, mat), min, max);
    }

    private static DemandEntry item(Supplier<ItemStack> supplier, int min, int max) {
        return new DemandEntry(supplier, min, max);
    }

    // Phase 1: EV-tier bulk materials
    private static final List<DemandEntry> PHASE_1_BULK = List.of(
            material(TagPrefix.ingot, GTMaterials.Steel, 500, 2000),
            material(TagPrefix.ingot, GTMaterials.Aluminium, 500, 2000),
            material(TagPrefix.ingot, GTMaterials.StainlessSteel, 200, 800),
            material(TagPrefix.plate, GTMaterials.Steel, 200, 1000),
            material(TagPrefix.plate, GTMaterials.Aluminium, 200, 800),
            material(TagPrefix.dust, GTMaterials.Redstone, 1000, 4000),
            material(TagPrefix.dust, GTMaterials.Glowstone, 500, 2000),
            material(TagPrefix.gem, GTMaterials.Diamond, 64, 256),
            material(TagPrefix.ingot, GTMaterials.Gold, 200, 800),
            material(TagPrefix.dust, GTMaterials.Lapis, 500, 2000));

    // Phase 1: HV-tier complex components
    private static final List<DemandEntry> PHASE_1_COMPLEX = List.of(
            material(TagPrefix.gear, GTMaterials.StainlessSteel, 32, 512),
            material(TagPrefix.spring, CosmicMaterials.EnergeticAlloy, 32, 512),
            material(TagPrefix.plate, CosmicMaterials.PrismaticTungstensteel, 64, 512),
            material(TagPrefix.frameGt, GTMaterials.StainlessSteel, 32, 512),
            material(TagPrefix.wireFine, CosmicMaterials.EnergeticAlloy, 128, 512),
            item(CosmicItems.ADVANCED_RAM_WAFER::asStack, 64, 256),
            item(CosmicItems.ENTHEL_CPU::asStack, 16, 64),
            item(CosmicItems.ENTHELIC_PCB::asStack, 16, 64),
            item(GTItems.SMD_DIODE::asStack, 64, 256),
            item(GTItems.SMD_INDUCTOR::asStack, 64, 256));

    // Phase 2: EV-tier bulk alloys
    private static final List<DemandEntry> PHASE_2_BULK = List.of(
            material(TagPrefix.ingot, GTMaterials.TungstenSteel, 512, 2048),
            material(TagPrefix.ingot, GTMaterials.Titanium, 512, 2048),
            material(TagPrefix.ingot, GTMaterials.Chromium, 512, 2048),
            material(TagPrefix.ingot, GTMaterials.Tungsten, 512, 2048),
            material(TagPrefix.ingot, CosmicMaterials.VibrantAlloy, 512, 2048),
            material(TagPrefix.ingot, CosmicMaterials.Signalum, 512, 2048),
            material(TagPrefix.ingot, CosmicMaterials.Lumium, 512, 2048),
            material(TagPrefix.ingot, CosmicMaterials.Enderium, 512, 2048),
            material(TagPrefix.dust, GTMaterials.Platinum, 512, 2048),
            material(TagPrefix.gem, GTMaterials.Emerald, 512, 2048));

    // Phase 2: EV-tier complex components
    private static final List<DemandEntry> PHASE_2_COMPLEX = List.of(
            material(TagPrefix.gear, CosmicMaterials.VibrantAlloy, 16, 64),
            material(TagPrefix.spring, CosmicMaterials.Signalum, 32, 128),
            material(TagPrefix.plate, CosmicMaterials.Enderium, 32, 128),
            material(TagPrefix.wireFine, CosmicMaterials.Lumium, 64, 256),
            item(GTItems.LAPOTRON_CRYSTAL::asStack, 16, 64),
            item(GTItems.ENERGIUM_CRYSTAL::asStack, 16, 64),
            item(CosmicItems.EFFICACY_CHIP::asStack, 256, 512),
            item(CosmicItems.CAPACITY_CHIP::asStack, 256, 512),
            item(CosmicItems.POTENCY_CHIP::asStack, 256, 512),
            item(CosmicItems.VERBOSITY_CHIP::asStack, 256, 512));

    // Phase 3: IV-tier exotic bulk
    private static final List<DemandEntry> PHASE_3_BULK = List.of(
            material(TagPrefix.ingot, GTMaterials.Uranium235, 32, 128),
            material(TagPrefix.ingot, GTMaterials.Osmium, 50, 200),
            material(TagPrefix.ingot, GTMaterials.Plutonium239, 50, 200),
            material(TagPrefix.ingot, GTMaterials.Americium, 32, 128),
            material(TagPrefix.ingot, CosmicMaterials.Virtue, 50, 200),
            material(TagPrefix.ingot, CosmicMaterials.MelodicAlloy, 32, 128),
            material(TagPrefix.ingot, GTMaterials.Stellite100, 16, 64),
            material(TagPrefix.ingot, GTMaterials.HSSG, 50, 200),
            material(TagPrefix.ingot, CosmicMaterials.Chlorophyte, 32, 128),
            material(TagPrefix.ingot, CosmicMaterials.Halizine, 50, 200));

    // Phase 3: IV-tier complex components
    private static final List<DemandEntry> PHASE_3_COMPLEX = List.of(
            material(TagPrefix.gear, CosmicMaterials.MelodicAlloy, 8, 32),
            material(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 16, 64),
            material(TagPrefix.plate, CosmicMaterials.SolSteel, 16, 64),
            material(TagPrefix.wireFine, CosmicMaterials.VoidSpark, 32, 128),
            item(CosmicItems.LUCIDIC_PROCESSOR::asStack, 4, 16),
            item(CosmicItems.ENTHELIC_PROCESSOR_MAINFRAME::asStack, 2, 8),
            item(CosmicItems.ESCHATON_PROCESSOR::asStack, 2, 8),
            item(CosmicItems.LUCIDIC_PROCESSOR_ASSEMBLY::asStack, 2, 8),
            item(CosmicItems.DRONE_FRAME_1::asStack, 2, 8),
            item(CosmicItems.INDUSTRIAL_DRONE::asStack, 2, 8));
}
