package com.ghostipedia.cosmiccore.mixin.deployer;

import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimit;
import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;

import net.createmod.catnip.nbt.NBTHelper;
import net.liukrast.deployer.lib.logistics.board.OrderingPanelBehaviour;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OrderingPanelBehaviour.class)
public abstract class OrderingPanelStateMixin {

    @Unique
    private static final String COSMICCORE_PROMISE_LIMIT_KEY = "CosmicCorePromiseLimit";

    @Inject(method = "easyWrite", at = @At("RETURN"))
    private void cosmiccore$writePromiseLimit(
                                              CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket,
                                              CallbackInfo ci) {
        FactoryPanelBehaviour behaviour = cosmiccore$self();
        if (!FactoryGaugePromiseLimitSupport.isFluid(behaviour) ||
                !(behaviour instanceof FactoryGaugePromiseLimit promiseLimit)) {
            return;
        }
        nbt.putInt(COSMICCORE_PROMISE_LIMIT_KEY, promiseLimit.cosmiccore$getPromiseLimit());
    }

    @Inject(method = "easyRead", at = @At("RETURN"))
    private void cosmiccore$restoreState(
                                         CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket,
                                         CallbackInfo ci) {
        FactoryPanelBehaviour behaviour = cosmiccore$self();
        behaviour.recipeAddress = nbt.getString("RecipeAddress");
        behaviour.recipeOutput = nbt.contains("RecipeOutput", Tag.TAG_INT) ? nbt.getInt("RecipeOutput") : 1;
        behaviour.promiseClearingInterval = nbt.contains("PromiseClearingInterval", Tag.TAG_INT) ?
                nbt.getInt("PromiseClearingInterval") : -1;
        if (nbt.hasUUID("Freq")) behaviour.network = nbt.getUUID("Freq");
        behaviour.activeCraftingArrangement = nbt.contains("Craft", Tag.TAG_LIST) ?
                NBTHelper.readItemList(nbt.getList("Craft", Tag.TAG_COMPOUND), registries) : java.util.List.of();
        if (FactoryGaugePromiseLimitSupport.isFluid(behaviour) &&
                behaviour instanceof FactoryGaugePromiseLimit promiseLimit) {
            promiseLimit.cosmiccore$setPromiseLimit(
                    nbt.contains(COSMICCORE_PROMISE_LIMIT_KEY, Tag.TAG_INT) ? nbt.getInt(COSMICCORE_PROMISE_LIMIT_KEY) :
                            -1);
        }
    }

    @Inject(
            method = "tickRequests",
            at = @At(value = "INVOKE",
                     target = "Lnet/liukrast/deployer/lib/logistics/board/OrderingPanelBehaviour;resetTimer()V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void cosmiccore$limitPromises(CallbackInfo ci) {
        FactoryPanelBehaviour behaviour = cosmiccore$self();
        if (!FactoryGaugePromiseLimitSupport.isFluid(behaviour) ||
                !(behaviour instanceof FactoryGaugePromiseLimit promiseLimit)) {
            return;
        }
        int configured = promiseLimit.cosmiccore$getPromiseLimit();
        if (configured < 0) return;
        OrderingPanelBehaviour ordering = (OrderingPanelBehaviour) (Object) this;
        int limit = ordering.hasInteraction("restocker") ? configured :
                FactoryGaugePromiseLimitSupport.effectiveRecipeLimit(behaviour, configured);
        if (limit <= 0 || ordering.getPromised() >= limit) ci.cancel();
    }

    @Unique
    private FactoryPanelBehaviour cosmiccore$self() {
        return (FactoryPanelBehaviour) (Object) this;
    }
}
