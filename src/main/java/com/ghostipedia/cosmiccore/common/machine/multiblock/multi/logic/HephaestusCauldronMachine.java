package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSavedData;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSync;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryDataStickLinking;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryPyrofluxPolicy;
import com.ghostipedia.cosmiccore.common.machine.foundry.HephaestusCauldronStructure;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HephaestusCauldronMachine extends WorkableElectricMultiblockMachine
                                             implements IDataStickInteractable, ITieredMultiblockMachine {

    @SaveField(nbtKey = "structure_tier")
    @SyncToClient
    private int structureTier;
    @SaveField
    private int generationProgress;
    @SaveField
    private int generationParallel;
    private TickableSubscription generationSubscription;
    private GenerationState generationState = GenerationState.UNFORMED;

    public HephaestusCauldronMachine(BlockEntityCreationInfo info) {
        super(info, new HephaestusCauldronRecipeLogic());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        structureTier = TieredMultiblockPatterns.clampTier(getDefinition(), structureTier);
        generationSubscription = subscribeServerTick(generationSubscription, this::generationTick);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName) || !isFormed()) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        UUID owner = resolveCampusOwner();
        if (owner == null) return;
        var tier = selectedPattern();
        FoundryCampusSavedData data = FoundryCampusSavedData.get(level.getServer());
        data.registerCore(owner, globalPosition(), tier.tier(), tier.sourceAnchor());
        data.setCoreOperational(globalPosition(), true, getMaxVoltage());
        FoundryCampusSync.send(level, globalPosition());
    }

    @Override
    public void invalidateStructure(String substructureName) {
        if (DEFAULT_STRUCTURE.equals(substructureName) && getLevel() instanceof ServerLevel level) {
            FoundryCampusSavedData.get(level.getServer()).setCoreOperational(globalPosition(), false, 0);
            FoundryCampusSync.clear(level, globalPosition());
        }
        super.invalidateStructure(substructureName);
    }

    @Override
    public void onUnload() {
        if (getLevel() instanceof ServerLevel level) {
            FoundryCampusSavedData.get(level.getServer()).setCoreOperational(globalPosition(), false, 0);
            FoundryCampusSync.clear(level, globalPosition());
        }
        generationSubscription = null;
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

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        UUID owner = resolveCampusOwner();
        return FoundryDataStickLinking.copy(player, dataStick, this, globalPosition(), owner);
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return FoundryDataStickLinking.link(player, dataStick, this);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue tier = integer(syncManager, "tier", () -> getStructureTier() + 1);
        IntSyncValue capacity = integer(syncManager, "capacity", this::capacity);
        IntSyncValue active = integer(syncManager, "active", this::activeLinked);
        IntSyncValue dormant = integer(syncManager, "dormant", this::dormantLinked);
        IntSyncValue rate = integer(syncManager, "rate", this::generationRate);
        IntSyncValue state = integer(syncManager, "state", () -> generationState.ordinal());
        LongSyncValue stored = longValue(syncManager, "stored", this::storedPyroflux);
        LongSyncValue chargeCapacity = longValue(syncManager, "charge_capacity", this::pyrofluxCapacity);
        LongSyncValue allocated = longValue(syncManager, "allocated", this::allocatedPyroflux);
        LongSyncValue demanded = longValue(syncManager, "demanded", this::demandedPyroflux);
        List<IWidget> lines = new ArrayList<>();
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.tier", tier.getIntValue())).asWidget().color(0xFFFFFF));
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.pyroflux",
                stored.getLongValue(), chargeCapacity.getLongValue())).asWidget().color(0xFFFFFF));
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.generation",
                rate.getIntValue(), generationState(state.getIntValue()).translationKey()))
                .asWidget().color(0xFFFFFF));
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.allocation",
                allocated.getLongValue(), demanded.getLongValue())).asWidget().color(0xFFFFFF));
        lines.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.machine.hephaestus_cauldron.status.links",
                active.getIntValue(), capacity.getIntValue(), dormant.getIntValue())).asWidget().color(0xFFFFFF));
        ListWidget<IWidget, ?> list = new ListWidget<>()
                .width(166)
                .height(130)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft)
                .left(3)
                .top(3);
        list.children(lines);
        mainWidget.size(172, 136).background(GuiTextures.DISPLAY).child(list);
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

    private void generationTick() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            setGenerationState(GenerationState.UNFORMED);
            return;
        }
        FoundryCampusSavedData data = FoundryCampusSavedData.get(level.getServer());
        data.setCoreOperational(globalPosition(), true, getMaxVoltage());
        if (generationParallel <= 0) {
            startGenerationBatch(data);
            return;
        }
        long output = FoundryPyrofluxPolicy.CHARGE_PER_BATCH * generationParallel;
        if (generationProgress + 1 >= FoundryPyrofluxPolicy.GENERATION_DURATION &&
                data.pyrofluxCapacity(globalPosition()) - data.storedPyroflux(globalPosition()) < output) {
            setGenerationState(GenerationState.STORAGE_FULL);
            return;
        }
        long eut = FoundryPyrofluxPolicy.GENERATION_EUT * generationParallel;
        EnergyContainerList energy = energyContainer == null ? getEnergyContainer() : energyContainer;
        if (energy.getEnergyStored() < eut || energy.removeEnergy(eut) < eut) {
            setGenerationState(GenerationState.NO_POWER);
            return;
        }
        setGenerationState(GenerationState.GENERATING);
        generationProgress++;
        if (generationProgress >= FoundryPyrofluxPolicy.GENERATION_DURATION) {
            data.addPyroflux(globalPosition(), output);
            generationProgress = 0;
            generationParallel = 0;
            markAsChanged();
        }
    }

    private void startGenerationBatch(FoundryCampusSavedData data) {
        int maximum = FoundryPyrofluxPolicy.generationParallel(selectedPattern().tier(), getMaxVoltage());
        if (maximum <= 0) {
            setGenerationState(GenerationState.NO_POWER);
            return;
        }
        long room = data.pyrofluxCapacity(globalPosition()) - data.storedPyroflux(globalPosition());
        maximum = Math.min(maximum, (int) (room / FoundryPyrofluxPolicy.CHARGE_PER_BATCH));
        if (maximum <= 0) {
            setGenerationState(GenerationState.STORAGE_FULL);
            return;
        }
        while (maximum > 0 && !handleFluidInput(maximum, true)) maximum--;
        if (maximum <= 0) {
            setGenerationState(GenerationState.NO_LIQUOR);
            return;
        }
        while (maximum > 0 && !handleStoneInput(maximum, true)) maximum--;
        if (maximum <= 0) {
            setGenerationState(GenerationState.NO_STONE);
            return;
        }
        int parallel = maximum;
        long eut = FoundryPyrofluxPolicy.GENERATION_EUT * parallel;
        EnergyContainerList energy = energyContainer == null ? getEnergyContainer() : energyContainer;
        if (energy.getEnergyStored() < eut) {
            setGenerationState(GenerationState.NO_POWER);
            return;
        }
        if (!handleFluidInput(parallel, true) || !handleStoneInput(parallel, true)) {
            setGenerationState(GenerationState.INPUT_CHANGED);
            return;
        }
        if (!handleFluidInput(parallel, false) || !handleStoneInput(parallel, false)) {
            setGenerationState(GenerationState.INPUT_CHANGED);
            return;
        }
        generationParallel = parallel;
        generationProgress = 0;
        setGenerationState(GenerationState.GENERATING);
        markAsChanged();
    }

    private void setGenerationState(GenerationState state) {
        generationState = state;
        ((HephaestusCauldronRecipeLogic) getRecipeLogic()).setGenerating(state == GenerationState.GENERATING);
    }

    private boolean handleFluidInput(int parallel, boolean simulate) {
        int amount = FoundryPyrofluxPolicy.LIQUOR_PER_BATCH * parallel;
        return handleInput(FluidRecipeCapability.CAP,
                SizedFluidIngredient.of(CosmicMaterials.AcidicWoodLiquor.getFluid(amount)), simulate);
    }

    private boolean handleStoneInput(int parallel, boolean simulate) {
        int amount = FoundryPyrofluxPolicy.STONE_DUST_PER_BATCH * parallel;
        ItemStack stoneDust = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Stone);
        return handleInput(ItemRecipeCapability.CAP, new SizedIngredient(Ingredient.of(stoneDust), amount), simulate);
    }

    private boolean handleInput(RecipeCapability<?> capability, Object ingredient, boolean simulate) {
        List<?> left = List.of(ingredient);
        for (var handler : getCapabilitiesFlat(IO.IN, capability)) {
            left = handler.handleRecipe(IO.IN, null, left, simulate);
            if (left.isEmpty()) return true;
        }
        return false;
    }

    private int capacity() {
        return isFormed() ? selectedPattern().tier().capacity() : 0;
    }

    private HephaestusCauldronStructure.TierPattern selectedPattern() {
        return HephaestusCauldronStructure.forStructureTier(getStructureTier());
    }

    private int activeLinked() {
        return (int) memberships().stream().filter(FoundryCampusSavedData.Membership::active).count();
    }

    private int dormantLinked() {
        return memberships().size() - activeLinked();
    }

    private List<FoundryCampusSavedData.Membership> memberships() {
        if (!(getLevel() instanceof ServerLevel level)) return List.of();
        return FoundryCampusSavedData.get(level.getServer()).memberships(globalPosition());
    }

    private long storedPyroflux() {
        if (!(getLevel() instanceof ServerLevel level)) return 0;
        return FoundryCampusSavedData.get(level.getServer()).storedPyroflux(globalPosition());
    }

    private long pyrofluxCapacity() {
        if (!(getLevel() instanceof ServerLevel level)) return 0;
        return FoundryCampusSavedData.get(level.getServer()).pyrofluxCapacity(globalPosition());
    }

    private long allocatedPyroflux() {
        if (!(getLevel() instanceof ServerLevel level)) return 0;
        return FoundryCampusSavedData.get(level.getServer()).allocatedPyroflux(globalPosition());
    }

    private long demandedPyroflux() {
        if (!(getLevel() instanceof ServerLevel level)) return 0;
        return FoundryCampusSavedData.get(level.getServer()).demandedPyroflux(globalPosition());
    }

    private int generationRate() {
        return generationState == GenerationState.GENERATING ?
                (int) (FoundryPyrofluxPolicy.CHARGE_PER_BATCH * generationParallel /
                        FoundryPyrofluxPolicy.GENERATION_DURATION) :
                0;
    }

    private static IntSyncValue integer(PanelSyncManager syncManager, String id,
                                        java.util.function.IntSupplier supplier) {
        IntSyncValue value = new IntSyncValue(supplier, ignored -> {});
        syncManager.syncValue(id, value);
        return value;
    }

    private static LongSyncValue longValue(PanelSyncManager syncManager, String id,
                                           java.util.function.LongSupplier supplier) {
        LongSyncValue value = new LongSyncValue(supplier, ignored -> {});
        syncManager.syncValue(id, value);
        return value;
    }

    private static GenerationState generationState(int ordinal) {
        return GenerationState.values()[Math.floorMod(ordinal, GenerationState.values().length)];
    }

    private enum GenerationState {

        UNFORMED("cosmiccore.machine.hephaestus_cauldron.state.unformed"),
        GENERATING("cosmiccore.machine.hephaestus_cauldron.state.generating"),
        NO_POWER("cosmiccore.machine.hephaestus_cauldron.state.no_power"),
        NO_LIQUOR("cosmiccore.machine.hephaestus_cauldron.state.no_liquor"),
        NO_STONE("cosmiccore.machine.hephaestus_cauldron.state.no_stone"),
        STORAGE_FULL("cosmiccore.machine.hephaestus_cauldron.state.storage_full"),
        INPUT_CHANGED("cosmiccore.machine.hephaestus_cauldron.state.input_changed");

        private final String translationKey;

        GenerationState(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component translationKey() {
            return Component.translatable(translationKey);
        }
    }
}
