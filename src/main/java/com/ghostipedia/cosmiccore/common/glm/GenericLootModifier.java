package com.ghostipedia.cosmiccore.common.glm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GenericLootModifier extends LootModifier {

    public static final MapCodec<GenericLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions")
                    .forGetter((GenericLootModifier lm) -> lm.conditions),
            ResourceLocation.CODEC.fieldOf("loot_table_id")
                    .forGetter((GenericLootModifier lm) -> lm.lootTableId),
            LootPool.CODEC.listOf().fieldOf("injected_loot")
                    .forGetter((GenericLootModifier lm) -> lm.injectedLoot))
            .apply(inst, GenericLootModifier::new));

    @Getter
    private final ResourceLocation lootTableId;
    @Getter
    private final List<LootPool> injectedLoot;

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */
    protected GenericLootModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTableId,
                                  List<LootPool> injectedLoot) {
        super(conditionsIn);
        this.lootTableId = lootTableId;
        this.injectedLoot = injectedLoot;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> original, LootContext context) {
        if (this.injectedLoot.isEmpty() || !context.getQueriedLootTableId().equals(this.lootTableId)) {
            return original;
        }
        for (LootPool pool : this.injectedLoot) {
            pool.addRandomItems(original::add, context);
        }
        return original;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
