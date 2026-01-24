package com.ghostipedia.cosmiccore.common.glm;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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

        Material material = null;
        for (ItemStack stack : generatedLoot) {
            var materialStack = ChemicalHelper.getMaterialStack(stack);
            if (materialStack != null && materialStack.material() != null) {
                Material mat = materialStack.material();
                if (mat.hasProperty(PropertyKey.ORE)) {
                    material = mat;
                    break;
                }
            }
        }

        if (material == null) {
            return generatedLoot;
        }

        ItemStack rawOre = ChemicalHelper.get(TagPrefix.rawOre, material);
        if (rawOre.isEmpty()) {
            return generatedLoot;
        }

        int dropCount = 1;
        var oreProperty = material.getProperty(PropertyKey.ORE);
        if (oreProperty != null) {
            dropCount = oreProperty.getOreMultiplier();
        }

        generatedLoot.clear();
        ItemStack drop = rawOre.copy();
        drop.setCount(dropCount);
        generatedLoot.add(drop);

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
