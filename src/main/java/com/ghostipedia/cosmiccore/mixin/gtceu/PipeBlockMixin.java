package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.misc.IPipeBlockEntityMixin;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;

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

@Mixin(value = PipeBlock.class, remap = false)
public class PipeBlockMixin implements IPipeBlockEntityMixin {

    @Inject(method = "use",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/item/tool/ToolHelper;getToolTypes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Set;"),
            cancellable = true)
    public void cc$use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                       BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir,
                       @Local ItemStack itemStack, @Local PipeBlockEntity<?, ?> pipeBlockEntity) {
        if (itemStack.getItem() instanceof ModifiableItem ticonTool) {
            var result = ((IPipeBlockEntityMixin) pipeBlockEntity).ccore$onToolClick(ticonTool,
                    new UseOnContext(player, hand, hit));
            if (result.getSecond() == InteractionResult.CONSUME && player instanceof ServerPlayer serverPlayer) {
                int a = 5;
            }

            if (result.getSecond() != InteractionResult.PASS) {
                cir.setReturnValue(result.getSecond());
            }
        }
    }
}
