package com.ghostipedia.cosmiccore.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;

import net.minecraft.world.effect.MobEffectInstance;

import com.mojang.datafixers.util.Pair;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@NoArgsConstructor
public class EffectApplicationBehavior implements IItemComponent/* , IItemLifeCycle */ {

    private final List<Pair<Supplier<MobEffectInstance>, Float>> effects = new ArrayList<>();

    public EffectApplicationBehavior addEffect(Supplier<MobEffectInstance> effect, float chance) {
        effects.add(Pair.of(effect, chance));
        return this;
    }

    public List<Pair<MobEffectInstance, Float>> getEffects() {
        return this.effects.stream()
                .map((pair) -> Pair.of(pair.getFirst() != null ? pair.getFirst().get() : null, pair.getSecond()))
                .collect(Collectors.toList());
    }

    /*
     * @Override
     * public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
     * if (entity instanceof LivingEntity living) {
     * for (var effect : getEffects()) {
     * if (level.getRandom().nextFloat() < effect.getSecond()) {
     * living.addEffect(new MobEffectInstance(effect.getFirst()));
     * }
     * }
     * }
     * }
     */
}
