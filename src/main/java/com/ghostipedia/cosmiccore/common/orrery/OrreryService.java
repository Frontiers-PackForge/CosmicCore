package com.ghostipedia.cosmiccore.common.orrery;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.deployment.*;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.OrreryPackets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import appeng.api.ids.AEComponents;

import java.util.*;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class OrreryService {

    private static final Map<ServerPlayer, Session> SESSIONS = new WeakHashMap<>();

    private OrreryService() {}

    public static void handle(ServerPlayer player, OrreryPackets.Request request) {
        if (!player.isAlive() || player.isSpectator()) return;
        var stack = player.getItemInHand(request.hand());
        if (!stack.is(CosmicItems.LEYLINE_ORRERY.get())) return;
        var session = SESSIONS.computeIfAbsent(player, ignored -> new Session());
        long now = player.server.getTickCount();
        if (session.packetTick != now) {
            session.packetTick = now;
            session.packets = 0;
        }
        if (++session.packets > 12) return;
        var state = OrreryState.read(stack);
        boolean query = request.action() == OrreryPackets.Action.QUERY;
        if ((!query || request.tool() != null) && !state.id.equals(request.tool())) return;
        if (request.sequence() <= session.sequence) return;
        boolean sameQuery = Objects.equals(session.tool, state.id) &&
                session.query.equals(request.text().strip().toLowerCase(Locale.ROOT)) &&
                session.sort == Math.clamp(request.bar(), 0, 2) && session.page == Math.max(0, request.value()) &&
                session.descending == request.preview() && session.browsing == request.hud();
        if (query && sameQuery && session.lastQuery != Long.MIN_VALUE && now - session.lastQuery < 4) return;
        if (query) session.lastQuery = now;
        session.sequence = request.sequence();
        if (!Objects.equals(session.tool, state.id)) {
            session.tool = state.id;
            session.proposal = null;
            session.query = "";
            session.page = 0;
        }
        var previous = state.selection();
        var connection = OrreryNetwork.connect(player, stack);
        Map<UUID, OrreryDesign> designs = connection.grid() == null ? Map.of() :
                connection.grid().getService(OrreryCatalogue.class).designs(player.serverLevel());
        switch (request.action()) {
            case QUERY -> {
                session.query = request.text().strip().toLowerCase(Locale.ROOT);
                session.sort = Math.clamp(request.bar(), 0, 2);
                session.descending = request.preview();
                session.page = Math.max(0, request.value());
                session.browsing = request.hud();
            }
            case ASSIGN -> {
                if (request.design() == null || designs.containsKey(request.design()) ||
                        contains(state, request.design()))
                    state.assign(request.bar(), request.slot(), request.design());
            }
            case SELECT -> state.select(request.bar(), request.slot());
            case RENAME -> state.rename(request.bar(), request.text());
            case SETTINGS -> {
                state.volume = Math.clamp(request.value(), 0, 100);
                state.showPreview = request.preview();
                state.showHud = request.hud();
            }
            case SWAP -> {
                if (OrreryState.valid(request.bar(), request.slot()) &&
                        OrreryState.valid(request.bar(), request.value())) {
                    var first = state.filter(request.bar(), request.slot());
                    state.assign(request.bar(), request.slot(), state.filter(request.bar(), request.value()));
                    state.assign(request.bar(), request.value(), first);
                }
            }
            case CANCEL -> session.proposal = null;
            case CRAFT -> {
                if (connection.grid() == null)
                    message(player, "status." + connection.status().name().toLowerCase(Locale.ROOT));
                else if (request.design() != null && designs.containsKey(request.design())) {
                    var output = connection.grid().getService(OrreryCatalogue.class).craftingOutput(request.design());
                    if (output == null) message(player, "pattern_missing");
                    else {
                        session.proposal = null;
                        appeng.menu.me.crafting.CraftAmountMenu.open(player,
                                OrreryCraftingMenuHost.Locator.forHand(player, request.hand()), output, 1);
                    }
                } else message(player, "unavailable_design");
            }
            case LOCK -> {
                if (session.proposal == null && state.selection() != null &&
                        !LeylineDeploymentService.isActive(player)) {
                    var hit = LeylineDeploymentTarget.trace(player, 1);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        session.proposal = new OrreryPackets.Proposal(UUID.randomUUID(), state.selection(),
                                LeylineDeploymentTarget.anchor(player, hit), hit.getDirection(),
                                player.getDirection().getOpposite());
                        session.dimension = player.level().dimension().location().toString();
                        session.hand = request.hand();
                        session.inventorySlot = player.getInventory().selected;
                    } else message(player, "no_target");
                }
            }
            case CONFIRM -> {
                var proposal = session.proposal;
                if (proposal != null && proposal.id().equals(request.design()) &&
                        proposal.design().equals(state.selection()) && validLock(player, session)) {
                    if (connection.grid() == null)
                        message(player, "status." + connection.status().name().toLowerCase(Locale.ROOT));
                    else if (LeylineDeploymentService.isActive(player)) message(player, "busy");
                    else if (proposal.anchor().distToCenterSqr(player.getEyePosition()) >
                            Math.pow(LeylineDeploymentTarget.MAX_REACH + 2, 2))
                        message(player, "too_far");
                    else {
                        var prefab = designs.containsKey(proposal.design()) ?
                                LeylineFabricationLibrary.get(player.serverLevel()).find(proposal.design()) : null;
                        if (prefab == null) message(player, "unavailable_design");
                        else if (LeylineDeploymentService.beginFromNetwork(player, request.hand(), prefab,
                                proposal.anchor(), proposal.face(), proposal.facing(),
                                stack.get(AEComponents.WIRELESS_LINK_TARGET),
                                () -> OrreryNetwork.extract(player, connection.grid(), prefab.id()))) {
                                    session.proposal = null;
                                    connection.grid().getService(OrreryCatalogue.class).invalidate();
                                    designs = connection.grid().getService(OrreryCatalogue.class)
                                            .designs(player.serverLevel());
                                }
                    }
                }
            }
        }
        if (!Objects.equals(previous, state.selection())) session.proposal = null;
        state.save(stack);
        player.inventoryMenu.broadcastChanges();
        send(player, state, session, connection.status(), designs);
    }

    private static boolean contains(OrreryState state, UUID id) {
        for (int bar = 0; bar < OrreryState.LOADOUTS; bar++)
            for (int slot = 0; slot < OrreryState.SLOTS; slot++) if (id.equals(state.filter(bar, slot))) return true;
        return false;
    }

    private static void send(ServerPlayer player, OrreryState state, Session session, OrreryNetwork.Status status,
                             Map<UUID, OrreryDesign> designs) {
        var comparator = switch (session.sort) {
            case 1 -> Comparator
                    .comparing((OrreryDesign d) -> d.machineName().getString(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(OrreryDesign::name, String.CASE_INSENSITIVE_ORDER);
            case 2 -> Comparator.comparingLong(OrreryDesign::count).thenComparing(OrreryDesign::name,
                    String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(OrreryDesign::name, String.CASE_INSENSITIVE_ORDER);
        };
        if (session.descending) comparator = comparator.reversed();
        var filtered = (session.browsing ? designs.values().stream() : java.util.stream.Stream.<OrreryDesign>empty())
                .filter(d -> session.query.isEmpty() ||
                        (d.name() + " " + d.machineName().getString() + " " + d.machine()).toLowerCase(Locale.ROOT)
                                .contains(session.query))
                .sorted(comparator.thenComparing(OrreryDesign::id)).toList();
        session.page = Math.min(session.page, Math.max(0, (filtered.size() - 1) / OrreryPackets.PAGE_SIZE));
        int start = session.page * OrreryPackets.PAGE_SIZE;
        var page = filtered.subList(start, Math.min(filtered.size(), start + OrreryPackets.PAGE_SIZE));
        var assigned = new LinkedHashMap<UUID, OrreryDesign>();
        for (int bar = 0; bar < OrreryState.LOADOUTS; bar++) for (int slot = 0; slot < OrreryState.SLOTS; slot++) {
            var id = state.filter(bar, slot);
            if (id == null || assigned.containsKey(id)) continue;
            var design = designs.get(id);
            if (design == null) {
                var prefab = LeylineFabricationLibrary.get(player.serverLevel()).find(id);
                if (prefab != null) design = OrreryDesign.of(prefab, -1, false);
            }
            if (design != null) assigned.put(id, design);
        }
        CCoreNetwork.sendToPlayer(player, new OrreryPackets.Snapshot(session.sequence, state, status,
                List.copyOf(page), List.copyOf(assigned.values()), filtered.size(), session.page, session.proposal));
    }

    private static boolean validLock(ServerPlayer player, Session session) {
        if (!player.isAlive() || !player.level().dimension().location().toString().equals(session.dimension))
            return false;
        ItemStack held = player.getItemInHand(session.hand);
        return held.is(CosmicItems.LEYLINE_ORRERY.get()) && Objects.equals(OrreryState.identity(held), session.tool) &&
                (session.hand != InteractionHand.MAIN_HAND || session.inventorySlot == player.getInventory().selected);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var session = SESSIONS.get(player);
            if (session != null && session.proposal != null && !validLock(player, session)) session.proposal = null;
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity());
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("cosmiccore.orrery." + key).withStyle(ChatFormatting.RED),
                true);
    }

    private static final class Session {

        UUID tool;
        OrreryPackets.Proposal proposal;
        String dimension = "";
        InteractionHand hand = InteractionHand.MAIN_HAND;
        int inventorySlot;
        int sequence = -1;
        int page;
        int sort;
        boolean descending;
        String query = "";
        long packetTick;
        int packets;
        long lastQuery = Long.MIN_VALUE;
        boolean browsing;
    }
}
