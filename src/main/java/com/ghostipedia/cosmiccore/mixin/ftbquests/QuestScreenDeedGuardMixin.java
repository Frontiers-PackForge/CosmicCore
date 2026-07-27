package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.client.compat.ftbquests.DeedQuestAccess;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenDeedGuardMixin {

    @WrapMethod(method = "viewQuest(Ldev/ftb/mods/ftbquests/quest/Quest;Z)V")
    private void cosmiccore$guardDeedQuestPanel(Quest quest, boolean addHistory, Operation<Void> original) {
        if (quest == null || DeedQuestAccess.canOpenPanel(quest)) original.call(quest, addHistory);
    }
}
