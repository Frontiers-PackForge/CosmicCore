package com.ghostipedia.cosmiccore.api.noctyx;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

// todo: remove interface abstraction that doesn't help
@NoArgsConstructor
@AllArgsConstructor
@MethodsReturnNonnullByDefault
public class NoctyxType implements ITagSerializable<CompoundTag> {

    @Accessors(fluent = true)
    @Getter
    protected String lang;
    @Accessors(fluent = true)
    @Getter
    protected float alpha;
    @Accessors(fluent = true)
    @Getter
    protected float red;
    @Accessors(fluent = true)
    @Getter
    protected float green;
    @Accessors(fluent = true)
    @Getter
    protected float blue;

    public NoctyxType(CompoundTag tag) {
        deserializeNBT(tag);
    }

    public NoctyxType(JsonObject json) {
        fromJson(json);
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(lang)
                .withStyle(style -> style.withColor(ColorUtils.color(alpha, red, green, blue)));
    }

    public String name() {
        return getDisplayName().getString();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString("lang", lang);
        tag.putFloat("a", alpha);
        tag.putFloat("r", red);
        tag.putFloat("g", green);
        tag.putFloat("b", blue);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        lang = tag.getString("lang");
        alpha = tag.getFloat("a");
        red = tag.getFloat("r");
        green = tag.getFloat("g");
        blue = tag.getFloat("b");
    }

    public JsonElement toJson() {
        var json = new JsonObject();
        json.addProperty("lang", lang);
        json.addProperty("a", alpha);
        json.addProperty("r", red);
        json.addProperty("g", green);
        json.addProperty("b", blue);
        return json;
    }

    public void fromJson(JsonElement json) {
        if (!json.isJsonObject()) {
            return;
        }
        var jsonObject = GsonHelper.convertToJsonObject(json, "type");
        this.lang = jsonObject.getAsJsonObject("lang").getAsString();
        this.alpha = jsonObject.getAsJsonObject("a").getAsFloat();
        this.red = jsonObject.getAsJsonObject("r").getAsFloat();
        this.green = jsonObject.getAsJsonObject("g").getAsFloat();
        this.blue = jsonObject.getAsJsonObject("b").getAsFloat();
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof NoctyxType that)) return false;

        return Float.compare(alpha, that.alpha) == 0 && Float.compare(red, that.red) == 0 &&
                Float.compare(green, that.green) == 0 && Float.compare(blue, that.blue) == 0 && lang.equals(that.lang);
    }

    @Override
    public int hashCode() {
        int result = lang.hashCode();
        result = 31 * result + Float.hashCode(alpha);
        result = 31 * result + Float.hashCode(red);
        result = 31 * result + Float.hashCode(green);
        result = 31 * result + Float.hashCode(blue);
        return result;
    }

    public static final Codec<NoctyxType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("lang").forGetter(NoctyxType::lang),
            Codec.FLOAT.fieldOf("alpha").forGetter(NoctyxType::alpha),
            Codec.FLOAT.fieldOf("red").forGetter(NoctyxType::red),
            Codec.FLOAT.fieldOf("green").forGetter(NoctyxType::green),
            Codec.FLOAT.fieldOf("blue").forGetter(NoctyxType::blue))
            .apply(instance, NoctyxType::new));
}
