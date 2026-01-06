package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.data.material.property.CosmicCorePropertyKeys;
import com.ghostipedia.cosmiccore.api.data.material.property.FluidTooltipProperty;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExoticCombustionEngineMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ExoticCombustionEngineMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private String currentLubricant;
    private String currentBooster;
    @Getter
    private final int tier;
    // Probably a bad idea, most likely a better way to do this
    @Getter
    @DescSynced
    private static final Object2IntMap<FluidStack> lubricantTiers = new Object2IntOpenHashMap<>();
    @Getter
    @DescSynced
    private static final Object2IntMap<FluidStack> boostingTiers = new Object2IntOpenHashMap<>();

    static {
        // Boosting Tiers
        boostRecipes = new ArrayList<>();
        addBooster(CosmicMaterials.Ichor.getFluid(1), 3, 2);
        addBooster(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 1), 2, 1);
        addBooster(GTMaterials.Oxygen.getFluid(1), 1, 1);
        // Lubricant Tiers
        lubricantRecipes = new ArrayList<>();
        addLube(CosmicMaterials.TearsOfTheUniverse.getFluid(FluidStorageKeys.LIQUID, 1), 4, 288);
        addLube(CosmicMaterials.Triphenylphosphine.getFluid(FluidStorageKeys.LIQUID, 1), 3, 144);
        addLube(GTMaterials.Lubricant.getFluid(1), 2, 72);

    }

    public static void init() {
        // load the static map
    }

    static List<GTRecipe> lubricantRecipes;
    static List<GTRecipe> boostRecipes;
    static final String LUBRICATION_KEY = "lubrication";
    static final String BOOST_KEY = "boost";
    static final String DURATION_KEY = "duration";

    static void addLube(FluidStack lube, int lubrication, int duration) {
        var mat = ChemicalHelper.getMaterial(lube.getFluid());
        mat.setProperty(CosmicCorePropertyKeys.FLUID_TOOLTIPS,
                new FluidTooltipProperty("cosmiccore.lubricant.tooltip.prefix", lubrication));
        lubricantTiers.put(lube, lubrication);
        lubricantRecipes.add(GTRecipeBuilder.ofRaw()
                .inputFluids(lube)
                .addData(LUBRICATION_KEY, lubrication)
                .addData(DURATION_KEY, duration)
                .buildRawRecipe());
    }

    static void addBooster(FluidStack booster, int boost, int duration) {
        var mat = ChemicalHelper.getMaterial(booster.getFluid());
        mat.setProperty(CosmicCorePropertyKeys.FLUID_TOOLTIPS,
                new FluidTooltipProperty("cosmiccore.booster.tooltip.prefix", boost));
        boostingTiers.put(booster, boost);
        boostRecipes.add(GTRecipeBuilder.ofRaw()
                .inputFluids(booster)
                .addData(BOOST_KEY, boost)
                .addData(DURATION_KEY, duration)
                .buildRawRecipe());
    }

    private int runningTimer = 0;
    private int boostAmount = 0, boostDuration = 0;
    private int lubeDuration = 0;

    public ExoticCombustionEngineMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    private boolean isIntakesObstructed() {
        var dir = this.getFrontFacing();
        var axis = dir.getAxis();
        var centerPos = this.getPos().relative(dir);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (x == 0 && y == 0)
                    continue;
                var blockPos = switch (axis) {
                    case X -> centerPos.offset(0, x, y);
                    case Y -> centerPos.offset(x, 0, y);
                    case Z -> centerPos.offset(x, y, 0);
                };
                var blockState = this.getLevel().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    @Override
    public long getOverclockVoltage() {
        return GTValues.V[tier];
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof ExoticCombustionEngineMachine engineMachine)) {
            return RecipeModifier.nullWrongType(ExoticCombustionEngineMachine.class, machine);
        }
        long EUt = recipe.getOutputEUt().voltage();
        if (EUt * recipe.duration < 720) {
            return ModifierFunction.NULL;
        }

        Optional<GTRecipe> lubeRecipe = lubricantRecipes.stream().filter(
                lr -> RecipeHelper.matchRecipe(engineMachine, lr).isSuccess()).findFirst();

        // Has a variant of lubricant
        if (EUt > 0 && !engineMachine.isIntakesObstructed() && lubeRecipe.isPresent()) {
            int maxParallel = (int) (engineMachine.getOverclockVoltage() / EUt);
            int actualParallel = ParallelLogic.getParallelAmount(engineMachine, recipe, maxParallel);
            int tier = lubeRecipe.get().data.getInt(LUBRICATION_KEY);
            float durationModifier = (tier / 2.0F);
            double eutMultiplier;
            int consumptionMult = 1;
            if (engineMachine.boostAmount == 0) {
                eutMultiplier = actualParallel;
            } else {
                consumptionMult = engineMachine.boostAmount * 2;
                eutMultiplier = actualParallel * engineMachine.boostAmount * 3;
            }

            return ModifierFunction.builder()
                    .inputModifier(ContentModifier.multiplier(consumptionMult * actualParallel))
                    .outputModifier(ContentModifier.multiplier(actualParallel))
                    .durationMultiplier(durationModifier)
                    .eutMultiplier(eutMultiplier)
                    .parallels(actualParallel)
                    .build();

        }
        return ModifierFunction.NULL;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        var recipe = recipeLogic.getLastRecipe();
        if (recipe != null) {
            long EUt = recipe.getOutputEUt().voltage();
            int duration = recipe.duration;
            if ((EUt / recipe.parallels) * duration < 720) {
                this.getRecipeLogic().setWaiting(Component.translatable("cosmiccore.errors.bad_fuel"));

            }
        }

        if (lubeDuration <= 0) {
            for (GTRecipe lubeRecipe : lubricantRecipes) {
                if (RecipeHelper.matchRecipe(this, lubeRecipe).isSuccess() &&
                        RecipeHelper.handleRecipeIO(this, lubeRecipe, IO.IN, getRecipeLogic().getChanceCaches())
                                .isSuccess()) {
                    lubeDuration = lubeRecipe.data.getInt(DURATION_KEY);
                    currentLubricant = RecipeHelper.getInputFluids(lubeRecipe).get(0).getTranslationKey();
                    break;
                }
            }
            // no lubricant matched
            if (lubeDuration == 0) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }
        lubeDuration--;

        if (boostDuration <= 0) {
            boostDuration = 1;
            boostAmount = 0;
            for (GTRecipe boostRecipe : boostRecipes) {
                if (RecipeHelper.matchRecipe(this, boostRecipe).isSuccess() &&
                        RecipeHelper.handleRecipeIO(this, boostRecipe, IO.IN, getRecipeLogic().getChanceCaches())
                                .isSuccess()) {
                    boostAmount = boostRecipe.data.getInt(BOOST_KEY);
                    boostDuration = boostRecipe.data.getInt(DURATION_KEY);
                    currentBooster = RecipeHelper.getInputFluids(boostRecipe).get(0).getTranslationKey();
                    break;
                }
            }
        }
        boostDuration--;

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000;

        return value;
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.Builder builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive());
        var voltageName = Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(getOverclockVoltage())]);
        var amperageName = boostAmount != 0 ? boostAmount * 3 : boostAmount;
        if (recipeLogic.isSuspend() && !recipeLogic.getFancyTooltip().isEmpty()) {
            builder.addCustom(t -> t.add(recipeLogic.getFancyTooltip().get(0)));
            return;
        }
        builder.addCustom(t -> t.add(Component.translatable("gtceu.multiblock.max_energy_per_tick_amps",
                FormattingUtil.formatNumbers(getOverclockVoltage() * amperageName),
                amperageName, voltageName).withStyle(ChatFormatting.GRAY)));
        if (isActive() && isWorkingEnabled()) {
            builder.addCurrentEnergyProductionLine(
                    recipeLogic.getLastRecipe() != null ? recipeLogic.getLastRecipe().getOutputEUt().voltage() : 0);
        }

        builder.addFuelNeededLine(getRecipeFluidInputInfo(), recipeLogic.getDuration());

        if (isFormed && currentBooster != null) {
            builder.addCustom(tl -> tl.add(Component
                    .translatable("cosmiccore.multiblock.booster_used",
                            Component.translatable(currentBooster))
                    .withStyle(ChatFormatting.AQUA)));
        }

        if (isFormed && currentLubricant != null) {
            builder.addCustom(tl -> tl.add(Component
                    .translatable("cosmiccore.multiblock.lubricant_used",
                            Component.translatable(currentLubricant))
                    .withStyle(ChatFormatting.YELLOW)));
        }

        builder.addWorkingStatusLine();
    }

    @Nullable
    public String getRecipeFluidInputInfo() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe == null) {
            Iterator<GTRecipe> iterator = recipeLogic.searchRecipe();
            // noinspection ConstantValue
            recipe = iterator != null && iterator.hasNext() ? iterator.next() : null;
            if (recipe == null) return null;
        }
        FluidStack requiredFluidInput = RecipeHelper.getInputFluids(recipe).get(0);

        long ocAmount = getMaxVoltage() / recipe.getOutputEUt().voltage();
        int neededAmount = GTMath.saturatedCast(ocAmount * requiredFluidInput.getAmount());
        return ChatFormatting.RED + FormattingUtil.formatNumbers(neededAmount) + "mB";
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        super.attachTooltips(tooltipsPanel);
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.INDICATOR_NO_STEAM.get(false),
                () -> List.of(Component.translatable("gtceu.multiblock.large_combustion_engine.obstructed")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                this::isIntakesObstructed,
                () -> null));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
