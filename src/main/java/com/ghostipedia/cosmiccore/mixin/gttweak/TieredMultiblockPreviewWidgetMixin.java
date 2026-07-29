package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockPreview;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockUi;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SetMultiblockStructureTierPacket;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.value.IntValue;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(value = MultiblockPreviewWidget.class, remap = false)
public abstract class TieredMultiblockPreviewWidgetMixin {

    @Shadow
    @Final
    private MultiblockMachineDefinition multiblockDefinition;
    @Shadow
    private MultiblockSchemaInfo multiblockSchemaInfo;
    @Shadow
    private @org.jetbrains.annotations.Nullable BlockPos controllerPos;
    @Shadow
    @Final
    private DynamicHandler selectedBlockHandler;

    @Shadow
    public abstract void refreshSchema();

    @Invoker("refreshViewWidget")
    abstract void cosmiccore$invokeRefreshViewWidget();

    @Unique
    private final DynamicHandler cosmiccore$tierControlsHandler = new DynamicHandler();
    @Unique
    private boolean cosmiccore$selectionSuppressed;
    @Unique
    private int cosmiccore$pendingPreviewTier = -1;
    @Unique
    private boolean cosmiccore$tierUpdateListenerRegistered;

    @ModifyArg(
               method = "<init>",
               at = @At(value = "INVOKE",
                        target = "Lbrachy/modularui/widgets/ListWidget;children(Ljava/lang/Iterable;Ljava/util/function/Function;)Lbrachy/modularui/widgets/ListWidget;"),
               index = 1,
               require = 1)
    private Function<Map.Entry<String, IBlockPattern>, IWidget> cosmiccore$makeTierControlsDynamic(
                                                                                                   Function<Map.Entry<String, IBlockPattern>, IWidget> original) {
        return entry -> {
            if (!"main".equals(entry.getKey()) || !TieredMultiblockPatterns.isTiered(multiblockDefinition)) {
                return original.apply(entry);
            }
            cosmiccore$tierControlsHandler.widgetProvider(() -> cosmiccore$buildTierControls(entry, original));
            return new DynamicWidget<>().coverChildren().clientOnlyHandler(cosmiccore$tierControlsHandler);
        };
    }

    @Unique
    private IWidget cosmiccore$buildTierControls(Map.Entry<String, IBlockPattern> entry,
                                                 Function<Map.Entry<String, IBlockPattern>, IWidget> original) {
        ITieredMultiblockPreview preview = (ITieredMultiblockPreview) multiblockSchemaInfo;
        IBlockPattern pattern = TieredMultiblockPatterns.pattern(multiblockDefinition,
                preview.cosmiccore$getPreviewTier());
        IWidget widget = original.apply(Map.entry(entry.getKey(), pattern));
        if (widget instanceof Flow patternColumn) {
            List<IWidget> children = patternColumn.getChildren();
            if (!children.isEmpty() && children.getLast() instanceof Flow predicatesRow) {
                predicatesRow.addChild(TieredMultiblockUi.createTierButton(multiblockDefinition,
                        new IntValue.Dynamic(preview::cosmiccore$getPreviewTier, this::cosmiccore$setPreviewTier),
                        () -> 0, 20), 0);
            }
        }
        return widget;
    }

    @Unique
    private void cosmiccore$setPreviewTier(int tier) {
        ITieredMultiblockPreview preview = (ITieredMultiblockPreview) multiblockSchemaInfo;
        int selectedTier = TieredMultiblockPatterns.clampTier(multiblockDefinition, tier);
        if (selectedTier == preview.cosmiccore$getPreviewTier()) return;
        if (cosmiccore$isControllerActive()) return;
        cosmiccore$pendingPreviewTier = selectedTier;
        if (!cosmiccore$tierUpdateListenerRegistered) {
            IWidget widget = (IWidget) (Object) this;
            widget.getScreen().registerFrameUpdateListener(widget, this::cosmiccore$applyPendingPreviewTier);
            cosmiccore$tierUpdateListenerRegistered = true;
        }
    }

    @Unique
    private void cosmiccore$applyPendingPreviewTier() {
        int selectedTier = cosmiccore$pendingPreviewTier;
        if (selectedTier < 0) return;
        cosmiccore$pendingPreviewTier = -1;
        ITieredMultiblockPreview preview = (ITieredMultiblockPreview) multiblockSchemaInfo;
        if (selectedTier == preview.cosmiccore$getPreviewTier() || cosmiccore$isControllerActive()) return;
        preview.cosmiccore$setPreviewTier(selectedTier);
        cosmiccore$selectionSuppressed = true;
        selectedBlockHandler.notifyUpdate();
        refreshSchema();
        cosmiccore$invokeRefreshViewWidget();
        if (multiblockSchemaInfo.getRenderer() != null && multiblockSchemaInfo.getMapSchema() != null) {
            var camera = multiblockSchemaInfo.getRenderer().camera();
            camera.setLookAtAndAngle(multiblockSchemaInfo.getMapSchema().getCenter(), camera.dist(), camera.yaw(),
                    camera.pitch());
        }
        cosmiccore$tierControlsHandler.notifyUpdate();
        if (controllerPos != null) {
            CCoreNetwork.sendToServer(new SetMultiblockStructureTierPacket(controllerPos, selectedTier));
        }
    }

    @Unique
    private boolean cosmiccore$isControllerActive() {
        return controllerPos != null && Minecraft.getInstance().level != null &&
                MetaMachine.getMachine(Minecraft.getInstance().level,
                        controllerPos) instanceof IRecipeLogicMachine recipeMachine &&
                recipeMachine.getRecipeLogic().isActive();
    }

    @Redirect(
              method = "lambda$new$4",
              at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
              require = 1)
    private Object cosmiccore$selectClickedBlockTierPattern(Map<?, ?> patterns, Object key) {
        if ("main".equals(key) && TieredMultiblockPatterns.isTiered(multiblockDefinition)) {
            ITieredMultiblockPreview preview = (ITieredMultiblockPreview) multiblockSchemaInfo;
            return (Supplier<IBlockPattern>) () -> TieredMultiblockPatterns.pattern(multiblockDefinition,
                    preview.cosmiccore$getPreviewTier());
        }
        return patterns.get(key);
    }

    @Inject(method = "lambda$new$4", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$hideStaleSelection(CallbackInfoReturnable<IWidget> cir) {
        if (cosmiccore$selectionSuppressed) cir.setReturnValue(null);
    }

    @Inject(method = "lambda$new$0", at = @At("HEAD"), require = 1)
    private void cosmiccore$enableFreshSelection(GuiContext context, int button,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) cosmiccore$selectionSuppressed = false;
    }
}
