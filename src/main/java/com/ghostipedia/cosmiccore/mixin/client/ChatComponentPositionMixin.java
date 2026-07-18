package com.ghostipedia.cosmiccore.mixin.client;

import net.minecraft.client.gui.components.ChatComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatComponent.class)
public abstract class ChatComponentPositionMixin {

    @ModifyConstant(method = "render", constant = @Constant(intValue = 40))
    private int cosmiccore$moveRenderedChatUp(int bottomMargin) {
        return bottomMargin + 8;
    }

    @ModifyConstant(method = { "screenToChatY", "handleChatQueueClicked" },
                    constant = @Constant(doubleValue = 40.0))
    private double cosmiccore$moveChatHitboxUp(double bottomMargin) {
        return bottomMargin + 8.0;
    }
}
