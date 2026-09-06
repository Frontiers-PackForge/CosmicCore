package com.ghostipedia.cosmiccore.common.orrery;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public final class OrreryState {

    public static final int LOADOUTS = 4;
    public static final int SLOTS = 8;
    private static final String DATA = "leyline_orrery";
    public final UUID id;
    private final String[] names = new String[LOADOUTS];
    private final UUID[][] filters = new UUID[LOADOUTS][SLOTS];
    private final int[] selected = new int[LOADOUTS];
    public int active;
    public int volume = 35;
    public boolean showPreview = true;
    public boolean showHud = true;

    public OrreryState(UUID id) {
        this.id = id;
        java.util.Arrays.fill(names, "");
    }

    public OrreryState copy() {
        var copy = new OrreryState(id);
        copy.active = active;
        copy.volume = volume;
        copy.showPreview = showPreview;
        copy.showHud = showHud;
        for (int bar = 0; bar < LOADOUTS; bar++) {
            copy.names[bar] = names[bar];
            copy.selected[bar] = selected[bar];
            System.arraycopy(filters[bar], 0, copy.filters[bar], 0, SLOTS);
        }
        return copy;
    }

    public UUID filter(int bar, int slot) {
        return valid(bar, slot) ? filters[bar][slot] : null;
    }

    public void assign(int bar, int slot, UUID design) {
        if (valid(bar, slot)) filters[bar][slot] = design;
    }

    public static boolean valid(int bar, int slot) {
        return bar >= 0 && bar < LOADOUTS && slot >= 0 && slot < SLOTS;
    }

    public UUID selection() {
        return filter(active, selected[active]);
    }

    public int selected(int bar) {
        return selected[bar];
    }

    public void select(int bar, int slot) {
        if (filter(bar, slot) != null) {
            active = bar;
            selected[bar] = slot;
        }
    }

    public String name(int bar) {
        return names[bar];
    }

    public Component label(int bar) {
        return names[bar].isBlank() ? Component.translatable("cosmiccore.orrery.loadout", bar + 1) :
                Component.literal(names[bar]);
    }

    public void rename(int bar, String name) {
        if (bar >= 0 && bar < LOADOUTS) names[bar] = name.strip().substring(0, Math.min(name.strip().length(), 32));
    }

    public static UUID identity(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(DATA);
        return tag.hasUUID("id") ? tag.getUUID("id") : null;
    }

    public static OrreryState read(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(DATA);
        var state = new OrreryState(tag.hasUUID("id") ? tag.getUUID("id") : UUID.randomUUID());
        state.active = Math.clamp(tag.getInt("active"), 0, LOADOUTS - 1);
        if (tag.contains("volume")) state.volume = Math.clamp(tag.getInt("volume"), 0, 100);
        if (tag.contains("preview")) state.showPreview = tag.getBoolean("preview");
        if (tag.contains("hud")) state.showHud = tag.getBoolean("hud");
        for (int bar = 0; bar < LOADOUTS; bar++) {
            var loadout = tag.getCompound("bar" + bar);
            state.rename(bar, loadout.getString("name"));
            state.selected[bar] = Math.clamp(loadout.getInt("selected"), 0, SLOTS - 1);
            for (int slot = 0; slot < SLOTS; slot++)
                if (loadout.hasUUID("slot" + slot)) state.filters[bar][slot] = loadout.getUUID("slot" + slot);
        }
        return state;
    }

    public void save(ItemStack stack) {
        var tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putInt("active", active);
        tag.putInt("volume", volume);
        tag.putBoolean("preview", showPreview);
        tag.putBoolean("hud", showHud);
        for (int bar = 0; bar < LOADOUTS; bar++) {
            var loadout = new CompoundTag();
            loadout.putString("name", names[bar]);
            loadout.putInt("selected", selected[bar]);
            for (int slot = 0; slot < SLOTS; slot++)
                if (filters[bar][slot] != null) loadout.putUUID("slot" + slot, filters[bar][slot]);
            tag.put("bar" + bar, loadout);
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.put(DATA, tag));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(id);
        buffer.writeByte(active);
        buffer.writeByte(volume);
        buffer.writeBoolean(showPreview);
        buffer.writeBoolean(showHud);
        for (int bar = 0; bar < LOADOUTS; bar++) {
            buffer.writeUtf(names[bar], 32);
            buffer.writeByte(selected[bar]);
            for (var filter : filters[bar]) buffer.writeNullable(filter, (b, id) -> b.writeUUID(id));
        }
    }

    public static OrreryState read(FriendlyByteBuf buffer) {
        var state = new OrreryState(buffer.readUUID());
        state.active = Math.clamp(buffer.readUnsignedByte(), 0, LOADOUTS - 1);
        state.volume = Math.clamp(buffer.readUnsignedByte(), 0, 100);
        state.showPreview = buffer.readBoolean();
        state.showHud = buffer.readBoolean();
        for (int bar = 0; bar < LOADOUTS; bar++) {
            state.names[bar] = buffer.readUtf(32);
            state.selected[bar] = Math.clamp(buffer.readUnsignedByte(), 0, SLOTS - 1);
            for (int slot = 0; slot < SLOTS; slot++)
                state.filters[bar][slot] = buffer.readNullable(b -> b.readUUID());
        }
        return state;
    }
}
