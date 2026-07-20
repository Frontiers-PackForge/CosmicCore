package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.client.compat.create.StockKeeperCraftingPlanner;

import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperRequestScreenMixin {

    @Shadow
    public List<BigItemStack> itemsToOrder;

    @Shadow
    public List<CraftableBigItemStack> recipesToOrder;

    @Shadow
    private boolean canRequestCraftingPackage;

    @Inject(method = "requestCraftable", at = @At("HEAD"))
    private void cosmiccore$beginRemainderAwareRequest(
                                                       CraftableBigItemStack recipe, int requestedDifference,
                                                       CallbackInfo ci) {
        StockKeeperCraftingPlanner.beginRequest(recipe, requestedDifference);
    }

    @Inject(method = "requestCraftable", at = @At("RETURN"))
    private void cosmiccore$endRemainderAwareRequest(
                                                     CraftableBigItemStack recipe, int requestedDifference,
                                                     CallbackInfo ci) {
        StockKeeperCraftingPlanner.endRequest();
    }

    @Inject(method = "updateCraftableAmounts", at = @At("HEAD"))
    private void cosmiccore$beginSharedToolCapacityPass(CallbackInfo ci) {
        StockKeeperCraftingPlanner.beginCapacityPass();
    }

    @WrapMethod(
                method = "maxCraftable(Lcom/simibubi/create/content/logistics/stockTicker/CraftableBigItemStack;Lcom/simibubi/create/content/logistics/packager/InventorySummary;Ljava/util/function/Function;I)Lnet/createmod/catnip/data/Pair;")
    private Pair<Integer, List<List<BigItemStack>>> cosmiccore$accountForReusableIngredients(
                                                                                             CraftableBigItemStack recipe,
                                                                                             InventorySummary summary,
                                                                                             Function<ItemStack, Integer> countModifier,
                                                                                             int newTypeLimit,
                                                                                             Operation<Pair<Integer, List<List<BigItemStack>>>> original) {
        StockKeeperCraftingPlanner.DirectRequest directRequest = StockKeeperCraftingPlanner.takeRequest(recipe,
                recipe.getOutputCount(Minecraft.getInstance().level));
        Pair<Integer, List<List<BigItemStack>>> planned = StockKeeperCraftingPlanner.plan(
                recipe,
                summary,
                countModifier,
                itemsToOrder,
                recipesToOrder,
                Minecraft.getInstance().player,
                newTypeLimit,
                directRequest,
                recipe.getOutputCount(Minecraft.getInstance().level));
        return planned != null ? planned : original.call(recipe, summary, countModifier, newTypeLimit);
    }

    @Inject(method = "updateCraftableAmounts", at = @At("RETURN"))
    private void cosmiccore$disableInvalidReusableCraftingPackages(CallbackInfo ci) {
        try {
            if (recipesToOrder.stream().anyMatch(recipe -> StockKeeperCraftingPlanner.hasReusableIngredient(
                    recipe, itemsToOrder, Minecraft.getInstance().player))) {
                canRequestCraftingPackage = false;
            }
        } finally {
            StockKeeperCraftingPlanner.endCapacityPass();
        }
    }
}
