package com.ghostipedia.cosmiccore.common.blockentity;

import com.ghostipedia.cosmiccore.common.block.AeroponicsCropHolderBlock.Planter;
import com.ghostipedia.cosmiccore.common.block.AeroponicsCropHolderBlock.SeedlingPlants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.List;
import java.util.UUID;

public class AeroponicsCropHolderBlockEntity extends BlockEntity {

    public static final ModelProperty<Seedlings> SEEDLINGS = new ModelProperty<>();

    private final NonNullList<ItemStack> seedlings = NonNullList.withSize(Planter.ALL.size(), ItemStack.EMPTY);
    private final UUID[] plantings = new UUID[Planter.ALL.size()];
    private Runnable plantingListener;

    public UUID plantingId(Planter planter) {
        int index = planter.ordinal();
        if (plantings[index] == null && !seedlings.get(index).isEmpty()) {
            plantings[index] = UUID.randomUUID();
            setChanged();
        }
        return plantings[index];
    }

    public void setPlantingListener(Runnable listener) {
        plantingListener = listener;
    }

    public void removePlantingListener(Runnable listener) {
        if (plantingListener == listener) plantingListener = null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (plantingListener != null) plantingListener.run();
        plantingListener = null;
    }

    public AeroponicsCropHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getSeedling(Planter planter) {
        return seedlings.get(planter.ordinal());
    }

    public boolean plant(Planter planter, ItemStack seed) {
        if (!getSeedling(planter).isEmpty() || !SeedlingPlants.canPlant(seed)) return false;
        seedlings.set(planter.ordinal(), seed.copyWithCount(1));
        plantings[planter.ordinal()] = UUID.randomUUID();
        sync();
        return true;
    }

    public ItemStack removeSeedling(Planter planter) {
        ItemStack removed = ContainerHelper.takeItem(seedlings, planter.ordinal());
        if (!removed.isEmpty()) {
            plantings[planter.ordinal()] = null;
            sync();
        }
        return removed;
    }

    public void dropSeedlings() {
        if (level == null || level.isClientSide) return;
        Containers.dropContents(level, worldPosition, seedlings);
        seedlings.clear();
        setChanged();
    }

    private void sync() {
        setChanged();
        refreshVisuals();
        if (level != null && !level.isClientSide) {
            if (plantingListener != null) plantingListener.run();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, seedlings, registries);
        for (int i = 0; i < plantings.length; i++) {
            if (plantings[i] != null) tag.putUUID("Planting" + i, plantings[i]);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        seedlings.clear();
        ContainerHelper.loadAllItems(tag, seedlings, registries);
        for (int i = 0; i < plantings.length; i++) {
            plantings[i] = tag.hasUUID("Planting" + i) ? tag.getUUID("Planting" + i) : null;
        }
        if (plantingListener != null) plantingListener.run();
        refreshVisuals();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshVisuals();
    }

    private void refreshVisuals() {
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public ModelData getModelData() {
        return ModelData.of(SEEDLINGS, new Seedlings(seedlings));
    }

    public static final class Seedlings {

        private final List<ItemStack> stacks;

        private Seedlings(List<ItemStack> stacks) {
            this.stacks = stacks.stream().map(ItemStack::copy).toList();
        }

        public ItemStack get(Planter planter) {
            return stacks.get(planter.ordinal()).copy();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
