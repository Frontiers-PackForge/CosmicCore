package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void entityPlacementEventHandler(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() instanceof MetaMachineBlock block && block.getMachine(event.getLevel(), event.getPos()) instanceof SoulHatchPartMachine soulHatch
            && event.getEntity() instanceof Player player) {
            soulHatch.attachSoulNetwork(player);
        }
    }
}
