package com.ghostipedia.cosmiccore.mixin.client;

import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {

    @Accessor("lastHealth")
    void cosmiccore$setLastHealth(int health);

    @Accessor("displayHealth")
    void cosmiccore$setDisplayHealth(int health);

    @Accessor("lastHealthTime")
    void cosmiccore$setLastHealthTime(long time);

    @Accessor("healthBlinkTime")
    void cosmiccore$setHealthBlinkTime(long time);
}
