package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.misc.IMetaMachineMixin;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

@Mixin(value = MetaMachineBlock.class, remap = false)
public class MetaMachineBlockMixin {

    @Inject(method = "use",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/item/tool/ToolHelper;getToolTypes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Set;"),
            cancellable = true)
    public void ccore$use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                          BlockHitResult hit,
                          CallbackInfoReturnable<InteractionResult> cir, @Local MetaMachine machine,
                          @Local ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModifiableItem ticonTool) {
            var result = ((IMetaMachineMixin) machine).ccore$onToolClick(ticonTool,
                    new UseOnContext(player, hand, hit));
            if (result == InteractionResult.CONSUME && player instanceof ServerPlayer serverPlayer) {
                int a = 5;
            }

            if (result != InteractionResult.PASS) {
                cir.setReturnValue(result);
            }
        }
    }
}
