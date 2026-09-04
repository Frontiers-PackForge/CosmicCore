package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.FilterEntryAccessor;

import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.Filters;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = Filters.class, remap = false)
public abstract class FilterDataComponentWriterFixMixin {

    @Shadow
    @Final
    private static Map<Item, ?> FILTERS;

    @Inject(method = "loadFilter", at = @At("RETURN"))
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void cosmiccore$repairDataComponentWriter(Class<?> filterableType, ItemStack stack,
                                                             CallbackInfoReturnable<Filter<?>> cir) {
        var entry = FILTERS.get(stack.getItem());
        var filter = cir.getReturnValue();
        if (entry instanceof FilterEntryAccessor accessor && filter != null) {
            DataComponentType componentType = accessor.cosmiccore$getDataComponentType().value();
            filter.setItemWriter(value -> stack.set(componentType, value));
        }
    }
}
