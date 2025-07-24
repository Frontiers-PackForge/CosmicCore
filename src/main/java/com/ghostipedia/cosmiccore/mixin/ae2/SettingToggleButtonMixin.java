package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.api.misc.ae2.BlockingMode;
import com.ghostipedia.cosmiccore.api.misc.ae2.CosmicBlockingSettings;

import net.minecraft.network.chat.Component;

import appeng.api.config.Setting;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.ButtonToolTips;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(value = SettingToggleButton.class, remap = false)
public class SettingToggleButtonMixin {

    @Shadow
    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val, ButtonToolTips title,
                                                        Component... tooltipLines) {}

    @WrapOperation(method = "<init>(Lappeng/api/config/Setting;Ljava/lang/Enum;Ljava/util/function/Predicate;Lappeng/client/gui/widgets/SettingToggleButton$IHandler;)V",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/client/gui/widgets/SettingToggleButton;registerApp(Lappeng/client/gui/Icon;Lappeng/api/config/Setting;Ljava/lang/Enum;Lappeng/core/localization/ButtonToolTips;Lappeng/core/localization/ButtonToolTips;)V",
                            ordinal = 0))
    private void cosmicCore$register(Icon icon, Setting<?> setting, @Coerce Object value,
                                     ButtonToolTips title, ButtonToolTips hint,
                                     Operation<Void> original) {
        original.call(icon, setting, value, title, hint);

        // Will block pushing patterns if there is anything contained in just the target inv
        registerApp(Icon.BLOCKING_MODE_YES, CosmicBlockingSettings.BLOCKING_MODE, BlockingMode.ALL,
                ButtonToolTips.InterfaceBlockingMode,
                Component.translatable("cosmiccore.pattern_block_mode.block_all"));

        // Default blocking behavior, ignores catalysts/non-pattern items/fluids
        registerApp(Icon.BLOCKING_MODE_NO, CosmicBlockingSettings.BLOCKING_MODE, BlockingMode.CONTAINS_SIMILAR,
                ButtonToolTips.InterfaceBlockingMode,
                Component.translatable("cosmiccore.pattern_block_mode.default_blocking_behavior"));

        // If the provider attempts to push a recipe that MATCHES the inventory contents, allow it.
        // But do not allow pushing of other patterns.
        registerApp(Icon.BLOCKING_MODE_NO, CosmicBlockingSettings.BLOCKING_MODE, BlockingMode.CONTAINS,
                ButtonToolTips.InterfaceBlockingMode,
                Component.translatable("cosmiccore.pattern_block_mode.allow_similar_patterns_only"));
    }
}
