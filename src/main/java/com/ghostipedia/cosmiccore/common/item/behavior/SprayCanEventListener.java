package com.ghostipedia.cosmiccore.common.item.behavior;

import appeng.api.util.AEColor;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.ghostipedia.cosmiccore.CosmicCore;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.http.io.SessionOutputBuffer;

import static com.ghostipedia.cosmiccore.common.data.CosmicItems.INFINITE_SPRAY_CAN;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SprayCanEventListener {

    @SubscribeEvent
    public static void onClickEvent(InputEvent.InteractionKeyMappingTriggered event) {
        // isPickBlock() returns button == 2 which is the middle mouse click
        if (!event.isPickBlock()) {
            return;
        }

        // grabs thine game
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        assert level != null;
        Player player = mc.player;
        if (player == null) {
            return;
        }
        if (mc.hitResult == null) {
            return;
        }
        ItemStack spraycan = player.getMainHandItem();
        if (spraycan.getItem() != INFINITE_SPRAY_CAN.get().asItem()) {
            return;

        }

        int dyeID = 0;
        DyeColor color = null;

        // check if it gets a block
        if (mc.hitResult.getType() == HitResult.Type.BLOCK) {


            BlockHitResult blockHit = (BlockHitResult) mc.hitResult;
            BlockPos pos = blockHit.getBlockPos();
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity != null) {
                if (entity instanceof ShulkerBoxBlockEntity shulker) {
                    color = shulker.getColor();
                } else if (entity instanceof CableBusBlockEntity cable) {
                    dyeID = cable.getColor().dye.getId();
                    color = DyeColor.values()[dyeID];
                }
            }

            //normal blocks
            BlockState state = level.getBlockState(pos);
            MapColor mapColor = state.getMapColor(level, pos);

            int id = mapColor.id;

            // map id to dye
            if (id >= 15 && id <= 29) {
                dyeID = id - 14;
                color = DyeColor.byId(dyeID);
            } else if (id >= 37 && id <= 51) {
                dyeID = id - 36;
                color = DyeColor.byId(dyeID);
            } else if(id == 8 || id == 36){

                dyeID = 0;
                color = DyeColor.byId(dyeID);

            }




        }


        // send to spraycan when finished
        if (spraycan.getItem() instanceof ComponentItem compItem && color != null) {

            for (var component : compItem.getComponents()) {
                if (component instanceof InfiniteSprayCanBehavior behavior) {
                    behavior.setColor(color);
                    behavior.PrintColorToActionBar(player, behavior.getColor());
                    event.setCanceled(true);
                }
            }

        }
    }


}


