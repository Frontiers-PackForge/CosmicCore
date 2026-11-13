package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.aeroponics.AeroponicsCupGrid;
import com.ghostipedia.cosmiccore.common.block.AeroponicsCropHolderBlock.Planter;
import com.ghostipedia.cosmiccore.common.block.AeroponicsCropHolderBlock.SeedlingPlants;
import com.ghostipedia.cosmiccore.common.blockentity.AeroponicsCropHolderBlockEntity;
import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.api.mui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.network.PacketDistributor;

import brachy.modularui.api.UIFactory;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.GuiManager;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.network.packets.CloseAllGuisPacket;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.PagedWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public final class AeroponicsTower extends MultiblockControllerMachine implements IMuiMachine {

    private static final int OVERVIEW_WIDTH = 316;

    public static final MachineEntry<MultiblockMachineDefinition> AEROPONICS_TOWER = REGISTRATE
            .multiblock("aeroponics_tower", AeroponicsTower::new)
            .langValue("Aeroponics Tower")
            .tooltipBuilder((stack, tooltip) -> {
                tooltip.add(
                        Component.translatable("cosmiccore.aeroponics.tooltip.title").withStyle(ChatFormatting.GRAY));
                if (!GTUtil.isShiftDown()) {
                    tooltip.add(Component.translatable("cosmiccore.aeroponics.tooltip.shift")
                            .withStyle(ChatFormatting.GRAY));
                    return;
                }
                for (var line : List.of("planting", "resources", "quality")) {
                    tooltip.add(Component.translatable("cosmiccore.aeroponics.tooltip." + line)
                            .withStyle(ChatFormatting.GRAY));
                }
            })
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .allowFlip(false)
            .appearanceBlock(VERDIGRIS_NOSTIUM_CASING)
            .partAppearance((controller, part, side) -> VERDIGRIS_NOSTIUM_CASING.getDefaultState())
            .pattern(definition -> pattern(definition, RotationState.NON_Y_AXIS.defaultDirection))
            .workableCasingModel(CosmicCore.id("block/casings/solid/verdigris_nostium_casing"),
                    CosmicCore.id("block/multiblock/mixing_vessel"))
            .register();

    private Direction patternFacing;
    private IBlockPattern facingPattern;
    @SaveField
    private final Farm farm = new Farm();
    private static final int[] PILLAR_SIDE = { -1, 1, 2, 2, 1, -1, -2, -2 };
    private static final int[] PILLAR_DEPTH = { 0, 0, 1, 3, 4, 4, 3, 1 };
    public static final ResourceLocation NUTRIENT = CosmicCore.id("world_dew");
    private final AeroponicsCropHolderBlockEntity[] holders = new AeroponicsCropHolderBlockEntity[40];
    private final Runnable plantingChanged = () -> rosterDirty = true;
    private final List<IRecipeHandler<?>> nutrientInputs = new ArrayList<>();
    private final List<IRecipeHandler<?>> fertilizerInputs = new ArrayList<>();
    private final List<IRecipeHandler<?>> itemOutputs = new ArrayList<>();
    private final List<ISubscription> outputSubscriptions = new ArrayList<>();
    private boolean outputChanged;
    private TickableSubscription subscription;
    private boolean rosterDirty = true;
    private int catalogRevision = -1;
    private double lastSpeed;
    @SyncToClient
    private boolean active;
    private Object workingSound;
    private double lastNutrient;
    private double lastFertilizer;
    private String limiting = "idle";
    private CompoundTag panelRoster = new CompoundTag();
    private CompoundTag panelStats = new CompoundTag();

    public AeroponicsTower(BlockEntityCreationInfo info) {
        super(info);
    }

    private static IBlockPattern pattern(MultiblockMachineDefinition definition, Direction front) {
        var holder = AEROPONICS_CROP_HOLDER.getDefaultState();
        var facing = HorizontalDirectionalBlock.FACING;
        return MultiblockPatternBuilder.start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                .startOffset(OriginOffset.of(RelativeDirection.RIGHT, 2))
                .slice(" AGA ", " F F ", " F F ", " F F ", " F F ", " F F ", " AAA ")
                .slice("AAAAA", "IDEDB", "IDEDB", "IDEDB", "IDEDB", "IDEDB", "AAAAA")
                .slice("AAAAA", " EHE ", " EHE ", " EHE ", " EHE ", " EHE ", "AAAAA")
                .slice("AAAAA", "IDEDB", "IDEDB", "IDEDB", "IDEDB", "IDEDB", "AAAAA")
                .slice(" AAA ", " C C ", " C C ", " C C ", " C C ", " C C ", " AAA ")
                .where(' ', any())
                .where('A', blocks(VERDIGRIS_NOSTIUM_CASING.get()).or(abilities(PartAbility.IMPORT_FLUIDS,
                        PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS, PartAbility.INPUT_ENERGY)))
                .where('B',
                        states(holder.setValue(facing, RelativeDirection.LEFT.getRelativeFacing(front, Direction.UP))))
                .where('C', states(holder.setValue(facing, front.getOpposite())))
                .where('D', blocks(LIGHTWEIGHT_DARK_STEEL_CASING.get()))
                .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, CosmicMaterials.Nostium)))
                .where('F', states(holder.setValue(facing, front)))
                .where('G', controller(definition))
                .where('H', blocks(VIBRANT_PIPE_FRAMEWORK.get()))
                .where('I',
                        states(holder.setValue(facing, RelativeDirection.RIGHT.getRelativeFacing(front, Direction.UP))))
                .build();
    }

    @Override
    public IBlockPattern getDefaultStructurePattern() {
        Direction front = getFrontFacing();
        if (facingPattern == null || patternFacing != front) {
            facingPattern = pattern(getDefinition(), front);
            patternFacing = front;
        }
        return facingPattern;
    }

    @Override
    public IBlockPattern getSubstructurePattern(String name) {
        return DEFAULT_STRUCTURE.equals(name) ? getDefaultStructurePattern() : super.getSubstructurePattern(name);
    }

    @Override
    public Map<String, IBlockPattern> getStructurePatterns() {
        return Map.of(DEFAULT_STRUCTURE, getDefaultStructurePattern());
    }

    @Override
    public void formStructure(String name) {
        super.formStructure(name);
        if (!isFormed() || !(getLevel() instanceof ServerLevel)) return;
        disconnect();
        for (var part : getParts()) {
            for (var list : part.getRecipeHandlers()) {
                if (list.getHandlerIO().support(IO.IN)) {
                    nutrientInputs.addAll(list.getCapability(FluidRecipeCapability.CAP));
                    fertilizerInputs.addAll(list.getCapability(ItemRecipeCapability.CAP));
                }
                if (list.getHandlerIO().support(IO.OUT) && !list.getCapability(ItemRecipeCapability.CAP).isEmpty()) {
                    itemOutputs.addAll(list.getCapability(ItemRecipeCapability.CAP));
                    outputSubscriptions.add(list.subscribe(() -> outputChanged = true, ItemRecipeCapability.CAP));
                }
            }
        }
        subscription = subscribeServerTick(subscription, this::grow);
    }

    private boolean bindHolders() {
        var front = getFrontFacing();
        for (int layer = 0; layer < 5; layer++) {
            for (int pillar = 0; pillar < 8; pillar++) {
                int index = layer * 8 + pillar;
                BlockPos pos = getBlockPos().above(layer + 1).relative(front.getOpposite(), PILLAR_DEPTH[pillar])
                        .relative(front.getClockWise(), PILLAR_SIDE[pillar]);
                if (!getLevel().isLoaded(pos) ||
                        !(getLevel().getBlockEntity(pos) instanceof AeroponicsCropHolderBlockEntity holder))
                    return false;
                if (holders[index] != holder) {
                    if (holders[index] != null) holders[index].removePlantingListener(plantingChanged);
                    holders[index] = holder;
                    holder.setPlantingListener(plantingChanged);
                }
            }
        }
        return true;
    }

    private void disconnect() {
        setActive(false);
        if (subscription != null) subscription.unsubscribe();
        subscription = null;
        for (var holder : holders) {
            if (holder != null) holder.removePlantingListener(plantingChanged);
        }
        Arrays.fill(holders, null);
        nutrientInputs.clear();
        fertilizerInputs.clear();
        itemOutputs.clear();
        for (var listener : outputSubscriptions) listener.unsubscribe();
        outputSubscriptions.clear();
        outputChanged = true;
        rosterDirty = true;
        lastSpeed = lastNutrient = lastFertilizer = 0;
        limiting = "idle";
        panelRoster = new CompoundTag();
        panelStats = farmStats();
    }

    @Override
    public void invalidateStructure(String name) {
        disconnect();
        super.invalidateStructure(name);
    }

    @Override
    public void onUnload() {
        disconnect();
        if (isRemote()) updateSound();
        super.onUnload();
    }

    public boolean isActive() {
        return active;
    }

    private void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, active ?
                com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic.Status.WORKING :
                com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic.Status.IDLE));
        if (!isRemote()) getSyncDataHolder().markClientSyncFieldDirty("active");
    }

    @OnlyIn(Dist.CLIENT)
    @ClientFieldChangeListener(fieldName = "active")
    private void updateSound() {
        if (active && ConfigHolder.INSTANCE.machines.machineSounds) {
            if (workingSound instanceof AutoReleasedSound sound && !sound.isStopped()) return;
            workingSound = GTSoundEntries.BATH.get().playAutoReleasedSound(
                    () -> active && ConfigHolder.INSTANCE.machines.machineSounds && !isRemoved() &&
                            getLevel().isLoaded(getBlockPos()) &&
                            MetaMachine.getMachine(getLevel(), getBlockPos()) == this,
                    getBlockPos(), true, 0, 1, 1);
        } else if (workingSound instanceof AutoReleasedSound sound) {
            sound.release();
            workingSound = null;
        }
    }

    @Override
    public void onMachineDestroyed() {
        if (getLevel() instanceof ServerLevel level) {
            for (var batch : farm.pending.values()) {
                for (var stack : batch.stacks) Containers.dropItemStack(level, getBlockPos().getX() + 0.5,
                        getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, stack.copy());
            }
            farm.pending.clear();
        }
        super.onMachineDestroyed();
    }

    private void grow() {
        if (getOffsetTimer() % 20 != 0 || !isFormed()) return;
        if (rosterDirty || catalogRevision != AeroponicsCrops.INSTANCE.revision ||
                com.ghostipedia.cosmiccore.common.data.CosmicMutations.REGISTRY.stream()
                        .flatMap(mutation -> mutation.traits().stream()).anyMatch(trait -> trait.requiresRefresh())) {
            if (!bindHolders()) {
                setActive(false);
                return;
            }
            var planted = new LinkedHashMap<Planting, AeroponicsCrops.Cycle>();
            for (var holder : holders) {
                for (var slot : Planter.ALL) {
                    var seed = holder.getSeedling(slot);
                    if (seed.isEmpty()) continue;
                    var cycle = AeroponicsCrops.INSTANCE.resolve(seed, this);
                    if (cycle != null) planted.put(new Planting(holder.getBlockPos().asLong(), slot.ordinal(),
                            holder.plantingId(slot)), cycle);
                }
            }
            farm.reconcile(planted);
            rosterDirty = false;
            catalogRevision = AeroponicsCrops.INSTANCE.revision;
            farm.layoutChanged = true;
            markAsChanged();
        }

        if (!farm.pending.isEmpty() && (outputChanged || getOffsetTimer() % 100 == 0)) flushHarvests();
        outputChanged = false;
        double previousNutrient = farm.nutrient, previousFertilizer = farm.fertilizer;
        double availableNutrient = farm.nutrient + pullNutrient(farm.topUp(farm.nutrientRate, farm.nutrient), true);
        double availableFertilizer = farm.fertilizer +
                pullFertilizer(farm.topUp(farm.fertilizerRate, farm.fertilizer), true);
        double funded = Math.min(1, Math.min(farm.nutrientRate == 0 ? 1 : availableNutrient / farm.nutrientRate,
                farm.fertilizerRate == 0 ? 1 : availableFertilizer / farm.fertilizerRate));
        farm.nutrient += pullNutrient(farm.topUp(farm.nutrientRate * funded, farm.nutrient), false);
        farm.fertilizer += pullFertilizer(farm.topUp(farm.fertilizerRate * funded, farm.fertilizer), false);
        double beforeNutrient = farm.nutrient, beforeFertilizer = farm.fertilizer;
        boolean nutrientLimited = farm.nutrientRate > 0 && (farm.fertilizerRate == 0 ||
                availableNutrient * farm.fertilizerRate <= availableFertilizer * farm.nutrientRate);
        lastSpeed = farm.advance(this::flushHarvests);
        setActive(lastSpeed > 0);
        lastNutrient = beforeNutrient - farm.nutrient;
        lastFertilizer = beforeFertilizer - farm.fertilizer;
        limiting = farm.growing.isEmpty() ? (farm.pending.isEmpty() ? "idle" : "output") :
                lastSpeed >= 0.999999 ? "running" : nutrientLimited ? "nutrient" : "fertilizer";
        if (farm.layoutChanged) {
            updatePanelRoster();
            farm.layoutChanged = false;
        }
        panelStats = farmStats();
        if (lastSpeed > 0 || previousNutrient != farm.nutrient || previousFertilizer != farm.fertilizer)
            markAsChanged();
    }

    private int pullNutrient(int amount, boolean simulate) {
        if (amount == 0) return 0;
        var fluid = BuiltInRegistries.FLUID.getOptional(NUTRIENT);
        if (fluid.isEmpty()) return 0;
        var input = SizedFluidIngredient.of(new FluidStack(fluid.get(), amount));
        var left = Farm.transfer(nutrientInputs, IO.IN, List.of(input), simulate);
        return amount - (left.isEmpty() ? 0 : ((SizedFluidIngredient) left.getFirst()).amount());
    }

    public record Fertilizer(ItemEntry<?> item, int points) {

        public static final List<Fertilizer> TIERS = List.of(new Fertilizer(GTItems.FERTILIZER, 16),
                new Fertilizer(CosmicItems.FERTILIZER_T2, 32), new Fertilizer(CosmicItems.FERTILIZER_T3, 64));
    }

    private long pullFertilizer(int amount, boolean simulate) {
        long supplied = 0;
        for (var tier : Fertilizer.TIERS.reversed()) {
            if (supplied >= amount) break;
            int count = Math.ceilDiv(amount - (int) supplied, tier.points());
            var input = new SizedIngredient(Ingredient.of(tier.item().get()), count);
            var left = Farm.transfer(fertilizerInputs, IO.IN, List.of(input), simulate);
            int taken = count - (left.isEmpty() ? 0 : ((SizedIngredient) left.getFirst()).count());
            supplied += (long) taken * tier.points();
        }
        return supplied;
    }

    private void flushHarvests() {
        if (itemOutputs.isEmpty()) return;
        if (farm.flush(itemOutputs)) markAsChanged();
    }

    private void updatePanelRoster() {
        var view = new CompoundTag();
        for (int i = 0; i < holders.length; i++) {
            var holder = holders[i];
            var layer = view.getCompound(Integer.toString(i / 8));
            for (var slot : Planter.ALL) {
                var seed = holder.getSeedling(slot);
                if (seed.isEmpty()) continue;
                var key = new Planting(holder.getBlockPos().asLong(), slot.ordinal(), holder.plantingId(slot));
                var row = new CompoundTag();
                row.putString("seed", BuiltInRegistries.ITEM.getKey(seed.getItem()).toString());
                row.putByte("quality", (byte) QualityFoodCompat.level(seed));
                var cycle = farm.planted.get(key);
                if (cycle != null) {
                    var rates = cycle.rates();
                    row.putDouble("duration", rates.duration());
                    row.putDouble("nutrient", rates.nutrient());
                    row.putDouble("fertilizer", rates.fertilizer());
                    row.putBoolean("ready", farm.pending.containsKey(key));
                    var deadline = farm.growing.get(key);
                    if (deadline != null) row.putDouble("due", deadline.due);
                }
                layer.put(Integer.toString((i % 8) * 4 + slot.ordinal()), row);
            }
            view.put(Integer.toString(i / 8), layer);
        }
        panelRoster = view;
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager sync, UISettings settings) {
        var panel = ModularPanel.defaultPanel("aeroponics_tower", OVERVIEW_WIDTH, 240)
                .background(GTGuiTextures.BACKGROUND);
        addViewSwitch(panel, sync, data, true);
        panel.child(Flow.row().decoration().top(4).height(12).childPadding(4)
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .child(new ItemDrawable(getDefinition().asStack()).asWidget().size(12))
                .child(Text.lang(getDefinition().getDescriptionId()).asWidget()));
        var time = new DoubleSyncValue(() -> farm.clock);
        sync.syncValue("clock", time);
        var stats = GenericSyncValue.<ByteBuf, CompoundTag>builder(CompoundTag.class).getter(() -> panelStats)
                .adapter(ByteBufAdapters.NBT).equalsDefault().copyImmutable().build();
        sync.syncValue("farm", stats);
        List<Supplier<CompoundTag>> layers = new ArrayList<>(5);
        for (int level = 0; level < 5; level++) {
            String layerKey = Integer.toString(level);
            var layer = GenericSyncValue.<ByteBuf, CompoundTag>builder(CompoundTag.class)
                    .getter(() -> panelRoster.getCompound(layerKey)).adapter(ByteBufAdapters.NBT).equalsDefault()
                    .copyImmutable().build();
            sync.syncValue("layer", level, layer);
            layers.add(layer::getValue);
        }
        if (sync.isClient()) {
            panel.child(new AeroponicsCupGrid(layers, row -> progress(row, time.getDoubleValue()),
                    row -> cupDetails(row, time.getDoubleValue())).pos(7, 18));
        }
        for (int i = 0; i < 5; i++) {
            int line = i;
            panel.child(new TextWidget<>(Text.dynamic(() -> statsLine(stats.getValue(), line)))
                    .pos(8, 189 + i * 9).size(OVERVIEW_WIDTH - 16, 9).scale(0.85f).color(0x404040)
                    .textAlign(Alignment.CenterLeft));
        }
        return panel;
    }

    private void addViewSwitch(ModularPanel<?> panel, PanelSyncManager sync, PosGuiData data, boolean toPlanting) {
        closePreviousTowerView(data, toPlanting ? "aeroponics_planting" : "aeroponics_tower");
        sync.registerServerSyncedAction("switch_view", buf -> {
            if (!(data.getPlayer() instanceof ServerPlayer player) || player.containerMenu != sync.getContainer() ||
                    !player.containerMenu.stillValid(player) || isRemoved())
                return;
            if (toPlanting) {
                if (isFormed() && player.mayBuild() && bindHolders())
                    GuiManager.open(PlantingFactory.INSTANCE, data, player);
            } else {
                MachineUIFactory.INSTANCE.open(player, this);
            }
        });
        panel.child(new ButtonWidget<>().right(5).top(4).size(70, 12)
                .setEnabledIf(w -> !toPlanting || (isFormed() && data.getPlayer().mayBuild()))
                .overlay(Text.lang("cosmiccore.aeroponics." + (toPlanting ? "management" : "overview")).scale(0.75f))
                .onMousePressed((context, button) -> {
                    if (button != 0) return false;
                    sync.callSyncedAction("switch_view");
                    return true;
                }));
    }

    private static void closePreviousTowerView(PosGuiData destination, String previousPanel) {
        if (!(destination.getPlayer() instanceof ServerPlayer player) ||
                !(player.containerMenu instanceof ModularContainerMenu menu) ||
                !(menu.getGuiData() instanceof PosGuiData previous) ||
                !previous.getBlockPos().equals(destination.getBlockPos()) ||
                !previousPanel.equals(menu.getSyncManager().getMainPSM().getPanelName()))
            return;
        PacketDistributor.sendToPlayer(player, CloseAllGuisPacket.INSTANCE);
        ModularNetwork.SERVER.closeAll(player, false);
    }

    public enum PlantingFactory implements UIFactory<PosGuiData> {

        INSTANCE;

        @Override
        public ResourceLocation getFactoryName() {
            return CosmicCore.id("aeroponics_planting");
        }

        @Override
        public ModularPanel<?> createPanel(PosGuiData data, PanelSyncManager sync, UISettings settings) {
            var panel = ModularPanel.defaultPanel("aeroponics_planting", PlantManagement.WIDTH, 240)
                    .background(GTGuiTextures.BACKGROUND);
            if (data.getLevel().isLoaded(data.getBlockPos()) &&
                    MachineUIFactory.getMachine(data) instanceof AeroponicsTower tower) {
                settings.canInteractWith(player -> !tower.isRemoved() && tower.isFormed() && player.mayBuild() &&
                        MachineUIFactory.INSTANCE.canInteractWith(player, data));
                tower.new PlantManagement(data).build(panel, sync, settings);
            } else {
                settings.canInteractWith(player -> false);
            }
            return panel;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ModularScreen createScreen(PosGuiData data, ModularPanel<?> panel) {
            return new GTGuiScreen(panel);
        }

        @Override
        public void writeGuiData(PosGuiData data, RegistryFriendlyByteBuf buffer) {
            MachineUIFactory.INSTANCE.writeGuiData(data, buffer);
        }

        @Override
        public PosGuiData readGuiData(Player player, RegistryFriendlyByteBuf buffer) {
            return MachineUIFactory.INSTANCE.readGuiData(player, buffer);
        }
    }

    final class PlantManagement implements IItemHandlerModifiable {

        private static final int WIDTH = 192, LAYERS = 5, PER_LAYER = 32;
        private final PosGuiData data;
        private final ItemStackHandler clientSlots;
        private int layer;
        private int lastTransfer;

        private PlantManagement(PosGuiData data) {
            this.data = data;
            clientSlots = data.isClient() ? new ItemStackHandler(getSlots()) : null;
        }

        private void build(ModularPanel<?> panel, PanelSyncManager sync, UISettings settings) {
            settings.customContainer(PlantingMenu::new);
            addViewSwitch(panel, sync, data, false);
            panel.child(Text.lang("cosmiccore.aeroponics.management").asWidget().pos(8, 5));
            var layerButtons = new ParentWidget<>().horizontalCenter().top(19).size(156, 16);
            var pages = new PagedWidget<>().horizontalCenter().top(39).size(156, 76);
            sync.registerSyncedAction("layer", buf -> {
                int selected = buf.readVarInt();
                if (selected < 0 || selected >= LAYERS) return;
                layer = selected;
                pages.setPage(layer);
                lastTransfer = 0;
            });
            for (int i = 0; i < LAYERS; i++) {
                int selected = i;
                layerButtons.child(new ButtonWidget<>().pos(32 * i, 0).size(28, 16)
                        .overlay(Text.str(Integer.toString(i + 1)))
                        .tooltipBuilder(tip -> tip.addLine(Text.lang("cosmiccore.aeroponics.layer", selected + 1)))
                        .onMousePressed((context, button) -> {
                            if (button != 0) return false;
                            sync.callSyncedAction("layer", buf -> buf.writeVarInt(selected));
                            return true;
                        }));
                var page = new ParentWidget<>().sizeRel(1f);
                for (int cup = 0; cup < PER_LAYER; cup++) {
                    int pillar = cup / 4;
                    int x = pillar % 4 * 40 + cup % 2 * 18;
                    int y = pillar / 4 * 40 + (cup % 4 < 2 ? 18 : 0);
                    page.child(new ItemSlot().slot(new ModularSlot(this, i * PER_LAYER + cup))
                            .background(GTGuiTextures.SLOT).pos(x, y));
                }
                pages.addPage(page);
            }
            panel.child(layerButtons).child(pages);
            panel.child(Text.dynamic(() -> Component.translatable("cosmiccore.aeroponics.layer", layer + 1))
                    .asWidget().pos(8, 118));
            panel.child(Text.dynamic(() -> {
                int planted = 0;
                for (int i = 0; i < PER_LAYER; i++) {
                    if (!getStackInSlot(layer * PER_LAYER + i).isEmpty()) planted++;
                }
                return Component.translatable("cosmiccore.aeroponics.layer_count", planted, PER_LAYER);
            }).asWidget().pos(8, 129)
                    .tooltipBuilder(tip -> tip.addLine(Text.lang("cosmiccore.aeroponics.transfer_hint"))));
            var moved = new IntSyncValue(() -> lastTransfer);
            sync.syncValue("transferred", moved);
            panel.child(Text.dynamic(() -> {
                int count = moved.getIntValue();
                return count == 0 ? Component.empty() :
                        Component.translatable("cosmiccore.aeroponics." + (count > 0 ? "planted" : "returned"),
                                Math.abs(count));
            }).asWidget().pos(8, 142));
            panel.child(SlotGroupWidget.playerInventory(7, true, (index, slot) -> slot.background(GTGuiTextures.SLOT)));
        }

        private AeroponicsCropHolderBlockEntity holder(int index) {
            var holder = holders[index / 4];
            return holder != null && !holder.isRemoved() && isFormed() ? holder : null;
        }

        @Override
        public int getSlots() {
            return PER_LAYER * LAYERS;
        }

        @Override
        public int getSlotLimit(int index) {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            if (clientSlots != null) return clientSlots.getStackInSlot(index);
            var holder = holder(index);
            return holder == null ? ItemStack.EMPTY : holder.getSeedling(Planter.ALL.get(index % 4));
        }

        @Override
        public boolean isItemValid(int index, ItemStack stack) {
            return (clientSlots != null || holder(index) != null) && getStackInSlot(index).isEmpty() &&
                    SeedlingPlants.canPlant(stack);
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack) {
            if (clientSlots != null) clientSlots.setStackInSlot(index, stack);
            else if (stack.isEmpty()) extractItem(index, 1, false);
            else insertItem(index, stack, false);
        }

        @Override
        public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
            if (!isItemValid(index, stack)) return stack;
            if (!simulate) {
                if (clientSlots != null) clientSlots.setStackInSlot(index, stack.copyWithCount(1));
                else if (!holder(index).plant(Planter.ALL.get(index % 4), stack)) return stack;
            }
            return stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int index, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;
            if (clientSlots != null) return clientSlots.extractItem(index, amount, simulate);
            var holder = holder(index);
            if (holder == null) return ItemStack.EMPTY;
            var planter = Planter.ALL.get(index % 4);
            return simulate ? holder.getSeedling(planter).copy() : holder.removeSeedling(planter);
        }

        static ItemStack plantStack(IItemHandlerModifiable cups, ItemStack seed, int layer) {
            int start = layer * PER_LAYER;
            return ItemHandlerHelper.insertItem(new RangedWrapper(cups, start, start + PER_LAYER), seed, false);
        }

        static int takeSeed(IItemHandlerModifiable cups, IItemHandler inventory, int clicked) {
            var cup = new RangedWrapper(cups, clicked, clicked + 1);
            return GTTransferUtils.transferItemsFiltered(cup, inventory, stack -> true, 1);
        }

        private final class PlantingMenu extends ModularContainerMenu {

            private PlantingMenu(int id) {
                super(id);
            }

            @Override
            public void clicked(int slotId, int button, ClickType type, Player player) {
                if (slotId >= 0 && slotId < slots.size()) {
                    var slot = getModularSlot(slotId);
                    if (!isVisible(slot)) return;
                    if (type == ClickType.QUICK_MOVE && (button == 0 || button == 1)) {
                        if (slot.mayPickup(player)) quickMoveStack(player, slotId);
                        broadcastChanges();
                        return;
                    }
                    if (slot.getItemHandler() == PlantManagement.this) {
                        if (type == ClickType.PICKUP && slot.hasItem() && !getCarried().isEmpty()) return;
                        superClicked(slotId, button, type, player);
                        return;
                    }
                }
                super.clicked(slotId, button, type, player);
            }

            @Override
            public boolean canDragTo(Slot slot) {
                return isVisible(slot) && super.canDragTo(slot);
            }

            @Override
            public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
                return isVisible(slot) && super.canTakeItemForPickAll(stack, slot);
            }

            private boolean isVisible(Slot slot) {
                return !(slot instanceof ModularSlot modular) || modular.getItemHandler() != PlantManagement.this ||
                        modular.getSlotIndex() / PER_LAYER == layer;
            }

            @Override
            public ItemStack quickMoveStack(Player player, int slotId) {
                var slot = getModularSlot(slotId);
                var seed = slot.getItem();
                if (seed.isEmpty() || !isVisible(slot)) return ItemStack.EMPTY;
                if (slot.getItemHandler() == PlantManagement.this) {
                    lastTransfer = -takeSeed(PlantManagement.this, new PlayerMainInvWrapper(player.getInventory()),
                            slot.getSlotIndex());
                } else if (ModularSlot.isPlayerSlot(slot) && SeedlingPlants.canPlant(seed)) {
                    var remaining = plantStack(PlantManagement.this, seed, layer);
                    lastTransfer = seed.getCount() - remaining.getCount();
                    slot.set(remaining);
                } else {
                    return ItemStack.EMPTY;
                }
                return seed.copyWithCount(Math.abs(lastTransfer));
            }
        }
    }

    private CompoundTag farmStats() {
        var tag = new CompoundTag();
        tag.putString("state", isFormed() && subscription != null ? limiting : "unformed");
        tag.putInt("speed", (int) Math.round(lastSpeed * 100));
        tag.putInt("growing", farm.growing.size());
        tag.putInt("pending", farm.pending.size());
        tag.putDouble("nutrient", lastNutrient * 60);
        tag.putDouble("fertilizer", lastFertilizer * 60);
        tag.putDouble("nutrientDemand", farm.nutrientRate * 60);
        tag.putDouble("fertilizerDemand", farm.fertilizerRate * 60);
        tag.putDouble("yield", farm.itemRate * 60 * lastSpeed);
        return tag;
    }

    private static int progress(CompoundTag cell, double clock) {
        return cell.getBoolean("ready") ? 100 :
                (int) Math.clamp(100 * (1 - (cell.getDouble("due") - clock) / cell.getDouble("duration")), 0, 99);
    }

    private static Component cupDetails(CompoundTag cell, double clock) {
        if (!cell.contains("seed")) return Component.translatable("cosmiccore.aeroponics.empty");
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(cell.getString("seed"))).getDescription();
        if (!cell.contains("duration")) return Component.translatable("cosmiccore.aeroponics.unsupported", item);
        if (cell.getBoolean("ready")) return Component.translatable("cosmiccore.aeroponics.ready", item);
        return Component.translatable("cosmiccore.aeroponics.progress", item, progress(cell, clock),
                displayNumber(cell.getDouble("duration")), displayNumber(cell.getDouble("nutrient")),
                displayNumber(cell.getDouble("fertilizer")));
    }

    private static String displayNumber(double value) {
        return FormattingUtil.formatNumbers(Math.round(value * 100) / 100.0);
    }

    private static Component statsLine(CompoundTag stats, int line) {
        return switch (line) {
            case 0 -> Component.translatable("cosmiccore.aeroponics.speed", stats.getInt("speed"),
                    Component.translatable("cosmiccore.aeroponics.state." +
                            (stats.contains("state") ? stats.getString("state") : "idle")));
            case 1 -> Component.translatable("cosmiccore.aeroponics.counts", stats.getInt("growing"),
                    stats.getInt("pending"));
            case 2 -> Component.translatable("cosmiccore.aeroponics.nutrient_rate",
                    displayNumber(stats.getDouble("nutrient")), displayNumber(stats.getDouble("nutrientDemand")));
            case 3 -> Component.translatable("cosmiccore.aeroponics.fertilizer_rate",
                    displayNumber(stats.getDouble("fertilizer")), displayNumber(stats.getDouble("fertilizerDemand")));
            default -> Component.translatable("cosmiccore.aeroponics.item_rate",
                    displayNumber(stats.getDouble("yield")));
        };
    }

    record Planting(long position, int slot, UUID id) {

        CompoundTag save() {
            var tag = new CompoundTag();
            tag.putLong("pos", position);
            tag.putInt("slot", slot);
            tag.putUUID("planting", id);
            return tag;
        }

        static Planting load(CompoundTag tag) {
            return new Planting(tag.getLong("pos"), tag.getInt("slot"), tag.getUUID("planting"));
        }
    }

    static final class Batch {

        final List<ItemStack> stacks;
        final List<SizedIngredient> ingredients;
        private final int hash;

        Batch(List<ItemStack> stacks) {
            this.stacks = stacks.stream().map(ItemStack::copy).toList();
            this.ingredients = this.stacks.stream()
                    .map(stack -> new SizedIngredient(DataComponentIngredient.of(true, stack), stack.getCount()))
                    .toList();
            this.hash = this.stacks.stream()
                    .mapToInt(stack -> 31 * ItemStack.hashItemAndComponents(stack) + stack.getCount())
                    .reduce(1, (a, b) -> 31 * a + b);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Batch batch && ItemStack.listMatches(stacks, batch.stacks);
        }
    }

    private record Deadline(double due, double duration) {}

    static final class Farm implements INBTSerializable<CompoundTag> {

        double clock, nutrient, fertilizer;
        double nutrientRate, fertilizerRate, itemRate, nextDue = Double.POSITIVE_INFINITY;
        boolean layoutChanged;
        Map<Planting, AeroponicsCrops.Cycle> planted = Map.of();
        final Map<Planting, Deadline> growing = new HashMap<>();
        final Map<Planting, Batch> pending = new LinkedHashMap<>();

        int topUp(double rate, double available) {
            return (int) Math.ceil(Math.max(0, rate - available - 1e-10));
        }

        void reconcile(Map<Planting, AeroponicsCrops.Cycle> current) {
            planted = current;
            growing.keySet().retainAll(current.keySet());
            current.forEach((key, cycle) -> {
                var previous = growing.get(key);
                double duration = cycle.rates().duration();
                if (previous == null) {
                    if (!pending.containsKey(key)) start(key);
                } else if (duration != previous.duration) {
                    growing.put(key, new Deadline(
                            clock + Math.clamp((previous.due - clock) / previous.duration, 0, 1) * duration, duration));
                }
            });
            recount();
        }

        void start(Planting key) {
            var cycle = planted.get(key);
            if (cycle != null) {
                double duration = cycle.rates().duration();
                growing.put(key, new Deadline(clock + duration, duration));
            }
        }

        void recount() {
            nutrientRate = fertilizerRate = itemRate = 0;
            nextDue = Double.POSITIVE_INFINITY;
            growing.forEach((key, deadline) -> {
                var cycle = planted.get(key);
                if (!cycle.requirements().isEmpty()) return;
                var rate = cycle.rates();
                nutrientRate += rate.nutrient() / rate.duration();
                fertilizerRate += rate.fertilizer() / rate.duration();
                for (var stack : cycle.harvest()) itemRate += stack.getCount() / rate.duration();
                nextDue = Math.min(nextDue, deadline.due);
            });
        }

        private static List<?> transfer(List<IRecipeHandler<?>> handlers, IO io, List<?> request, boolean simulate) {
            List<?> remaining = request;
            for (var handler : handlers) {
                remaining = handler.handleRecipe(io, null, remaining, simulate);
                if (remaining.isEmpty()) break;
            }
            return remaining;
        }

        boolean flush(List<IRecipeHandler<?>> itemOutputs) {
            boolean changed = false;
            var blocked = new HashSet<Batch>();
            for (var it = pending.entrySet().iterator(); it.hasNext();) {
                var entry = it.next();
                Batch batch = entry.getValue();
                if (blocked.contains(batch)) continue;
                if (!transfer(itemOutputs, IO.OUT, batch.ingredients, true).isEmpty()) {
                    blocked.add(batch);
                    continue;
                }
                var remaining = transfer(itemOutputs, IO.OUT, batch.ingredients, false);
                if (remaining.isEmpty()) {
                    var key = entry.getKey();
                    it.remove();
                    start(key);
                } else {
                    entry.setValue(new Batch(remaining.stream().map(SizedIngredient.class::cast)
                            .map(ingredient -> ingredient.getItems()[0].copyWithCount(ingredient.count())).toList()));
                }
                changed = true;
            }
            if (changed) {
                layoutChanged = true;
                recount();
            }
            return changed;
        }

        double advance(Runnable harvestReady) {
            if (!Double.isFinite(nextDue)) return 0;
            double elapsed = 0;
            while (elapsed < 1 && !growing.isEmpty()) {
                double step = Math.min(1 - elapsed, Math.max(0, nextDue - clock));
                if (nutrientRate > 0) step = Math.min(step, nutrient / nutrientRate);
                if (fertilizerRate > 0) step = Math.min(step, fertilizer / fertilizerRate);
                nutrient = Math.max(0, nutrient - step * nutrientRate);
                fertilizer = Math.max(0, fertilizer - step * fertilizerRate);
                double progress = step;
                growing.replaceAll((key, deadline) -> planted.get(key).requirements().isEmpty() ? deadline :
                        new Deadline(deadline.due + progress, deadline.duration));
                clock += step;
                elapsed += step;
                if (clock + 1e-9 < nextDue) break;
                growing.entrySet().removeIf(entry -> {
                    if (!planted.get(entry.getKey()).requirements().isEmpty()) return false;
                    if (entry.getValue().due > clock + 1e-9) return false;
                    pending.put(entry.getKey(), new Batch(planted.get(entry.getKey()).harvest()));
                    return true;
                });
                layoutChanged = true;
                recount();
                harvestReady.run();
            }
            return elapsed;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            var tag = new CompoundTag();
            tag.putDouble("clock", clock);
            tag.putDouble("nutrient", nutrient);
            tag.putDouble("fertilizer", fertilizer);
            var deadlines = new ListTag();
            growing.forEach((key, time) -> {
                var row = key.save();
                row.putDouble("due", time.due);
                row.putDouble("duration", time.duration);
                deadlines.add(row);
            });
            tag.put("growing", deadlines);
            var harvests = new ListTag();
            pending.forEach((key, batch) -> {
                var row = key.save();
                var items = new ListTag();
                for (var stack : batch.stacks) items.add(stack.save(registries));
                row.put("items", items);
                harvests.add(row);
            });
            tag.put("pending", harvests);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
            clock = tag.getDouble("clock");
            nutrient = tag.getDouble("nutrient");
            fertilizer = tag.getDouble("fertilizer");
            growing.clear();
            pending.clear();
            for (var element : tag.getList("growing", Tag.TAG_COMPOUND)) {
                var row = (CompoundTag) element;
                growing.put(Planting.load(row), new Deadline(row.getDouble("due"), row.getDouble("duration")));
            }
            for (var element : tag.getList("pending", Tag.TAG_COMPOUND)) {
                var row = (CompoundTag) element;
                var items = row.getList("items", Tag.TAG_COMPOUND).stream()
                        .map(item -> ItemStack.parse(registries, item).orElseThrow()).toList();
                pending.put(Planting.load(row), new Batch(items));
            }
        }
    }

    public static void init() {}
}
