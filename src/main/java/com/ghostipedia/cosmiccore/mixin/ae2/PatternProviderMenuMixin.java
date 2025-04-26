package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.api.misc.ae2.BlockingMode;
import com.ghostipedia.cosmiccore.api.misc.ae2.CosmicBlockingSettings;
import com.ghostipedia.cosmiccore.api.misc.ae2.IPatternProviderMenu;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternProviderMenu.class)
public class PatternProviderMenuMixin implements IPatternProviderMenu {

    @Shadow(remap = false)
    @Final
    protected PatternProviderLogic logic;

    @Unique
    @GuiSync(8)
    private BlockingMode cosmicCore$UpgradedBlockingMode = BlockingMode.ALL;

    @Inject(method = "broadcastChanges",
            at = @At(value = "INVOKE",
                     target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getUnlockStack()Lappeng/api/stacks/GenericStack;",
                     remap = false))
    private void broadcastChanges(CallbackInfo ci) {
        cosmicCore$UpgradedBlockingMode = logic.getConfigManager().getSetting(CosmicBlockingSettings.BLOCKING_MODE);
    }

    @Override
    public BlockingMode cosmicCore$getBlockingMode() {
        return cosmicCore$UpgradedBlockingMode;
    }
}
