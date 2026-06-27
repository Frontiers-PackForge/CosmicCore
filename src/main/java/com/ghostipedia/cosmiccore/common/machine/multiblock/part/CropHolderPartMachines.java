package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.data.CosmicBotanyItemRegistration;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BushBlock;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

// TODO(8.0.0 MUI2): custom UI shelved; default UI used (orig in git)
public class CropHolderPartMachines extends MultiblockPartMachine {

    @SaveField
    private final CropHolderHandler heldCrops;
    @Getter
    @Setter
    @SaveField
    @SyncToClient
    private boolean isLocked;

    public CropHolderPartMachines(BlockEntityCreationInfo info) {
        super(info);
        heldCrops = attachTrait(new CropHolderHandler());
    }

    private class CropHolderHandler extends NotifiableItemStackHandler {

        public CropHolderHandler() {
            super(1, IO.IN, IO.BOTH, size -> new CustomItemStackHandler(size) {

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            });
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isLocked()) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            var item = stack.getItem();
            if (stack.isEmpty()) {
                return true;
            }
            if (item instanceof ItemNameBlockItem plantBlock) {
                var block = plantBlock.getBlock();
                if (block instanceof BushBlock) {
                    return true;
                }
            }
            if (item instanceof BlockItem plantBlock) {
                var block = plantBlock.getBlock();
                if (block instanceof BushBlock) {
                    return true;
                }
            }
            var flowers = Arrays.stream(CosmicBotanyItemRegistration.CosmicBotanyItem.values())
                    .filter(i -> i.item.is(item)).toList();
            return !flowers.isEmpty();

            // TODO; Come back for manual Recipe map Injection
        }
    }
}
