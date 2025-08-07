package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.google.common.collect.HashMultimap;

import java.util.ArrayList;
import java.util.List;

public class DroneStationMachine extends WorkableElectricMultiblockMachine {

    // A MultiMap from Dimension -> DroneStation, such that all Drone Maintenance Interfaces can
    // find their closest DroneStation in their world
    public static final HashMultimap<ResourceLocation, DroneStationMachine> droneStations = HashMultimap.create();

    private TickableSubscription tickSubscription;

    public final List<DroneStationConnection> connections = new ArrayList<>();

    public DroneTier currentTier = null;

    public enum DroneTier {
        // In this specific order so values are highest first
        // for the case of having multiple drones in a hatch

        // spotless:off
        PLASMATIC(  4096,   GTValues.V[GTValues.UV],    0f,     CosmicItems.PLASMATIC_DRONE.asItem()),
        SANGUINE(   1024,   GTValues.V[GTValues.ZPM],   0.25f,  CosmicItems.SANGUINE_DRONE.asItem()),
        INDUSTRIAL( 512,    GTValues.V[GTValues.LuV],   0.5f,   CosmicItems.INDUSTRIAL_DRONE.asItem()),
        ROBUST(     256,    GTValues.V[GTValues.IV],    0.75f,  CosmicItems.ROBUST_DRONE.asItem()),
        RUSTY(      64,     GTValues.V[GTValues.EV],    1,      CosmicItems.RUSTY_DRONE.asItem()),
        // spotless:on
        ;

        public long range;
        public long EUt;
        public float consumptionChance;
        public Item item;

        DroneTier(long range, long EUt, float consumptionChance, Item item) {
            this.range = range;
            this.EUt = EUt;
            this.consumptionChance = consumptionChance;
            this.item = item;
        }
    }

    public DroneStationMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!isRemote()) {
            droneStations.put(this.getLevel().dimension().location(), this);
            tickSubscription = this.subscribeServerTick(this::updateDroneHatches);
        }
    }

    @Override
    public void onStructureInvalid() {
        setWorkingEnabled(false);
        super.onStructureInvalid();
        if (!isRemote()) {
            droneStations.remove(this.getLevel().dimension().location(), this);
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    // This method is called every tick
    public void updateDroneHatches() {
        if (energyContainer != null) {
            if (drainEnergy(false)) {
                this.currentTier = null;
            }
        }
        if (getOffsetTimer() % 20 == 0) {
            connections.removeIf(connection -> !connection.isValid());
            updateDroneTier();
        }
    }

    // Update the multi's currentTier
    private void updateDroneTier() {
        // Find current highest drone in bus
        var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        boolean found = false;
        for (DroneTier tier : DroneTier.values()) {
            for (var handler : itemHandlers) {
                if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;
                for (var content : itemHandler.getContents()) {
                    if (tier.item.equals(((ItemStack) content).getItem())) {
                        this.currentTier = tier;
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
    }

    public long getBlockLimit() {
        if (currentTier == null) return 0;
        return currentTier.range;
    }

    public boolean drainEnergy(boolean simulate) {
        if (currentTier == null) return false;
        long powerCost = currentTier.EUt;
        long resultEnergy = energyContainer.getEnergyStored() - powerCost;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(powerCost);
            setWorkingEnabled(true);
            getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
            return true;
        }
        return false;
    }

    /**
     * Attempt to fix a maintenance issue, potentially consuming the current max tier drone in the process
     * 
     * @return whether the issue should be fixed
     */
    public boolean fixMaintenanceIssue() {
        // Note that this tries to consume a drone of the currentTier, which is only updated once per second.
        if (currentTier == null) return false;

        var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        for (var handler : itemHandlers) {
            if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;
            var items = itemHandler.getContents();
            for (var stackObject : items) {
                ItemStack stack = (ItemStack) stackObject;
                if (stack.getItem().equals(currentTier.item)) {
                    // We have found the stack with the drone, try consuming and return true
                    if (currentTier.consumptionChance == 0) return true;
                    float randomValue = GTValues.RNG.nextFloat();
                    if (randomValue < currentTier.consumptionChance) {
                        stack.setCount(stack.getCount() - 1);
                    }
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.connections.isEmpty()) {
            textList.add(Component
                    .translatable("cosmiccore.multiblock.drone_station_machine.drone_amount", this.connections.size())
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        } else {
            textList.add(Component
                    .translatable("cosmiccore.multiblock.drone_station_machine.no_drones")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

        }
    }

    // EXAMPLE CODE, REMOVE LATER MAYBE?
    // Or keep in, in which case, this should be a feature and remove this comment :eugeneThumbsUpCool:
    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        int i = 0;
        System.out.println("Toggling all multis");
        for (var con : connections) {
            toggleMultiblock(i);
            i++;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Disables a multi.
     * 
     * @param index the index in the connections list
     */
    public boolean toggleMultiblock(int index) {
        if (isRemote()) return false;
        if (index > connections.size()) return false;
        DroneStationConnection connection = connections.get(index);
        if (!connection.isValid()) return false;
        if (connection.machine == null) return false;
        if (!(connection.machine instanceof DroneMaintenanceInterfacePartMachine droneInterface)) return false;
        IMultiController controller = droneInterface.getControllers().first();
        if (!(controller instanceof IControllable controllable)) return false;
        controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
        return true;
    }

    // TODO: Add functions for UI to disable/enable/read status/etc machines remotely
}
