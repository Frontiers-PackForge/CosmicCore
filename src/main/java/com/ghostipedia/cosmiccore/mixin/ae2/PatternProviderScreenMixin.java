package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.api.misc.ae2.BlockingMode;
import com.ghostipedia.cosmiccore.api.misc.ae2.CosmicBlockingSettings;
import com.ghostipedia.cosmiccore.api.misc.ae2.IPatternProviderMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.PatternProviderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternProviderScreen.class)
public class PatternProviderScreenMixin<C extends PatternProviderMenu> extends AEBaseScreen<C> {

    @Unique
    private SettingToggleButton<BlockingMode> cosmicCore$blockingModeButton;

    protected PatternProviderScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void cosmicCore$addBlockingModeButton(PatternProviderMenu menu, Inventory playerInventory,
                                                  Component title, ScreenStyle style, CallbackInfo ci) {
        this.cosmicCore$blockingModeButton = new ServerSettingToggleButton<>(
                CosmicBlockingSettings.BLOCKING_MODE, BlockingMode.ALL);
        this.addToLeftToolbar(this.cosmicCore$blockingModeButton);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"), remap = false)
    private void cosmicCore$updateBeforeRender(CallbackInfo ci) {
        this.cosmicCore$blockingModeButton.set(((IPatternProviderMenu) menu).cosmicCore$getBlockingMode());
    }
}
