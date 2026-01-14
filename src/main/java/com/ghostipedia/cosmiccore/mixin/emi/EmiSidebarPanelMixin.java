package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;

import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public abstract class EmiSidebarPanelMixin {

    @Shadow
    public EmiScreenManager.ScreenSpace space;

    @Shadow
    public int page;

    @Unique
    private boolean cosmiccore$isTodoListMode() {
        if (space == null || space.getType() != SidebarType.FAVORITES) {
            return false;
        }
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        return manager.getActiveViewMode() == CosmicBookmarkGroup.ViewMode.TODO_LIST;
    }

    @Unique
    private int cosmiccore$getEffectivePageSize() {
        if (space == null) {
            return 1;
        }
        if (cosmiccore$isTodoListMode()) {
            return space.th; // One item per row
        }
        return space.pageSize;
    }

    @ModifyArg(
               method = "render",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/screen/EmiScreenManager$ScreenSpace;render(Ldev/emi/emi/runtime/EmiDrawContext;IIFI)V",
                        ordinal = 0),
               index = 4,
               require = 0)
    private int cosmiccore$modifyStartIndex(int originalStartIndex) {
        if (cosmiccore$isTodoListMode()) {
            return space.th * page;
        }
        return originalStartIndex;
    }

    @ModifyVariable(
                    method = "render",
                    at = @At("STORE"),
                    ordinal = 0,
                    require = 0)
    private int cosmiccore$modifyTotalPages(int totalPages) {
        if (cosmiccore$isTodoListMode()) {
            int effectivePageSize = space.th;
            if (effectivePageSize <= 0) {
                return 1;
            }
            return (space.getStacks().size() - 1) / effectivePageSize + 1;
        }
        return totalPages;
    }

    @Inject(method = "wrapPage", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$wrapPageTodoList(CallbackInfo ci) {
        if (!cosmiccore$isTodoListMode()) {
            return;
        }

        int effectivePageSize = space.th;
        if (effectivePageSize <= 0) {
            ci.cancel();
            return;
        }

        int totalPages = (space.getStacks().size() - 1) / effectivePageSize + 1;
        if (page >= totalPages) {
            page = 0;
            space.batcher.repopulate();
        } else if (page < 0) {
            page = totalPages - 1;
            space.batcher.repopulate();
        }

        ci.cancel();
    }

    @Inject(method = "hasMultiplePages", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$hasMultiplePagesTodoList(CallbackInfoReturnable<Boolean> cir) {
        if (!cosmiccore$isTodoListMode()) {
            return;
        }

        cir.setReturnValue(space != null && space.getStacks().size() > space.th);
    }

    @Inject(method = "scroll", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$scrollTodoList(int delta, CallbackInfo ci) {
        if (!cosmiccore$isTodoListMode()) {
            return;
        }

        if (space == null || space.th == 0) {
            ci.cancel();
            return;
        }

        page += delta;
        int effectivePageSize = space.th;
        int totalPages = (space.getStacks().size() - 1) / effectivePageSize + 1;

        if (totalPages <= 1) {
            page = 0;
            ci.cancel();
            return;
        }

        if (page >= totalPages) {
            page = 0;
        } else if (page < 0) {
            page = totalPages - 1;
        }

        space.batcher.repopulate();
        ci.cancel();
    }
}
