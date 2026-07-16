package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay;
import com.ghostipedia.cosmiccore.client.FoodHealthClient;
import com.ghostipedia.cosmiccore.common.food.ActiveFood;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodData;
import com.ghostipedia.cosmiccore.common.food.FoodBar;
import com.ghostipedia.cosmiccore.common.food.FoodMemory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SyncFoodDataPacket implements CustomPacketPayload {

    public static final Type<SyncFoodDataPacket> TYPE = new Type<>(CosmicCore.id("sync_food_data"));
    public static final StreamCodec<FriendlyByteBuf, SyncFoodDataPacket> CODEC = StreamCodec
            .ofMember(SyncFoodDataPacket::encode, SyncFoodDataPacket::new);

    private final List<FoodBar> foods;
    private final List<FoodBar> brews;
    @Nullable
    private final FoodMemory memory;
    private final boolean sickened;
    private final double foodHealthBonus;

    public SyncFoodDataPacket(CosmicFoodData data) {
        this.foods = toBars(data.foods);
        this.brews = toBars(data.brews);
        this.memory = data.memory;
        this.sickened = data.sickened;
        this.foodHealthBonus = data.totalHeartBonus();
    }

    public SyncFoodDataPacket(FriendlyByteBuf buf) {
        this.foods = readBars(buf);
        this.brews = readBars(buf);
        this.memory = buf.readBoolean() ? FoodMemory.read(buf) : null;
        this.sickened = buf.readBoolean();
        this.foodHealthBonus = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        writeBars(buf, foods);
        writeBars(buf, brews);
        buf.writeBoolean(memory != null);
        if (memory != null) memory.write(buf);
        buf.writeBoolean(sickened);
        buf.writeDouble(foodHealthBonus);
    }

    public void execute(IPayloadContext context) {
        CosmicHudGuiOverlay.setFoodData(foods, brews);
        CosmicHudGuiOverlay.setMemory(memory);
        CosmicHudGuiOverlay.setSickened(sickened);
        FoodHealthClient.sync(foodHealthBonus);
    }

    private static List<FoodBar> toBars(List<ActiveFood> list) {
        List<FoodBar> out = new ArrayList<>();
        for (ActiveFood af : list) {
            out.add(new FoodBar(new ItemStack(af.item), af.ticksLeft(), af.baseDuration(), af.quality()));
        }
        return out;
    }

    private static void writeBars(FriendlyByteBuf buf, List<FoodBar> bars) {
        buf.writeVarInt(bars.size());
        for (FoodBar bar : bars) {
            buf.writeVarInt(BuiltInRegistries.ITEM.getId(bar.icon().getItem()));
            buf.writeVarInt(bar.ticksLeft());
            buf.writeVarInt(bar.base());
            buf.writeVarInt(bar.quality());
        }
    }

    private static List<FoodBar> readBars(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<FoodBar> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Item item = BuiltInRegistries.ITEM.byId(buf.readVarInt());
            out.add(new FoodBar(new ItemStack(item), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        }
        return out;
    }

    @Override
    public @NotNull Type<SyncFoodDataPacket> type() {
        return TYPE;
    }
}
