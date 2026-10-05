package com.ghostipedia.cosmiccore.utils;

import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.UsernameCache;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class OwnershipUtils {

    public static @Nullable UUID getTeamUUID(@Nullable MachineOwner owner) {
        if (owner == null) return null;
        if (owner instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getTeam();
            if (team != null) return team.getTeamId();
        }
        return owner.getPlayerUUID();
    }

    public static String getName(MachineOwner owner) {
        if (owner instanceof PlayerOwner playerOwner) {
            return UsernameCache.getLastKnownUsername(playerOwner.getUUID());
        } else if (owner instanceof FTBOwner ftOwner) {
            return ftOwner.getTeam().getName().getString();
        } else return "NaN";
    }

    public static void addOwnerLine(List<Component> textList, MachineOwner owner, boolean withNetworkOwner) {
        if (owner instanceof PlayerOwner playerOwner) {
            var name = UsernameCache.getLastKnownUsername(playerOwner.getUUID());
            textList.add(Component.translatable("behavior.wireless_data.owner.player").append(name));
        } else if (owner instanceof FTBOwner ftOwner) {
            textList.add(Component.translatable("behavior.wireless_data.owner.team").append(ftOwner.getName()));
        }
    }
}
