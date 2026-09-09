package com.ghostipedia.cosmiccore.common.machine.transmission;

import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;
import com.ghostipedia.cosmiccore.common.transmission.me.PowerTowerMEBinding;
import com.ghostipedia.cosmiccore.common.transmission.me.PowerTowerMEPanel;
import com.ghostipedia.cosmiccore.common.transmission.me.PowerTowerMERegistry;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.items.tools.MemoryCardItem;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

public final class PowerTowerMEHatch extends MultiblockPartMachine implements IGridConnectedMachine, IMuiMachine {

    private final boolean input;
    @SaveField
    private final GridNodeHolder nodeHolder;
    @SaveField
    private CompoundTag circuitData = new CompoundTag();
    private @Nullable PowerTowerMachine tower;
    private @Nullable PowerTowerMERegistry registry;
    private boolean loaded;
    private boolean online;

    public PowerTowerMEHatch(BlockEntityCreationInfo info, boolean input) {
        super(info);
        this.input = input;
        nodeHolder = attachTrait(new GridNodeHolder(this) {

            @Override
            protected void createMainNode() {
                if (!loaded || mainNode.getNode() != null) return;
                super.createMainNode();
                if (registry != null) registry.changed();
            }
        });
        getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(getLevel() instanceof ServerLevel level)) return;
        loaded = true;
        registry = PowerTowerSavedData.getOrCreate(level).meCircuits();
        if (input) {
            UUID previous = circuitId();
            UUID claimed = registry.claimInput(previous, getBlockPos());
            if (!claimed.equals(previous)) {
                circuitData.putUUID("Id", claimed);
                setChanged();
            }
        }
        registry.register(this);
    }

    @Override
    public void onUnload() {
        loaded = false;
        tower = null;
        if (registry != null) registry.unregister(this);
        super.onUnload();
        registry = null;
    }

    @Override
    public void onMachineDestroyed() {
        loaded = false;
        tower = null;
        if (registry != null) registry.remove(this);
        super.onMachineDestroyed();
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller, String name) {
        super.addedToController(controller, name);
        if (controller instanceof PowerTowerMachine powerTower) setTower(powerTower);
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        if (controller == tower) setTower(null);
        super.removedFromController(controller);
    }

    public void setTower(@Nullable PowerTowerMachine tower) {
        if (this.tower != tower && registry != null) registry.disconnect(this);
        this.tower = tower;
        if (registry != null) registry.changed();
    }

    public boolean towerReady() {
        return loaded && !isRemoved() && tower != null && !tower.isRemoved() && tower.isFormed();
    }

    public @Nullable UUID towerId() {
        return towerReady() ? tower.getGraphNodeId() : null;
    }

    public boolean isInput() {
        return input;
    }

    public @Nullable UUID circuitId() {
        return circuitData.hasUUID("Id") ? circuitData.getUUID("Id") : null;
    }

    public @Nullable PowerTowerMEBinding identity() {
        return circuitId() == null || getLevel() == null ? null :
                new PowerTowerMEBinding(circuitId(), getLevel().dimension().location(), getBlockPos());
    }

    public @Nullable PowerTowerMEBinding binding() {
        return PowerTowerMEBinding.load(circuitData.getCompound("Binding"));
    }

    public String circuitName() {
        return circuitData.getString("Name");
    }

    public void rename(String name) {
        if (!input || isRemote()) return;
        String updated = name.substring(0, Math.min(48, name.length()));
        if (updated.equals(circuitName())) return;
        circuitData.putString("Name", updated);
        setChanged();
    }

    public void clearBinding() {
        if (input || isRemote()) return;
        circuitData.remove("Binding");
        if (registry != null) {
            registry.disconnect(this);
            registry.updateBinding(this);
        }
        setChanged();
    }

    public @Nullable PowerTowerMERegistry registry() {
        return registry;
    }

    public boolean canConfigure(Player player) {
        return loaded && !isRemoved() && player.level() == getLevel() && !player.isSpectator() &&
                player.distanceToSqr(getBlockPos().getCenter()) <= 64 && canAccess(player);
    }

    public boolean canAccess(Player player) {
        return MachineOwner.canBreakOwnerMachine(player, this) &&
                (tower == null || MachineOwner.canBreakOwnerMachine(player, tower));
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (!(context.getItemInHand().getItem() instanceof IMemoryCard)) return super.onUseWithItem(context);
        if (isRemote()) return InteractionResult.SUCCESS;
        var player = context.getPlayer();
        if (!canConfigure(player) || registry == null) return feedback(player, "denied");
        var card = context.getItemInHand();
        if (input) {
            if (!player.isShiftKeyDown()) return feedback(player, "copy_hint");
            var identity = identity();
            if (identity == null) return feedback(player, "connecting");
            MemoryCardItem.clearCard(card);
            card.set(AEComponents.EXPORTED_SETTINGS, identity.cardSettings());
            card.set(AEComponents.EXPORTED_SETTINGS_SOURCE, Component.translatable("cosmiccore.tower_me.card",
                    circuitName().isBlank() ? identity.circuit().toString() : circuitName()));
            return feedback(player, "copied");
        }
        if (player.isShiftKeyDown()) {
            clearBinding();
            return feedback(player, "cleared");
        }
        var binding = PowerTowerMEBinding.fromCard(card.getOrDefault(AEComponents.EXPORTED_SETTINGS, Map.of()));
        if (binding == null) return feedback(player, "invalid_card");
        if (!binding.dimension().equals(getLevel().dimension().location())) return feedback(player, "wrong_dimension");
        var source = registry.source(binding);
        if (source == null) return feedback(player, "input_unavailable");
        if (!source.canAccess(player)) return feedback(player, "denied");
        if (binding.equals(binding())) return feedback(player, "bound");
        registry.disconnect(this);
        circuitData.put("Binding", binding.save());
        registry.updateBinding(this);
        setChanged();
        return feedback(player, "bound");
    }

    private static InteractionResult feedback(Player player, String key) {
        player.displayClientMessage(Component.translatable("cosmiccore.tower_me." + key), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String name) {
        return false;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    @Override
    public AECableType getCableConnectionType(Direction side) {
        return side == getFrontFacing() ? AECableType.DENSE_SMART : AECableType.NONE;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public void saveChanges() {
        setChanged();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        if (loaded && registry != null) registry.changed();
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return PowerTowerMEPanel.build(this, data, syncManager);
    }
}
