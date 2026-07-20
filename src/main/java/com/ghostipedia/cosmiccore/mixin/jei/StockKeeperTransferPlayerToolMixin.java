package com.ghostipedia.cosmiccore.mixin.jei;

import com.ghostipedia.cosmiccore.client.compat.create.StockKeeperCraftingPlanner;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(targets = "com.simibubi.create.compat.jei.StockKeeperTransferHandler", remap = false)
public abstract class StockKeeperTransferPlayerToolMixin {

    @Inject(
            method = "transferRecipeOnClient",
            at = @At(
                     value = "INVOKE",
                     target = "Lmezz/jei/common/transfer/RecipeTransferUtil;getRecipeTransferOperations(Lmezz/jei/api/helpers/IStackHelper;Ljava/util/Map;Ljava/util/List;Ljava/util/List;)Lmezz/jei/common/transfer/RecipeTransferOperationsResult;"))
    private void cosmiccore$includeHeldReusableTools(
                                                     StockKeeperRequestMenu container,
                                                     RecipeHolder<Recipe<?>> recipeHolder,
                                                     IRecipeSlotsView recipeSlots,
                                                     Player player,
                                                     boolean maxTransfer,
                                                     boolean doTransfer,
                                                     CallbackInfoReturnable<IRecipeTransferError> cir,
                                                     @Local Map<Slot, ItemStack> availableItemStacks) {
        List<ItemStack> reusable = StockKeeperCraftingPlanner.reusablePlayerStacks(player,
                recipeHolder.value().getIngredients());
        if (reusable.isEmpty()) {
            return;
        }
        SimpleContainer containerInventory = new SimpleContainer(reusable.size());
        for (int index = 0; index < reusable.size(); index++) {
            availableItemStacks.put(new Slot(containerInventory, index, 0, 0), reusable.get(index));
        }
    }
}
