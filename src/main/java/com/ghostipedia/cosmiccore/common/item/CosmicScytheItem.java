package com.ghostipedia.cosmiccore.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.sammy.malum.common.item.curiosities.weapons.scythe.MagicScytheItem;
import com.sammy.malum.common.item.curiosities.weapons.scythe.MalumScytheItem;
import com.sammy.malum.registry.common.item.EnchantmentRegistry;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CosmicScytheItem extends MalumScytheItem {


    public static final long CAPACITY = 2_000_000L;
    public static final int GT_TIER = GTValues.HV;
    public static long DRAIN_RATE = 2048;

    public static final int USE_HIT = 240;
    public static final int USE_SLASH = 160;
    public static final int USE_ASCENSION = 4300;
    public static final int USE_REBOUND = 4300;

    @Getter
    private final String chargeTag = "gt_charge";

    public CosmicScytheItem(Tier tier, float attackDamageIn, float attackSpeedIn, float magicDamage, Properties builderIn) {
        super(tier, 50, 5, builderIn.stacksTo(1));
    }


    // TODO MIGRATE TO ENERGY ITEM CAP DRAIN

    public long getCharge(ItemStack stack){
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getLong(chargeTag);
    }

    public void setCharge(ItemStack stack, long value) {
        stack.getOrCreateTag().putLong(chargeTag, (long) Mth.clamp(value, 0L, CAPACITY));
    }

    private boolean tryConsume(ItemStack stack, int eu) {
        long cur = getCharge(stack);
        if (cur < eu) return false;
        setCharge(stack, cur - eu);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getEnchantmentLevel(EnchantmentRegistry.ASCENSION.get()) > 0) {
            if (!tryConsume(stack, USE_ASCENSION)) {
                player.displayClientMessage(Component.translatable("tooltip.gt_scythe.no_energy"), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        if (stack.getEnchantmentLevel(EnchantmentRegistry.REBOUND.get()) > 0) {
            if (!tryConsume(stack, USE_REBOUND)) {
                player.displayClientMessage(Component.translatable("tooltip.gt_scythe.no_energy"), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        return super.use(level, player, hand);
    }
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker){
        return false;
    }

    @Override
    public void hurtEvent(net.minecraftforge.event.entity.living.LivingHurtEvent event, LivingEntity attacker, LivingEntity target, ItemStack stack) {
        super.hurtEvent(event, attacker, target, stack);
    }

    public final class ElectricItemCap implements ICapabilityProvider, IElectricItem {

        @Override
        public boolean canProvideChargeExternally() {
            return false;
        }

        @Override
        public boolean chargeable() {
            return false;
        }

        @Override
        public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
            return 0;
        }

        @Override
        public long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
            return 0;
        }

        @Override
        public long getTransferLimit() {
            return 0;
        }

        @Override
        public long getMaxCharge() {
            return 0;
        }

        @Override
        public long getCharge() {
            return 0;
        }

        @Override
        public int getTier() {
            return 0;
        }

        private final LazyOptional<IElectricItem> thisSelfWhatDoICallThis = LazyOptional.of(() -> this);

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            if (capability == CapabilityManager.get(new CapabilityToken<IElectricItem>() {
            })) return thisSelfWhatDoICallThis.cast();
            return LazyOptional.empty();

        }


    }
}
