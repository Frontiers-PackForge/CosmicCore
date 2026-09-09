package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.compat.gtceu.RainSealable;
import com.ghostipedia.cosmiccore.common.compat.gtceu.RainSealant;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.item.behavior.ColorSprayBehaviour;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public final class RainSealantSprayBehavior extends ColorSprayBehaviour {

    public RainSealantSprayBehavior() {
        super(GTItems.SPRAY_EMPTY::asStack, 256, -1);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.mayBuild()) return InteractionResult.PASS;
        var machine = MetaMachine.getMachine(context.getLevel(), context.getClickedPos());
        if (machine == null) return InteractionResult.PASS;
        var targets = RainSealant.targets(machine);
        if (targets.isEmpty()) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (getUsesLeft(stack) <= 0) return InteractionResult.FAIL;
        for (var target : targets) {
            if (!context.getLevel().mayInteract(player, target.getBlockPos())) return InteractionResult.FAIL;
        }
        int changed = 0;
        for (var target : targets) {
            if (!RainSealant.sealed(target)) {
                ((RainSealable) target).cosmiccore$setRainSealed(true);
                changed++;
            }
        }
        if (changed == 0) {
            player.displayClientMessage(Component.translatable("cosmiccore.sealant.already_sealed"), true);
            return InteractionResult.CONSUME;
        }
        if (!player.isCreative()) {
            int left = getUsesLeft(stack) - 1;
            if (left == 0) player.setItemInHand(context.getHand(), GTItems.SPRAY_EMPTY.asStack());
            else setUsesLeft(stack, left);
        }
        GTSoundEntries.SPRAY_CAN_TOOL.play(context.getLevel(), null, player.position(), 1.0f, 1.0f);
        player.displayClientMessage(Component.translatable("cosmiccore.sealant.applied", changed), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.addAll(LangHandler.getMultiLang("cosmiccore.sealant.tooltip"));
        lines.add(Component.translatable("behaviour.paintspray.uses", getUsesLeft(stack)));
    }
}
