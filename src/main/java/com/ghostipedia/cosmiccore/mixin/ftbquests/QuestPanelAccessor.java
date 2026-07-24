package com.ghostipedia.cosmiccore.mixin.ftbquests;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = QuestPanel.class, remap = false)
public interface QuestPanelAccessor {

    @Accessor("mouseOverQuest")
    QuestButton cosmiccore$getMouseOverQuest();
}
