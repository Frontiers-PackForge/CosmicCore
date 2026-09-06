package com.ghostipedia.cosmiccore.common.orrery;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.OrreryPackets;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ISubMenuHost;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;

import java.util.Objects;
import java.util.UUID;

public final class OrreryCraftingMenuHost extends ItemMenuHost<ComponentItem> implements ISubMenuHost, IActionHost {

    private final Locator locator;
    private final IGrid grid;
    private volatile IGridNode validatedNode;

    private OrreryCraftingMenuHost(Player player, Locator locator) {
        super(CosmicItems.LEYLINE_ORRERY.get(), player, locator);
        this.locator = locator;
        var connection = player instanceof ServerPlayer serverPlayer ?
                OrreryNetwork.connect(serverPlayer, getItemStack()) : null;
        grid = connection == null ? null : connection.grid();
        validatedNode = connection == null ? null : connection.node();
    }

    public static void register() {
        MenuLocators.register(Locator.class, (locator, buffer) -> {
            buffer.writeVarInt(locator.slot());
            buffer.writeUUID(locator.tool());
            buffer.writeGlobalPos(locator.binding());
        }, buffer -> new Locator(buffer.readVarInt(), buffer.readUUID(), buffer.readGlobalPos()));
    }

    @Override
    public boolean isValid() {
        if (isClientSide()) return super.isValid();
        if (getPlayer() instanceof ServerPlayer player && player.server.isSameThread()) {
            if (!super.isValid() || grid == null) validatedNode = null;
            else {
                var connection = OrreryNetwork.connect(player, getItemStack());
                validatedNode = connection.grid() == grid ? connection.node() : null;
            }
        }
        return validatedNode != null;
    }

    @Override
    public IGridNode getActionableNode() {
        return validatedNode;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        serverPlayer.closeContainer();
        if (!locator.locateItem(player).isEmpty())
            CCoreNetwork.sendToPlayer(serverPlayer, new OrreryPackets.OpenConfig(locator.tool()));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return getItemStack();
    }

    public static boolean allowCraft(ISubMenuHost host) {
        if (!(host instanceof OrreryCraftingMenuHost orrery) || orrery.isClientSide() || orrery.isValid()) return true;
        if (orrery.getPlayer() instanceof ServerPlayer player) {
            player.closeContainer();
            player.displayClientMessage(Component.translatable("cosmiccore.orrery.crafting_unavailable"), false);
        }
        return false;
    }

    public record Locator(int slot, UUID tool, GlobalPos binding) implements ItemMenuHostLocator {

        public Locator {
            if (slot < 0 || slot >= 41) throw new IllegalArgumentException("Invalid Orrery inventory slot");
            Objects.requireNonNull(tool);
            Objects.requireNonNull(binding);
        }

        public static Locator forHand(ServerPlayer player, InteractionHand hand) {
            var stack = player.getItemInHand(hand);
            return new Locator(Objects.requireNonNull(MenuLocators.forHand(player, hand).getPlayerInventorySlot()),
                    OrreryState.identity(stack), stack.get(AEComponents.WIRELESS_LINK_TARGET));
        }

        @Override
        public <T> T locate(Player player, Class<T> hostInterface) {
            if (locateItem(player).isEmpty()) return null;
            var host = new OrreryCraftingMenuHost(player, this);
            return hostInterface.isInstance(host) ? hostInterface.cast(host) : null;
        }

        @Override
        public ItemStack locateItem(Player player) {
            if (slot >= player.getInventory().getContainerSize()) return ItemStack.EMPTY;
            var stack = player.getInventory().getItem(slot);
            return stack.is(CosmicItems.LEYLINE_ORRERY.get()) && tool.equals(OrreryState.identity(stack)) &&
                    binding.equals(stack.get(AEComponents.WIRELESS_LINK_TARGET)) ? stack : ItemStack.EMPTY;
        }

        @Override
        public Integer getPlayerInventorySlot() {
            return slot;
        }

        @Override
        public BlockHitResult hitResult() {
            return null;
        }
    }
}
