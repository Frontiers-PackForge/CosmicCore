package com.ghostipedia.cosmiccore.api.recipe.ingredient;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.stream.Stream;
public class TinkerIngredient extends Ingredient {

    public static final ResourceLocation TYPE = CosmicCore.id("tool_ingredient");

    @Getter
    private final ToolDefinition definition;

    private ItemStack[] cacheStacks;

    public TinkerIngredient(ToolDefinition definition) {
        super(Stream.empty());
        Preconditions.checkNotNull(definition);
        this.definition = definition;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("definition", definition.getId().toString());
        return json;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ModifiableItem)) return false;
        ToolStack toolStack = ToolStack.from(stack);

        if (toolStack.isBroken()) return false;

        return super.test(stack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public ItemStack[] getItems() {
        if (cacheStacks == null) {
            cacheStacks = new ItemStack[] { BuiltInRegistries.ITEM.get(definition.getId()).getDefaultInstance() };
        }
        return cacheStacks;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static final IIngredientSerializer<TinkerIngredient> SERIALIZER = new IIngredientSerializer<TinkerIngredient>() {

        @Override
        public TinkerIngredient parse(FriendlyByteBuf friendlyByteBuf) {
            ResourceLocation resLoc = friendlyByteBuf.readResourceLocation();
            var toolDef = ToolDefinitionLoader.getInstance();
            ToolDefinition def = toolDef.getRegisteredToolDefinitions().stream()
                    .filter(d -> d.getId().equals(resLoc))
                    .findFirst()
                    .orElse(null);
            return new TinkerIngredient(def);
        }

        @Override
        public TinkerIngredient parse(JsonObject jsonObject) {
            ResourceLocation resLoc = new ResourceLocation(jsonObject.get("definition").getAsString());
            var toolDef = ToolDefinitionLoader.getInstance();
            ToolDefinition def = toolDef.getRegisteredToolDefinitions().stream()
                    .filter(d -> d.getId().equals(resLoc))
                    .findFirst()
                    .orElse(null);
            return new TinkerIngredient(def);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, TinkerIngredient tinkerIngredient) {
            friendlyByteBuf.writeResourceLocation(tinkerIngredient.definition.getId());
        }
    };
}
