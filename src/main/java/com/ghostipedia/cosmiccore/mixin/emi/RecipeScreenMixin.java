package com.ghostipedia.cosmiccore.mixin.emi;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import dev.emi.emi.screen.WidgetGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(RecipeScreen.class)
public abstract class RecipeScreenMixin extends Screen {

    @Shadow(remap = false)
    int x;

    @Shadow(remap = false)
    int backgroundHeight;

    @Shadow(remap = false)
    int backgroundWidth;

    protected RecipeScreenMixin(Component title) {
        super(title);
    }

    @Shadow(remap = false)
    public abstract int getResolveOffset();

    @Shadow(remap = false)
    int y;

    @Shadow(remap = false)
    private List<RecipeTab> tabs;

    @Shadow(remap = false)
    private int tab;

    @Override
    @Shadow(remap = false)
    public abstract void onClose();

    @Shadow(remap = false)
    private List<WidgetGroup> currentPage;

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
                        ordinal = 4,
                        remap = false),
               index = 2)
    private int modifyx(int x) {
        return x + 18 - 18 * cosmicCore$getList(cosmicCore$getWorkstationAmount());
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
                        ordinal = 4,
                        remap = false),
               index = 4)
    private int modifyw(int x) {
        return x - 18 + 18 * cosmicCore$getList(cosmicCore$getWorkstationAmount());
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
                        ordinal = 4,
                        remap = false),
               index = 5)
    private int modifyh(int x) {
        return 10 + Math.min(cosmicCore$getWorkstationAmount(), cosmicCore$maxWorkstations()) * 18 + getResolveOffset();
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public Bounds getWorkstationBounds(int i) {
        Bounds bounds = Bounds.EMPTY;
        int offset = 0;
        if (i == -1) {
            i = 0;
            offset = -getResolveOffset();
        }
        if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
            bounds = new Bounds(x - (cosmicCore$getList(i) * 18),
                    y + 9 + getResolveOffset() + (i % cosmicCore$maxWorkstations() * 18) + offset, 18, 18);
        } else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
            bounds = new Bounds(x + (cosmicCore$getList(i) * backgroundWidth),
                    y + 9 + getResolveOffset() + (i % cosmicCore$maxWorkstations() * 18) + offset, 18, 18);
        } else if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
            bounds = new Bounds(x + 5 + getResolveOffset() + i * 18 + offset, y + backgroundHeight - 23, 18, 18);
        }
        return bounds;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public int getMaxWorkstations() {
        return Integer.MAX_VALUE;
    }

    @Unique
    private int cosmicCore$getWorkstationAmount() {
        return EmiApi.getRecipeManager().getWorkstations(tabs.get(tab).category).size();
    }

    @Unique
    private int cosmicCore$getList(int i) {
        return (int) Math.floor((double) i / cosmicCore$maxWorkstations()) + 1;
    }

    @Unique
    private int cosmicCore$maxWorkstations() {
        return switch (EmiConfig.workstationLocation) {
            case LEFT, RIGHT -> (backgroundHeight - getResolveOffset() - 18) / 18;
            case BOTTOM -> (backgroundWidth - getResolveOffset() - 18) / 18;
            default -> 0;
        };
    }
}
