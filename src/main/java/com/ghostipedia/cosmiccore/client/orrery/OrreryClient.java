package com.ghostipedia.cosmiccore.client.orrery;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.gui.MajorInfoPanelRenderer;
import com.ghostipedia.cosmiccore.client.renderer.deployment.LeylinePrefabClient;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.OrreryPackets;
import com.ghostipedia.cosmiccore.common.orrery.*;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.event.level.LevelEvent;

import appeng.api.ids.AEComponents;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class OrreryClient {

    public static KeyMapping RADIAL;
    private static OrreryPackets.Snapshot snapshot;
    private static OrreryState visibleState;
    private static final Map<UUID, OrreryDesign> DESIGNS = new HashMap<>();
    private static final Map<ViewKey, CachedView> CACHED_DESIGNS = new LinkedHashMap<>(8, 0.75f, true);
    private static final Map<Integer, CatalogueQuery> QUERIES = new LinkedHashMap<>();
    private static final Set<UUID> FRESH_DESIGNS = new HashSet<>();
    private static CachedCatalogue catalogue;
    private static CatalogueQuery queryView;
    private static ViewKey viewKey;
    private static boolean awaitingRefresh;
    private static OrreryConfigScreen.View craftingReturnView;
    private static InteractionHand hand;
    private static UUID tool;
    private static int inventorySlot = -1;
    private static long ticks;
    private static long receivedAt;
    private static long queriedAt = -20;
    private static int sequence;
    private static int receivedSequence = -1;
    private static boolean useConsumed;
    private static boolean attackConsumed;
    private static boolean radialWasDown;
    private static boolean placementPending;
    private static boolean suppressLock;
    private static int latestMutation;
    private static long pendingAt;
    private static long soundAt;

    private OrreryClient() {}

    public static InteractionHand heldHand() {
        var player = Minecraft.getInstance().player;
        if (player == null) return null;
        for (var value : InteractionHand.values())
            if (player.getItemInHand(value).is(CosmicItems.LEYLINE_ORRERY.get())) return value;
        return null;
    }

    public static OrreryState state() {
        return visibleState;
    }

    public static UUID toolId() {
        return tool;
    }

    public static OrreryPackets.Snapshot snapshot() {
        return snapshot;
    }

    public static OrreryDesign design(UUID id) {
        return DESIGNS.get(id);
    }

    public static boolean live() {
        return snapshot != null && ticks - receivedAt < 50 && snapshot.status() == OrreryNetwork.Status.CONNECTED;
    }

    public static OrreryPackets.Proposal proposal() {
        return snapshot == null || suppressLock ? null : snapshot.proposal();
    }

    public static Component status() {
        var status = snapshot == null || ticks - receivedAt >= 50 ? OrreryNetwork.Status.NETWORK_UNAVAILABLE :
                snapshot.status();
        return text("status." + status.name().toLowerCase(Locale.ROOT));
    }

    public static Component text(String key, Object... args) {
        return Component.translatable("cosmiccore.orrery." + key, args);
    }

    public static String count(OrreryDesign design) {
        return !live() || design == null || !FRESH_DESIGNS.contains(design.id()) || design.count() < 0 ? "?" :
                Long.toString(design.count());
    }

    public static List<OrreryDesign> catalogue(OrreryConfigScreen.View view) {
        return catalogue != null && catalogue.query().equals(CatalogueQuery.of(view)) ? catalogue.entries() : List.of();
    }

    public static int catalogueTotal(OrreryConfigScreen.View view) {
        return catalogue != null && catalogue.query().equals(CatalogueQuery.of(view)) ? catalogue.total() : 0;
    }

    public static boolean keyDown() {
        if (RADIAL == null || RADIAL.isUnbound()) return false;
        var window = Minecraft.getInstance().getWindow().getWindow();
        return switch (RADIAL.getKey().getType()) {
            case KEYSYM -> InputConstants.isKeyDown(window, RADIAL.getKey().getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(window, RADIAL.getKey().getValue()) == GLFW.GLFW_PRESS;
            default -> RADIAL.isDown();
        };
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tick(ClientTickEvent.Pre event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        ticks++;
        if (!mc.options.keyUse.isDown()) useConsumed = false;
        if (!mc.options.keyAttack.isDown()) attackConsumed = false;
        var held = heldHand();
        UUID heldId = held == null ? null : OrreryState.identity(mc.player.getItemInHand(held));
        int slot = held == InteractionHand.MAIN_HAND ? mc.player.getInventory().selected : -1;
        var binding = held == null ? null : mc.player.getItemInHand(held).get(AEComponents.WIRELESS_LINK_TARGET);
        var heldKey = heldId == null ? null : new ViewKey(heldId, binding, mc.level.dimension().location());
        if (held != hand || slot != inventorySlot || !Objects.equals(heldKey, viewKey)) {
            if (proposal() != null && hand != null) send(OrreryPackets.Action.CANCEL, 0, 0, null, "", 0, false, false);
            cacheDesigns();
            snapshot = null;
            DESIGNS.clear();
            FRESH_DESIGNS.clear();
            QUERIES.clear();
            queryView = null;
            catalogue = null;
            viewKey = heldKey;
            var cached = viewKey == null ? null : CACHED_DESIGNS.get(viewKey);
            if (cached != null) {
                DESIGNS.putAll(cached.designs());
                catalogue = cached.catalogue();
            }
            visibleState = held == null ? null : heldId == null ? new OrreryState(new UUID(0, 0)) :
                    OrreryState.read(mc.player.getItemInHand(held));
            suppressLock = false;
            placementPending = false;
            hand = held;
            tool = heldId;
            inventorySlot = slot;
            receivedSequence = sequence + 1;
            queriedAt = ticks - 20;
            awaitingRefresh = true;
            warmSelection();
        }
        if (held == null) {
            radialWasDown = keyDown();
            return;
        }
        if (ticks - queriedAt >= (awaitingRefresh ? 4 : 20)) query();
        boolean down = keyDown();
        if (down && !radialWasDown && mc.screen == null) {
            mc.setScreen(new OrreryRadialScreen());
        }
        radialWasDown = down;
        if (placementPending && ticks - pendingAt > 100) placementPending = false;
    }

    public static void query() {
        var screen = OrreryConfigScreen.current();
        send(OrreryPackets.Action.QUERY, screen == null ? 0 : screen.sort(), 0, null,
                screen == null ? "" : screen.query(), screen == null ? 0 : screen.page(),
                screen != null && screen.descending(), screen != null);
        queriedAt = ticks;
    }

    public static void send(OrreryPackets.Action action, int bar, int slot, UUID design, String text, int value,
                            boolean preview, boolean hud) {
        if (hand == null || tool == null && action != OrreryPackets.Action.QUERY) return;
        if (action == OrreryPackets.Action.QUERY) {
            var nextView = hud ? new CatalogueQuery(text.strip().toLowerCase(Locale.ROOT), bar, value, preview) : null;
            if (!Objects.equals(nextView, queryView)) receivedSequence = sequence + 1;
            queryView = nextView;
        }
        QUERIES.put(sequence + 1, queryView);
        while (QUERIES.size() > 32) QUERIES.remove(QUERIES.keySet().iterator().next());
        if (action != OrreryPackets.Action.QUERY) latestMutation = sequence + 1;
        CCoreNetwork.sendToServer(new OrreryPackets.Request(++sequence, hand, tool, action, bar, slot,
                design, text, value, preview, hud));
    }

    public static void select(int bar, int slot) {
        if (state() == null || state().filter(bar, slot) == null) return;
        visibleState = state().copy();
        visibleState.select(bar, slot);
        suppressLock = true;
        warmSelection();
        send(OrreryPackets.Action.SELECT, bar, slot, null, "", 0, false, false);
    }

    private static void warmSelection() {
        if (visibleState != null && visibleState.selection() != null)
            LeylinePrefabClient.request(visibleState.selection());
    }

    private static void cacheDesigns() {
        if (viewKey == null || DESIGNS.isEmpty()) return;
        CACHED_DESIGNS.put(viewKey, new CachedView(Map.copyOf(DESIGNS), catalogue));
        while (CACHED_DESIGNS.size() > 8) CACHED_DESIGNS.remove(CACHED_DESIGNS.keySet().iterator().next());
    }

    public static void requestCraft(UUID design, OrreryConfigScreen.View returnView) {
        if (tool == null) return;
        craftingReturnView = returnView;
        send(OrreryPackets.Action.CRAFT, 0, 0, design, "", 0, false, false);
    }

    public static void returnToConfig(UUID expectedTool) {
        var mc = Minecraft.getInstance();
        var held = heldHand();
        if (held == null || !expectedTool.equals(OrreryState.identity(mc.player.getItemInHand(held)))) return;
        brachy.modularui.factory.ClientGUI.open(new OrreryConfigScreen(craftingReturnView));
        craftingReturnView = null;
        awaitingRefresh = true;
        queriedAt = ticks - 20;
    }

    public static void receive(OrreryPackets.Snapshot packet) {
        var mc = Minecraft.getInstance();
        if (heldHand() != hand || hand == null || packet.sequence() < receivedSequence ||
                packet.sequence() < latestMutation ||
                tool != null && !tool.equals(packet.state().id))
            return;
        if (hand == InteractionHand.MAIN_HAND && inventorySlot != mc.player.getInventory().selected) return;
        if (viewKey != null && !Objects.equals(viewKey.binding(),
                mc.player.getItemInHand(hand).get(AEComponents.WIRELESS_LINK_TARGET)))
            return;
        UUID heldId = OrreryState.identity(mc.player.getItemInHand(hand));
        if (heldId != null && !heldId.equals(packet.state().id)) return;
        tool = packet.state().id;
        snapshot = packet;
        visibleState = packet.state();
        viewKey = new ViewKey(tool, mc.player.getItemInHand(hand).get(AEComponents.WIRELESS_LINK_TARGET),
                mc.level.dimension().location());
        receivedSequence = packet.sequence();
        receivedAt = ticks;
        awaitingRefresh = false;
        placementPending = false;
        suppressLock = false;
        var responseView = QUERIES.get(packet.sequence());
        QUERIES.keySet().removeIf(sequence -> sequence <= packet.sequence());
        if (responseView != null) catalogue = new CachedCatalogue(new CatalogueQuery(responseView.search(),
                responseView.sort(), packet.pageIndex(), responseView.descending()), packet.page(), packet.total());
        var assigned = new HashSet<UUID>();
        for (int bar = 0; bar < OrreryState.LOADOUTS; bar++)
            for (int slot = 0; slot < OrreryState.SLOTS; slot++) assigned.add(visibleState.filter(bar, slot));
        DESIGNS.keySet().retainAll(assigned);
        FRESH_DESIGNS.clear();
        packet.assigned().forEach(d -> FRESH_DESIGNS.add(d.id()));
        packet.page().forEach(d -> FRESH_DESIGNS.add(d.id()));
        packet.assigned().forEach(d -> DESIGNS.put(d.id(), d));
        packet.page().forEach(d -> DESIGNS.put(d.id(), d));
        cacheDesigns();
        warmSelection();
        var config = OrreryConfigScreen.current();
        if (config != null) config.received();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        var mc = Minecraft.getInstance();
        if (event.isAttack() && attackConsumed && mc.screen == null) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        if (mc.screen != null || heldHand() == null) return;
        if (event.isAttack() && (proposal() != null || placementPending || attackConsumed)) {
            event.setCanceled(true);
            event.setSwingHand(false);
            if (attackConsumed) return;
            attackConsumed = true;
            suppressLock = true;
            placementPending = false;
            send(OrreryPackets.Action.CANCEL, 0, 0, null, "", 0, false, false);
            sound(Feedback.DISMISS);
            return;
        }
        if (!event.isUseItem()) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if (useConsumed) return;
        useConsumed = true;
        if (mc.player.isShiftKeyDown()) {
            brachy.modularui.factory.ClientGUI.open(new OrreryConfigScreen());
        } else if (state() != null && !placementPending) {
            var proposal = proposal();
            if (state().selection() == null) mc.player.displayClientMessage(text("choose"), true);
            else {
                placementPending = true;
                pendingAt = ticks;
                send(proposal == null ? OrreryPackets.Action.LOCK : OrreryPackets.Action.CONFIRM,
                        0, 0, proposal == null ? null : proposal.id(), "", 0, false, false);
            }
        }
    }

    @SubscribeEvent
    public static void hud(RenderGuiLayerEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR) || mc.screen != null || heldHand() == null ||
                state() == null || !state().showHud)
            return;
        var design = design(state().selection());
        var g = event.getGuiGraphics();
        int x = 18, y = g.guiHeight() - 92;
        Component title = design == null ? text("choose") : Component.literal(design.name());
        int width = Math.min(280, Math.max(190, mc.font.width(title) + 18));
        MajorInfoPanelRenderer.draw(g, x - 10, y - 10, width + 20, 55);
        g.drawString(mc.font, mc.font.plainSubstrByWidth(title.getString(), width - 8), x, y, 0xFFE8F1EF);
        g.drawString(mc.font, live() ? text("charges", count(design)) : status(), x, y + 12, 0xFFB2CAD0);
        g.drawString(mc.font, text(proposal() == null ? "aim_hint" : "lock_hint"), x, y + 25, 0xFFE3AC70);
    }

    @SubscribeEvent
    public static void crosshair(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.CROSSHAIR) &&
                Minecraft.getInstance().screen instanceof OrreryRadialScreen)
            event.setCanceled(true);
    }

    public enum Feedback {
        OPEN,
        WEDGE,
        BAR,
        SELECT,
        DISMISS
    }

    public static void sound(Feedback feedback) {
        if (state() == null || state().volume == 0) return;
        long now = net.minecraft.Util.getMillis();
        if ((feedback == Feedback.WEDGE || feedback == Feedback.BAR) && now - soundAt < 85) return;
        soundAt = now;
        SoundEvent sound = switch (feedback) {
            case OPEN -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case WEDGE -> SoundEvents.UI_BUTTON_CLICK.value();
            case BAR -> SoundEvents.BOOK_PAGE_TURN;
            case SELECT -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case DISMISS -> SoundEvents.UI_BUTTON_CLICK.value();
        };
        float pitch = switch (feedback) {
            case WEDGE -> 1.7f;
            case DISMISS -> 0.8f;
            default -> 1.25f;
        };
        float gain = feedback == Feedback.WEDGE ? 0.12f : 0.3f;
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(sound, pitch, gain * state().volume / 100f));
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        snapshot = null;
        visibleState = null;
        DESIGNS.clear();
        CACHED_DESIGNS.clear();
        QUERIES.clear();
        FRESH_DESIGNS.clear();
        catalogue = null;
        queryView = null;
        viewKey = null;
        craftingReturnView = null;
        awaitingRefresh = false;
        hand = null;
        tool = null;
        sequence = 0;
        receivedSequence = -1;
        placementPending = suppressLock = useConsumed = radialWasDown = attackConsumed = false;
        latestMutation = 0;
        inventorySlot = -1;
        ticks = receivedAt = 0;
        queriedAt = -20;
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;
        snapshot = null;
        visibleState = null;
        DESIGNS.clear();
        CACHED_DESIGNS.clear();
        QUERIES.clear();
        FRESH_DESIGNS.clear();
        catalogue = null;
        queryView = null;
        viewKey = null;
        hand = null;
        inventorySlot = -1;
        suppressLock = true;
        placementPending = false;
        craftingReturnView = null;
    }

    private record ViewKey(UUID tool, GlobalPos binding, ResourceLocation dimension) {}

    private record CachedView(Map<UUID, OrreryDesign> designs, CachedCatalogue catalogue) {}

    private record CachedCatalogue(CatalogueQuery query, List<OrreryDesign> entries, int total) {}

    private record CatalogueQuery(String search, int sort, int page, boolean descending) {

        private static CatalogueQuery of(OrreryConfigScreen.View view) {
            return new CatalogueQuery(view.search().strip().toLowerCase(Locale.ROOT), view.sort(), view.page(),
                    view.descending());
        }
    }

    @EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Keys {

        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            RADIAL = new KeyMapping("key.cosmiccore.orrery", new IKeyConflictContext() {

                @Override
                public boolean isActive() {
                    return heldHand() != null && (Minecraft.getInstance().screen == null ||
                            Minecraft.getInstance().screen instanceof OrreryRadialScreen);
                }

                @Override
                public boolean conflicts(IKeyConflictContext other) {
                    return other == this;
                }
            }, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.cosmiccore.orrery");
            event.register(RADIAL);
        }
    }
}
