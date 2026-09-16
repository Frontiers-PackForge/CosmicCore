package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSavedData;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSync;
import com.ghostipedia.cosmiccore.common.machine.foundry.HephaestusCauldronStructure;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HephaestusCauldronMachine extends MultiblockControllerMachine
                                             implements IMuiMachine, ITieredMultiblockMachine {

    @SaveField(nbtKey = "structure_tier")
    @SyncToClient
    private int structureTier;

    public HephaestusCauldronMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        structureTier = TieredMultiblockPatterns.clampTier(getDefinition(), structureTier);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName) || !isFormed()) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        UUID owner = resolveCampusOwner();
        if (owner == null) return;
        var tier = selectedPattern();
        FoundryCampusSavedData.get(level.getServer()).registerCore(
                owner, globalPosition(), tier.tier(), tier.sourceAnchor());
        FoundryCampusSync.send(level, globalPosition());
    }

    @Override
    public void invalidateStructure(String substructureName) {
        if (DEFAULT_STRUCTURE.equals(substructureName) && getLevel() instanceof ServerLevel level) {
            FoundryCampusSync.clear(level, globalPosition());
        }
        super.invalidateStructure(substructureName);
    }

    @Override
    public void onUnload() {
        if (getLevel() instanceof ServerLevel level) FoundryCampusSync.clear(level, globalPosition());
        super.onUnload();
    }

    @Override
    public void onMachineDestroyed() {
        if (getLevel() instanceof ServerLevel level) {
            FoundryCampusSync.clear(level, globalPosition());
            FoundryCampusSavedData.get(level.getServer()).removeCore(globalPosition());
        }
        super.onMachineDestroyed();
    }

    @Override
    public int getStructureTier() {
        return TieredMultiblockPatterns.clampTier(getDefinition(), structureTier);
    }

    @Override
    public void setStructureTier(int tier) {
        int selectedTier = TieredMultiblockPatterns.clampTier(getDefinition(), tier);
        if (selectedTier == getStructureTier()) return;
        if (isRemote()) {
            structureTier = selectedTier;
            return;
        }

        PatternState state = getDefaultPatternState();
        if (getLevel() instanceof ServerLevel level) {
            MultiblockWorldSavedData.getOrCreate(level).removeMapping(state);
        }
        if (isFormed()) invalidateStructure();
        structureTier = selectedTier;
        markAsChanged();
        getSyncDataHolder().markClientSyncFieldDirty("structureTier");
        state.getCache().clear();
        state.clearErrors();
        state.setShouldUpdate(true);
        state.setState(PatternState.CheckState.UNINITIALIZED);
        checkAndFormStructure();
    }

    public GlobalPos globalPosition() {
        return GlobalPos.of(getLevel().dimension(), getBlockPos());
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue tier = integer(syncManager, "tier", () -> getStructureTier() + 1);
        IntSyncValue capacity = integer(syncManager, "capacity", this::capacity);
        IntSyncValue total = integer(syncManager, "total", this::totalLinked);
        IntSyncValue active = integer(syncManager, "active", this::activeLinked);
        IntSyncValue dormant = integer(syncManager, "dormant", this::dormantLinked);
        List<IWidget> lines = new ArrayList<>();
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.tier", tier.getIntValue())).asWidget());
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.capacity", capacity.getIntValue())).asWidget());
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.total", total.getIntValue())).asWidget());
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.active", active.getIntValue())).asWidget());
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.dormant", dormant.getIntValue())).asWidget());
        ListWidget<IWidget, ?> list = new ListWidget<>()
                .width(166)
                .height(88)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft)
                .left(3)
                .top(3);
        list.children(lines);
        mainWidget.size(172, 94).background(GuiTextures.DISPLAY).child(list);
    }

    private int capacity() {
        return isFormed() ? selectedPattern().tier().capacity() : 0;
    }

    private HephaestusCauldronStructure.TierPattern selectedPattern() {
        return HephaestusCauldronStructure.forStructureTier(getStructureTier());
    }

    private int totalLinked() {
        return memberships().size();
    }

    private int activeLinked() {
        return (int) memberships().stream().filter(FoundryCampusSavedData.Membership::active).count();
    }

    private int dormantLinked() {
        return totalLinked() - activeLinked();
    }

    private List<FoundryCampusSavedData.Membership> memberships() {
        if (!(getLevel() instanceof ServerLevel level)) return List.of();
        return FoundryCampusSavedData.get(level.getServer()).memberships(globalPosition());
    }

    private @Nullable UUID resolveCampusOwner() {
        UUID owner = getOwnerUUID();
        if (owner == null) return null;
        if (getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(owner);
            if (team != null) return team.getTeamId();
        }
        return owner;
    }

    private static IntSyncValue integer(PanelSyncManager syncManager, String id,
                                        java.util.function.IntSupplier supplier) {
        IntSyncValue value = new IntSyncValue(supplier, ignored -> {});
        syncManager.syncValue(id, value);
        return value;
    }
}
