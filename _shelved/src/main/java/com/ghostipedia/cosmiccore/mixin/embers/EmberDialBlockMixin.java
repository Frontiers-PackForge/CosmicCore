package com.ghostipedia.cosmiccore.mixin.embers;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.llamalad7.mixinextras.sugar.Local;
import com.rekindled.embers.block.EmberDialBlock;
import com.rekindled.embers.blockentity.EmberDialBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = EmberDialBlock.class, remap = false)
public class EmberDialBlockMixin {

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "getBEData",
            at = @At(value = "INVOKE",
                     target = "Lcom/rekindled/embers/block/EmberDialBlock;formatEmber(DD)Lnet/minecraft/network/chat/MutableComponent;"),
            cancellable = true)
    public void formatEmber(Direction facing, ArrayList<Component> text, BlockEntity blockEntity, int maxLines,
                            CallbackInfo ci, @Local EmberDialBlockEntity dial) {
        text.add(Component.translatable("embers.tooltip.emberdial.ember", FormattingUtil.formatNumbers(dial.ember),
                FormattingUtil.formatNumbers(dial.capacity)));
        ci.cancel();
    }
}
