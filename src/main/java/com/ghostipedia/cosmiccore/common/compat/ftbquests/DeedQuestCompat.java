package com.ghostipedia.cosmiccore.common.compat.ftbquests;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;

final class DeedQuestCompat {

    private static final TaskType TASK_TYPE = TaskTypes
            .register(CosmicCore.id("deed"), DeedTask::new, () -> Icons.ACCEPT_GRAY)
            .setDisplayName(Component.translatable("button.cosmiccore.deeds"));
    private static final Set<UUID> RECONCILING = new HashSet<>();
    private static final Set<MinecraftServer> RELOADS_QUEUED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean registered;

    private DeedQuestCompat() {}

    static TaskType taskType() {
        return TASK_TYPE;
    }

    static void register() {
        if (registered) return;
        registered = true;
        ClearFileCacheEvent.EVENT.register(file -> {
            if (file instanceof ServerQuestFile serverFile) queueReloadReconcile(serverFile);
        });
        ObjectCompletedEvent.QUEST.register(event -> reconcileCompleted(event.getData()));
        ObjectCompletedEvent.TASK.register(event -> reconcileCompleted(event.getData()));
    }

    private static EventResult reconcileCompleted(TeamData teamData) {
        if (teamData.getFile() instanceof ServerQuestFile serverFile) {
            reconcileTeam(serverFile.server, teamData.getTeamId().toString());
        }
        return EventResult.pass();
    }

    static void reconcileTeam(MinecraftServer server, String teamKey) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null || file.server != server) return;
        UUID teamId;
        try {
            teamId = UUID.fromString(teamKey);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        if (!RECONCILING.add(teamId)) return;
        TeamData teamData = file.getOrCreateTeamData(teamId);
        boolean syncPlayers = false;
        try {
            DeedLedger ledger = DeedLedger.get(server);
            for (var task : file.getAllTasks()) {
                if (!(task instanceof DeedTask deedTask)) continue;
                boolean woven = DeedsAPI.isWoven(server, teamKey, deedTask.deedId());
                if (!woven && DeedRegistry.get(deedTask.deedId()) != null &&
                        deedTask.presentation().grantsWhenReady() &&
                        deedTask.requirementsComplete(teamData)) {
                    syncPlayers |= ledger.grantCoil(teamKey, deedTask.deedId());
                }
                long expected = woven ? 1L : 0L;
                if (teamData.getProgress(deedTask) != expected ||
                        expected == 0L && (teamData.isStarted(deedTask) || teamData.isCompleted(deedTask))) {
                    teamData.setProgress(deedTask, expected);
                }
            }
        } finally {
            RECONCILING.remove(teamId);
        }
        if (syncPlayers) {
            for (var player : teamData.getOnlineMembers()) {
                DeedsAPI.syncPlayer(player);
            }
        }
    }

    private static void queueReloadReconcile(ServerQuestFile file) {
        MinecraftServer server = file.server;
        synchronized (RELOADS_QUEUED) {
            if (!RELOADS_QUEUED.add(server)) return;
        }
        server.execute(() -> {
            synchronized (RELOADS_QUEUED) {
                RELOADS_QUEUED.remove(server);
            }
            if (ServerQuestFile.INSTANCE != file) return;
            for (TeamData teamData : file.getAllTeamData()) {
                reconcileTeam(server, teamData.getTeamId().toString());
            }
        });
    }
}
