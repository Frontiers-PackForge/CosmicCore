package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.client.compat.ftbquests.DependencyLineMenus;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineSettings;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.QuestDependencyLineExtension;

import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestLinkButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if ((Object) this instanceof QuestLinkButton linkButton) {
            QuestLink link = ((QuestLinkButtonAccessor) linkButton).cosmiccore$getLink();
            return DependencyLineMenus.appendAliasTop(items, questScreen, quest, link);
        }
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
        return DependencyLineMenus.appendSelected(items, questScreen, quest);
    }

    @Inject(
            method = "editDependency",
            at = @At(
                     value = "INVOKE",
                     target = "Ldev/ftb/mods/ftbquests/net/EditObjectMessage;forQuestObject(Ldev/ftb/mods/ftbquests/quest/QuestObjectBase;)Ldev/ftb/mods/ftbquests/net/EditObjectMessage;"))
    private void cosmiccore$routeNewDependencyThroughSelectedAlias(Quest dependent, QuestObject dependency,
                                                                   boolean add, CallbackInfo ci) {
        if (!add || !(dependency instanceof Quest dependencyQuest)) return;
        long selectedAliasId = 0L;
        for (var selected : ((QuestScreenAccessor) questScreen).cosmiccore$getSelectedObjects()) {
            if (selected instanceof QuestLink link && link.linksTo(dependencyQuest)) {
                selectedAliasId = link.id;
            }
        }
        if (selectedAliasId == 0L) return;
        QuestDependencyLineExtension extension = (QuestDependencyLineExtension) (Object) dependent;
        DependencyLineSettings settings = extension.cosmiccore$getDependencyLineSettings(dependencyQuest.id);
        extension.cosmiccore$setDependencyLineSettings(
                dependencyQuest.id,
                settings.withTargetLinkId(selectedAliasId));
    }
}
