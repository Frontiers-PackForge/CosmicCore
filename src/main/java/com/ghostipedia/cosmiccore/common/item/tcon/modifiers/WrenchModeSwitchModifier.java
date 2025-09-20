package com.ghostipedia.cosmiccore.common.item.tcon.modifiers;

import com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.ToolAction;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolActionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

@RequiredArgsConstructor
public class WrenchModeSwitchModifier extends Modifier implements GeneralInteractionModifierHook,
                                      ToolActionModifierHook, TooltipModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOL_ACTION, ModifierHooks.TOOLTIP);
    }

    private static ResourceLocation MODE_SWITCH = new ResourceLocation("ccore", "mode");

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand,
                                       InteractionSource source) {
        var nbt = tool.getPersistentData();
        boolean shift = player.isShiftKeyDown();
        if (shift && source == InteractionSource.RIGHT_CLICK && !tool.isBroken()) {
            nbt.putInt(MODE_SWITCH,
                    nbt.getInt(MODE_SWITCH) + 1 % ToolModeSwitchBehavior.WrenchModeType.values().length);
            player.displayClientMessage(Component.translatable("metaitem.machine_configuration.mode",
                    ToolModeSwitchBehavior.WrenchModeType.values()[nbt.getInt(MODE_SWITCH)].getName()), true);
        }
        return InteractionResult.sidedSuccess(false);
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player,
                           List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        var nbt = tool.getPersistentData();
        tooltip.add(Component.translatable("metaitem.machine_configuration.mode",
                ToolModeSwitchBehavior.WrenchModeType.values()[nbt.getInt(MODE_SWITCH)].getName()));
    }

    @Override
    public boolean canPerformAction(IToolStackView tool, ModifierEntry modifier, ToolAction toolAction) {
        // todo check if we are on wrenches
        return true;
    }
}
