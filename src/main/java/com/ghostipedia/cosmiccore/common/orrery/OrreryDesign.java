package com.ghostipedia.cosmiccore.common.orrery;

import com.ghostipedia.cosmiccore.common.deployment.LeylinePrefab;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record OrreryDesign(UUID id, String name, ResourceLocation machine, ResourceLocation icon,
                           long count, boolean craftable) {

    public static OrreryDesign of(LeylinePrefab prefab, long count, boolean craftable) {
        return new OrreryDesign(prefab.id(), prefab.name(), prefab.machine(), prefab.icon(), count, craftable);
    }

    public Component machineName() {
        var definition = GTRegistries.MACHINES.get(machine);
        return definition == null ? Component.literal(machine.toString()) : definition.asStack().getHoverName();
    }

    public ItemStack iconStack() {
        return new ItemStack(BuiltInRegistries.ITEM.get(icon));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(id);
        buffer.writeUtf(name, 80);
        buffer.writeResourceLocation(machine);
        buffer.writeResourceLocation(icon);
        buffer.writeVarLong(count);
        buffer.writeBoolean(craftable);
    }

    public static OrreryDesign read(FriendlyByteBuf buffer) {
        return new OrreryDesign(buffer.readUUID(), buffer.readUtf(80), buffer.readResourceLocation(),
                buffer.readResourceLocation(), buffer.readVarLong(), buffer.readBoolean());
    }
}
