package com.ghostipedia.cosmiccore.mixin.tinkers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Debug(
        export = true)
@Mixin(value = ModifiableItem.class, remap = false)
public class ModifiableItemMixin extends TieredItem {


    public ModifiableItemMixin(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return !ToolStack.from(stack).isBroken();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack stack = itemStack.copy();
        Player player = ForgeHooks.getCraftingPlayer();

        ToolDamageUtil.handleDamageItem(stack, 1, player, p -> {});

        return stack;
    }
}
