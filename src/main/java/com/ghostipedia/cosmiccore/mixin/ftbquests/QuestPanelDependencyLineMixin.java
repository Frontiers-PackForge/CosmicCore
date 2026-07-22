package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineSettings;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.QuestDependencyLineExtension;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = QuestPanel.class, remap = false)
public class QuestPanelDependencyLineMixin {

    @Unique
    private static final ImageIcon cosmiccore$defaultDependencyLine = (ImageIcon) Icon
            .getIcon("ftbquests:textures/gui/dependency.png");

    @Shadow
    @Final
    private QuestScreen questScreen;

    @WrapMethod(method = "renderConnection")
    private void cosmiccore$renderPerDependencyLine(Widget source, QuestButton target, PoseStack poseStack,
                                                    float halfWidth, int red, int green, int blue, int startAlpha,
                                                    int endAlpha, float textureOffset, Tesselator tesselator,
                                                    Operation<Void> original) {
        if (!(source instanceof QuestButton sourceButton)) {
            original.call(source, target, poseStack, halfWidth, red, green, blue, startAlpha, endAlpha,
                    textureOffset, tesselator);
            return;
        }
        Quest dependent = ((QuestButtonAccessor) sourceButton).cosmiccore$getQuest();
        Quest dependency = ((QuestButtonAccessor) target).cosmiccore$getQuest();
        DependencyLineSettings settings = ((QuestDependencyLineExtension) (Object) dependent)
                .cosmiccore$getDependencyLineSettings(dependency.id);
        Quest viewed = questScreen.getViewedQuest();
        boolean reveal = sourceButton.isMouseOver() || target.isMouseOver() || viewed == dependent ||
                viewed == dependency || questScreen.getSelectedQuests().contains(dependent) ||
                questScreen.getSelectedQuests().contains(dependency);
        if (!settings.visible() && !reveal) {
            return;
        }
        cosmiccore$resolveDependencyLine(settings).bindTexture();
        float renderedHalfWidth = settings.asset().equals(DependencyLineSettings.MAIN_QUESTLINE_ASSET) ?
                halfWidth * 1.5F : halfWidth;
        original.call(source, target, poseStack, renderedHalfWidth, red, green, blue, startAlpha, endAlpha,
                textureOffset, tesselator);
    }

    @WrapOperation(
                   method = "drawOffsetBackground",
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestPanel;renderConnection(Ldev/ftb/mods/ftblibrary/ui/Widget;Ldev/ftb/mods/ftbquests/client/gui/quests/QuestButton;Lcom/mojang/blaze3d/vertex/PoseStack;FIIIIIFLcom/mojang/blaze3d/vertex/Tesselator;)V",
                            ordinal = 0))
    private void cosmiccore$skipBasePassForActiveCustomLine(QuestPanel instance, Widget source, QuestButton target,
                                                            PoseStack poseStack, float halfWidth, int red, int green,
                                                            int blue, int startAlpha, int endAlpha,
                                                            float textureOffset, Tesselator tesselator,
                                                            Operation<Void> original) {
        if (source instanceof QuestButton sourceButton) {
            Quest dependent = ((QuestButtonAccessor) sourceButton).cosmiccore$getQuest();
            Quest dependency = ((QuestButtonAccessor) target).cosmiccore$getQuest();
            DependencyLineSettings settings = ((QuestDependencyLineExtension) (Object) dependent)
                    .cosmiccore$getDependencyLineSettings(dependency.id);
            if (!settings.asset().isEmpty() && (sourceButton.isMouseOver() || target.isMouseOver())) {
                return;
            }
        }
        original.call(instance, source, target, poseStack, halfWidth, red, green, blue, startAlpha, endAlpha,
                textureOffset, tesselator);
    }

    @Unique
    private ImageIcon cosmiccore$resolveDependencyLine(DependencyLineSettings settings) {
        Icon icon = settings.assetLocation()
                .<Icon>map(Icon::getIcon)
                .orElseGet(() -> questScreen.getSelectedChapter()
                        .<Icon>map(ThemeProperties.DEPENDENCY_LINE_TEXTURE::get)
                        .orElse(cosmiccore$defaultDependencyLine));
        return icon instanceof ImageIcon image ? image : cosmiccore$defaultDependencyLine;
    }
}
