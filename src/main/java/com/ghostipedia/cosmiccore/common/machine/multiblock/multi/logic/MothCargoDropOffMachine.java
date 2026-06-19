package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableMultiblockMachine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Moth Cargo Drop Off - The "receiver" multiblock for the Cargo Moths system.
 * <p>
 * Receives items and fluids from linked Moth Cargo Stations.
 * Does NOT require power - just a place for moths to land!
 * <p>
 * This is a simple, compact multiblock designed for easy placement at outposts.
 */
public class MothCargoDropOffMachine extends LinkedWorkableMultiblockMachine {


    // ==================== Constructor ====================

    public MothCargoDropOffMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }


    // ==================== Linking Overrides ====================

    @Override
    public LinkRole getLinkRole() {
        // Drop Off is REMOTE - it receives from Stations but doesn't initiate
        return LinkRole.REMOTE;
    }

    @Override
    public int getMaxPartners() {
        // Can receive from multiple stations (N:1 support)
        return 16;
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        // Only link to Cargo Stations
        if (!(partnerMachine instanceof MothCargoStationMachine)) {
            return false;
        }

        // Same dimension only
        GlobalPos myPos = getGlobalPos();
        if (myPos == null) return false;

        return myPos.dimension().equals(partner.dimension());
    }

    // ==================== Handler Access ====================

    /**
     * Get all item output handlers from the multiblock.
     * Called by MothCargoStationMachine to insert items.
     */
    public List<IItemHandler> getItemOutputHandlers() {
        List<IItemHandler> handlers = new ArrayList<>();

        var itemCaps = getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP);
        if (itemCaps != null) {
            for (var handler : itemCaps) {
                if (handler instanceof NotifiableItemStackHandler itemHandler) {
                    handlers.add(itemHandler);
                }
            }
        }

        return handlers;
    }

    /**
     * Get all fluid output handlers from the multiblock.
     * Called by MothCargoStationMachine to insert fluids.
     */
    public List<IFluidHandler> getFluidOutputHandlers() {
        List<IFluidHandler> handlers = new ArrayList<>();

        var fluidCaps = getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP);
        if (fluidCaps != null) {
            for (var handler : fluidCaps) {
                if (handler instanceof NotifiableFluidTank fluidHandler) {
                    handlers.add(fluidHandler);
                }
            }
        }

        return handlers;
    }

    // ==================== UI ====================

    @Override
    public void addDisplayText(List<Component> textList) {
        if (!isFormed()) {
            textList.add(Component.literal("Structure not formed")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            return;
        }

        // Linked stations
        int linkedCount = getLinkedPartners().size();
        if (linkedCount > 0) {
            textList.add(Component.literal("Receiving from " + linkedCount + " station(s)")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        } else {
            textList.add(Component.literal("No stations linked!")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            textList.add(Component.literal("Use a datastick to link to a Moth Cargo Station")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }
    }
}
