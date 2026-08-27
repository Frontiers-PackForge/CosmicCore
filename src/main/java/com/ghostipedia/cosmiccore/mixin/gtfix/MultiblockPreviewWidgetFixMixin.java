package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.multiblock.GroupedSlicePreviewSupport;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.value.IntValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MultiblockPreviewWidget.class, remap = false)
public abstract class MultiblockPreviewWidgetFixMixin {

    @Unique
    private static final int COSMICCORE_GROUP_CONTROL_HEIGHT = 25;

    @Unique
    private static final int COSMICCORE_MINIMUM_SCHEMA_HEIGHT = 100;

    @Unique
    private ButtonWidget<?> cosmiccore$worldPreviewButton;

    @Shadow
    private MultiblockSchemaInfo multiblockSchemaInfo;

    @Shadow
    private BlockPos controllerPos;

    @Shadow
    public abstract void refreshSchema();

    @Invoker("refreshViewWidget")
    abstract void cosmiccore$refreshViewWidget();

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 1)
    private static int cosmiccore$reserveGroupedSliceControls(int height,
                                                              MultiblockMachineDefinition definition) {
        IBlockPattern pattern = definition.getStructurePatterns().get("main").get();
        if (!(pattern instanceof BlockPattern blockPattern)) return height;
        int controls = GroupedSlicePreviewSupport.variableGroups(blockPattern).size();
        return Math.max(COSMICCORE_MINIMUM_SCHEMA_HEIGHT,
                height - controls * COSMICCORE_GROUP_CONTROL_HEIGHT);
    }

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void cosmiccore$hideWorldPreviewOutsideTerminal(CallbackInfo ci) {
        ParentWidget<?> parent = (ParentWidget<?>) (Object) this;
        for (IWidget child : parent.getChildren()) {
            if (child instanceof ButtonWidget<?> button) {
                cosmiccore$worldPreviewButton = button;
                if (controllerPos == null) parent.remove(button);
                break;
            }
        }
    }

    @Inject(method = "setControllerPos", at = @At("RETURN"), require = 1)
    private void cosmiccore$updateWorldPreviewVisibility(BlockPos controllerPos,
                                                         CallbackInfoReturnable<MultiblockPreviewWidget> cir) {
        if (cosmiccore$worldPreviewButton == null) return;
        ParentWidget<?> parent = (ParentWidget<?>) (Object) this;
        boolean attached = parent.getChildren().contains(cosmiccore$worldPreviewButton);
        if (controllerPos != null && !attached) parent.addChild(cosmiccore$worldPreviewButton, 0);
        if (controllerPos == null && attached) parent.remove(cosmiccore$worldPreviewButton);
    }

    @Inject(method = "createSliceSliders", at = @At("TAIL"), require = 1)
    private void cosmiccore$addGroupedSliceSliders(Flow column, BlockPattern pattern, CallbackInfo ci) {
        for (GroupedSlicePreviewSupport.Group group : GroupedSlicePreviewSupport.variableGroups(pattern)) {
            int key = GroupedSlicePreviewSupport.repeatKey(group.index());
            if (!multiblockSchemaInfo.getUserSliceRepeats().containsKey(key)) {
                multiblockSchemaInfo.getUserSliceRepeats().put(key, group.minRepeats());
            }
            column.child(Text.dynamic(() -> Component.translatable("cosmiccore.multiblock.preview.group_repeats",
                    multiblockSchemaInfo.getUserSliceRepeats().getOrDefault(key, group.minRepeats()))).asWidget());
            column.child(new SliderWidget()
                    .background(GTGuiTextures.FLUID_SLOT)
                    .height(16)
                    .width(group.maxRepeats() * 12)
                    .stopper(1.0f)
                    .bounds(group.minRepeats(), group.maxRepeats())
                    .value(new IntValue.Dynamic(
                            () -> multiblockSchemaInfo.getUserSliceRepeats().getOrDefault(key, group.minRepeats()),
                            value -> cosmiccore$setGroupedSliceRepeats(key, value))));
        }
    }

    @Unique
    private void cosmiccore$setGroupedSliceRepeats(int key, int repeats) {
        int previous = multiblockSchemaInfo.getUserSliceRepeats().getOrDefault(key, 0);
        if (previous == repeats) return;
        multiblockSchemaInfo.getUserSliceRepeats().put(key, repeats);
        multiblockSchemaInfo.getUserGlobalBlockPreferences().clear();
        refreshSchema();
        cosmiccore$refreshViewWidget();
    }

    @ModifyExpressionValue(
                           method = "createPredicateMenus",
                           at = @At(value = "NEW", target = "brachy/modularui/widgets/menu/ContextMenuButton"),
                           require = 1)
    private ContextMenuButton<?> cosmiccore$openPredicateMenuBelow(ContextMenuButton<?> menu) {
        return menu.openDown();
    }

    @WrapOperation(
                   method = "createPredicateMenus",
                   at = @At(value = "INVOKE",
                            target = "Lbrachy/modularui/drawable/text/ModularComponent;asIcon()Lbrachy/modularui/drawable/Icon;"),
                   require = 1)
    private Icon cosmiccore$showPredicateCandidates(ModularComponent text, Operation<Icon> original,
                                                    @Local(ordinal = 0) MultiPredicate predicate) {
        List<ItemStack> candidates = cosmiccore$displayCandidates(predicate);
        if (candidates.isEmpty()) return original.call(text);
        return new ItemDrawable(candidates.toArray(ItemStack[]::new)).cycleTime(900).asIcon();
    }

    @ModifyArg(
               method = "createPredicateMenus",
               at = @At(value = "INVOKE",
                        target = "Lbrachy/modularui/drawable/Icon;size(I)Lbrachy/modularui/drawable/Icon;"),
               index = 0,
               require = 1)
    private int cosmiccore$enlargePredicateCandidate(int size) {
        return 14;
    }

    @Inject(method = "createInnerPredicateMenu", at = @At("RETURN"), require = 1)
    private void cosmiccore$centerNestedPredicateMenu(MultiPredicate predicate, BasePredicate basePredicate,
                                                      List<BlockInfo> candidates,
                                                      CallbackInfoReturnable<ContextMenuButton<?>> cir) {
        if (candidates.isEmpty()) return;
        ContextMenuButton<?> button = cir.getReturnValue();
        button.size(20);
        button.overlay(cosmiccore$centeredItem(candidates.getFirst().getItemStackForm()));
    }

    @Inject(method = "lambda$createPredicateMenus$27", at = @At("RETURN"), require = 1)
    private void cosmiccore$centerPredicateMenuEntry(MultiPredicate predicate, BasePredicate basePredicate,
                                                     CallbackInfoReturnable<IWidget> cir) {
        if (!(cir.getReturnValue() instanceof ToggleButton toggle) || basePredicate.getCandidates().isEmpty()) return;
        toggle.size(20);
        toggle.overlay(cosmiccore$centeredItem(basePredicate.getCandidates().getFirst().getItemStackForm()));
    }

    @Inject(method = "lambda$createInnerPredicateMenu$33", at = @At("RETURN"), require = 1)
    private void cosmiccore$centerNestedPredicateMenuEntry(MultiPredicate predicate, BasePredicate basePredicate,
                                                           BlockInfo candidate,
                                                           CallbackInfoReturnable<IWidget> cir) {
        if (!(cir.getReturnValue() instanceof ToggleButton toggle)) return;
        toggle.size(20);
        toggle.overlay(cosmiccore$centeredItem(candidate.getItemStackForm()));
    }

    @Unique
    private static Icon cosmiccore$centeredItem(ItemStack stack) {
        return new ItemDrawable(stack).asIcon().size(16).center();
    }

    @Unique
    private static List<ItemStack> cosmiccore$displayCandidates(MultiPredicate predicate) {
        List<ItemStack> result = new ArrayList<>();
        for (BasePredicate basePredicate : predicate.expand()) {
            List<BlockInfo> candidates = basePredicate.getCandidates();
            if (candidates.isEmpty()) continue;
            for (int index = 0; index < candidates.size(); index++) {
                ItemStack stack = candidates.get(index).getItemStackForm();
                if (stack.isEmpty() || cosmiccore$contains(result, stack)) continue;
                result.add(stack.copyWithCount(1));
            }
        }
        return result;
    }

    @Unique
    private static boolean cosmiccore$contains(List<ItemStack> stacks, ItemStack candidate) {
        for (ItemStack stack : stacks) {
            if (ItemStack.isSameItemSameComponents(stack, candidate)) return true;
        }
        return false;
    }
}
