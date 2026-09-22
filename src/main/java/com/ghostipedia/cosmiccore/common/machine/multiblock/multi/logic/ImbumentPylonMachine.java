package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusDataStickLinking;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusSavedData;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusSoulHandler;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ImbumentPylonMachine extends WorkableElectricMultiblockMachine implements IDataStickInteractable {

    public static final String ALTAR_TIER_KEY = "altar_tier";

    public ImbumentPylonMachine(BlockEntityCreationInfo info) {
        super(info);
        new VitaeCampusSoulHandler(this);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return access().isPresent() && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        return access().isPresent() && super.onWorking();
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ImbumentPylonMachine pylon)) {
            return RecipeModifier.nullWrongType(ImbumentPylonMachine.class, machine);
        }
        var access = pylon.access().orElse(null);
        int requiredTier = recipe.data.contains(ALTAR_TIER_KEY) ? recipe.data.getInt(ALTAR_TIER_KEY) : 4;
        if (access == null || requiredTier < 4 || requiredTier > access.altarLevel()) return ModifierFunction.NULL;
        return ModifierFunction.IDENTITY;
    }

    @Override
    public void onMachineDestroyed() {
        if (getLevel() instanceof ServerLevel level) {
            VitaeCampusSavedData.get(level.getServer()).unlink(globalPosition());
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

    public void onCampusRelinked() {
        recipeLogic.interruptRecipe();
        recipeLogic.markLastRecipeDirty();
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>(super.getWidgetsForDisplay(syncManager));
        BooleanSyncValue linked = new BooleanSyncValue(() -> access().isPresent());
        IntSyncValue altar = new IntSyncValue(() -> access().map(VitaeCampusSavedData.Access::altarLevel).orElse(0));
        IntSyncValue limit = new IntSyncValue(() -> access().map(VitaeCampusSavedData.Access::resourceLimit).orElse(0));
        syncManager.syncValue("vitae_pylon_linked", linked);
        syncManager.syncValue("vitae_pylon_altar", altar);
        syncManager.syncValue("vitae_pylon_limit", limit);
        widgets.add(Text.dynamic(() -> Component.translatable(linked.getBoolValue() ?
                "cosmiccore.machine.imbument_pylon.status.linked" :
                "cosmiccore.machine.imbument_pylon.status.unlinked")
                .withStyle(linked.getBoolValue() ? ChatFormatting.GREEN : ChatFormatting.RED)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.imbument_pylon.status.altar", altar.getIntValue())).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.imbument_pylon.status.limit", limit.getIntValue())).asWidget());
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

    private java.util.Optional<VitaeCampusSavedData.Access> access() {
        if (!isFormed() || !(getLevel() instanceof ServerLevel level)) return java.util.Optional.empty();
        return VitaeCampusSavedData.get(level.getServer()).accessFor(globalPosition());
    }
}
