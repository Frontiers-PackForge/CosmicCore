package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.foundry.AlloyBlastingKilnStructure;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSavedData;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSync;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryDataStickLinking;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryFurnaceEndpoint;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryPyrofluxPolicy;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryRenderAnchor;
import com.ghostipedia.cosmiccore.common.machine.multiblock.PhysicalRecipeParallel;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AlloyBlastingKilnMachine extends CoilWorkableElectricMultiblockMachine
                                            implements FoundryFurnaceEndpoint, IDataStickInteractable {

    public static final int MAX_PARALLEL = 4;
    private static final UUID UNOWNED = new UUID(0, 0);
    private boolean unloading;

    public AlloyBlastingKilnMachine(BlockEntityCreationInfo info) {
        super(info, new AlloyBlastingKilnRecipeLogic());
    }

    @Override
    public void onLoad() {
        unloading = false;
        super.onLoad();
    }

    @Override
    public void onUnload() {
        unloading = true;
        notifyCampus();
        super.onUnload();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        notifyCampus();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        notifyCampus();
    }

    @Override
    public void onMachineDestroyed() {
        unloading = true;
        releasePyrofluxReservation();
        notifyCampus();
        super.onMachineDestroyed();
    }

    @Override
    public UUID foundryOwner() {
        UUID owner = getOwnerUUID();
        if (owner == null) return UNOWNED;
        if (getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(owner);
            if (team != null) return team.getTeamId();
        }
        return owner;
    }

    @Override
    public ResourceLocation foundryFurnaceType() {
        return CosmicCore.id("alloy_blasting_kiln");
    }

    @Override
    public FoundryRenderAnchor foundryReceivingAnchor() {
        return AlloyBlastingKilnStructure.receivingAnchor();
    }

    @Override
    public boolean isFoundryStructureFormed() {
        return isFormed() && !unloading;
    }

    @Override
    public boolean isFoundryWorking() {
        return getRecipeLogic().isWorking();
    }

    public boolean canRunFoundryRecipe() {
        if (!(getLevel() instanceof ServerLevel level)) return false;
        return FoundryCampusSavedData.get(level.getServer()).canOperate(globalPosition(), this);
    }

    public boolean reservePyroflux(GTRecipe recipe) {
        if (!(getLevel() instanceof ServerLevel level) || !canRunFoundryRecipe()) return false;
        return FoundryCampusSavedData.get(level.getServer()).reservePyroflux(
                globalPosition(), FoundryPyrofluxPolicy.demand(recipe));
    }

    public boolean ensureRunningReservation(GTRecipe recipe, int progress, int duration) {
        if (!(getLevel() instanceof ServerLevel level) || !canRunFoundryRecipe()) return false;
        FoundryCampusSavedData data = FoundryCampusSavedData.get(level.getServer());
        return data.hasReservation(globalPosition()) || data.restorePyrofluxReservation(
                globalPosition(), FoundryPyrofluxPolicy.demand(recipe), progress, duration);
    }

    public boolean consumePyrofluxForProgress(int progress, int duration) {
        if (!(getLevel() instanceof ServerLevel level)) return false;
        return FoundryCampusSavedData.get(level.getServer())
                .consumePyrofluxForProgress(globalPosition(), progress, duration);
    }

    public void completePyrofluxReservation() {
        if (getLevel() instanceof ServerLevel level) {
            FoundryCampusSavedData.get(level.getServer()).completeReservation(globalPosition());
        }
    }

    public void releasePyrofluxReservation() {
        if (getLevel() instanceof ServerLevel level) {
            FoundryCampusSavedData.get(level.getServer()).releaseReservation(globalPosition());
        }
    }

    public static int boundedParallel(int availableParallel) {
        return Math.max(0, Math.min(MAX_PARALLEL, availableParallel));
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof AlloyBlastingKilnMachine kiln)) {
            return com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
                    .nullWrongType(AlloyBlastingKilnMachine.class, machine);
        }
        if (!kiln.canRunFoundryRecipe()) return ModifierFunction.NULL;
        int parallel = PhysicalRecipeParallel.highestMatchingWithoutEnergy(kiln, recipe, MAX_PARALLEL);
        if (parallel < 1) return ModifierFunction.NULL;
        if (parallel == 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel))
                .parallels(parallel)
                .build();
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        return FoundryDataStickLinking.copy(player, dataStick, this, globalPosition(), foundryOwner());
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return FoundryDataStickLinking.link(player, dataStick, this);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>(super.getWidgetsForDisplay(syncManager));
        BooleanSyncValue linked = new BooleanSyncValue(this::isLinked);
        LongSyncValue available = new LongSyncValue(this::availablePyroflux);
        IntSyncValue temperature = new IntSyncValue(this::maximumTemperature);
        IntSyncValue stall = new IntSyncValue(() -> stallReason().ordinal());
        syncManager.syncValue("foundry_kiln_linked", linked);
        syncManager.syncValue("foundry_kiln_pyroflux", available);
        syncManager.syncValue("foundry_kiln_temperature", temperature);
        syncManager.syncValue("foundry_kiln_stall", stall);
        widgets.add(Text.dynamic(() -> Component.translatable(
                "gtceu.multiblock.blast_furnace.max_temperature",
                Component.literal(temperature.getIntValue() + "K").withStyle(ChatFormatting.RED))).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                linked.getBoolValue() ? "cosmiccore.machine.alloy_blasting_kiln.status.linked" :
                        "cosmiccore.machine.alloy_blasting_kiln.status.unlinked")
                .withStyle(linked.getBoolValue() ? ChatFormatting.GREEN : ChatFormatting.RED)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.alloy_blasting_kiln.status.pyroflux", available.getLongValue())
                .withStyle(available.getLongValue() > 0 ? ChatFormatting.AQUA : ChatFormatting.GOLD)).asWidget());
        widgets.add(Text.dynamic(() -> {
            StallReason reason = StallReason.values()[Math.floorMod(stall.getIntValue(), StallReason.values().length)];
            return Component.translatable(reason.translationKey)
                    .withStyle(reason == StallReason.READY ? ChatFormatting.GREEN : ChatFormatting.RED);
        }).asWidget());
        return widgets;
    }

    public GlobalPos globalPosition() {
        return GlobalPos.of(getLevel().dimension(), getBlockPos());
    }

    private boolean isLinked() {
        if (!(getLevel() instanceof ServerLevel level)) return false;
        return FoundryCampusSavedData.get(level.getServer()).coreFor(globalPosition()).isPresent();
    }

    private long availablePyroflux() {
        if (!(getLevel() instanceof ServerLevel level)) return 0;
        return FoundryCampusSavedData.get(level.getServer()).storedPyrofluxFor(globalPosition());
    }

    private int maximumTemperature() {
        return FoundryPyrofluxPolicy.maximumTemperature(getCoilType().getCoilTemperature(), getTier());
    }

    private StallReason stallReason() {
        if (!(getLevel() instanceof ServerLevel level)) return StallReason.CORE_UNAVAILABLE;
        FoundryCampusSavedData data = FoundryCampusSavedData.get(level.getServer());
        if (data.coreFor(globalPosition()).isEmpty()) return StallReason.UNLINKED;
        if (!data.isActive(globalPosition())) return StallReason.DORMANT;
        if (!data.coreOperationalFor(globalPosition())) return StallReason.CORE_UNAVAILABLE;
        if (!data.hasReservation(globalPosition()) && data.storedPyrofluxFor(globalPosition()) <= 0) {
            return StallReason.NO_PYROFLUX;
        }
        return StallReason.READY;
    }

    private void notifyCampus() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        FoundryCampusSavedData.get(level.getServer()).coreFor(globalPosition())
                .ifPresent(core -> FoundryCampusSync.send(level, core));
    }

    private enum StallReason {

        READY("cosmiccore.machine.alloy_blasting_kiln.status.ready"),
        UNLINKED("cosmiccore.machine.alloy_blasting_kiln.status.unlinked"),
        DORMANT("cosmiccore.machine.alloy_blasting_kiln.status.dormant"),
        CORE_UNAVAILABLE("cosmiccore.machine.alloy_blasting_kiln.status.core_unavailable"),
        NO_PYROFLUX("cosmiccore.machine.alloy_blasting_kiln.status.no_pyroflux");

        private final String translationKey;

        StallReason(String translationKey) {
            this.translationKey = translationKey;
        }
    }
}
