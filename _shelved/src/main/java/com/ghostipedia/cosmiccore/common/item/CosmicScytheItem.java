package com.ghostipedia.cosmiccore.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IElectricItem;

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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import com.sammy.malum.common.enchantment.scythe.AscensionEnchantment;
import com.sammy.malum.common.enchantment.scythe.ReboundEnchantment;
import com.sammy.malum.common.item.curiosities.weapons.scythe.MalumScytheItem;
import com.sammy.malum.registry.common.DamageTypeRegistry;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.sammy.malum.registry.common.item.EnchantmentRegistry.ASCENSION;
import static com.sammy.malum.registry.common.item.EnchantmentRegistry.REBOUND;

public class CosmicScytheItem extends MalumScytheItem {

    public static final long CAPACITY = 2_000_000L;
    public static final int GT_TIER = GTValues.HV;
    public static long DRAIN_RATE = 2048;

    public static final int USE_HIT = 2048;
    public static final int USE_SLASH = 2048;
    public static final int USE_ASCENSION = 4300;
    public static final int USE_REBOUND = 4300;

    @Getter
    private final String chargeTag = "gt_charge";

    public CosmicScytheItem(Tier tier, float attackDamageIn, float attackSpeedIn, Properties builderIn) {
        super(tier, attackDamageIn, attackSpeedIn, builderIn.stacksTo(1));
    }

    public long getCharge(ItemStack stack) {
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

        if (stack.getEnchantmentLevel(REBOUND.get()) > 0) {
            if (tryConsume(stack, USE_REBOUND)) {
                ReboundEnchantment.throwScythe(level, player, hand, stack);
                return InteractionResultHolder.success(stack);
            }
        }

        if (stack.getEnchantmentLevel(ASCENSION.get()) > 0) {
            if (tryConsume(stack, USE_ASCENSION)) {
                AscensionEnchantment.triggerAscension(level, player, hand, stack);
                return InteractionResultHolder.success(stack);
            }
        }

        player.sendSystemMessage(Component.literal("charge=" + getCharge(stack)));
        return super.use(level, player, hand);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        tryConsume(stack, USE_HIT);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void hurtEvent(net.neoforged.neoforge.event.entity.living.LivingHurtEvent event, LivingEntity attacker,
                          LivingEntity target, ItemStack stack) {
        super.hurtEvent(event, attacker, target, stack);
        if (event.getSource().is(DamageTypeRegistry.SCYTHE_SWEEP)) {
            tryConsume(stack, USE_SLASH);
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13.0 * getCharge(stack) / (double) CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55D8FF;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    // This works!
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    // This *kind of* works, it makes it so the anvil works but NOT the table!?!?!?!? AUGGGGHG
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.equals(REBOUND.get())) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.gt_scythe.energy", getCharge(stack), CAPACITY));
        tooltipComponents.add(Component.translatable("tooltip.gt_scythe.per_hit", USE_HIT));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    private static final Capability<IElectricItem> ELECTRIC_CAP = CapabilityManager
            .get(new CapabilityToken<IElectricItem>() {});

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        ICapabilityProvider parent = super.initCapabilities(stack, nbt);
        ElectricItemCap electric = new ElectricItemCap(stack);
        if (parent == null) return electric;
        return new ICapabilityProvider() {

            private final LazyOptional<IElectricItem> self = LazyOptional.of(() -> electric);

            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ELECTRIC_CAP) return self.cast();
                return parent.getCapability(cap, side);
            }
        };
    }

    public final class ElectricItemCap implements ICapabilityProvider, IElectricItem {

        private final ItemStack stack;

        public ElectricItemCap(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean canProvideChargeExternally() {
            return false;
        }

        @Override
        public boolean chargeable() {
            return true;
        }

        @Override
        public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
            if (chargerTier < GT_TIER) return 0;
            long chargeLimit = ignoreTransferLimit ? amount : Math.min(amount, getTransferLimit());
            long currentCharge = CosmicScytheItem.this.getCharge(stack);
            long space = CAPACITY - currentCharge;
            long accept = Math.min(chargeLimit, space);

            if (!simulate && accept > 0) {
                CosmicScytheItem.this.setCharge(stack, currentCharge + accept);
            }
            return accept;
        }

        @Override
        public long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally,
                              boolean simulate) {
            if (externally && !canProvideChargeExternally()) return 0;
            if (dischargerTier < GT_TIER) return 0;
            long dischargeLimit = ignoreTransferLimit ? amount : Math.min(amount, getTransferLimit());
            long currentCharge = CosmicScytheItem.this.getCharge(stack);
            long extracted = Math.min(dischargeLimit, currentCharge);
            if (!simulate && extracted > 0) CosmicScytheItem.this.setCharge(stack, currentCharge - extracted);
            return extracted;
        }

        @Override
        public long getTransferLimit() {
            return DRAIN_RATE;
        }

        @Override
        public long getMaxCharge() {
            return CAPACITY;
        }

        @Override
        public long getCharge() {
            return CosmicScytheItem.this.getCharge(stack);
        }

        @Override
        public int getTier() {
            return GT_TIER;
        }

        private final LazyOptional<IElectricItem> thisSelfWhatDoICallThis = LazyOptional.of(() -> this);

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            if (capability == ELECTRIC_CAP) return thisSelfWhatDoICallThis.cast();
            return LazyOptional.empty();
        }
    }
}
