package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay;
import com.ghostipedia.cosmiccore.common.food.ActiveFood;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodData;
import com.ghostipedia.cosmiccore.common.food.FoodBar;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SyncFoodDataPacket implements CustomPacketPayload {

    public static final Type<SyncFoodDataPacket> TYPE = new Type<>(CosmicCore.id("sync_food_data"));
    public static final StreamCodec<FriendlyByteBuf, SyncFoodDataPacket> CODEC = StreamCodec
            .ofMember(SyncFoodDataPacket::encode, SyncFoodDataPacket::new);

    private final List<FoodBar> foods;
    private final List<FoodBar> brews;

    public SyncFoodDataPacket(CosmicFoodData data) {
        this.foods = toBars(data.foods);
        this.brews = toBars(data.brews);
    }

    public SyncFoodDataPacket(FriendlyByteBuf buf) {
        this.foods = readBars(buf);
        this.brews = readBars(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        writeBars(buf, foods);
        writeBars(buf, brews);
    }

    public void execute(IPayloadContext context) {
        CosmicHudGuiOverlay.setFoodData(foods, brews);
    }

    private static List<FoodBar> toBars(List<ActiveFood> list) {
        List<FoodBar> out = new ArrayList<>();
        for (ActiveFood af : list) {
            out.add(new FoodBar(new ItemStack(af.item), af.ticksLeft, af.def.durationTicks()));
        }
        return out;
    }

    private static void writeBars(FriendlyByteBuf buf, List<FoodBar> bars) {
        buf.writeVarInt(bars.size());
        for (FoodBar bar : bars) {
            buf.writeVarInt(BuiltInRegistries.ITEM.getId(bar.icon().getItem()));
            buf.writeVarInt(bar.ticksLeft());
            buf.writeVarInt(bar.base());
        }
    }

    private static List<FoodBar> readBars(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<FoodBar> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Item item = BuiltInRegistries.ITEM.byId(buf.readVarInt());
            out.add(new FoodBar(new ItemStack(item), buf.readVarInt(), buf.readVarInt()));
        }
        return out;
    }

    @Override
    public @NotNull Type<SyncFoodDataPacket> type() {
        return TYPE;
    }
}
