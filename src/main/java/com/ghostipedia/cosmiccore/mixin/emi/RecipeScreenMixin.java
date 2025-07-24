package com.ghostipedia.cosmiccore.mixin.emi;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.screen.RecipeScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = RecipeScreen.class, remap = false)
public abstract class RecipeScreenMixin extends Screen {

    protected RecipeScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    public abstract int getResolveOffset();

    @Shadow
    public abstract int getMaxWorkstations();

    @ModifyExpressionValue(method = "setPage",
                           at = @At(value = "INVOKE",
                                    target = "Ldev/emi/emi/screen/RecipeScreen;getMaxWorkstations()I"))
    private int cosmicCore$removeWorkstationListLimit1(int originalMax) {
        return Integer.MAX_VALUE;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int cosmicCore$removeWorkstationListLimit2(int listSize, int maxWorkstations) {
        return listSize;
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"),
               slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), to = @At("TAIL")),
               index = 2)
    private int cosmicCore$modifyLeftSidebarWorkstationListX(int originalX,
                                                             @Local Bounds bounds,
                                                             @Local(ordinal = 5,
                                                                    name = "workstationAmount") int workstationAmount,
                                                             @Local(ordinal = 6, name = "offset") int offset) {
        return originalX + 18 - 18 * cosmicCore$getList(workstationAmount);
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"),
               slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), to = @At("TAIL")),
               index = 4)
    private int cosmicCore$modifyLeftSidebarWorkstationListWidth(int originalWidth,
                                                                 @Local(ordinal = 5,
                                                                        name = "workstationAmount") int workstationAmount) {
        return originalWidth - 18 + 18 * cosmicCore$getList(workstationAmount);
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"),
               slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), to = @At("TAIL")),
               index = 5)
    private int cosmicCore$modifyHLeftSidebarWorkstationListHeight(int originalHeight,
                                                                   @Local(ordinal = 5,
                                                                          name = "workstationAmount") int workstationAmount) {
        return 10 + workstationAmount * 18 + getResolveOffset();
    }

    @ModifyConstant(method = "getWorkstationBounds", constant = @Constant(intValue = 18, ordinal = 0))
    private int cosmicCore$addLeftSideOffset(int constant, int i) {
        return constant * cosmicCore$getList(i);
    }

    @ModifyExpressionValue(method = "getWorkstationBounds",
                           at = @At(value = "FIELD",
                                    opcode = Opcodes.GETFIELD,
                                    target = "Ldev/emi/emi/screen/RecipeScreen;backgroundWidth:I"))
    private int cosmicCore$addRightSideOffset(int backgroundWidth, int i) {
        return backgroundWidth * cosmicCore$getList(i);
    }

    @ModifyVariable(method = "getWorkstationBounds", at = @At("HEAD"), ordinal = 0, name = "i", argsOnly = true)
    private int cosmicCore$limitWorkstationListHeight(int i) {
        if (EmiConfig.workstationLocation == SidebarSide.NONE) return i;
        return i % getMaxWorkstations();
    }

    @Unique
    private int cosmicCore$getList(int i) {
        if (EmiConfig.workstationLocation == SidebarSide.NONE) return 1;
        return i / getMaxWorkstations() + 1;
    }
}
