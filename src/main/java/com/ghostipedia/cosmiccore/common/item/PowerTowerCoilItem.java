package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.common.item.behavior.PowerTowerLineToolBehavior;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public final class PowerTowerCoilItem extends Item {

    private final int voltageTier;
    private static final PowerTowerLineToolBehavior LINKING = new PowerTowerLineToolBehavior();

    public PowerTowerCoilItem(Properties properties, int voltageTier) {
        super(properties);
        if (voltageTier < GTValues.LV || voltageTier >= GTValues.V.length) {
            throw new IllegalArgumentException("Power Tower Coil voltage tier is outside the registered GT range");
        }
        this.voltageTier = voltageTier;
    }

    public int getVoltageTier() {
        return voltageTier;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return LINKING.useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return LINKING.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return LINKING.use(player.getItemInHand(hand), level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        LINKING.appendHoverText(stack, context, lines, flag);
    }
}
