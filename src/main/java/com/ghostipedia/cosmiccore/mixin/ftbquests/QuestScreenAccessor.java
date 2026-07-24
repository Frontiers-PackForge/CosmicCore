package com.ghostipedia.cosmiccore.mixin.ftbquests;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = QuestScreen.class, remap = false)
public interface QuestScreenAccessor {

    @Accessor("selectedObjects")
    List<Movable> cosmiccore$getSelectedObjects();
}
