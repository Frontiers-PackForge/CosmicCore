package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.client.compat.ftbquests.DependencyLineMenus;

import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;
import java.util.List;

@Mixin(value = QuestButton.class, remap = false)
public class QuestButtonDependencyLineMenuMixin {

    @Shadow
    @Final
    protected QuestScreen questScreen;

    @Shadow
    @Final
    private Quest quest;

    @ModifyArg(
               method = "onClicked",
               at = @At(
                        value = "INVOKE",
                        target = "Ldev/ftb/mods/ftbquests/client/gui/ContextMenuBuilder;insertAtTop(Ljava/util/Collection;)Ldev/ftb/mods/ftbquests/client/gui/ContextMenuBuilder;"),
               index = 0)
    private Collection<ContextMenuItem> cosmiccore$addDependencyLinesToQuestMenu(
                                                                                 Collection<ContextMenuItem> items) {
        return DependencyLineMenus.appendTop(items, questScreen, quest);
    }

    @ModifyArg(
               method = "onClicked",
               at = @At(
                        value = "INVOKE",
                        target = "Ldev/ftb/mods/ftblibrary/ui/BaseScreen;openContextMenu(Ljava/util/List;)Ldev/ftb/mods/ftblibrary/ui/ContextMenu;"),
               index = 0)
    private List<ContextMenuItem> cosmiccore$addDependencyLinesToSelectedQuestMenu(List<ContextMenuItem> items) {
        if (!questScreen.getSelectedQuests().contains(quest)) return items;
        return DependencyLineMenus.append(items, questScreen, quest);
    }
}
