package com.ghostipedia.cosmiccore.common.glm;

import com.ghostipedia.cosmiccore.mixin.accessor.LootTableAccessor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GenericLootModifier extends LootModifier {

    public static final Gson LOOT_GSON = Deserializers.createLootTableSerializer().create();

    // hack to parse the loot pools SINCE FORGE PATCHES THEM AND THEY DON'T HAVE CODECS
    // FUCK
    public static final Codec<List<LootPool>> LOOT_POOL_LIST_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> {
                JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
                JsonObject fullJson = new JsonObject();
                fullJson.add("pools", json);

                LootTable table = ForgeHooks.loadLootTable(LOOT_GSON, LootTableIdCondition.UNKNOWN_LOOT_TABLE, fullJson,
                        true);
                return ((LootTableAccessor) table).getPools();
            },
            lootPool -> new Dynamic<>(JsonOps.INSTANCE, LOOT_GSON.toJsonTree(lootPool)));

    public static final Codec<GenericLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(inst.group(
                    ResourceLocation.CODEC.fieldOf("loot_table_id").forGetter(GenericLootModifier::getLootTableId),
                    LOOT_POOL_LIST_CODEC.fieldOf("injected_loot").forGetter(GenericLootModifier::getInjectedLoot)))
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
    public Codec<? extends IGlobalLootModifier> codec() {
        return null;
    }
}
