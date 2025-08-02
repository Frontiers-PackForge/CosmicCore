package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

    // TODO: Make this configurable? Maybe per voltage you give it?
    public long blockRangeLimit = 16777216;

    // Rusty = 4096 : 64 Range : 1A HV
    // Robust = 65536 : 256 Range : 1A EV
    // Industrial = 262144 : 512 Range : 1 IV
    // Sanguine = 1048576 : 1024 Range : 1 LUV
    // Plasmatic = 16777216 : 4096 Range : 1 ZPM

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

    public void updateDroneHatches() {
        if (energyContainer != null) {
            if (drainEnergy(false)) {
                // Should we do anything else if the multi is running?
                // or just passively drain energy and let the rest take care of itself
            }
        }
        if (getOffsetTimer() % 20 == 0) {
            connections.removeIf(connection -> !connection.isValid());
        }
    }

    public boolean drainEnergy(boolean simulate) {
        // Cost is 1A LV per module per second
        long powerCost = connections.size() * GTValues.V[GTValues.LV];
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
