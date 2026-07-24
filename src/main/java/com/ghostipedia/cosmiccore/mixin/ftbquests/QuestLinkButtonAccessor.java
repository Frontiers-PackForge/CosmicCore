package com.ghostipedia.cosmiccore.mixin.ftbquests;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestLinkButton;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = QuestLinkButton.class, remap = false)
public interface QuestLinkButtonAccessor {

    @Accessor("link")
    QuestLink cosmiccore$getLink();
}
