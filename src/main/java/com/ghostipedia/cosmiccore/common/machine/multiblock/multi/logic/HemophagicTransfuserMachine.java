package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.QuintessentiaHatchPartMachine;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusDataStickLinking;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusSavedData;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HemophagicTransfuserMachine extends WorkableElectricMultiblockMachine
                                               implements IDataStickInteractable {

    public HemophagicTransfuserMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName) || !isFormed() ||
                !(getLevel() instanceof ServerLevel level))
            return;
        UUID owner = resolveCampusOwner();
        int altarLevel = detectAltarLevel();
        int resourceLimit = detectResourceLimit();
        if (owner == null || altarLevel == 0 || resourceLimit == 0) {
            invalidateStructure(substructureName);
            return;
        }
        VitaeCampusSavedData data = VitaeCampusSavedData.get(level.getServer());
        if (data.registerCore(owner, globalPosition(), altarLevel, resourceLimit) ==
                VitaeCampusSavedData.CoreResult.OWNER_MISMATCH) {
            invalidateStructure(substructureName);
            return;
        }
        data.setCoreOperational(globalPosition(), true);
    }

    @Override
    public void invalidateStructure(String substructureName) {
        if (DEFAULT_STRUCTURE.equals(substructureName) && getLevel() instanceof ServerLevel level) {
            VitaeCampusSavedData.get(level.getServer()).setCoreOperational(globalPosition(), false);
        }
        super.invalidateStructure(substructureName);
    }

    @Override
    public void onUnload() {
        if (getLevel() instanceof ServerLevel level) {
            VitaeCampusSavedData.get(level.getServer()).setCoreOperational(globalPosition(), false);
        }
        super.onUnload();
    }

    @Override
    public void onMachineDestroyed() {
        if (getLevel() instanceof ServerLevel level) {
            VitaeCampusSavedData.get(level.getServer()).removeCore(globalPosition());
        }
        super.onMachineDestroyed();
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        return VitaeCampusDataStickLinking.copy(player, dataStick, this, globalPosition(), resolveCampusOwner());
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return VitaeCampusDataStickLinking.link(player, dataStick, this);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        IntSyncValue altar = new IntSyncValue(this::currentAltarLevel);
        IntSyncValue limit = new IntSyncValue(this::currentResourceLimit);
        syncManager.syncValue("vitae_campus_altar", altar);
        syncManager.syncValue("vitae_campus_limit", limit);
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hemophagic_transfuser.status.altar", altar.getIntValue())).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hemophagic_transfuser.status.limit", limit.getIntValue())).asWidget());
        return widgets;
    }

    public GlobalPos globalPosition() {
        return GlobalPos.of(getLevel().dimension(), getBlockPos());
    }

    public @Nullable UUID resolveCampusOwner() {
        UUID owner = getOwnerUUID();
        if (owner == null) return null;
        if (getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(owner);
            if (team != null) return team.getTeamId();
        }
        return owner;
    }

    private int detectAltarLevel() {
        int hv = countInPattern(GTBlocks.MACHINE_CASING_HV.get());
        int ev = countInPattern(GTBlocks.MACHINE_CASING_EV.get());
        int iv = countInPattern(GTBlocks.MACHINE_CASING_IV.get());
        return VitaeCampusSavedData.altarLevelForCasingCounts(hv, ev, iv);
    }

    private int countInPattern(Block block) {
        int count = 0;
        for (var info : getDefaultPatternState().getCache().values()) {
            if (info.getBlockState().is(block)) count++;
        }
        return count;
    }

    private int detectResourceLimit() {
        return getParts().stream()
                .filter(QuintessentiaHatchPartMachine.class::isInstance)
                .map(QuintessentiaHatchPartMachine.class::cast)
                .mapToInt(hatch -> QuintessentiaHatchPartMachine.getMaxTransfer(hatch.getTier(),
                        com.ghostipedia.cosmiccore.api.capability.souls.SoulType.Anima))
                .findFirst().orElse(0);
    }

    private int currentAltarLevel() {
        if (!(getLevel() instanceof ServerLevel level)) return 0;
        return VitaeCampusSavedData.get(level.getServer()).accessForAnyCore(globalPosition())
                .map(VitaeCampusSavedData.Access::altarLevel).orElse(0);
    }

    private int currentResourceLimit() {
        if (!(getLevel() instanceof ServerLevel level)) return 0;
        return VitaeCampusSavedData.get(level.getServer()).accessForAnyCore(globalPosition())
                .map(VitaeCampusSavedData.Access::resourceLimit).orElse(0);
    }
}
