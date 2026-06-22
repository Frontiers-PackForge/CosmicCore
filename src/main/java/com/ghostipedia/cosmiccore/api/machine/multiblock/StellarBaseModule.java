package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarModuleReceiver;
import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarModuleContentWidget;
import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarModuleUIWidget;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StellarBaseModule extends WorkableMultiblockMachine
                               implements IStellarModuleReceiver, IDisplayUIMachine, IFancyUIMachine,
                               IOverclockMachine {

    @Nullable
    private IStellarIrisProvider stellarIris;

    @DescSynced
    private long energyConsumedPerTick = 0;

    @DescSynced
    private boolean wirelessEnergyAvailable = false;

    @DescSynced
    private boolean powerFailure = false;

    @Persisted
    @DescSynced
    private int configuredMaxParallel = 1;

    @Persisted
    @DescSynced
    private long configuredVoltagePerParallel = 32;

    private NotifiableEnergyContainer virtualEnergyContainer;

    public StellarBaseModule(BlockEntityCreationInfo holder) {
        super(holder);
        this.virtualEnergyContainer = new NotifiableEnergyContainer(this,
                Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 0, 0);
        this.virtualEnergyContainer.setSideInputCondition(side -> false);
        this.virtualEnergyContainer.setSideOutputCondition(side -> false);
    }

    @Override
    @Nullable
    public IStellarIrisProvider getStellarIris() {
        return stellarIris;
    }

    @Override
    public void setStellarIris(@Nullable IStellarIrisProvider provider) {
        this.stellarIris = provider;
    }

    public long getEnergyConsumedPerTick() {
        return energyConsumedPerTick;
    }

    public boolean isWirelessEnergyAvailable() {
        return wirelessEnergyAvailable;
    }

    public int getConfiguredMaxParallel() {
        return configuredMaxParallel;
    }

    public void setConfiguredMaxParallel(int configuredMaxParallel) {
        this.configuredMaxParallel = configuredMaxParallel;
    }

    public long getConfiguredVoltagePerParallel() {
        return configuredVoltagePerParallel;
    }

    public void setConfiguredVoltagePerParallel(long configuredVoltagePerParallel) {
        this.configuredVoltagePerParallel = configuredVoltagePerParallel;
    }

    protected UUID getTeamUUID() {
        var owner = getOwner();
        var ownerUUID = getOwnerUUID();

        if (owner == null) return MachineOwner.EMPTY;
        if (ownerUUID == null) return MachineOwner.EMPTY;

        if (owner instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(ownerUUID);
            if (team != null) {
                return team.getTeamId();
            }
        }
        return ownerUUID;
    }

    protected boolean drainWirelessEnergy(long amount) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }

        UUID owner = getTeamUUID();
        if (owner == MachineOwner.EMPTY) {
            return false;
        }

        WirelessEnergySavedData data = WirelessEnergySavedData.getOrCreate(serverLevel);

        if (!data.isActive(owner)) {
            return false;
        }

        BigInteger stored = data.getEnergyStored(owner);
        if (stored.compareTo(BigInteger.valueOf(amount)) < 0) {
            return false;
        }

        BigInteger leftover = data.addEUToGlobalWirelessEnergy(owner, BigInteger.valueOf(-amount));
        if (leftover.equals(BigInteger.ZERO)) {
            data.setEnergyOutput(owner, getBlockPos(), amount);
            return true;
        }
        return false;
    }

    protected boolean checkWirelessEnergyAvailable() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }

        UUID owner = getTeamUUID();
        if (owner == MachineOwner.EMPTY) {
            return false;
        }

        WirelessEnergySavedData data = WirelessEnergySavedData.getOrCreate(serverLevel);
        return data.isActive(owner);
    }

    protected BigInteger getWirelessEnergyStored() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return BigInteger.ZERO;
        }

        UUID owner = getTeamUUID();
        if (owner == MachineOwner.EMPTY) {
            return BigInteger.ZERO;
        }

        WirelessEnergySavedData data = WirelessEnergySavedData.getOrCreate(serverLevel);
        return data.getEnergyStored(owner);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isFormed() && stellarIris == null) {
            findAndRegisterWithIris();
        }
        virtualEnergyContainer.setEnergyStored(Long.MAX_VALUE / 2);
        this.wirelessEnergyAvailable = checkWirelessEnergyAvailable();

        if (isFormed() && getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                    serverLevel.getServer().getTickCount() + 20,
                    () -> {
                        if (isFormed() && stellarIris == null) {
                            findAndRegisterWithIris();
                        }
                        getRecipeLogic().updateTickSubscription();
                    }));
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.wirelessEnergyAvailable = checkWirelessEnergyAvailable();
        findAndRegisterWithIris();
        virtualEnergyContainer.setEnergyStored(Long.MAX_VALUE / 2);
    }

    @Override
    public Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> getCapabilitiesFlat() {
        Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> flat = super.getCapabilitiesFlat();
        Map<RecipeCapability<?>, List<IRecipeHandler<?>>> inputCaps = flat.get(IO.IN);
        boolean hasEnergy = inputCaps != null && inputCaps.containsKey(EURecipeCapability.CAP) &&
                !inputCaps.get(EURecipeCapability.CAP).isEmpty();
        if (!hasEnergy) {
            flat.computeIfAbsent(IO.IN, k -> new HashMap<>())
                    .computeIfAbsent(EURecipeCapability.CAP, k -> new ArrayList<>())
                    .add(virtualEnergyContainer);
        }
        return flat;
    }

    protected void findAndRegisterWithIris() {
        if (getLevel() == null || stellarIris != null) return;

        BlockPos modulePos = getBlockPos();
        int maxRadius = 80;

        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;

                    for (int y = -10; y <= 10; y++) {
                        BlockPos checkPos = modulePos.offset(x, y, z);
                        MetaMachine machine = MetaMachine.getMachine(getLevel(), checkPos);

                        if (machine instanceof IrisMultiblockMachine iris && iris.isFormed()) {
                            if (iris.registerModule(this)) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();

        if (stellarIris instanceof IrisMultiblockMachine iris) {
            iris.unregisterModule(this);
        }

        this.stellarIris = null;
        this.wirelessEnergyAvailable = false;
        this.energyConsumedPerTick = 0;
        clearEnergyOutput();
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        if (!super.isRecipeLogicAvailable()) {
            return false;
        }

        IStellarIrisProvider iris = getStellarIris();
        if (iris == null) {
            findAndRegisterWithIris();
            iris = getStellarIris();
        }

        if (iris == null || !iris.isFormed()) {
            return false;
        }

        if (!iris.canProcess()) {
            return false;
        }

        this.wirelessEnergyAvailable = checkWirelessEnergyAvailable();
        return wirelessEnergyAvailable;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null) return false;

        long euPerTick = RecipeHelper.getRealEUt(recipe).getTotalEU();
        euPerTick = applyEnergyDiscount(euPerTick);

        if (!drainWirelessEnergy(euPerTick)) {
            this.powerFailure = true;
            return false;
        }

        this.powerFailure = false;
        this.energyConsumedPerTick = euPerTick;
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        if (!super.onWorking()) {
            return false;
        }

        virtualEnergyContainer.setEnergyStored(Long.MAX_VALUE / 2);

        GTRecipe lastRecipe = getRecipeLogic().getLastRecipe();
        if (lastRecipe != null) {
            long euPerTick = RecipeHelper.getRealEUt(lastRecipe).getTotalEU();
            euPerTick = applyEnergyDiscount(euPerTick);
            this.energyConsumedPerTick = euPerTick;

            if (!drainWirelessEnergy(euPerTick)) {
                this.powerFailure = true;
                return false;
            }
        }

        this.powerFailure = false;
        return true;
    }

    private long applyEnergyDiscount(long baseEU) {
        IStellarIrisProvider iris = getStellarIris();
        if (iris != null && iris.canProcess()) {
            double discount = iris.getEnergyDiscount();
            baseEU = (long) (baseEU * discount);
        }
        return Math.max(1, baseEU);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        this.energyConsumedPerTick = 0;
        this.powerFailure = false;
        clearEnergyOutput();
    }

    private void clearEnergyOutput() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID owner = getTeamUUID();
        if (owner == MachineOwner.EMPTY) {
            return;
        }
        WirelessEnergySavedData data = WirelessEnergySavedData.getOrCreate(serverLevel);
        data.removeEnergyOutput(owner, getBlockPos());
    }

    @Override
    @Nullable
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        GTRecipe modified = super.getRealRecipe(recipe);
        if (modified == null) {
            return null;
        }

        int recipeTier = RecipeHelper.getRecipeEUtTier(recipe);
        if (recipeTier > getOverclockTier()) {
            return null;
        }

        IStellarIrisProvider iris = getStellarIris();
        if (iris == null || !iris.canProcess()) {
            return modified;
        }

        double speedBonus = iris.getSpeedBonus();
        if (speedBonus > 1.0) {
            int newDuration = (int) Math.max(1, modified.duration / speedBonus);
            modified = modified.copy();
            modified.duration = newDuration;
        }

        return modified;
    }

    public int getEffectiveParallelLimit() {
        IStellarIrisProvider iris = getStellarIris();
        int irisLimit = (iris != null && iris.canProcess()) ? iris.getParallelLimit() : 1;
        return Math.min(configuredMaxParallel, irisLimit);
    }

    public int getIrisParallelLimit() {
        IStellarIrisProvider iris = getStellarIris();
        if (iris == null || !iris.canProcess()) {
            return 1;
        }
        return iris.getParallelLimit();
    }

    @Override
    public int getOverclockTier() {
        return GTUtil.getTierByVoltage(configuredVoltagePerParallel);
    }

    @Override
    public void setOverclockTier(int tier) {
        tier = Math.max(getMinOverclockTier(), Math.min(tier, getMaxOverclockTier()));
        this.configuredVoltagePerParallel = GTValues.V[tier];
    }

    @Override
    public int getMaxOverclockTier() {
        return GTValues.MAX;
    }

    @Override
    public int getMinOverclockTier() {
        return GTValues.ULV;
    }

    @Override
    public long getOverclockVoltage() {
        return configuredVoltagePerParallel * getEffectiveParallelLimit();
    }

    public long getMaxEUt() {
        return configuredVoltagePerParallel * getEffectiveParallelLimit();
    }

    @Override
    public Widget createUIWidget() {
        return new StellarModuleContentWidget(() -> this);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer)
                .widget(new StellarModuleUIWidget(this, 198, 208, () -> this));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);

        if (isFormed()) {
            if (powerFailure) {
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.power_failure")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)));
            }

            if (!wirelessEnergyAvailable) {
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.no_wireless")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            } else if (energyConsumedPerTick > 0) {
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.energy_usage",
                        String.format("%,d", energyConsumedPerTick))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            }

            String tierName = GTValues.VNF[getOverclockTier()];
            textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.power_config",
                    tierName, getEffectiveParallelLimit())
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));

            IStellarIrisProvider iris = getStellarIris();
            if (iris == null) {
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.not_connected"));
            } else if (!iris.isFormed()) {
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.iris_not_formed"));
            } else if (!iris.canProcess()) {
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.iris_not_ready"));
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.stage",
                        iris.getStage().toString()));
            } else {
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.connected"));
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.stage",
                        iris.getStage().toString()));
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.speed_bonus",
                        String.format("%.1fx", iris.getSpeedBonus())));
                textList.add(Component.translatable("cosmiccore.multiblock.stellar_module.parallel",
                        iris.getParallelLimit()));
            }
        }
    }

    public boolean isPowerFailure() {
        return powerFailure;
    }
}
