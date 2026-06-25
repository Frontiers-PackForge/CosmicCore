package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The DONK item: a GTCEu ComponentItem that opens the LDLib recipe-maker UI on right-click. GTCEu 8.0 moved its own
 * held-item UI to MUI2, so we bridge to LDLib's HeldItemUIFactory directly - use() opens the UI, and as an
 * {@link HeldItemUIFactory.IHeldItemUIHolder} this item supplies the ModularUI built by {@link RecipeMakerBehavior}.
 * use() is overridden (not delegated to a component) so the inherited MUI2 ComponentItem UI path never fires.
 */
public class RecipeMakerItem extends ComponentItem implements HeldItemUIFactory.IHeldItemUIHolder {

    public RecipeMakerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            HeldItemUIFactory.INSTANCE.openUI(serverPlayer, hand);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public ModularUI createUI(Player player, HeldItemUIFactory.HeldItemHolder holder) {
        return new RecipeMakerBehavior().createUI(holder, player);
    }
}
