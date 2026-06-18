package com.ghostipedia.cosmiccore.common.glm;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.Tags;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

public class NoSilkTouchOreLootModifier extends LootModifier {

    public static final Codec<NoSilkTouchOreLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, NoSilkTouchOreLootModifier::new));

    /**
     * Re-entry guard. We re-run the block's loot table with a "fake" tool to get the
     * non-silk-touch drops, but that re-fires every global loot modifier — including this one.
     * For tools whose silk-touch comes from a non-Forge source (e.g. Tinkers' modifier system),
     * stripping the standard enchantment NBT doesn't make {@code getEnchantmentLevel(SILK_TOUCH)}
     * return 0, so the bailout at the top doesn't trigger and we'd recurse forever (StackOverflow,
     * server crash). The flag short-circuits the inner call cleanly without depending on how the
     * tool reports enchantments.
     */
    private static final ThreadLocal<Boolean> REENTRY = ThreadLocal.withInitial(() -> false);

    protected NoSilkTouchOreLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                          LootContext context) {
        if (REENTRY.get()) return generatedLoot;

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

        // Use a vanilla netherite pickaxe as the fake tool. Copying the original tool and stripping
        // its enchantment NBT doesn't actually remove silk-touch behavior for tools whose silk
        // touch lives outside Forge's enchantment system (Tinkers' Silky modifier, Apotheosis
        // affixes, any other modded tool). A clean vanilla pickaxe has none of those layers, mines
        // any tier, and gets the recipient ore's natural non-silk-touch drops. Fortune is the only
        // enchantment that affects ore drop counts, so we forward that and drop everything else.
        ItemStack fakeTool = new ItemStack(Items.NETHERITE_PICKAXE);
        int fortune = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        if (fortune > 0) {
            fakeTool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
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

        REENTRY.set(true);
        try {
            generatedLoot.clear();
            generatedLoot.addAll(state.getDrops(builder));
        } finally {
            REENTRY.set(false);
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
