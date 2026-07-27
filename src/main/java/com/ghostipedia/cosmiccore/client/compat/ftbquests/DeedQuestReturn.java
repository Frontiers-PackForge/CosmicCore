package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import net.minecraft.resources.ResourceLocation;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class DeedQuestReturn {

    private static final Map<ResourceLocation, Long> QUESTS = new HashMap<>();

    private DeedQuestReturn() {}

    public static void remember(ResourceLocation deedId, long questId) {
        QUESTS.put(deedId, questId);
    }

    @Nullable
    public static Long questId(ResourceLocation deedId) {
        return QUESTS.get(deedId);
    }

    public static void forget(ResourceLocation deedId) {
        QUESTS.remove(deedId);
    }

    public static void clear() {
        QUESTS.clear();
    }

    public static void open(long questId) {
        ClientQuestFile.openBookToQuestObject(questId);
    }
}
