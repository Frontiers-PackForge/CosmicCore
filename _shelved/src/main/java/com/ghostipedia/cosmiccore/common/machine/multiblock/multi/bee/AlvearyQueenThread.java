package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.core.IProduct;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class AlvearyQueenThread {

    private static final int WORK_CYCLE_TICKS = 550;
    private static final int BREED_TICKS = 100;

    private final MechanicalAlvearyMachine machine;
    private final int threadIndex;
    private final int color;
    private final List<RecipeHandlerList> inputHandlers;
    private final List<RecipeHandlerList> outputHandlers;

    @Nullable
    private AlvearyBeeHousing housing;
    @lombok.Setter
    private int workCounter;
    @lombok.Setter
    private int breedCounter;
    private boolean hasActiveQueen;
    @Nullable
    private String activeSpeciesName;
    private int queenHealth;
    private int queenMaxHealth;

    public AlvearyQueenThread(MechanicalAlvearyMachine machine, int threadIndex, int color,
                              List<RecipeHandlerList> inputHandlers,
                              List<RecipeHandlerList> outputHandlers) {
        this.machine = machine;
        this.threadIndex = threadIndex;
        this.color = color;
        this.inputHandlers = inputHandlers;
        this.outputHandlers = outputHandlers;
        initHousing();
    }

    private void initHousing() {
        var input = getItemInputHandler();
        var output = getItemOutputHandler();
        if (input != null && output != null) {
            try {
                var inventory = new AlvearyBeeInventory(input, output);
                this.housing = new AlvearyBeeHousing(machine, inventory);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    @Nullable
    public IRecipeHandler<?> getItemInputHandler() {
        for (RecipeHandlerList handler : inputHandlers) {
            var itemHandlers = handler.getHandlerMap().get(ItemRecipeCapability.CAP);
            if (itemHandlers != null && !itemHandlers.isEmpty()) {
                return itemHandlers.get(0);
            }
        }
        return null;
    }

    @Nullable
    public IRecipeHandler<?> getItemOutputHandler() {
        for (RecipeHandlerList handler : outputHandlers) {
            var itemHandlers = handler.getHandlerMap().get(ItemRecipeCapability.CAP);
            if (itemHandlers != null && !itemHandlers.isEmpty()) {
                return itemHandlers.get(0);
            }
        }
        return null;
    }

    public void tick() {
        if (housing == null) return;

        var level = machine.getLevel();
        if (level == null || level.isClientSide()) return;

        AlvearyBeeInventory inventory = housing.getBeeInventory();
        ItemStack queenStack = inventory.getQueen();

        if (queenStack.isEmpty()) {
            resetState();
            return;
        }

        var handlerItem = IIndividualHandlerItem.get(queenStack);
        if (handlerItem == null || !(handlerItem.getIndividual() instanceof IBee bee)) {
            resetState();
            return;
        }

        var stage = handlerItem.getStage();

        if (stage == BeeLifeStage.PRINCESS) {
            tickBreed(bee, queenStack, inventory);
        } else if (stage == BeeLifeStage.QUEEN) {
            tickQueen(bee, queenStack, level, inventory);
        }
    }

    private void tickBreed(IBee princess, ItemStack princessStack, AlvearyBeeInventory inventory) {
        ItemStack droneStack = inventory.getDrone();
        if (droneStack.isEmpty()) {
            hasActiveQueen = false;
            activeSpeciesName = princess.getSpecies().getDisplayName().getString();
            breedCounter = 0;
            return;
        }

        var droneHandler = IIndividualHandlerItem.get(droneStack);
        if (droneHandler == null || !(droneHandler.getIndividual() instanceof IBee drone)) {
            breedCounter = 0;
            return;
        }

        breedCounter++;
        hasActiveQueen = false;
        activeSpeciesName = princess.getSpecies().getDisplayName().getString();

        if (breedCounter >= BREED_TICKS) {
            breedCounter = 0;
            princess.setMate(drone.getGenome());
            ItemStack queenStack = princess.createStack(BeeLifeStage.QUEEN);
            inventory.setQueen(queenStack);

            droneStack.shrink(1);
            if (droneStack.isEmpty()) {
                inventory.setDrone(ItemStack.EMPTY);
            }
        }
    }

    private void tickQueen(IBee queen, ItemStack queenStack, Level level, AlvearyBeeInventory inventory) {
        if (!queen.isAlive()) {
            killQueen(queen, inventory);
            return;
        }

        hasActiveQueen = true;
        activeSpeciesName = queen.getSpecies().getDisplayName().getString();
        queenHealth = queen.getHealth();
        queenMaxHealth = queen.getMaxHealth();

        workCounter++;
        if (workCounter >= WORK_CYCLE_TICKS) {
            workCounter = 0;

            produceWithPity(queen, level.getRandom(), inventory);

            IBeeModifier modifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
            float aging = modifier.modifyAging(queen.getGenome(), queen.getMate(), 1.0f);
            queen.age(level, aging);

            inventory.setQueen(queen.createStack(BeeLifeStage.QUEEN));

            if (!queen.isAlive()) {
                killQueen(queen, inventory);
            }
        }
    }

    private void killQueen(IBee queen, AlvearyBeeInventory inventory) {
        if (queen.getMate() == null) {
            queen.setMate(queen.getGenome());
        }

        IBee princess = queen.spawnPrincess(housing);
        if (princess != null) {
            ItemStack princessStack = princess.createStack(BeeLifeStage.PRINCESS);
            inventory.addProduct(princessStack, false);
        }

        List<IBee> drones = queen.spawnDrones(housing);
        for (IBee drone : drones) {
            ItemStack droneStack = drone.createStack(BeeLifeStage.DRONE);
            inventory.addProduct(droneStack, false);
        }

        inventory.setQueen(ItemStack.EMPTY);
        resetState();
    }

    private void resetState() {
        hasActiveQueen = false;
        activeSpeciesName = null;
        workCounter = 0;
        breedCounter = 0;
        queenHealth = 0;
        queenMaxHealth = 0;
    }

    private void produceWithPity(IBee queen, RandomSource random, AlvearyBeeInventory inventory) {
        var composite = machine.getModifierComposite();
        int productivityCount = composite != null ? composite.getProductivityCount() : 0;
        float multiplier = 1 + productivityCount;

        var species = queen.getSpecies();

        for (IProduct product : species.getProducts()) {
            produceProduct(product, multiplier, random, inventory);
        }

        if (species.isJubilant(queen.getGenome(), housing)) {
            for (IProduct specialty : species.getSpecialties()) {
                produceProduct(specialty, multiplier, random, inventory);
            }
        }
    }

    private void produceProduct(IProduct product, float multiplier, RandomSource random,
                                AlvearyBeeInventory inventory) {
        float totalChance = product.chance() * multiplier;
        int guaranteed = (int) totalChance;
        float remainder = totalChance - guaranteed;

        int count = guaranteed;
        if (random.nextFloat() < remainder) {
            count++;
        }

        for (int i = 0; i < count; i++) {
            inventory.addProduct(product.createRandomStack(random), false);
        }
    }

    public int getProgressPercent() {
        if (hasActiveQueen) {
            return (int) ((workCounter / (float) WORK_CYCLE_TICKS) * 100);
        }
        if (breedCounter > 0) {
            return (int) ((breedCounter / (float) BREED_TICKS) * 100);
        }
        return 0;
    }

    public int getLifecyclePercent() {
        if (queenMaxHealth <= 0) return 0;
        return (int) (((queenMaxHealth - queenHealth) / (float) queenMaxHealth) * 100);
    }

    public String getStatusText() {
        if (housing == null) return "No I/O";
        if (!hasActiveQueen && activeSpeciesName == null) return "Idle";
        if (!hasActiveQueen && activeSpeciesName != null && breedCounter > 0) return "Breeding";
        if (!hasActiveQueen && activeSpeciesName != null) return "Waiting for Drone";
        return "Working";
    }

    public String getColorName() {
        if (color == -1) return "Unpainted";
        for (DyeColor dye : DyeColor.values()) {
            if (dye.getMapColor().col == color) {
                String name = dye.getName().replace("_", " ");
                StringBuilder result = new StringBuilder();
                for (String word : name.split(" ")) {
                    result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                }
                return result.toString().trim();
            }
        }
        return "Color #" + Integer.toHexString(color);
    }
}
