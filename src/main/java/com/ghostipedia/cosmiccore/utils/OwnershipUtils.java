package com.ghostipedia.cosmiccore.utils;

import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.UsernameCache;

import java.util.List;

public class OwnershipUtils {

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
