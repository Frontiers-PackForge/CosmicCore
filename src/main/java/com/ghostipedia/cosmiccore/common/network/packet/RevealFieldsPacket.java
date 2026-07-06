package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.client.map.RevealedFieldStorage;
import com.ghostipedia.cosmiccore.client.map.RevealedFields;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement.FieldProfile;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement.OreField;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RevealFieldsPacket implements CustomPacketPayload {

    public static final Type<RevealFieldsPacket> TYPE = new Type<>(CosmicCore.id("reveal_fields"));
    public static final StreamCodec<FriendlyByteBuf, RevealFieldsPacket> CODEC = StreamCodec
            .ofMember(RevealFieldsPacket::encode, RevealFieldsPacket::new);

    private final ResourceKey<Level> dimension;
    private final List<RevealedField> fields;

    public RevealFieldsPacket(ResourceKey<Level> dimension, List<RevealedField> fields) {
        this.dimension = dimension;
        this.fields = fields;
    }

    public RevealFieldsPacket(FriendlyByteBuf buffer) {
        this.dimension = buffer.readResourceKey(Registries.DIMENSION);
        int count = buffer.readVarInt();
        this.fields = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            fields.add(RevealedField.STREAM_CODEC.decode(buffer));
        }
    }

    public static List<RevealedField> toRevealedFields(List<OreField> oreFields, byte tier) {
        List<RevealedField> fields = new ArrayList<>(oreFields.size());
        for (OreField field : oreFields) {
            Material bundle = field.bundle();
            FieldProfile profile = OreFieldPlacement.profileFor(bundle);
            int radius = profile != null ? profile.fieldRadius() : OreFieldPlacement.DEFAULT_FIELD_RADIUS;
            fields.add(new RevealedField(field.core().getX(), field.core().getZ(),
                    bundle.getMaterialARGB(), bundle.getName(), tier, radius));
        }
        return fields;
    }

    public static RevealFieldsPacket of(ResourceKey<Level> dimension, List<OreField> oreFields, byte tier) {
        return new RevealFieldsPacket(dimension, toRevealedFields(oreFields, tier));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(dimension);
        buffer.writeVarInt(fields.size());
        for (RevealedField field : fields) {
            RevealedField.STREAM_CODEC.encode(buffer, field);
        }
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            RevealedFieldStorage.ensureLoaded();
            for (RevealedField field : fields) {
                RevealedFields.INSTANCE.put(dimension, field);
            }
            RevealedFieldStorage.save();
        });
    }

    @Override
    public @NotNull Type<RevealFieldsPacket> type() {
        return TYPE;
    }
}
