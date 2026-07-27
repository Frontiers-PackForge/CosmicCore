package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.client.compat.ftbquests.DeedQuestAccess;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedTask;

import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.List;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelDeedMixin {

    @Shadow
    private Quest quest;

    @WrapOperation(
                   method = "addWidgets",
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/ftb/mods/ftbquests/quest/Quest;getTitle()Lnet/minecraft/network/chat/Component;"))
    private Component cosmiccore$sealDeedTitle(Quest instance, Operation<Component> original) {
        return DeedQuestAccess.sealsText(instance) ?
                Component.translatable("cosmiccore.ftbquests.deed.sealed_title") : original.call(instance);
    }

    @WrapOperation(
                   method = "addWidgets",
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/ftb/mods/ftbquests/quest/Quest;getSubtitle()Lnet/minecraft/network/chat/Component;"))
    private Component cosmiccore$sealDeedSubtitle(Quest instance, Operation<Component> original) {
        return DeedQuestAccess.sealsText(instance) ? Component.empty() : original.call(instance);
    }

    @WrapOperation(
                   method = "addWidgets",
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/ftb/mods/ftbquests/quest/Quest;getTasks()Ljava/util/Collection;"))
    private Collection<Task> cosmiccore$hideDeedTask(Quest instance, Operation<Collection<Task>> original) {
        Collection<Task> tasks = original.call(instance);
        if (ClientQuestFile.INSTANCE.canEdit()) return tasks;
        return tasks.stream().filter(task -> !(task instanceof DeedTask)).toList();
    }

    @WrapOperation(
                   method = { "buildPageIndices", "addDescriptionText" },
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/ftb/mods/ftbquests/quest/Quest;getDescription()Ljava/util/List;"))
    private List<Component> cosmiccore$sealDeedDescription(Quest instance, Operation<List<Component>> original) {
        if (!DeedQuestAccess.sealsText(instance)) return original.call(instance);
        DeedTask task = DeedQuestAccess.task(quest);
        return task == null ? List.of() : List.of(DeedQuestAccess.sealedHint(task));
    }
}
