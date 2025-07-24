package com.ghostipedia.cosmiccore.mixin.emi;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.screen.RecipeScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
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

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), remap = true)
    private int cosmicCore$removeWorkstationListLimit2(int workstationAmount, int maxWorkstations) {
        return workstationAmount;
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"),
               slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), to = @At("TAIL")),
               index = 2,
               remap = true)
    private int cosmicCore$modifyWorkstationListX(int originalX,
                                                  @Local Bounds bounds,
                                                  @Local(ordinal = 5, name = "workstationAmount") int workstationAmount,
                                                  @Local(ordinal = 6, name = "offset") int offset) {
        if (!cosmicCore$canExpandWorkstationList()) return originalX;
        return originalX - 18 * cosmicCore$getListWidth(workstationAmount);
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"),
               slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), to = @At("TAIL")),
               index = 4,
               remap = true)
    private int cosmicCore$modifyWorkstationListWidth(int originalWidth,
                                                      @Local(ordinal = 5,
                                                             name = "workstationAmount") int workstationAmount) {
        if (!cosmicCore$canExpandWorkstationList()) return originalWidth;
        return originalWidth + 18 * (cosmicCore$getListWidth(workstationAmount) - 1);
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"),
               slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), to = @At("TAIL")),
               index = 5,
               remap = true)
    private int cosmicCore$modifyWorkstationListHeight(int originalHeight,
                                                       @Local(ordinal = 5,
                                                              name = "workstationAmount") int workstationAmount) {
        if (!cosmicCore$canExpandWorkstationList()) return originalHeight;
        return 10 + Math.min(workstationAmount, getMaxWorkstations()) * 18 + getResolveOffset();
    }

    @ModifyConstant(method = "getWorkstationBounds", constant = @Constant(intValue = 18, ordinal = 0))
    private int cosmicCore$addLeftSideOffset(int constant, @Share("i") LocalIntRef iRef) {
        return constant * cosmicCore$getListWidth(iRef.get());
    }

    @ModifyExpressionValue(method = "getWorkstationBounds",
                           at = @At(value = "FIELD",
                                    opcode = Opcodes.GETFIELD,
                                    target = "Ldev/emi/emi/screen/RecipeScreen;backgroundWidth:I"))
    private int cosmicCore$addRightSideOffset(int backgroundWidth, @Share("i") LocalIntRef iRef) {
        return backgroundWidth * cosmicCore$getListWidth(iRef.get());
    }

    @ModifyVariable(method = "getWorkstationBounds", at = @At("HEAD"), ordinal = 0, name = "i", argsOnly = true)
    private int cosmicCore$limitWorkstationListHeight(int i, @Share("i") LocalIntRef iRef) {
        iRef.set(i);

        if (!cosmicCore$canExpandWorkstationList() || i <= 0) return i;
        return i % getMaxWorkstations();
    }

    @Unique
    private int cosmicCore$getListWidth(int i) {
        if (!cosmicCore$canExpandWorkstationList()) return 1;
        return i / getMaxWorkstations() + 1;
    }

    @Unique
    private boolean cosmicCore$canExpandWorkstationList() {
        return (EmiConfig.workstationLocation == SidebarSide.LEFT ||
                EmiConfig.workstationLocation == SidebarSide.RIGHT) &&
                getMaxWorkstations() > 0;
    }
}
