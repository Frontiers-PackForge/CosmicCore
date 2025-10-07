package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.common.ascension.AscensionCap;
import com.ghostipedia.cosmiccore.common.ascension.AscensionConsumables;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SyncAscensionProgressPacket;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CosmicCurrencyItem extends ComponentItem {

    private final AscensionConsumables currency;
    private final long amount;


    public CosmicCurrencyItem(Properties props, AscensionConsumables currency, long amountPerUse) {
        super(props.stacksTo(64));
        this.currency = currency;
        this.amount = amountPerUse;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.consume(stack);
        }
        int toConsume = player.isShiftKeyDown() ? stack.getCount() : 1;
        if (toConsume <= 0) return InteractionResultHolder.pass(stack);
        sp.getCapability(AscensionCap.CAP).ifPresent(cap -> {
            cap.addCurrency(currency, amount * toConsume);
            stack.shrink(toConsume);
            CCoreNetwork.sendToPlayer(sp, new SyncAscensionProgressPacket(cap.save()));
        });
        stack.shrink(toConsume);
        level.playSound(null, sp.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f, 1.2f);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

}
