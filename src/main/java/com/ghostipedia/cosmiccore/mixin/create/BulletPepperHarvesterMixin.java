package com.ghostipedia.cosmiccore.mixin.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HarvesterMovementBehaviour.class)
public abstract class BulletPepperHarvesterMixin {

    private static final ResourceLocation BULLET_PEPPER = ResourceLocation.fromNamespaceAndPath("mynethersdelight",
            "bullet_pepper");

    @Inject(method = "isValidCrop", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$excludeBulletPepperFromSeededCrops(
                                                               Level level, BlockPos pos, BlockState state,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (!cosmiccore$isBulletPepper(state)) return;

        cir.setReturnValue(false);
    }

    @Inject(method = "isValidOther", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$recognizeRipeBulletPepper(
                                                      Level level, BlockPos pos, BlockState state,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (!cosmiccore$isBulletPepper(state)) return;

        BooleanProperty lit = cosmiccore$findBooleanProperty(state, "lit");
        cir.setReturnValue(lit != null && state.getValue(lit));
    }

    @Inject(
            method = "visitNewPosition",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/simibubi/create/foundation/utility/BlockHelper;destroyBlockAs" +
                             "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;" +
                             "Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;" +
                             "FLjava/util/function/Consumer;)V",
                     shift = At.Shift.AFTER))
    private void cosmiccore$collectNativeBulletPepperBonus(
                                                           MovementContext context, BlockPos pos, CallbackInfo ci) {
        BlockState state = context.world.getBlockState(pos);
        if (!cosmiccore$isBulletPepper(state)) return;

        BooleanProperty lit = cosmiccore$findBooleanProperty(state, "lit");
        if (lit == null || !state.getValue(lit)) return;

        int bonus = context.world.random.nextInt(2);
        IntegerProperty age = cosmiccore$findIntegerProperty(state, "age");
        if (age != null && state.getValue(age) == 3) bonus++;
        if (bonus == 0) return;

        MovementBehaviour movementBehaviour = (MovementBehaviour) (Object) this;
        movementBehaviour.collectOrDropItem(context, new ItemStack(state.getBlock(), bonus));
    }

    @Inject(method = "cutCrop", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$resetHarvestedBulletPepper(
                                                       Level level, BlockPos pos, BlockState state,
                                                       CallbackInfoReturnable<BlockState> cir) {
        if (!cosmiccore$isBulletPepper(state)) return;

        BlockState harvestedState = cir.getReturnValue();
        if (!cosmiccore$isBulletPepper(harvestedState)) return;

        BooleanProperty lit = cosmiccore$findBooleanProperty(harvestedState, "lit");
        IntegerProperty age = cosmiccore$findIntegerProperty(harvestedState, "age");
        IntegerProperty pressure = cosmiccore$findIntegerProperty(harvestedState, "pressure");
        if (lit != null) harvestedState = harvestedState.setValue(lit, false);
        if (age != null) harvestedState = harvestedState.setValue(age, 0);
        if (pressure != null) harvestedState = harvestedState.setValue(pressure, 0);
        cir.setReturnValue(harvestedState);
    }

    private static boolean cosmiccore$isBulletPepper(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(BULLET_PEPPER);
    }

    private static BooleanProperty cosmiccore$findBooleanProperty(BlockState state, String name) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && property.getName().equals(name)) {
                return booleanProperty;
            }
        }
        return null;
    }

    private static IntegerProperty cosmiccore$findIntegerProperty(BlockState state, String name) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && property.getName().equals(name)) {
                return integerProperty;
            }
        }
        return null;
    }
}
