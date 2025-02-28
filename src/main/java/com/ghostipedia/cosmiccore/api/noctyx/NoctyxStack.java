package com.ghostipedia.cosmiccore.api.noctyx;

import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.util.INBTSerializable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Setter
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class NoctyxStack implements INBTSerializable<CompoundTag> {

    public static final Codec<NoctyxStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NoctyxType.CODEC.fieldOf("type").forGetter(NoctyxStack::type),
            Codec.INT.fieldOf("amount").forGetter(NoctyxStack::amount))
            .apply(instance, NoctyxStack::new));

    private NoctyxType type;
    private int amount;

    public NoctyxStack copy() {
        return new NoctyxStack(type, amount);
    }

    public NoctyxStack copyAmount(int amount) {
        return new NoctyxStack(type, amount);
    }

    public NoctyxStack copyFrom(NoctyxStack other) {
        this.type(other.type());
        this.amount(other.amount());
        return this;
    }

    public boolean isEmpty() {
        return amount <= 0 || type == null;
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof NoctyxStack that)) return false;

        return amount == that.amount && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(type);
        result = 31 * result + amount;
        return result;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (type != null) {
            tag.put("type", type.serializeNBT());
        }
        tag.putInt("amount", amount);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("type")) {
            type = new NoctyxType(tag.getCompound("type"));
        }
    }

    @Override
    public String toString() {
        return type().name() + ": " + amount();
    }

    public MutableComponent displayName() {
        return type.getDisplayName().append(": ").append(String.valueOf(amount));
    }

    public static final IContentSerializer<NoctyxStack> SERIALIZER = new IContentSerializer<>() {

        @Override
        public NoctyxStack fromJson(JsonElement json) {
            if (!json.isJsonObject()) {
                return null;
            }
            var jsonObject = GsonHelper.convertToJsonObject(json, "ingredient");
            var type = new NoctyxType(GsonHelper.getAsJsonObject(jsonObject, "type"));
            var amount = GsonHelper.getAsInt(jsonObject, "amount");
            return new NoctyxStack(type, amount);
        }

        @Override
        public JsonElement toJson(NoctyxStack content) {
            var json = new JsonObject();
            json.add("type", content.type().toJson());
            json.addProperty("amount", content.amount);
            return json;
        }

        @Override
        public NoctyxStack of(Object o) {
            if (o instanceof NoctyxStack stack) {
                return stack.copy();
            }
            return null;
        }

        @Override
        public NoctyxStack defaultValue() {
            return new NoctyxStack();
        }

        @Override
        public Codec<NoctyxStack> codec() {
            return CODEC;
        }
    };
}
