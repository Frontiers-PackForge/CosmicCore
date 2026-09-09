package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.cover.filter.TagFilter;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TagFilter.class, remap = false)
public abstract class TagFilterLoadedExpressionFixMixin {

    @Shadow
    protected String filterString;

    @Shadow
    protected TagExprFilter.TagExprParser.MatchExpr matchExpr;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;)V",
            at = @At("RETURN"))
    private void cosmiccore$restoreSavedExpression(CallbackInfo ci) {
        matchExpr = TagExprFilter.parseExpression(filterString);
    }
}
