package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.api.misc.ae2.BlockingMode;
import com.ghostipedia.cosmiccore.api.misc.ae2.CosmicBlockingSettings;
import com.ghostipedia.cosmiccore.api.misc.ae2.IPatternProviderLogic;
import com.ghostipedia.cosmiccore.api.misc.ae2.PatternProviderTargetCache;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.util.ConfigManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternProviderLogic.class, priority = 10000)
public class PatternProviderLogicMixin implements IPatternProviderLogic {

    @Unique
    private final PatternProviderTargetCache[] cosmicCore$targetCaches = new PatternProviderTargetCache[6];

    @Shadow(remap = false)
    @Final
    private ConfigManager configManager;

    @Shadow(remap = false)
    @Final
    private PatternProviderLogicHost host;

    @Shadow(remap = false)
    @Final
    private IActionSource actionSource;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL"),
            remap = false)
    private void PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host,
                                      int patternInventorySize, CallbackInfo ci) {
        configManager.registerSetting(CosmicBlockingSettings.BLOCKING_MODE, BlockingMode.ALL);
    }

    @Override
    public BlockingMode cosmicCore$getBlockingMode() {
        return configManager.getSetting(CosmicBlockingSettings.BLOCKING_MODE);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    @Nullable
    private PatternProviderTarget findAdapter(Direction side) {
        if (cosmicCore$targetCaches[side.get3DDataValue()] == null) {
            var thisBe = host.getBlockEntity();
            cosmicCore$targetCaches[side.get3DDataValue()] = new PatternProviderTargetCache(this,
                    (ServerLevel) thisBe.getLevel(), thisBe.getBlockPos().relative(side), side.getOpposite(),
                    actionSource);
        }
        @Nullable
        PatternProviderTarget ret = cosmicCore$targetCaches[side.get3DDataValue()].find();
        return ret;
    }
}
