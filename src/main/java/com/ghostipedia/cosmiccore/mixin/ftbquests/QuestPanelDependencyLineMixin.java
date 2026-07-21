package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.client.compat.ftbquests.DependencyLineRenderer;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineSettings;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineStyle;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.QuestDependencyLineExtension;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestPanel.class, remap = false)
public class QuestPanelDependencyLineMixin {

    @Shadow
    @Final
    private QuestScreen questScreen;

    @Inject(method = "renderConnection", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$renderPerDependencyLine(Widget source, QuestButton target, PoseStack poseStack,
                                                    float halfWidth, int red, int green, int blue, int startAlpha,
                                                    int endAlpha, float textureOffset, Tesselator tesselator,
                                                    CallbackInfo ci) {
        if (!(source instanceof QuestButton sourceButton)) return;
        Quest dependent = ((QuestButtonAccessor) sourceButton).cosmiccore$getQuest();
        Quest dependency = ((QuestButtonAccessor) target).cosmiccore$getQuest();
        DependencyLineSettings settings = ((QuestDependencyLineExtension) (Object) dependent)
                .cosmiccore$getDependencyLineSettings(dependency.id);
        Quest viewed = questScreen.getViewedQuest();
        boolean reveal = sourceButton.isMouseOver() || target.isMouseOver() || viewed == dependent ||
                viewed == dependency || questScreen.getSelectedQuests().contains(dependent) ||
                questScreen.getSelectedQuests().contains(dependency);
        if (!settings.visible() && !reveal) {
            ci.cancel();
            return;
        }
        if (settings.style() == DependencyLineStyle.DEFAULT) return;

        DependencyLineRenderer.render(
                poseStack,
                source.getX() + source.width / 2.0,
                source.getY() + source.height / 2.0,
                target.getX() + target.width / 2.0,
                target.getY() + target.height / 2.0,
                halfWidth,
                red,
                green,
                blue,
                startAlpha,
                endAlpha,
                settings.style(),
                tesselator);
        ci.cancel();
    }
}
