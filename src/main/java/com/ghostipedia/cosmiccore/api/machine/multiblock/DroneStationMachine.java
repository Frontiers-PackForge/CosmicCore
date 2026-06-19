package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.HashMultimap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        PLASMATIC(  4096,   GTValues.V[GTValues.UV],    0f,     CosmicItems.PLASMATIC_DRONE.asStack(1)),
        SANGUINE(   2048,   GTValues.V[GTValues.ZPM],   0.25f,  CosmicItems.SANGUINE_DRONE.asStack(1)),
        INDUSTRIAL( 1024,   GTValues.V[GTValues.LuV],   0.5f,   CosmicItems.INDUSTRIAL_DRONE.asStack(1)),
        ROBUST(     512,    GTValues.V[GTValues.IV],    0.75f,  CosmicItems.ROBUST_DRONE.asStack(1)),
        RUSTY(      256,    GTValues.V[GTValues.EV],    1,      CosmicItems.RUSTY_DRONE.asStack(1)),
        // spotless:on
        ;

        public final long range;
        public final long EUt;
        public final float consumptionChance;
        public final ItemStack item;

        DroneTier(long range, long EUt, float consumptionChance, ItemStack item) {
            this.range = range;
            this.EUt = EUt;
            this.consumptionChance = consumptionChance;
            this.item = item;
        }
    }

    static List<GTRecipe> droneTierRecipes = new ArrayList<>();
    static final String TIER_KEY = "drone_tier";
    static {
        for (DroneTier tier : DroneTier.values()) {

            droneTierRecipes.add(GTRecipeBuilder.ofRaw()
                    .notConsumable(tier.item) // we need this so it doesn't match empty stuff
                    .chancedInput(tier.item, (int) (tier.consumptionChance * 10000), 0)
                    .addData(TIER_KEY, tier.ordinal())
                    .build());
        }
    }

    public DroneStationMachine(BlockEntityCreationInfo holder) {
        super(holder);
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
            if (!drainEnergy(false)) {
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
        Optional<GTRecipe> maybeDroneRecipe = droneTierRecipes.stream().filter(
                dr -> RecipeHelper.matchRecipe(this, dr).isSuccess()).findFirst();
        if (maybeDroneRecipe.isEmpty()) return;
        GTRecipe droneRecipe = maybeDroneRecipe.get();
        currentTier = DroneTier.values()[droneRecipe.data.getInt(TIER_KEY)];
        // var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        // boolean found = false;
        // for (DroneTier tier : DroneTier.values()) {
        // for (var handler : itemHandlers) {
        // if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;
        // for (var content : itemHandler.getContents()) {
        // if (tier.item.equals(((ItemStack) content).getItem())) {
        // this.currentTier = tier;
        // found = true;
        // break;
        // }
        // }
        // }
        // if (found) break;
        // }
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
        return RecipeHelper.handleRecipeIO(this, droneTierRecipes.get(currentTier.ordinal()), IO.IN,
                getRecipeLogic().getChanceCaches()).isSuccess();
        // var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        // for (var handler : itemHandlers) {
        // if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;
        // for (int i = 0; i < itemHandler.getSlots(); i++) {
        // ItemStack stack = itemHandler.getStackInSlot(i);
        // if (stack.getItem().equals(currentTier.item)) {
        // // We have found the stack with the drone, try consuming and return true
        // if (currentTier.consumptionChance == 0) return true;
        // float randomValue = GTValues.RNG.nextFloat();
        // if (randomValue < currentTier.consumptionChance) {
        // var stackTaken = itemHandler.extractItemInternal(i, 1, false);
        // if (!stackTaken.getItem().equals(currentTier.item) || stackTaken.getCount() != 1) {
        // CosmicCore.LOGGER.error("Something went wrong when extracting done for Drone Multi: " +
        // stackTaken.getDisplayName());
        // return false;
        // }
        // }
        // return true;
        // }
        // }
        //
        // }
        // return false;
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
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        int i = 0;
        System.out.println("Toggling all multis");
        for (var con : connections) {
            toggleMultiblock(i);
            i++;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Force turns all connected machines in range on
     */
    public void turnAllMachinesOn() {
        System.out.println("Toggling all multis");
        for (int i = 0; i < connections.size(); i++) {
            forceTurnOnMultiblock(i);
            i++;
        }
    }

    /**
     * Force turns all connected machines in range on
     */
    public void turnAllMachinesOff() {
        System.out.println("Toggling all multis");
        for (int i = 0; i < connections.size(); i++) {
            forceTurnOffMultiblock(i);
            i++;
        }
    }

    /**
     * Disables a connected multi.
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
        MultiblockControllerMachine controller = droneInterface.getControllers().first();
        if (!(controller instanceof IControllable controllable)) return false;
        controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
        return true;
    }

    /**
     * Force turns all connected machines in range on
     *
     * @param index the index in the connections list
     */
    public boolean forceTurnOnMultiblock(int index) {
        if (isRemote()) return false;
        if (index > connections.size()) return false;
        DroneStationConnection connection = connections.get(index);
        if (!connection.isValid()) return false;
        if (connection.machine == null) return false;
        if (!(connection.machine instanceof DroneMaintenanceInterfacePartMachine droneInterface)) return false;
        MultiblockControllerMachine controller = droneInterface.getControllers().first();
        if (!(controller instanceof IControllable controllable)) return false;
        if (controllable.isWorkingEnabled()) return false;
        controllable.setWorkingEnabled(true);
        return true;
    }

    /**
     * Force turns all connected machines in range off
     *
     * @param index the index in the connections list
     */
    public boolean forceTurnOffMultiblock(int index) {
        if (isRemote()) return false;
        if (index > connections.size()) return false;
        DroneStationConnection connection = connections.get(index);
        if (!connection.isValid()) return false;
        if (connection.machine == null) return false;
        if (!(connection.machine instanceof DroneMaintenanceInterfacePartMachine droneInterface)) return false;
        MultiblockControllerMachine controller = droneInterface.getControllers().first();
        if (!(controller instanceof IControllable controllable)) return false;
        if (!controllable.isWorkingEnabled()) return false;
        controllable.setSuspendAfterFinish(true);
        return true;
    }

    // TODO: Add functions for UI to disable/enable/read status/etc machines remotely

    @Override
    public @NotNull Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                        .setMaxWidthLimit(150)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new ButtonWidget(
                6,
                80,
                178,
                20,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture("cosmiccore.multiblock.reboot_powergrid")),
                clickData -> turnAllMachinesOn()));
        group.addWidget(new ButtonWidget(
                6,
                100,
                178,
                20,
                new GuiTextureGroup(
                        GuiTextures.BUTTON,
                        new TextTexture("cosmiccore.multiblock.sleep_powergrid")),
                clickData -> turnAllMachinesOff()));
        return group;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }
}
