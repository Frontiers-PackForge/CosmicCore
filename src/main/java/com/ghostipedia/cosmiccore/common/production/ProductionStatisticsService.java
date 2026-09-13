package com.ghostipedia.cosmiccore.common.production;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.machine.activity.ProductionBindingSource;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.math.BigDecimal;
import java.util.UUID;

public final class ProductionStatisticsService {

    private ProductionStatisticsService() {}

    public static void item(MetaMachine machine, ItemStack stack, long amount, boolean input) {
        if (machine == null || stack.isEmpty() || amount <= 0 || !(machine.getLevel() instanceof ServerLevel level))
            return;
        record(machine, level, ProductionResource.item(stack, level.registryAccess()), integerAmount(amount), input,
                "item");
    }

    public static void fluid(MetaMachine machine, FluidStack stack, long amount, boolean input) {
        if (machine == null || stack.isEmpty() || amount <= 0 || !(machine.getLevel() instanceof ServerLevel level))
            return;
        record(machine, level, ProductionResource.fluid(stack, level.registryAccess()), integerAmount(amount), input,
                "fluid");
    }

    public static void ember(MetaMachine machine, double amount, boolean input) {
        if (machine == null || !Double.isFinite(amount) || amount <= 0 ||
                !(machine.getLevel() instanceof ServerLevel level))
            return;
        record(machine, level, ProductionResource.value("ember", "embers:ember"), emberAmount(amount), input,
                "ember");
    }

    public static void soul(MetaMachine machine, SoulType type, long amount, boolean input) {
        if (machine == null || amount <= 0 || type != SoulType.Anima && type != SoulType.Spiritus ||
                !(machine.getLevel() instanceof ServerLevel level))
            return;
        record(machine, level, ProductionResource.value("soul", type.getSerializedName()), integerAmount(amount), input,
                "soul");
    }

    public static void energy(MetaMachine machine, long amount, boolean input) {
        if (machine == null || amount <= 0 || !(machine.getLevel() instanceof ServerLevel level)) return;
        record(machine, level, ProductionResource.value("energy", "gtceu:eu"), integerAmount(amount), input,
                "energy");
    }

    private static void record(MetaMachine machine, ServerLevel level, ProductionResource resource, BigDecimal amount,
                               boolean input, String kind) {
        if (!(machine instanceof ProductionBindingSource source)) return;
        UUID pool = source.cosmiccore$productionBinding().pool(machine);
        if (resource == null) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                    "Production statistics retained a visible coverage gap for an oversized {} identity", kind);
            ProductionStatisticsData.get(level.getServer()).markPartial(pool,
                    level.dimension().location().toString(), kind, input);
            return;
        }
        ProductionStatisticsData.get(level.getServer()).record(pool, level.dimension().location().toString(), resource,
                amount, input);
    }

    public static void partial(MetaMachine machine, String kind, boolean input) {
        if (machine == null || !(machine.getLevel() instanceof ServerLevel level) ||
                !(machine instanceof ProductionBindingSource source))
            return;
        UUID pool = source.cosmiccore$productionBinding().pool(machine);
        ProductionStatisticsData.get(level.getServer()).markPartial(pool, level.dimension().location().toString(), kind,
                input);
    }

    public static UUID viewerPool(ServerPlayer player) {
        try {
            return UUID.fromString(DeedTeams.teamKey(player));
        } catch (IllegalArgumentException ignored) {
            return player.getUUID();
        }
    }

    static BigDecimal integerAmount(long amount) {
        return BigDecimal.valueOf(amount);
    }

    static BigDecimal emberAmount(double amount) {
        return BigDecimal.valueOf(amount);
    }
}
