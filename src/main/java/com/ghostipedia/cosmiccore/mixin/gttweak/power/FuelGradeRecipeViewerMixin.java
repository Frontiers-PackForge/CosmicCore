package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.machine.generator.GeneratorFuelGrades;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.widgets.TextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTRecipeViewerWidget.class, remap = false)
public abstract class FuelGradeRecipeViewerMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cosmiccore$showFuelRules(GTRecipe recipe, CallbackInfo ci) {
        if (!GeneratorFuelGrades.isGradedRecipeType(recipe.recipeType.id)) return;
        var widget = (GTRecipeViewerWidget) (Object) this;
        int grade = GeneratorFuelGrades.grade(recipe);
        if (grade == 0) {
            widget.textComponents.child(new TextWidget<>(Text.lang("cosmiccore.fuel_grade.unassigned"))
                    .color(0xFFAA3333));
            return;
        }
        widget.textComponents.child(new TextWidget<>(Text.lang("cosmiccore.fuel_grade.label", grade,
                GTValues.VN[grade])).color(0xFF404040));
        widget.textComponents.child(new TextWidget<>(Text.lang("cosmiccore.fuel_grade.normal",
                GTValues.VN[grade])).color(0xFF404040));
        widget.textComponents.child(new TextWidget<>(Text.lang("cosmiccore.fuel_grade.migration_density",
                GTValues.VN[grade + 1])).color(0xFF404040));
        widget.textComponents.child(new TextWidget<>(Text.lang("cosmiccore.fuel_grade.migration_power",
                GTValues.VN[grade + 1], GTValues.VN[grade])).color(0xFF404040));
        widget.textComponents.child(new TextWidget<>(Text.lang("cosmiccore.fuel_grade.cutoff",
                GTValues.VN[grade + 2])).color(0xFFAA3333));
    }
}
