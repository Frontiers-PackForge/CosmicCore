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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class NoctyxStack implements INBTSerializable<CompoundTag> {

    public static final NoctyxStack EMPTY = new NoctyxStack(NoctyxTypes.EMPTY, 0);

    public static final Codec<NoctyxStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NoctyxType.CODEC.fieldOf("type").forGetter(NoctyxStack::getType),
            Codec.INT.fieldOf("amount").forGetter(NoctyxStack::getAmount))
            .apply(instance, NoctyxStack::new));

    @Getter
    private @NotNull NoctyxType type = NoctyxTypes.EMPTY;
    private int amount;

    public static NoctyxStack of(NoctyxType type, int amount) {
        return new NoctyxStack(type, amount);
    }

    public static NoctyxStack of(NoctyxStack stack, int amount) {
        return new NoctyxStack(stack.getType(), amount);
    }

    public static NoctyxStack of(NoctyxStack other) {
        return of(other.type, other.amount);
    }

    public static NoctyxStack of(CompoundTag tag) {
        var stack = new NoctyxStack();
        stack.deserializeNBT(tag);
        return stack;
    }

    public int getAmount() {
        return isEmpty() ? 0 : this.amount;
    }

    public void setAmount(int amount) {
        if (type == NoctyxTypes.EMPTY) throw new IllegalStateException("Can't modify the empty stack");
        this.amount = amount;
    }

    public void setType(@NotNull NoctyxType type) {
        if (this.type == NoctyxTypes.EMPTY) throw new IllegalStateException("Can't modify the empty stack");
        this.type = type;
    }

    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        setAmount(this.amount - amount);
    }

    public NoctyxStack copy() {
        return new NoctyxStack(type, amount);
    }

    public NoctyxStack copyAmount(int amount) {
        return new NoctyxStack(type, amount);
    }

    public NoctyxStack copyFrom(@NotNull NoctyxStack other) {
        this.setType(other.getType());
        this.setAmount(other.getAmount());
        return this;
    }

    public boolean isEmpty() {
        return amount <= 0 || type.equals(NoctyxTypes.EMPTY);
    }

    public static boolean isEmpty(NoctyxStack stack) {
        return stack == null || stack.isEmpty();
    }

    public boolean isSameType(NoctyxStack other) {
        if (other == null) {
            return false;
        }
        return this.type.equals(other.getType());
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
        tag.putInt("amount", getAmount());
        if (isEmpty()) {
            return tag;
        }
        tag.put("type", type.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        amount = tag.getInt("amount");
        if (amount <= 0) {
            this.type = NoctyxTypes.EMPTY;
        } else {
            if (tag.contains("type")) {
                type = new NoctyxType(tag.getCompound("type"));
            } else {
                type = NoctyxTypes.EMPTY;
            }
        }
    }

    @Override
    public String toString() {
        return getType().name() + ": " + getAmount();
    }

    public MutableComponent displayName() {
        return type.getDisplayName().append(": ").append(String.valueOf(amount));
    }

    public static final IContentSerializer<NoctyxStack> SERIALIZER = new IContentSerializer<>() {

        @Override
        public NoctyxStack fromJson(JsonElement json) {
            if (!json.isJsonObject()) {
                return EMPTY;
            }
            var jsonObject = GsonHelper.convertToJsonObject(json, "ingredient");
            var amount = GsonHelper.getAsInt(jsonObject, "amount");
            if (amount <= 0) {
                return EMPTY;
            }
            var type = new NoctyxType(GsonHelper.getAsJsonObject(jsonObject, "type"));
            return new NoctyxStack(type, amount);
        }

        @Override
        public JsonElement toJson(NoctyxStack content) {
            var json = new JsonObject();
            json.addProperty("amount", content.amount);
            if (!content.isEmpty()) {
                json.add("type", content.getType().toJson());
            }
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
            return EMPTY;
        }

        @Override
        public Codec<NoctyxStack> codec() {
            return CODEC;
        }
    };
}
