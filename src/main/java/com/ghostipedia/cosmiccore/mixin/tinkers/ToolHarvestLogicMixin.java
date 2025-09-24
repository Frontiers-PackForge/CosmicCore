package com.ghostipedia.cosmiccore.mixin.tinkers;

import com.ghostipedia.cosmiccore.common.item.tcon.base.ChargableModifiableItem;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Debug(export = true)
@Mixin(ToolHarvestLogic.class)
class ToolHarvestLogicMixin {


    @WrapOperation(
            remap = false,
            method = "mineBlock(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lslimeknights/tconstruct/library/tools/helper/ToolDamageUtil;damageAnimated(Lslimeknights/tconstruct/library/tools/nbt/IToolStackView;ILnet/minecraft/world/entity/LivingEntity;)Z"
            )
    )
    private static boolean cosmiccore$RedirectDurability(IToolStackView tool, int amount, LivingEntity entity, Operation<Boolean> original) {
        if(tool instanceof ToolStack toolStack){
            ItemStack stack = toolStack.createStack();
            if(stack.getItem() instanceof ChargableModifiableItem) {
                return stack.getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM)
                        .map(cap -> {
                            long energyCost = (long) amount * GTValues.VA[GTValues.LV];
                            long extracted = cap.discharge(energyCost, GTValues.LV, false, false, false);
                            if(extracted >= energyCost){
                                return true; // skip durability
                            }
                            return original.call(tool, amount, entity);
                        }).orElseGet(() -> original.call(tool, amount, entity));
            }
        }
        return original.call(tool, amount, entity);
    }
}
