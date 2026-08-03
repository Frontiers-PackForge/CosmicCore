package com.ghostipedia.cosmiccore.common.dimension;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class FirmamentPortalActivation {

    private FirmamentPortalActivation() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getEntity().getItemInHand(event.getHand());
        if (!held.is(Items.WATER_BUCKET)) return;
        FirmamentPortalShape.Found found = FirmamentPortalShape.find(event.getLevel(), event.getPos());
        if (found == null) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel().isClientSide) return;

        FirmamentPortalShape.fill(event.getLevel(), found);
        if (!event.getEntity().getAbilities().instabuild) {
            event.getEntity().setItemInHand(event.getHand(), new ItemStack(Items.BUCKET));
        }
        event.getLevel().playSound(null, event.getPos(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
