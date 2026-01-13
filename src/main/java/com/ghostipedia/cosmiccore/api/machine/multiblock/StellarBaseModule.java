package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarModuleReceiver;
import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarModuleContentWidget;
import com.ghostipedia.cosmiccore.client.gui.widget.stellar.StellarModuleUIWidget;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * Base class for Stellar Iris modules. Energy is drawn from the owner's wireless EU network.
 */
@Getter
public class StellarBaseModule extends WorkableMultiblockMachine
                               implements IStellarModuleReceiver, IDisplayUIMachine, IFancyUIMachine,
                               IOverclockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            StellarBaseModule.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Setter
    private IStellarIrisProvider stellarIris;

    @DescSynced
    private long energyConsumedPerTick = 0;

    @DescSynced
    private boolean wirelessEnergyAvailable = false;

    @DescSynced
    private boolean powerFailure = false;

    @Getter
    @Setter
    @Persisted
    @DescSynced
    private int configuredMaxParallel = 1;

    @Getter
    @Setter
    @Persisted
    @DescSynced
    private long configuredVoltagePerParallel = 32;

    public StellarBaseModule(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
        return leftover.equals(BigInteger.ZERO);
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
    public void onStructureFormed() {
        super.onStructureFormed();
        this.wirelessEnergyAvailable = checkWirelessEnergyAvailable();
        findAndRegisterWithIris();
    }

    protected void findAndRegisterWithIris() {
        if (getLevel() == null || stellarIris != null) return;

        BlockPos modulePos = getPos();
        int maxRadius = 80;

        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;

                    for (int y = -10; y <= 10; y++) {
                        BlockPos checkPos = modulePos.offset(x, y, z);
                        var blockEntity = getLevel().getBlockEntity(checkPos);

                        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
                            var machine = machineBlockEntity.getMetaMachine();
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
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        if (!super.isRecipeLogicAvailable()) {
            return false;
        }

        IStellarIrisProvider iris = getStellarIris();
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

        long euPerTick = getRecipeEUPerTick(recipe);

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

        if (energyConsumedPerTick > 0) {
            if (!drainWirelessEnergy(energyConsumedPerTick)) {
                this.powerFailure = true;
                return false;
            }
        }

        this.powerFailure = false;
        return true;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        this.energyConsumedPerTick = 0;
        this.powerFailure = false;
    }

    protected long getRecipeEUPerTick(GTRecipe recipe) {
        long baseEU = RecipeHelper.getRealEUt(recipe).getTotalEU();

        IStellarIrisProvider iris = getStellarIris();
        if (iris != null && iris.canProcess()) {
            double discount = iris.getEnergyDiscount();
            baseEU = (long) (baseEU * discount);
        }

        return Math.max(1, baseEU);
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
