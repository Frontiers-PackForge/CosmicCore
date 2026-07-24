package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineSettings;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.QuestDependencyLineExtension;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestLinkButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(value = QuestButton.class, remap = false)
public class QuestButtonAliasMixin {

    @Shadow
    @Final
    private Quest quest;

    @Inject(method = "getDependencies", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$removeAliasDependencies(CallbackInfoReturnable<Collection<QuestButton>> cir) {
        if ((Object) this instanceof QuestLinkButton) {
            cir.setReturnValue(List.of());
        }
    }

    @ModifyReturnValue(method = "getDependencies", at = @At("RETURN"))
    private Collection<QuestButton> cosmiccore$routeDependencyTargets(Collection<QuestButton> original) {
        if ((Object) this instanceof QuestLinkButton || original.isEmpty()) return original;
        return original.stream().filter(this::cosmiccore$isDependencyTarget).toList();
    }

    @Unique
    private boolean cosmiccore$isDependencyTarget(QuestButton target) {
        Quest dependency = ((QuestButtonAccessor) target).cosmiccore$getQuest();
        DependencyLineSettings settings = ((QuestDependencyLineExtension) (Object) quest)
                .cosmiccore$getDependencyLineSettings(dependency.id);
        long targetLinkId = settings.targetLinkId();
        if (targetLinkId == 0L || !cosmiccore$isValidAliasRoute(dependency, targetLinkId)) {
            return !(target instanceof QuestLinkButton);
        }
        return target instanceof QuestLinkButton linkButton &&
                ((QuestLinkButtonAccessor) linkButton).cosmiccore$getLink().id == targetLinkId;
    }

    @Unique
    private static boolean cosmiccore$isValidAliasRoute(Quest dependency, long targetLinkId) {
        return dependency.getQuestFile().getBase(targetLinkId) instanceof QuestLink link && link.linksTo(dependency);
    }

    @WrapOperation(
                   method = "draw",
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestScreen;getViewedQuest()Ldev/ftb/mods/ftbquests/quest/Quest;"))
    private Quest cosmiccore$highlightAliasGroup(QuestScreen instance, Operation<Quest> original) {
        Quest viewed = original.call(instance);
        QuestButton button = (QuestButton) (Object) this;
        Panel parent = button.getParent();
        if (parent instanceof QuestPanel panel) {
            QuestButton hovered = ((QuestPanelAccessor) panel).cosmiccore$getMouseOverQuest();
            if (hovered != null && ((QuestButtonAccessor) hovered).cosmiccore$getQuest() == quest &&
                    (hovered == button || hovered instanceof QuestLinkButton || button instanceof QuestLinkButton)) {
                return quest;
            }
        }
        return viewed;
    }

    @Inject(method = "addMouseOverText", at = @At("TAIL"))
    private void cosmiccore$describeAlias(TooltipList tooltip, CallbackInfo ci) {
        if ((Object) this instanceof QuestLinkButton) {
            tooltip.add(Component.translatable("cosmiccore.ftbquests.quest_alias").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("cosmiccore.ftbquests.quest_alias.hint").withStyle(ChatFormatting.GRAY));
        }
    }
}
