package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.gtceu.RainSealant;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum RainSealantProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return CosmicCore.id("rain_sealant");
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var machine = MetaMachine.getMachine(accessor.getLevel(), accessor.getPosition());
        if (machine == null) return;
        var targets = RainSealant.targets(machine);
        if (targets.isEmpty()) return;
        var seal = new CompoundTag();
        seal.putInt("total", targets.size());
        seal.putInt("sealed", (int) targets.stream().filter(RainSealant::sealed).count());
        data.put("cosmiccore_rain_sealant", seal);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData();
        if (!data.contains("cosmiccore_rain_sealant")) return;
        var seal = data.getCompound("cosmiccore_rain_sealant");
        int total = seal.getInt("total");
        int sealed = seal.getInt("sealed");
        tooltip.add((total == 1 ? Component.translatable(sealed == 1 ? "cosmiccore.sealant.sealed" :
                "cosmiccore.sealant.unsealed") : Component.translatable("cosmiccore.sealant.coverage", sealed, total))
                .withStyle(sealed == total ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
    }
}
