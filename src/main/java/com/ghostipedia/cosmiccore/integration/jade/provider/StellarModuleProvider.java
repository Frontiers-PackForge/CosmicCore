package com.ghostipedia.cosmiccore.integration.jade.provider;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class StellarModuleProvider extends CapabilityBlockProvider<StellarBaseModule> {

    public StellarModuleProvider() {
        super(CosmicCore.id("stellar_module"));
    }

    @Nullable
    @Override
    protected StellarBaseModule getCapability(Level level, BlockPos blockPos, @Nullable Direction direction) {
        if (MetaMachine.getMachine(level, blockPos) instanceof IMultiController controller) {
            if (controller instanceof StellarBaseModule module) {
                return module;
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag tag, StellarBaseModule module) {
        IStellarIrisProvider iris = module.getStellarIris();
        boolean connected = iris != null && iris.isFormed();
        boolean canProcess = connected && iris.canProcess();

        tag.putBoolean("connected", connected);
        tag.putBoolean("canProcess", canProcess);
        tag.putBoolean("wirelessAvailable", module.isWirelessEnergyAvailable());
        tag.putLong("energyPerTick", module.getEnergyConsumedPerTick());

        if (iris != null) {
            tag.putString("stage", iris.getStage().toString());
            if (canProcess) {
                tag.putDouble("speedBonus", iris.getSpeedBonus());
                tag.putInt("parallel", iris.getParallelLimit());
            }
        }
    }

    @Override
    protected void addTooltip(CompoundTag tag, ITooltip tooltip, Player player, BlockAccessor accessor,
                              BlockEntity blockEntity, IPluginConfig config) {
        // Only show tooltip if we have data (i.e., this is actually a StellarBaseModule)
        if (!tag.contains("connected")) {
            return;
        }

        boolean connected = tag.getBoolean("connected");
        boolean canProcess = tag.getBoolean("canProcess");
        boolean wirelessAvailable = tag.getBoolean("wirelessAvailable");

        // Iris connection status
        if (!connected) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.not_connected")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        } else if (!canProcess) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.iris_not_ready")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            if (tag.contains("stage")) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.stage",
                        tag.getString("stage"))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
            }
        } else {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.connected")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            if (tag.contains("stage")) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.stage",
                        tag.getString("stage"))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
            }
            if (tag.contains("speedBonus")) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.speed_bonus",
                        String.format("%.1fx", tag.getDouble("speedBonus")))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            }
        }

        // Wireless energy status
        if (!wirelessAvailable) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.no_wireless")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        } else {
            long euPerTick = tag.getLong("energyPerTick");
            if (euPerTick > 0) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.energy_usage",
                        formatEnergy(euPerTick))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            }
        }
    }

    private String formatEnergy(long eu) {
        if (eu >= 1_000_000_000) return String.format("%.1fG EU/t", eu / 1_000_000_000.0);
        if (eu >= 1_000_000) return String.format("%.1fM EU/t", eu / 1_000_000.0);
        if (eu >= 1000) return String.format("%.1fk EU/t", eu / 1000.0);
        return String.format("%d EU/t", eu);
    }
}
