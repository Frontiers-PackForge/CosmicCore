package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.client.gui.IPatternEncodingTerminalMenu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.util.ConfigInventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternEncodingTermMenu.class, remap = false)
public abstract class PatternEncodingTermMenuMixin extends MEStorageMenu
                                                   implements IMenuCraftingPacket, IPatternEncodingTerminalMenu {

    @Shadow
    @Final
    private ConfigInventory encodedInputsInv;

    @Shadow
    @Final
    private ConfigInventory encodedOutputsInv;

    public PatternEncodingTermMenuMixin(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
            at = @At("TAIL"))
    private void cosmicCore$injectInit(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host,
                                       boolean bindInventory, CallbackInfo ci) {
        registerClientAction("modifyPattern", Integer.class,
                this::cosmicCore$modifyPattern);
    }

    @Override
    public void cosmicCore$modifyPattern(int data) {
        if (isClientSide()) {
            sendClientAction("modifyPattern", data);
        } else {
            // modify
            var output = cosmicCore$modifyInventoryStacks(encodedOutputsInv, data);
            if (output == null) {
                return;
            }
            var input = cosmicCore$modifyInventoryStacks(encodedInputsInv, data);
            if (input == null) {
                return;
            }
            for (int slot = 0; slot < output.length; ++slot) {
                if (output[slot] != null) {
                    encodedOutputsInv.setStack(slot, output[slot]);
                }
            }
            for (int slot = 0; slot < input.length; ++slot) {
                if (input[slot] != null) {
                    encodedInputsInv.setStack(slot, input[slot]);
                }
            }
        }
    }

    @Unique
    private static GenericStack[] cosmicCore$modifyInventoryStacks(ConfigInventory inv, int data) {
        boolean positive = data > 0;
        if (!positive) {
            data = -data;
        }
        GenericStack[] result = new GenericStack[inv.size()];
        for (int slot = 0; slot < inv.size(); ++slot) {
            GenericStack stack = inv.getStack(slot);
            if (stack != null) {
                if (positive) {
                    if (data * stack.amount() > Integer.MAX_VALUE) {
                        return null;
                    } else {
                        result[slot] = new GenericStack(stack.what(), data * stack.amount());
                    }
                } else {
                    if (stack.amount() % data != 0) {
                        return null;
                    } else {
                        // indivisible
                        result[slot] = new GenericStack(stack.what(), stack.amount() / data);
                    }
                }
            }
        }
        return result;
    }
}
