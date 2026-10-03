package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusDataStickLinking;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusEnergyHandler;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusSavedData;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeCampusSoulHandler;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaePylonRecipes;
import com.ghostipedia.cosmiccore.common.machine.vitae.VitaeRenderAnchors;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ImbumentPylonMachine extends WorkableElectricMultiblockMachine implements IDataStickInteractable {

    public static final String ALTAR_TIER_KEY = "altar_tier";

    @SaveField
    private final VitaeCampusEnergyHandler campusEnergy;
    @SaveField
    private final VitaeCampusSoulHandler campusSouls;
    @SyncToClient
    private boolean campusAvailable;
    @SyncToClient
    private double sourceX;
    @SyncToClient
    private double sourceY;
    @SyncToClient
    private double sourceZ;
    @SyncToClient
    private long campusVoltage;
    @SaveField
    @SyncToClient
    private ItemStack focalItem = ItemStack.EMPTY;
    @SaveField
    @SyncToClient
    private FluidStack focalFluid = FluidStack.EMPTY;
    @SyncToClient
    private ItemStack completedItem = ItemStack.EMPTY;
    @SyncToClient
    private FluidStack completedFluid = FluidStack.EMPTY;
    @SyncToClient
    private long completedAt = Long.MIN_VALUE;
    private TickableSubscription campusSubscription;

    public ImbumentPylonMachine(BlockEntityCreationInfo info) {
        super(info, new ImbumentPylonRecipeLogic());
        campusEnergy = attachTrait(new VitaeCampusEnergyHandler(this::getEnergyContainer));
        campusSouls = new VitaeCampusSoulHandler(this);
        recipeLogic.setKeepSubscribing(true);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        campusSubscription = subscribeServerTick(campusSubscription, this::syncCampus);
    }

    @Override
    public void onUnload() {
        campusSubscription = null;
        super.onUnload();
    }

    public @Nullable HemophagicTransfuserMachine campusCore() {
        var access = access().orElse(null);
        if (access == null || !Objects.equals(access.owner(), resolveCampusOwner())) return null;
        if (!getLevel().hasChunkAt(access.core().pos())) return null;
        var machine = MetaMachine.getMachine(getLevel(), access.core().pos());
        return machine instanceof HemophagicTransfuserMachine core && core.isFormed() &&
                core.isRecipeLogicAvailable() &&
                Objects.equals(access.owner(), core.resolveCampusOwner()) ? core : null;
    }

    public Optional<VitaeCampusSavedData.Access> campusAccess() {
        return campusCore() == null ? Optional.empty() : access();
    }

    @Override
    public EnergyContainerList getEnergyContainer() {
        var core = campusCore();
        return core == null ? new EnergyContainerList(List.of()) : core.getEnergyContainer();
    }

    @Override
    public long getMaxVoltage() {
        if (isRemote()) return campusVoltage;
        var core = campusCore();
        return core == null ? 0 : core.getMaxVoltage();
    }

    @Override
    public long getOverclockVoltage() {
        return getMaxVoltage();
    }

    @Override
    public long getDisplayRecipeVoltage() {
        return getMaxVoltage();
    }

    @Override
    public int getTier() {
        return GTUtil.getFloorTierByVoltage(getMaxVoltage());
    }

    private void syncCampus() {
        var core = campusCore();
        boolean available = core != null;
        Vec3 source = available ? VitaeRenderAnchors.transfuserCube(core) : Vec3.ZERO;
        long voltage = available ? core.getMaxVoltage() : 0;
        if (campusAvailable == available && sourceX == source.x && sourceY == source.y &&
                sourceZ == source.z && campusVoltage == voltage)
            return;
        campusAvailable = available;
        sourceX = source.x;
        sourceY = source.y;
        sourceZ = source.z;
        campusVoltage = voltage;
        for (String field : List.of("campusAvailable", "sourceX", "sourceY", "sourceZ", "campusVoltage")) {
            getSyncDataHolder().markClientSyncFieldDirty(field);
        }
        recipeLogic.markLastRecipeDirty();
        recipeLogic.updateTickSubscription();
    }

    public boolean hasRenderSource() {
        return campusAvailable;
    }

    public Vec3 renderSource() {
        return new Vec3(sourceX, sourceY, sourceZ);
    }

    public ItemStack focalItem() {
        return focalItem;
    }

    public FluidStack focalFluid() {
        return focalFluid;
    }

    public ItemStack completedItem() {
        return completedItem;
    }

    public FluidStack completedFluid() {
        return completedFluid;
    }

    public long completedAt() {
        return completedAt;
    }

    public void captureInputs() {
        focalItem = recipeLogic.getConsumedInputs().getConsumedInputs(ItemRecipeCapability.CAP).stream()
                .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                .filter(stack -> !stack.isEmpty()).findFirst().map(stack -> stack.copyWithCount(1))
                .orElse(ItemStack.EMPTY);
        focalFluid = recipeLogic.getConsumedInputs().getConsumedInputs(FluidRecipeCapability.CAP).stream()
                .flatMap(ingredient -> Arrays.stream(ingredient.getFluids()))
                .filter(stack -> !stack.isEmpty()).findFirst().map(FluidStack::copy).orElse(FluidStack.EMPTY);
        getSyncDataHolder().markClientSyncFieldDirty("focalItem");
        getSyncDataHolder().markClientSyncFieldDirty("focalFluid");
        markAsChanged();
    }

    public void captureOutput(GTRecipe recipe) {
        completedItem = recipe.getOutputContents(ItemRecipeCapability.CAP).stream()
                .flatMap(content -> Arrays.stream(ItemRecipeCapability.CAP.of(content.content()).getItems()))
                .filter(stack -> !stack.isEmpty()).findFirst().map(stack -> stack.copyWithCount(1))
                .orElse(ItemStack.EMPTY);
        completedFluid = recipe.getOutputContents(FluidRecipeCapability.CAP).stream()
                .flatMap(
                        content -> Arrays.stream(FluidRecipeCapability.CAP.of(content.content()).getFluids()))
                .filter(stack -> !stack.isEmpty()).findFirst().map(FluidStack::copy).orElse(FluidStack.EMPTY);
        completedAt = getLevel().getGameTime();
        for (String field : List.of("completedItem", "completedFluid", "completedAt")) {
            getSyncDataHolder().markClientSyncFieldDirty(field);
        }
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return recipe != null && canProcessRecipe(recipe) && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        return campusAccess().isPresent() && super.onWorking();
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ImbumentPylonMachine pylon)) {
            return RecipeModifier.nullWrongType(ImbumentPylonMachine.class, machine);
        }
        if (!pylon.canProcessRecipe(recipe)) return ModifierFunction.NULL;
        int requiredTier = recipe.data.contains(ALTAR_TIER_KEY) ? recipe.data.getInt(ALTAR_TIER_KEY) : 4;
        int limit = VitaePylonRecipes.parallelLimit(pylon.campusAccess().orElseThrow().altarLevel(), requiredTier);
        limit = VitaePylonRecipes.limitByEnergy(recipe.getInputEUt(), pylon.getEnergyContainer().getEnergyStored(),
                limit);
        if (limit == 0) return ModifierFunction.NULL;
        return VitaePylonRecipes.parallel(ParallelLogic.getParallelAmountWithoutEU(pylon, recipe, limit));
    }

    public boolean canProcessRecipe(GTRecipe recipe) {
        var access = campusAccess().orElse(null);
        int requiredTier = recipe.data.contains(ALTAR_TIER_KEY) ? recipe.data.getInt(ALTAR_TIER_KEY) : 4;
        return access != null && requiredTier >= 4 && requiredTier <= 6 && requiredTier <= access.altarLevel() &&
                recipe.getInputEUt().voltage() <= getMaxVoltage();
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
        syncCampus();
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>(super.getWidgetsForDisplay(syncManager));
        BooleanSyncValue linked = new BooleanSyncValue(() -> campusAccess().isPresent());
        IntSyncValue altar = new IntSyncValue(
                () -> campusAccess().map(VitaeCampusSavedData.Access::altarLevel).orElse(0));
        IntSyncValue limit = new IntSyncValue(
                () -> campusAccess().map(VitaeCampusSavedData.Access::resourceLimit).orElse(0));
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

    private Optional<VitaeCampusSavedData.Access> access() {
        if (!isFormed() || !(getLevel() instanceof ServerLevel level)) return Optional.empty();
        return VitaeCampusSavedData.get(level.getServer()).accessFor(globalPosition());
    }
}
