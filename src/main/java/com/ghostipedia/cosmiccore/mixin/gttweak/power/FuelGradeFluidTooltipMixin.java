package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.machine.generator.GeneratorFuelGrades;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.TooltipsHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = TooltipsHandler.class, remap = false)
public abstract class FuelGradeFluidTooltipMixin {

    @Inject(method = "appendFluidTooltips", at = @At("HEAD"))
    private static void cosmiccore$showFuelGrade(FluidStack fluid, Consumer<Component> tooltips, TooltipFlag flag,
                                                 Item.TooltipContext context, CallbackInfo ci) {
        int grade = GeneratorFuelGrades.grade(fluid);
        if (grade > 0) {
            tooltips.accept(Component.translatable("cosmiccore.fuel_grade.label", grade, GTValues.VN[grade])
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
