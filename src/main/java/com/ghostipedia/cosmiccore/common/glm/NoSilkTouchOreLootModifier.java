package com.ghostipedia.cosmiccore.common.glm;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

public class NoSilkTouchOreLootModifier extends LootModifier {

    public static final Codec<NoSilkTouchOreLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, NoSilkTouchOreLootModifier::new));

    protected NoSilkTouchOreLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                          LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) <= 0) {
            return generatedLoot;
        }

        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || !state.is(Tags.Blocks.ORES)) {
            return generatedLoot;
        }

        ItemStack blockItem = new ItemStack(state.getBlock());
        var materialStack = ChemicalHelper.getMaterialStack(blockItem);
        if (materialStack == null || materialStack.material() == null) {
            return generatedLoot;
        }

        if (!materialStack.material().hasProperty(PropertyKey.ORE)) {
            return generatedLoot;
        }

        ItemStack fakeTool = tool.copy();
        fakeTool.getEnchantmentTags().clear();
        for (var entry : tool.getAllEnchantments().entrySet()) {
            if (entry.getKey() != Enchantments.SILK_TOUCH) {
                fakeTool.enchant(entry.getKey(), entry.getValue());
            }
        }

        LootParams.Builder builder = new LootParams.Builder(context.getLevel())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, context.getParam(LootContextParams.ORIGIN))
                .withParameter(LootContextParams.TOOL, fakeTool);

        var entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null) {
            builder.withOptionalParameter(LootContextParams.THIS_ENTITY, entity);
        }

        var explosionRadius = context.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (explosionRadius != null) {
            builder.withOptionalParameter(LootContextParams.EXPLOSION_RADIUS, explosionRadius);
        }

        generatedLoot.clear();
        generatedLoot.addAll(state.getDrops(builder));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
