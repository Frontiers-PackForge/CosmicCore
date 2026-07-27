package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import com.ghostipedia.cosmiccore.client.mirror.ClientDeedCache;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedTask;
import com.ghostipedia.cosmiccore.common.config.CosmicCoreConfig;
import com.ghostipedia.cosmiccore.common.mirror.deed.Deed;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;

import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.jetbrains.annotations.Nullable;

public final class DeedQuestAccess {

    private DeedQuestAccess() {}

    @Nullable
    public static DeedTask task(Quest quest) {
        for (Task task : quest.getTasks()) {
            if (task instanceof DeedTask deedTask) return deedTask;
        }
        return null;
    }

    public static boolean canOpenPanel(Quest quest) {
        DeedTask task = task(quest);
        if (task == null) return true;
        if (ClientQuestFile.INSTANCE.canEdit()) return CosmicCoreConfig.devVisor();
        if (ClientDeedCache.woven().contains(task.deedId())) return true;
        return task.disclosure() != DeedTask.Disclosure.ASCENSION;
    }

    public static boolean sealsText(Quest quest) {
        DeedTask task = task(quest);
        if (task == null || task.disclosure() != DeedTask.Disclosure.SEALED) return false;
        if (ClientQuestFile.INSTANCE.canEdit() && CosmicCoreConfig.devVisor()) return false;
        return !ClientDeedCache.woven().contains(task.deedId());
    }

    public static Component sealedHint(DeedTask task) {
        Deed deed = DeedRegistry.get(task.deedId());
        String key = deed == null ? "cosmiccore.ftbquests.deed.sealed_hint" : deed.nameKey() + ".sealed_hint";
        return Component.translatable(key);
    }
}
