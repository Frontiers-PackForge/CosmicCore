package com.ghostipedia.cosmiccore.common.item;

import java.util.Optional;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;

import com.ghostipedia.cosmiccore.common.airControl.OxygenConfig;
import com.ghostipedia.cosmiccore.common.airControl.OxygenRules;
import com.ghostipedia.cosmiccore.utils.ItemData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AirBladderItem extends Item {

    private static final String TAG_CHARGES = "Charges";

    public AirBladderItem(Properties props) {
        super(props);
    }

    public static int getCharges(ItemStack stack) {
        CompoundTag tag = ItemData.readTag(stack);
        if (!tag.contains(TAG_CHARGES)) {
            return OxygenConfig.AIR_BLADDER_MAX_CHARGES;
        }
        return tag.getInt(TAG_CHARGES);
    }

    private static void setCharges(ItemStack stack, int charges) {
        ItemData.mutateTag(stack, tag -> tag.putInt(TAG_CHARGES, charges));
    }

    private static boolean isInSafeAir(ServerPlayer player) {
        ServerLevel serverLevel = player.serverLevel();
        int y = player.blockPosition().getY();
        OxygenRules.ResolvedAirRange resolved = OxygenRules.resolve(serverLevel.dimension(), y);
        if (resolved.airQuality != OxygenRules.AirQuality.SAFE) return false;

        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        boolean eyesInFluid = !serverLevel.getFluidState(eyePos).isEmpty();
        return !eyesInFluid;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int charges = getCharges(stack);

            if (charges < OxygenConfig.AIR_BLADDER_MAX_CHARGES && isInSafeAir(serverPlayer)) {
                setCharges(stack, OxygenConfig.AIR_BLADDER_MAX_CHARGES);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.8f, 1.0f);
                player.getCooldowns().addCooldown(this, OxygenConfig.AIR_BLADDER_COOLDOWN);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }

            if (charges > 0) {
                ServerLevel serverLevel = serverPlayer.serverLevel();
                Optional.of(serverPlayer.getData(CosmicAttachmentTypes.OXYGEN_BUDGET)).ifPresent(cap -> {
                    long current = cap.getOxygenTicks(serverLevel.dimension());
                    long max = OxygenConfig.getMaxOxygenTicks(serverPlayer);

                    if (current < max) {
                        long restored = Math.min(OxygenConfig.AIR_BLADDER_RESTORE_TICKS, max - current);
                        cap.setOxygenTicks(serverLevel.dimension(), current + restored);
                        setCharges(stack, getCharges(stack) - 1);

                        level.playSound(null, player.blockPosition(),
                                SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0f, 0.8f);
                    }
                });
                player.getCooldowns().addCooldown(this, OxygenConfig.AIR_BLADDER_COOLDOWN);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getCharges(stack) / OxygenConfig.AIR_BLADDER_MAX_CHARGES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55D8FF;
    }
}
