package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.mixin.support.CosmicMixinTaintTracker;

import net.minecraft.CrashReport;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrashReport.class)
public abstract class CrashReportCosmicFrontiersNoticeMixin {

    @ModifyReturnValue(method = "getFriendlyReport(Lnet/minecraft/ReportType;Ljava/util/List;)Ljava/lang/String;",
                       at = @At("RETURN"),
                       require = 1,
                       expect = 1)
    private String cosmiccore$prependCosmicFrontiersNotice(String report) {
        return CosmicMixinTaintTracker.crashHeader() + report;
    }
}
