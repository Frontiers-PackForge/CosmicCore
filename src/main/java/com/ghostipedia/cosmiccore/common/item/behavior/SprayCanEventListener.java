package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import appeng.blockentity.networking.CableBusBlockEntity;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.ghostipedia.cosmiccore.common.data.CosmicItems.INFINITE_SPRAY_CAN;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SprayCanEventListener {

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        if (level.isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack can = player.getOffhandItem();
        var behavior = getSprayCanBehavior(can);
        if (behavior == null) return;
        if (hasSprayCan(can)) return;

        UseOnContext fakeContext = new UseOnContext(player, InteractionHand.OFF_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        if (!behavior.handleSpecialBlockEntities(level.getBlockEntity(pos),
                ConfigHolder.INSTANCE.tools.sprayCanChainLength, fakeContext)) {
            behavior.handleBlocks(pos, ConfigHolder.INSTANCE.tools.sprayCanChainLength, fakeContext);
        }

        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, player.position(), 1.0f, 1.0f);
    }


    // just pulls out some repeated code for cleanliness into this method to check if it is a spraycan
    static boolean hasSprayCan(ItemStack stack) {
        return stack.getItem() != INFINITE_SPRAY_CAN.get().asItem();
    }

    // gets the bahavior to reduce repeated code
    static InfiniteSprayCanBehavior getSprayCanBehavior(ItemStack stack) {
        if (!(stack.getItem() instanceof ComponentItem compItem)) return null;

        for (var component : compItem.getComponents()) {
            if (component instanceof InfiniteSprayCanBehavior behavior) {
                return behavior;
            }
        }
        return null;
    }
}
